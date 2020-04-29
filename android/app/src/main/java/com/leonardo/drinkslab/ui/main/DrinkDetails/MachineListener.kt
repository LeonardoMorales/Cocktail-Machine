package com.leonardo.drinkslab.ui.main.DrinkDetails

interface MachineListener {
    fun onStarted()

    fun onSuccess()

    fun onFailure(message: String)
}
