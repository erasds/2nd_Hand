package com.esardo.a2ndhand.model

import java.io.Serializable

data class Rating(
    val id: String, //Identificador de la colección, se genera solo
    val From: String, //El Id del usuario autor de la valoración
    val Observations: String,
    val Points: Int
) : Serializable {
    //Constructor sin argumentos
    constructor() : this("", "", "", 0)
}