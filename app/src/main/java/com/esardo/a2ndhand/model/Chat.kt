package com.esardo.a2ndhand.model

import java.io.Serializable
import java.util.*

data class Chat(
    val id: String, //Identificador de la colección, se genera solo
    val OtherUser: String, //El otro usuario del chat (se tiene que crear en el usuario logeado, y en el dueño del producto donde pulsemos enviar mensaje)
    val Message: Message
) : Serializable
{
    //Constructor sin argumentos
    constructor() : this("", "", Message("", "", "", "", Date()))
}
data class Message(
    var id: String, //Identificador de la subcolección, se genera solo
    var Text: String, //Contenido del mensaje
    var FromUser: String, //Id del usuario que envía el mensaje (al mostrarlo será el nombre)
    var ToUser: String, //Id del usuario que recibe el mensaje
    var Date: Date //Fecha del mensaje
) : Serializable
{
    //Constructor sin argumentos
    constructor() : this("", "", "", "", Date())
}