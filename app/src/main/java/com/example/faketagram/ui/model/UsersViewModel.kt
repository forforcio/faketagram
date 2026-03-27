package com.example.faketagram.ui.model

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.faketagram.BuildConfig
import com.example.faketagram.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faketagram.data.UsersUiState
import com.example.faketagram.data.model.Message
import com.example.faketagram.data.service.DataManagementService
import com.example.faketagram.notifications.ChatNotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UsersViewModel: ViewModel() {

    private val db: FirebaseDatabase by lazy {
        Firebase.database.apply {
            if (BuildConfig.DEBUG) {
                useEmulator("127.0.0.1", 9000)
            }
        }
    }

    private val _uiState = MutableStateFlow(UsersUiState(emptyList()))
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()
    private var messagesListener: ValueEventListener? = null

    // applicationContext is safe to keep in a ViewModel.
    private var appContext: Context? = null
    private var didBootstrapMessages = false
    private val seenMessageKeys = mutableSetOf<String>()

    // Tracks which user's chat is currently open so we skip notifications for it
    @Volatile
    private var activeChatUserId: Int? = null

    fun setActiveChatUserId(userId: Int) { activeChatUserId = userId }
    fun clearActiveChatUserId() { activeChatUserId = null }

    context(dataService: DataManagementService, context: Context)
    fun init() {
        appContext = context.applicationContext

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val users = dataService.getAllUsers()
            val currentUserUid = Firebase.auth.currentUser?.uid.orEmpty()

            _uiState.update {
                it.copy(
                    users = users,
                    authenticatedUserUid = currentUserUid,
                    isLoading = false
                )
            }

            startMessagesListener()
        }
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
                    val message = child.getValue(Message::class.java) ?: return@mapNotNull null
                    val key = child.key ?: buildFallbackMessageKey(message)
                    key to message
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

                    val sender = _uiState.value.users
                        .find { it.firebaseUid == message.senderUid }

                    val senderUserId: Int = sender?.userId ?: 0

                    // Skip notification if the chat with this sender is currently open
                    if (senderUserId != 0 && activeChatUserId == senderUserId) return@forEach

                    val senderName = sender
                        ?.username
                        ?: appContext?.getString(R.string.notification_sender_fallback)
                        ?: "Nuevo mensaje"

                    val preview = when {
                        !message.text.isNullOrBlank() -> message.text
                        !message.imageUrl.isNullOrBlank() -> appContext?.getString(R.string.notification_photo_preview)
                        else -> appContext?.getString(R.string.notification_new_message_preview)
                    } ?: "Tienes un nuevo mensaje"

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

    private fun shouldNotifyIncoming(message: Message, currentUserUid: String): Boolean {
        if (currentUserUid.isBlank()) return false
        return message.receiverUid == currentUserUid && message.senderUid != currentUserUid
    }

    private fun buildFallbackMessageKey(message: Message): String {
        return "${message.senderUid}_${message.receiverUid}_${message.timestamp}_${message.text}_${message.imageUrl}"
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
                        ?: "Unable to resolve sender or receiver"
                )
            }
            return
        }

        val message = Message(
            text = text,
            senderUid = senderUid,
            receiverUid = receiverUid,
            timestamp = System.currentTimeMillis(),
        )

        val ref = db.reference.child("messages").push()
        Log.d("UsersViewModel", "  Writing to path: $ref")

        ref.setValue(message)
            .addOnSuccessListener {
                Log.d("UsersViewModel", "  SUCCESS: message written to Firebase")
            }
            .addOnFailureListener { exception ->
                Log.e("UsersViewModel", "  FAILURE writing to Firebase: ${exception.message}", exception)
                _uiState.update {
                    it.copy(
                        error = appContext?.getString(
                            R.string.chat_error_send_message,
                            exception.message.orEmpty()
                        ) ?: "Error al enviar: ${exception.message}"
                    )
                }
            }
    }

    fun deleteMessage(message: Message) {
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
                                            ) ?: "Error deleting message: ${e.message}"
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
        val listener = messagesListener
        if (listener != null) {
            db.reference.child("messages").removeEventListener(listener)
        }
        messagesListener = null
        didBootstrapMessages = false
        seenMessageKeys.clear()
        super.onCleared()
    }

    fun logout() {
        Firebase.auth.signOut()
    }

    fun onImageSelected(receiverUid: String, uri: Uri) {
        val user = Firebase.auth.currentUser
        val photoURL = user?.photoUrl?.toString()
        val tempMessage = Message(
            photoUrl = photoURL,
            receiverUid = receiverUid,
            senderUid = user?.uid,
            imageUrl = LOADING_IMAGE_URL,
            timestamp = System.currentTimeMillis())
        db.reference
            .child(MESSAGES_CHILD)
            .push()
            .setValue(
                tempMessage,
                DatabaseReference.CompletionListener { databaseError, databaseReference ->
                    if (databaseError != null) {
                        Log.w(
                            TAG, "Unable to write message to database.",
                            databaseError.toException()
                        )
                        return@CompletionListener
                    }

                    // Build a StorageReference and then upload the file
                    val key = databaseReference.key
                    val storageReference = Firebase.storage
                        .getReference(user!!.uid)
                        .child(key!!)
                        .child(uri.lastPathSegment!!)
                    putImageInStorage(tempMessage, storageReference, uri, key)
                })
    }

    private fun putImageInStorage(message: Message, storageReference: StorageReference, uri: Uri, key: String?) {
        // First upload the image to Cloud Storage
        storageReference.putFile(uri)
            .addOnSuccessListener { taskSnapshot -> // After the image loads, get a public downloadUrl for the image
                // and add it to the message.
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        val friendlyMessage =
                            message.copy(imageUrl = uri.toString())
                        db.reference
                            .child(MESSAGES_CHILD)
                            .child(key!!)
                            .setValue(friendlyMessage)
                    }
            }
            .addOnFailureListener { e ->
                Log.w(
                    TAG,
                    "Image upload task was unsuccessful.",
                    e
                )
            }
    }

    companion object {
        private const val TAG = "MainActivity"
        const val MESSAGES_CHILD = "messages"
        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    }
}
