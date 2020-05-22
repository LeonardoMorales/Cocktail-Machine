package com.leonardo.drinkslab.data.model

data class Ingredient(
    val category: String? = "",
    val id: Long? = -1L,
    val name: String? = "",
    val position: Long? = -1L,
    var quantity: Int? = -1
)