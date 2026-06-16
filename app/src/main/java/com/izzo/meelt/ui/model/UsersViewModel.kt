package com.izzo.meelt.ui.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Context
import android.net.Uri
import android.util.Log
import android.app.ActivityManager
import com.izzo.meelt.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.izzo.meelt.data.UsersUiState
import com.izzo.meelt.data.model.Message
import com.izzo.meelt.data.service.DataManagementService
import com.izzo.meelt.notifications.ChatNotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UsersViewModel : ViewModel() {

    private val db: FirebaseDatabase by lazy {
        Firebase.database.apply {}
    }

    private val _uiState = MutableStateFlow(UsersUiState(emptyList()))
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()
    private var messagesListener: ValueEventListener? = null

    // applicationContext is safe to keep in a ViewModel.
    private var appContext: Context? = null
    private var didBootstrapMessages = false
    private val seenMessageKeys = mutableSetOf<String>()

    @Volatile
    private var activeChatUserId: Int? = null

    context(dataService: DataManagementService, resources: com.izzo.meelt.data.service.ResourcesService, context: Context)
    fun loadUiStateContents() {
        appContext = context.applicationContext

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val selectedUsersJsonName = dataService.loadSelectedUsersFromPreferences(context)
            val users = dataService.getAllUsers()
            val currentUserUid = Firebase.auth.currentUser?.uid.orEmpty()
            val availableUsersJsonNames = dataService.getAvailableUsersJsonNames(context)

            _uiState.update { current ->
                current.copy(
                    users = users,
                    authenticatedUserUid = currentUserUid,
                    availableUsersJsonNames = availableUsersJsonNames,
                    selectedUsersJsonName = selectedUsersJsonName,
                    isLoading = false
                )
            }

            // On app start, never override chat history with seed messages.
            startMessagesListener()
        }
    }

    context(dataService: DataManagementService, resources: com.izzo.meelt.data.service.ResourcesService, context: Context)
    fun selectUsersJson(jsonName: String) {
        appContext = context.applicationContext

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val selectedUsersJsonName = dataService.selectUsersJson(context, jsonName)
            val users = dataService.getAllUsers()
            val preloadedMessages = dataService.getAllMessages()
            val currentUserUid = Firebase.auth.currentUser?.uid.orEmpty()
            val availableUsersJsonNames = dataService.getAvailableUsersJsonNames(context)

            _uiState.update {
                it.copy(
                    users = users,
                    messages = preloadedMessages,
                    authenticatedUserUid = currentUserUid,
                    availableUsersJsonNames = availableUsersJsonNames,
                    selectedUsersJsonName = selectedUsersJsonName,
                    isLoading = false
                )
            }

            replaceDatabaseMessagesWith(preloadedMessages)
        }
    }

    fun setActiveChatUserId(userId: Int) {
        activeChatUserId = userId
    }

    fun clearActiveChatUserId() {
        activeChatUserId = null
    }

    fun blockUser(userId: Int) {
        _uiState.update { state ->
            state.copy(
                users = state.users.map { user ->
                    if (user.userId == userId) user.copy(isBlocked = true) else user
                }
            )
        }
    }

    fun unblockUser(userId: Int) {
        _uiState.update { state ->
            state.copy(
                users = state.users.map { user ->
                    if (user.userId == userId) user.copy(isBlocked = false) else user
                }
            )
        }
    }

    private fun startMessagesListener() {
        if (messagesListener != null) return

        val ref = db.getReference("messages")
        messagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val keyedMessages = snapshot.children.mapNotNull { child ->
                    val rawMessage = child.getValue(Message::class.java) ?: return@mapNotNull null
                    val resolvedId = rawMessage.messageId ?: child.key ?: buildFallbackMessageKey(rawMessage)
                    val message = rawMessage.copy(messageId = resolvedId)
                    resolvedId to message
                }

                val sortedMessages = keyedMessages
                    .map { it.second }
                    .sortedBy { it.timestamp }

                _uiState.update { current ->
                    current.copy(messages = sortedMessages, error = null)
                }

                if (!didBootstrapMessages) {
                    seenMessageKeys += keyedMessages.map { it.first }
                    didBootstrapMessages = true
                    return
                }

                val currentUserUid = _uiState.value.authenticatedUserUid
                keyedMessages.forEach { (key, message) ->
                    if (!seenMessageKeys.add(key)) return@forEach
                    if (!shouldNotifyIncoming(message, currentUserUid)) return@forEach
                    if (!isAppInForeground()) return@forEach

                    val sender = _uiState.value.users
                        .find { it.firebaseUid == message.senderUid }

                    val senderUserId: Int = sender?.userId ?: 0

                    // Skip notification if the chat with this sender is currently open
                    if (senderUserId != 0 && activeChatUserId == senderUserId) return@forEach

                    val senderName = sender
                        ?.username
                        ?: appContext?.getString(R.string.notification_sender_fallback).orEmpty()

                    val preview = when {
                        !message.text.isNullOrBlank() -> message.text
                        !message.imageUrl.isNullOrBlank() -> appContext?.getString(R.string.notification_photo_preview)
                        else -> appContext?.getString(R.string.notification_new_message_preview)
                    }.orEmpty()

                    if (senderName.isBlank() || preview.isBlank()) return@forEach

                    appContext?.let { context ->
                        ChatNotificationHelper.showIncomingMessage(
                            context = context,
                            senderName = senderName,
                            senderUserId = senderUserId,
                            messagePreview = preview
                        )
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _uiState.update { current ->
                    current.copy(error = error.message)
                }
            }
        }

        ref.addValueEventListener(messagesListener as ValueEventListener)
    }

    private fun stopMessagesListener() {
        val listener = messagesListener ?: return
        db.reference.child(MESSAGES_CHILD).removeEventListener(listener)
        messagesListener = null
    }

    private fun resetMessagesTracking() {
        didBootstrapMessages = false
        seenMessageKeys.clear()
    }

    private fun replaceDatabaseMessagesWith(messages: List<Message>) {
        stopMessagesListener()
        resetMessagesTracking()

        val payload = messages
            .sortedBy { it.timestamp }
            .mapIndexed { index, message ->
                val key = message.messageId ?: buildPreloadedMessageKey(index, message)
                key to message.copy(messageId = key)
            }
            .toMap()

        db.reference
            .child(MESSAGES_CHILD)
            .setValue(payload)
            .addOnSuccessListener {
                startMessagesListener()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Unable to preload dataset messages into Firebase", exception)
                _uiState.update {
                    it.copy(
                        error = appContext?.getString(
                            R.string.chat_error_send_message,
                            exception.message.orEmpty()
                        ) ?: exception.message.orEmpty()
                    )
                }
                startMessagesListener()
            }
    }

    private fun shouldNotifyIncoming(message: Message, currentUserUid: String): Boolean {
        if (currentUserUid.isBlank()) return false
        return message.receiverUid == currentUserUid && message.senderUid != currentUserUid
    }

    private fun isAppInForeground(): Boolean {
        val context = appContext ?: return false
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return false
        val runningProcesses = activityManager.runningAppProcesses ?: return false

        return runningProcesses.any { process ->
            process.processName == context.packageName &&
                process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }
    }

    private fun buildFallbackMessageKey(message: Message): String {
        return message.messageId
            ?: "${message.senderUid}_${message.receiverUid}_${message.timestamp}_${message.text}_${message.imageUrl}"
    }

    private fun buildPreloadedMessageKey(index: Int, message: Message): String {
        val timestampPart = message.timestamp.toString().ifBlank { "0" }
        return "seed_${index}_$timestampPart"
    }

    fun sendMessage(receiverUid: String, text: String) {
        val senderUid = _uiState.value.authenticatedUserUid
        val currentUser = Firebase.auth.currentUser

        Log.d("UsersViewModel", "=== sendMessage called ===")
        Log.d("UsersViewModel", "  senderUid (from uiState): '$senderUid'")
        Log.d("UsersViewModel", "  receiverUid: '$receiverUid'")
        Log.d("UsersViewModel", "  text: '$text'")
        Log.d("UsersViewModel", "  Firebase.auth.currentUser: ${currentUser?.uid ?: "NULL"}")
        Log.d("UsersViewModel", "  Firebase DB instance: ${db}")

        if (senderUid.isBlank() || receiverUid.isBlank()) {
            Log.e("UsersViewModel", "  ERROR: senderUid or receiverUid is blank")
            _uiState.update {
                it.copy(
                    error = appContext?.getString(R.string.chat_error_sender_receiver_unresolved)
                        ?: ""
                )
            }
            return
        }

        val ref = db.reference.child(MESSAGES_CHILD).push()
        val messageKey = ref.key ?: buildFallbackMessageKey(
            Message(
                senderUid = senderUid,
                receiverUid = receiverUid,
                text = text,
                timestamp = System.currentTimeMillis()
            )
        )

        val message = Message(
            messageId = messageKey,
            text = text,
            senderUid = senderUid,
            receiverUid = receiverUid,
            timestamp = System.currentTimeMillis(),
        )

        Log.d("UsersViewModel", "  Writing to path: $ref")

        ref.setValue(message)
            .addOnSuccessListener {
                Log.d("UsersViewModel", "  SUCCESS: message written to Firebase")
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "UsersViewModel",
                    "  FAILURE writing to Firebase: ${exception.message}",
                    exception
                )
                _uiState.update {
                    it.copy(
                        error = appContext?.getString(
                            R.string.chat_error_send_message,
                            exception.message.orEmpty()
                        ) ?: exception.message.orEmpty()
                    )
                }
            }
    }

    fun deleteMessage(message: Message) {
        val imageUrl = message.imageUrl
        val hasFirebaseImage = !imageUrl.isNullOrBlank() &&
                imageUrl != LOADING_IMAGE_URL &&
                (imageUrl.startsWith("https://firebasestorage.googleapis.com") || imageUrl.startsWith("gs://"))

        if (!hasFirebaseImage) {
            deleteMessageFromDatabase(message)
            return
        }

        val firebaseImageUrl = imageUrl ?: run {
            deleteMessageFromDatabase(message)
            return
        }

        Firebase.storage.getReferenceFromUrl(firebaseImageUrl)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Image deleted from Storage for messageId=${message.messageId}")
                deleteMessageFromDatabase(message)
            }
            .addOnFailureListener { e ->
                val storageError = e as? StorageException
                if (storageError?.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    // File is already gone; keep deleting the message to avoid stale rows.
                    Log.w(TAG, "Image already removed in Storage for messageId=${message.messageId}")
                    deleteMessageFromDatabase(message)
                } else {
                    Log.w(TAG, "Unable to delete image from Storage for messageId=${message.messageId}", e)
                    _uiState.update {
                        it.copy(
                            error = appContext?.getString(
                                R.string.chat_error_send_message,
                                e.message.orEmpty()
                            ) ?: e.message.orEmpty()
                        )
                    }
                }
            }
    }

    private fun deleteMessageFromDatabase(message: Message) {
        val messageId = message.messageId
        if (!messageId.isNullOrBlank()) {
            db.reference
                .child(MESSAGES_CHILD)
                .child(messageId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "Message deleted successfully by messageId=$messageId")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Unable to delete message messageId=$messageId", e)
                    _uiState.update {
                        it.copy(
                            error = appContext?.getString(
                                R.string.chat_error_send_message,
                                e.message.orEmpty()
                            ) ?: e.message.orEmpty()
                        )
                    }
                }
            return
        }

        val targetTimestamp = message.timestamp.toDouble()

        db.reference
            .child(MESSAGES_CHILD)
            .orderByChild("timestamp")
            .equalTo(targetTimestamp)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Log.w(TAG, "No message found to delete for timestamp=${message.timestamp}")
                        return
                    }

                    var deletedCount = 0
                    snapshot.children.forEach { child ->
                        val candidate = child.getValue(Message::class.java) ?: return@forEach
                        val isSameMessage =
                            candidate.senderUid == message.senderUid &&
                                    candidate.receiverUid == message.receiverUid &&
                                    candidate.messageId == message.messageId &&
                                    candidate.text == message.text &&
                                    candidate.imageUrl == message.imageUrl &&
                                    candidate.photoUrl == message.photoUrl &&
                                    candidate.timestamp == message.timestamp

                        if (isSameMessage) {
                            child.ref.removeValue()
                                .addOnSuccessListener {
                                    Log.d(TAG, "Message deleted successfully: key=${child.key}")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Unable to delete message key=${child.key}", e)
                                    _uiState.update {
                                        it.copy(
                                            error = appContext?.getString(
                                                R.string.chat_error_send_message,
                                                e.message.orEmpty()
                                            ) ?: e.message.orEmpty()
                                        )
                                    }
                                }
                            deletedCount++
                        }
                    }

                    if (deletedCount == 0) {
                        Log.w(TAG, "No exact matching message found for deletion")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Message delete query cancelled", error.toException())
                    _uiState.update { it.copy(error = error.message) }
                }
            })
    }

    override fun onCleared() {
        stopMessagesListener()
        resetMessagesTracking()
        super.onCleared()
    }

    fun logout() {
        Firebase.auth.signOut()

    }

    fun onImageSelected(receiverUid: String, uri: Uri) {
        val user = Firebase.auth.currentUser
        val photoURL = user?.photoUrl?.toString()
        val messageRef = db.reference
            .child(MESSAGES_CHILD)
            .push()
        val key = messageRef.key
        if (key.isNullOrBlank()) {
            Log.w(TAG, "Unable to create key for image message")
            return
        }

        val tempMessage = Message(
            messageId = key,
            photoUrl = photoURL,
            receiverUid = receiverUid,
            senderUid = user?.uid,
            imageUrl = LOADING_IMAGE_URL,
            timestamp = System.currentTimeMillis()
        )

        addPendingImagePreview(key, uri.toString())

        messageRef.setValue(
                tempMessage,
                DatabaseReference.CompletionListener { databaseError, databaseReference ->
                    if (databaseError != null) {
                        removePendingImagePreview(key)
                        Log.w(
                            TAG, "Unable to write message to database.",
                            databaseError.toException()
                        )
                        return@CompletionListener
                    }

                    // Build a StorageReference and then upload the file
                    val key = databaseReference.key ?: tempMessage.messageId
                    val storageReference = Firebase.storage
                        .getReference(user!!.uid)
                        .child(key!!)
                        .child(uri.lastPathSegment!!)
                    putImageInStorage(tempMessage, storageReference, uri, key)
                })
    }

    private fun putImageInStorage(
        message: Message,
        storageReference: StorageReference,
        uri: Uri,
        key: String?
    ) {
        val compressedBytes = buildCompressedImageBytes(uri)
        val uploadTask = if (compressedBytes != null) {
            storageReference.putBytes(compressedBytes)
        } else {
            storageReference.putFile(uri)
        }

        // First upload the image to Cloud Storage
        uploadTask
            .addOnSuccessListener { taskSnapshot -> // After the image loads, get a public downloadUrl for the image
                // and add it to the message.
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        val friendlyMessage =
                            message.copy(
                                messageId = key,
                                imageUrl = uri.toString()
                            )
                        db.reference
                            .child(MESSAGES_CHILD)
                            .child(key!!)
                            .setValue(friendlyMessage)
                            .addOnCompleteListener {
                                removePendingImagePreview(key)
                            }
                    }
            }
            .addOnFailureListener { e ->
                removePendingImagePreview(key)
                Log.w(
                    TAG,
                    "Image upload task was unsuccessful.",
                    e
                )
                // Clean temporary placeholder message if upload fails.
                if (!key.isNullOrBlank()) {
                    db.reference.child(MESSAGES_CHILD).child(key).removeValue()
                }
            }
    }

    private fun addPendingImagePreview(messageId: String, previewUri: String) {
        _uiState.update { current ->
            current.copy(
                pendingImagePreviews = current.pendingImagePreviews + (messageId to previewUri)
            )
        }
    }

    private fun removePendingImagePreview(messageId: String?) {
        if (messageId.isNullOrBlank()) return
        _uiState.update { current ->
            current.copy(
                pendingImagePreviews = current.pendingImagePreviews - messageId
            )
        }
    }

    private fun buildCompressedImageBytes(uri: Uri): ByteArray? {
        val context = appContext ?: return null
        return try {
            val resolver = context.contentResolver

            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            } ?: return null

            val maxDimension = 1280
            val sampleSize = calculateInSampleSize(
                width = boundsOptions.outWidth,
                height = boundsOptions.outHeight,
                maxDimension = maxDimension
            )

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val bitmap = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            val output = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 72, output)
            bitmap.recycle()
            output.toByteArray()
        } catch (e: Exception) {
            Log.w(TAG, "Unable to compress image before upload, using original file", e)
            null
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        if (width <= 0 || height <= 0 || maxDimension <= 0) return 1
        var inSampleSize = 1
        var halfWidth = width / 2
        var halfHeight = height / 2
        while ((halfWidth / inSampleSize) >= maxDimension || (halfHeight / inSampleSize) >= maxDimension) {
            inSampleSize *= 2
        }
        return inSampleSize.coerceAtLeast(1)
    }

    fun setAllMessagesAsReadByUser(senderId: Int) {
        val receiverUid = _uiState.value.authenticatedUserUid
        val senderUid = _uiState.value.users.find { it.userId == senderId }?.firebaseUid ?: return
        db.reference
            .child(MESSAGES_CHILD)
            .orderByChild("timestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { child ->
                        val message = child.getValue(Message::class.java) ?: return@forEach
                        if (message.senderUid == senderUid && message.receiverUid == receiverUid && !message.read) {
                            child.ref.child("read").setValue(true)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Mark as read query cancelled", error.toException())
                }
            })
    }

    companion object {
        private const val TAG = "MainActivity"
        const val MESSAGES_CHILD = "messages"
        const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    }
}
