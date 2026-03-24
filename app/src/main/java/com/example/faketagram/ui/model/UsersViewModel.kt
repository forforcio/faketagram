package com.example.faketagram.ui.model

import android.net.Uri
import android.util.Log
import com.example.faketagram.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faketagram.data.UsersUiState
import com.example.faketagram.data.model.Message
import com.example.faketagram.data.service.DataManagementService
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

    context(dataService: DataManagementService)
    fun init() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val users = dataService.getAllUsers()
            val currentUserUid = Firebase.auth.currentUser?.uid.orEmpty()

            _uiState.update {
                it.copy(
                    users = users,
                    currentUserUid = currentUserUid,
                    isLoading = false
                )
            }

            startMessagesListener()
        }
    }

    private fun startMessagesListener() {
        if (messagesListener != null) return

        val ref = db.getReference("messages")
        messagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                    .sortedBy { it.timestamp }

                _uiState.update { current ->
                    current.copy(messages = list, error = null)
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

    fun sendMessage(receiverUid: String, text: String) {
        val senderUid = _uiState.value.currentUserUid
        val currentUser = Firebase.auth.currentUser

        Log.d("UsersViewModel", "=== sendMessage called ===")
        Log.d("UsersViewModel", "  senderUid (from uiState): '$senderUid'")
        Log.d("UsersViewModel", "  receiverUid: '$receiverUid'")
        Log.d("UsersViewModel", "  text: '$text'")
        Log.d("UsersViewModel", "  Firebase.auth.currentUser: ${currentUser?.uid ?: "NULL"}")
        Log.d("UsersViewModel", "  Firebase DB instance: ${db}")

        if (senderUid.isBlank() || receiverUid.isBlank()) {
            Log.e("UsersViewModel", "  ERROR: senderUid or receiverUid is blank")
            _uiState.update { it.copy(error = "Unable to resolve sender or receiver") }
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
                _uiState.update { it.copy(error = "Error al enviar: ${exception.message}") }
            }
    }

    override fun onCleared() {
        val listener = messagesListener
        if (listener != null) {
            db.reference.child("messages").removeEventListener(listener)
        }
        messagesListener = null
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
        const val ANONYMOUS = "anonymous"
        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    }
}

