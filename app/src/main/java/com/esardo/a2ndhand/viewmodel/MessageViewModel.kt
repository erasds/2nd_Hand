package com.esardo.a2ndhand.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esardo.a2ndhand.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

class MessageViewModel: ViewModel() {
    var messageLiveData: MutableLiveData<List<Message>?> = MutableLiveData()

    var messageList = mutableListOf<Message>()

    val db = FirebaseFirestore.getInstance()

    fun getAllMessageObserver(): MutableLiveData<List<Message>?> {
        return messageLiveData
    }

    //Función para obtener todos los mensajes
    fun getAllMessages(userId: String, chatId: String) {
        val chatDoc = db.collection("User").document(userId)
            .collection("Chat").document(chatId)
        val messageCol = chatDoc.collection("Message")
        val query = messageCol.orderBy("Date", Query.Direction.ASCENDING)
        query.addSnapshotListener { documents, exception ->
            if (exception != null) {
                Log.w("TAG", "Error leyendo los datos", exception)
                return@addSnapshotListener
            } else {
                if (documents != null) {
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
                }
            }
        }
    }

    //Función para enviar (insertar) un mensaje
    fun sendMessage(userId: String, chatId: String, message: Message) {
        //Crea un nuevo mensaje con los datos obtenidos
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
                //Si el mensaje se ha enviado, tenemos que añadirlo a la colección del otherUser también
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
                            Log.d(ContentValues.TAG, "Mensaje añadido en las colecciones de ambos usuarios")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error enviando mensaje: ", exception)
            }
    }

}