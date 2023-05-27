package com.esardo.a2ndhand.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esardo.a2ndhand.model.Chat
import com.esardo.a2ndhand.model.Message
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class ChatViewModel: ViewModel() {
    var chatLiveData: MutableLiveData<List<Chat>?> = MutableLiveData()

    var chatList = mutableListOf<Chat>()

    val db = FirebaseFirestore.getInstance()

    fun getAllChatObserver(): MutableLiveData<List<Chat>?> {
        return chatLiveData
    }

    // Función para obtener todos los chats y mensajes
    fun getAllChats(userId: String) {
        val chatCol = db.collection("User").document(userId).collection("Chat")
        chatCol.get().addOnSuccessListener { chatDocuments ->
            if (chatDocuments != null) {
                val chatList = mutableListOf<Chat>()

                for (chatDocument in chatDocuments) {
                    val chatId = chatDocument.id
                    val chatData = chatDocument.toObject<Chat>()
                    val otherUser = chatData.OtherUser
                    val chat = Chat(chatId, otherUser, Message("", "", "", "", Date()))

                    val messageCol = chatCol.document(chatId).collection("Message")
                    val query = messageCol.orderBy("Date", Query.Direction.DESCENDING).limit(1)
                    query.addSnapshotListener { messageDocuments, exception ->
                        if (exception != null) {
                            Log.w("TAG", "Error leyendo los documentos de mensajes", exception)
                            return@addSnapshotListener
                        }

                        if (messageDocuments != null && !messageDocuments.isEmpty) {
                            for (messageDocument in messageDocuments) {
                                val messageId = messageDocument.id
                                val msgData = messageDocument.toObject<Message>()
                                val text = msgData.Text
                                val fromUser = msgData.FromUser
                                val toUser = msgData.ToUser
                                val date = msgData.Date
                                chat.Message.id = messageId
                                chat.Message.Text = text
                                chat.Message.FromUser = fromUser
                                chat.Message.ToUser = toUser
                                chat.Message.Date = date
                            }
                        }

                        val chatIndex = chatList.indexOfFirst { it.id == chatId }
                        if (chatIndex != -1) {
                            chatList[chatIndex] = chat
                        } else {
                            chatList.add(chat)
                        }

                        chatLiveData.postValue(chatList)
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.w("TAG", "Error obteniendo los chats", exception)
            // Manejar el error según tus necesidades
        }
    }



    //Función para insertar un nuevo chat
    fun createNewChat(userId: String, productUserId: String): LiveData<Chat?> {
        val chatLiveData = MutableLiveData<Chat?>()
        var chat: Chat? = null
        val chatCol = db.collection("User").document(userId).collection("Chat")
        chatCol.whereEqualTo("OtherUser", productUserId)
            .get().addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for(document in documents) {
                        val id = document.id
                        val data = document.toObject<Chat>()
                        val otherUser = data.OtherUser
                        chat = Chat(id, otherUser, Message("", "", "", "", Date()))
                    }

                    //Si el chat ya existe ya podemos devolverlo
                    chatLiveData.postValue(chat)
                } else {
                    //Si no existe primero lo creamos para el usuario logeado
                    val chat1 = hashMapOf(
                        "OtherUser" to productUserId
                    )
                    db.collection("User").document(userId)
                        .collection("Chat").add(chat1)
                        .addOnSuccessListener { documentReference ->
                            //Ahora podemos obtener sus datos
                            val documentId = documentReference.id
                            chatCol.document(documentId).get()
                                .addOnSuccessListener { documentSnapshot ->
                                    chat = Chat(documentId, productUserId, Message("", "", "", "", Date()))

                                    Log.d(ContentValues.TAG, "Chat creado para el usuario logeado")

                                    //Lo creamos también para el otro usuario
                                    val chat2 = hashMapOf(
                                        "OtherUser" to userId
                                    )
                                    db.collection("User").document(productUserId)
                                        .collection("Chat").add(chat2)
                                        .addOnSuccessListener {
                                            Log.d(ContentValues.TAG, "Chat creado para el otro usuario")

                                            //Después devolvemos el chat de nuestro usuario
                                            chatLiveData.postValue(chat)
                                        }
                                }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(ContentValues.TAG, "Error al intentar crear un chat: ", exception)
                        }
                }
            }
        return chatLiveData
    }
}