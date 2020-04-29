package com.leonardo.drinkslab.ui.main

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.leonardo.drinkslab.R
import com.leonardo.drinkslab.util.startLoginActivity

class Main : BaseActivity() {

    override fun getViewID(): Int = R.layout.activity_main

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("com.leonardo.drinkslab", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.right_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.nav_exit -> {
                editor.clear()
                editor.apply()
                startLoginActivity()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
