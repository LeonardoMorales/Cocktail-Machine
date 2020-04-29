package com.leonardo.drinkslab.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getViewID())
    }

    protected abstract fun getViewID(): Int

    fun showProgress() {
        progress_bar.isVisible = true
    }

    fun hideProgress() {
        progress_bar.isVisible = false
    }
}