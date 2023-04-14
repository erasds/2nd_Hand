package com.esardo.a2ndhand.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esardo.a2ndhand.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class MessageViewModel: ViewModel() {
    var messageLiveData: MutableLiveData<List<Message>?> = MutableLiveData()

    var messageList = mutableListOf<Message>()

    val db = FirebaseFirestore.getInstance()

    fun getAllMessageObserver(): MutableLiveData<List<Message>?> {
        return messageLiveData
    }

    fun getAllMessages(userId: String, chatId: String) {
        /*val messageCol = db.collection("User").document(userId)
            .collection("Chat").document(chatId)
            .collection("Message")*/
        val chatDoc = db.collection("User").document(userId)
            .collection("Chat").document(chatId)
        val messageCol = chatDoc.collection("Message")
        messageCol.get().addOnSuccessListener { documents ->
            messageList.clear()
            for(document in documents) {
                val id = document.id
                val data = document.toObject<Message>()
                val text = data.Text
                val fromUser = data.FromUser
                val toUser = data.ToUser
                val date = data.Date
                val message = Message(id, text, fromUser, toUser, date)
                messageList.add(message)
            }
            messageLiveData.postValue(messageList)
        }.addOnFailureListener { exception ->
            Log.w(ContentValues.TAG, "Error obteniendo mensajes: ", exception)
        }
    }

    fun sendMessage(userId: String, chatId: String, message: Message) {
        //Create new Message with the data obtained
        val msg = hashMapOf(
            "Text" to message.Text,
            "FromUser" to message.FromUser,
            "ToUser" to message.ToUser,
            "Date" to message.Date
        )

        val messageCol = db.collection("User").document(userId)
            .collection("Chat").document(chatId)
            .collection("Message")

        messageCol.add(msg)
            .addOnSuccessListener {
                //If the message has been sended, now we have to add it to the otherUser collection too
                val messageCol = db.collection("User").document(message.ToUser)
                    .collection("Chat")
                messageCol.whereEqualTo("OtherUser", userId).get()
                    .addOnSuccessListener { documents ->
                        var otherUserChatId = ""
                        for (document in documents) {
                            otherUserChatId = document.id
                        }
                        val msgCol = messageCol.document(otherUserChatId).collection("Message")
                        msgCol.add(msg).addOnSuccessListener {
                            //Message upload completed
                            Log.d(ContentValues.TAG, "Mensaje añadido en las colecciones de ambos usuarios")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error enviando mensaje: ", exception)
            }
    }

}