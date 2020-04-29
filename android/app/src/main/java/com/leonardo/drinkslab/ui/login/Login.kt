package com.leonardo.drinkslab.ui.login

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import com.leonardo.drinkslab.R
import com.leonardo.drinkslab.util.startHomeActivity

class Login : AppCompatActivity() {

    private val TAG: String = "AppDebug"

    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreference = getSharedPreferences("com.leonardo.drinkslab", Context.MODE_PRIVATE)
        editor = sharedPreference.edit()

        loadInitialValues()
    }

    private fun loadInitialValues() {
        val idMachine = sharedPreference.getString(getString(R.string.key_id_machine), "")
        val passwordMachine = sharedPreference.getString(getString(R.string.key_password_machine), "")

        if(!idMachine.isNullOrEmpty() || !passwordMachine.isNullOrEmpty()){
            Log.d(TAG, "idMachine: $idMachine\npasswordMachine: $passwordMachine")
            startHomeActivity()
        }
    }
}
