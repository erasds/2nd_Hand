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
    val Picture : Picture,
    val CategoryId : String,
    val IsSell : Boolean,
    val UserId : String,
    val TownId : String,
    val PublishDate : Date,
    var isChecked : Boolean
) : Serializable
{
    // Constructor without arguments
    constructor() : this("", "","",0.0, Picture("", "", "", "", ""), "",false,"","",Date(), false)
}

@Keep
data class Picture(
    var Pic1 : String,
    var Pic2 : String,
    var Pic3 : String,
    var Pic4 : String,
    var Pic5 : String
) : Serializable
{
    // Constructor without arguments
    constructor() : this("", "","","","")
}

