package com.leonardo.drinkslab.ui.login.PasswordInput

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.leonardo.drinkslab.R
import com.leonardo.drinkslab.util.startHomeActivity
import kotlinx.android.synthetic.main.fragment_password_input.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class PasswordInputFragment : Fragment(), KodeinAware {

    private val TAG: String = "AppDebug"

    override val kodein by kodein()
    private val factory: PasswordInputViewModelFactory by instance()

    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private val viewModel by lazy {
        ViewModelProvider(activity!!, factory).get(PasswordInputViewModel::class.java)
    }

    private val args: PasswordInputFragmentArgs by navArgs()
    private lateinit var idMachine: String
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreference = activity!!.getSharedPreferences("com.leonardo.drinkslab", Context.MODE_PRIVATE)
        editor = sharedPreference.edit()

        args.idMachine?.let {
            idMachine = it
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_password_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(::idMachine.isInitialized){
            searchIdInDatabase()
        }else {
            val dialog = MaterialDialog(activity!!)
                .title(R.string.text_error)
                .message(text = getString(R.string.error_qr_scan))
                .positiveButton(R.string.text_ok)

            dialog.show()
        }

        btnContinuarPasswordInput.setOnClickListener {
            if(isValidForPassNextActivity()){
                //activity!!.finish()
                validatePassword()
            } else {
                Log.d(TAG, "Error, digite la contraseña")
            }
        }
    }

    private fun searchIdInDatabase() {
        viewModel.getMachine(idMachine).observe(viewLifecycleOwner, Observer { machine ->
            password = machine.password
            Log.d(TAG, "Machine Password: ${machine.password}")
        })
    }

    private fun validatePassword() {
        if(password == textInputEditTextPasswordMachine.text.toString()){
            // Save data on SharedPreferences
            editor.putString(getString(R.string.key_id_machine), idMachine)
            editor.putString(getString(R.string.key_password_machine), password)
            editor.commit()

            Log.d(TAG, "CORRECT PASSWORD, NAVIGATING TO NEXT ACTIVITY...")
            activity!!.startHomeActivity()
        } else {
            val dialog = MaterialDialog(activity!!)
                .title(R.string.text_error)
                .message(text = getString(R.string.invalid_password))
                .positiveButton(R.string.text_ok)

            dialog.show()
        }

    }

    private fun isValidForPassNextActivity():Boolean {
        val password = textInputEditTextPasswordMachine.text

        return if(password.isNullOrBlank()){
            textInputLayoutPasswordMachine.isErrorEnabled = true
            textInputLayoutPasswordMachine.error = "Digite la contraseña de la máquina"
            false
        }else{
            textInputLayoutPasswordMachine.isErrorEnabled = false
            true
        }
    }

}
