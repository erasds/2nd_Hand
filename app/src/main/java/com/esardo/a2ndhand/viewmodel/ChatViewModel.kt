package com.esardo.a2ndhand.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esardo.a2ndhand.model.Chat
import com.esardo.a2ndhand.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import java.util.*

class ChatViewModel: ViewModel() {
    var chatLiveData: MutableLiveData<List<Chat>?> = MutableLiveData()

    var chatList = mutableListOf<Chat>()

    val db = FirebaseFirestore.getInstance()

    fun getAllChatObserver(): MutableLiveData<List<Chat>?> {
        return chatLiveData
    }

    fun getAllChats(userId: String) {
        val chatCol = db.collection("User").document(userId).collection("Chat")
        chatCol.addSnapshotListener { documents, exception ->
            if (exception != null) {
                Log.w("TAG", "Listen failed.", exception)
                return@addSnapshotListener
            } else {
                if(documents != null) {
                    chatList.clear()
                    for(document in documents) {
                        val id = document.id
                        val data = document.toObject<Chat>()
                        val otherUser = data.OtherUser
                        val chat = Chat(id, otherUser, Message("", "", "", "", Timestamp(Date())))
                        chatList.add(chat)
                    }

                    //Ahora recorremos los chats para consultar la subcolección de Message
                    for (chat in chatList) {
                        val messageCol = chatCol.document(chat.id).collection("Message")
                        val query = messageCol.orderBy("Date", Query.Direction.DESCENDING).limit(1)
                        query.addSnapshotListener { documents, exception ->
                            if (exception != null) {
                                Log.w("TAG", "Listen failed.", exception)
                                return@addSnapshotListener
                            } else {
                                if (documents != null) {
                                    for (document in documents) {
                                        val id = document.id
                                        val msgData = document.toObject<Message>()
                                        val text = msgData.Text
                                        val fromUser = msgData.FromUser
                                        val toUser = msgData.ToUser
                                        val date = msgData.Date
                                        chat.Message.id = id
                                        chat.Message.Text = text
                                        chat.Message.FromUser = fromUser
                                        chat.Message.ToUser = toUser
                                        chat.Message.Date = date
                                    }

                                    chatLiveData.postValue(chatList)
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}