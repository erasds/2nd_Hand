package com.esardo.a2ndhand.model

import com.google.firebase.Timestamp
import java.io.Serializable
import java.util.*

data class Product(
    val id : String,
    val Name : String,
    val Description : String,
    val Price : Double,
    val Image : String,
    val CategoryId : String,
    val IsSell : Boolean,
    val UserId : String,
    val TownId : String,
    val PublishDate : Timestamp
) : Serializable
{
    // Constructor without arguments
    constructor() : this("", "","",0.0,"","",false,"","",Timestamp(Date()))
}

