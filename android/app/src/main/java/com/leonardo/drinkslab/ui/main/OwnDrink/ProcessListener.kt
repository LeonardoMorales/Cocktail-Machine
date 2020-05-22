package com.leonardo.drinkslab.ui.main.OwnDrink

interface ProcessListener {

    fun onProcessStarted()

    fun onProcessSuccess()

    fun onProcessFailure(message: String)

}