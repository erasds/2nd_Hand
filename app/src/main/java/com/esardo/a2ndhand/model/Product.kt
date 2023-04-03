package com.esardo.a2ndhand.model

import com.google.errorprone.annotations.Keep
import com.google.firebase.Timestamp
import java.io.Serializable
import java.util.*

@Keep
data class Product(
    val id : String,
    val Name : String,
    val Description : String,
    val Price : Double,
    //val Image : String,
    val Picture : Picture,
    val CategoryId : String,
    val IsSell : Boolean,
    val UserId : String,
    val TownId : String,
    val PublishDate : Timestamp,
    var isChecked : Boolean
) : Serializable
{
    // Constructor without arguments
    constructor() : this("", "","",0.0, Picture("", "", "", "", ""), "",false,"","",Timestamp(Date()), false)
}

@Keep
data class Picture(
    val Pic1 : String,
    val Pic2 : String,
    val Pic3 : String,
    val Pic4 : String,
    val Pic5 : String
) : Serializable
{
    // Constructor without arguments
    constructor() : this("", "","","","")
}

