package com.leonardo.drinkslab.util

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import com.leonardo.drinkslab.R
import com.leonardo.drinkslab.ui.login.Login
import com.leonardo.drinkslab.ui.main.Main
import com.shashank.sony.fancytoastlib.FancyToast

fun Context.startHomeActivity() =
    Intent(this, Main::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }

fun Context.startLoginActivity() =
    Intent(this, Login::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }

fun Context.diplayToast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.displayToast(message: String, style: String) {
    when (style) {
        "Default" -> FancyToast.makeText(this, message, FancyToast.LENGTH_LONG, FancyToast.DEFAULT,false).show()
        "Success" -> FancyToast.makeText(this ,message, FancyToast.LENGTH_LONG, FancyToast.SUCCESS,false).show()
        "Error" -> FancyToast.makeText(this, message, FancyToast.LENGTH_LONG, FancyToast.ERROR,false).show()
        "Info" -> FancyToast.makeText(this, message, FancyToast.LENGTH_LONG, FancyToast.INFO,false).show()
        "Warning" -> FancyToast.makeText(this, message, FancyToast.LENGTH_LONG, FancyToast.WARNING,false).show()
        else -> FancyToast.makeText(this, message, FancyToast.LENGTH_LONG, FancyToast.DEFAULT,false).show()
    }
    //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.displaySuccessDialog(message: String) {
    MaterialDialog(this)
        .show {
            title(R.string.text_success)
            message(text = message)
            positiveButton(R.string.text_ok)
        }
}

fun Context.displayErrorDialog(message: String) {
    MaterialDialog(this)
        .show {
            title(R.string.text_error)
            message(text = message)
            positiveButton(R.string.text_ok)
        }
}

fun View.showSnackbar(msgId: Int, length: Int) {
    showSnackbar(context.getString(msgId), length)
}
fun View.showSnackbar(msg: String, length: Int) {
    showSnackbar(msg, length, null, {})
}
fun View.showSnackbar(
    msgId: Int,
    length: Int,
    actionMessageId: Int,
    action: (View) -> Unit
) {
    showSnackbar(context.getString(msgId), length, context.getString(actionMessageId), action)
}
fun View.showSnackbar(
    msg: String,
    length: Int,
    actionMessage: CharSequence?,
    action: (View) -> Unit
) {
    val snackbar = Snackbar.make(this, msg, length)
    if (actionMessage != null) {
        snackbar.setAction(actionMessage) {
            action(this)
        }
    }
    snackbar.show()
}