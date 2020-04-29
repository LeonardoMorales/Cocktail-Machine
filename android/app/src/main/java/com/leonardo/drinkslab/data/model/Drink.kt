package com.leonardo.drinkslab.data.model

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class Drink(
    val id: Long? = 0L,
    val name: String? = "",
    val image: String? = "",
    val ingredients: ArrayList<String>?,
    val process: ArrayList<HashMap<String, Long>>?
): Parcelable