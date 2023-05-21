package com.esardo.a2ndhand.model

import com.google.errorprone.annotations.Keep
import java.io.Serializable
import java.util.*

@Keep
data class Product(
    var id: String,
    var Name: String,
    var Description: String,
    var Price: Double,
    var Picture: Picture?,
    var CategoryId: String,
    val IsSell: Boolean,
    val UserId: String,
    var TownId: String,
    var PublishDate: Date,
    var isChecked: Boolean
) : Serializable
{
    //Constructor sin argumentos
    constructor() : this("", "","",0.0, Picture("", "", "", "", "", ""), "",false,"","",Date(), false)
}

@Keep
data class Picture(
    val id: String,
    var Pic1 : String,
    var Pic2 : String,
    var Pic3 : String,
    var Pic4 : String,
    var Pic5 : String
) : Serializable
{
    //Constructor sin argumentos
    constructor() : this("", "", "","","","")
}

