package com.example.faketagram.data

data class Sender(
    val id: String,
    val name: String
)

data class Message(
    val id: String,
    val sender: Sender,
    val text: String,
    val timestamp: Long,
    val isSentByMe: Boolean = false
)

object FakeChatData {
    val senders = listOf(
        Sender("alice", "Alice Smith"),
        Sender("bob", "Bob Johnson"),
        Sender("carol", "Carol Williams")
    )

    val conversations: MutableMap<String, MutableList<Message>> = mutableMapOf(
        "alice" to mutableListOf(
            Message(
                "1", senders[0],
                "Hey there! How are you?",
                System.currentTimeMillis() - 3_600_000
            ),
            Message(
                "2", senders[0],
                "Did you see the latest post?",
                System.currentTimeMillis() - 1_800_000
            )
        ),
        "bob" to mutableListOf(
            Message(
                "3", senders[1],
                "Check out this cool photo!",
                System.currentTimeMillis() - 7_200_000
            )
        ),
        "carol" to mutableListOf(
            Message(
                "4", senders[2],
                "We should catch up soon!",
                System.currentTimeMillis() - 86_400_000
            )
        )
    )

    fun getSender(id: String): Sender? = senders.find { it.id == id }

    fun addIncomingMessage(senderId: String, text: String) {
        val sender = getSender(senderId) ?: return
        val message = Message(
            id = System.currentTimeMillis().toString(),
            sender = sender,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        conversations.getOrPut(senderId) { mutableListOf() }.add(message)
    }
}
