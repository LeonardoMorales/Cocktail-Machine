package com.leonardo.drinkslab.ui.main.DrinkDetails

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import com.leonardo.drinkslab.R
import com.leonardo.drinkslab.data.firebase.FirebaseSource
import com.leonardo.drinkslab.data.model.Drink
import com.leonardo.drinkslab.data.repositories.DocumentsRepository
import com.leonardo.drinkslab.util.ErrorHandling
import com.leonardo.drinkslab.util.ErrorHandling.Companion.MACHINE_OFFLINE
import com.leonardo.drinkslab.util.displayToast
import com.leonardo.drinkslab.util.vo.Resource
import kotlinx.android.synthetic.main.fragment_drink_details.*
import kotlinx.coroutines.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import java.util.HashMap
import kotlin.math.sign

class DrinkDetailsFragment : Fragment(), MachineListener {

    private val TAG: String = "AppDebug"

    private val args: DrinkDetailsFragmentArgs by navArgs()
    private lateinit var drink: Drink

    private lateinit var factory: DrinkDetailViewModelFactory
    private lateinit var viewModel: DrinkDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drink = args.drink
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        factory = DrinkDetailViewModelFactory(DocumentsRepository(FirebaseSource()), args.idDocument)
        viewModel = ViewModelProvider(activity!!, factory).get(DrinkDetailViewModel::class.java)
        return inflater.inflate(R.layout.fragment_drink_details, container, false)
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        suscribeObservers()

        btn_make_drink.setOnClickListener {
            makeDrink()
        }

    }

    private fun initView() {
        viewModel.machineListener = this
        tv_drink_name.text = drink.name

        val adapter = ArrayAdapter(
            activity!!, android.R.layout.simple_list_item_1,
            drink.ingredients!!.toMutableList())

        lv_drink_ingredient.adapter = adapter
    }

    @ExperimentalCoroutinesApi
    private fun suscribeObservers() {
        val dialog = MaterialDialog(activity!!)
            .noAutoDismiss()
            .cancelOnTouchOutside(false)
            .customView(R.layout.layout_creating_drink)

        viewModel.fetchMachineState.observe(viewLifecycleOwner, Observer {result ->
            when(result){

                is Resource.Loading -> {
                    Log.d(TAG, "LOADING...")
                }

                is Resource.Success -> {
                    Log.d(TAG, "Machine #${args.idDocument} is working?: ${result.data}")

                    if(result.data){
                        dialog.show()
                        dialog.setOnKeyListener { _, keyCode, event ->
                            if(keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP){
                                findNavController().navigateUp()
                            }
                            return@setOnKeyListener false
                        }
                    }else{
                        dialog.dismiss()
                    }
                }

                is Resource.Failure -> {
                    Log.d(TAG, "ERROR: ${result.exception}")
                }

            }
        })
    }

    private fun makeDrink() {
        viewModel.updateProcess(args.idDocument, drink.process!!.toMutableList())
    }

    override fun onStarted() {
        showProgressBar()
    }

    override fun onSuccess() {
        hideProgressBar()
    }

    override fun onFailure(message: String) {
        hideProgressBar()
        if(message == MACHINE_OFFLINE){
            activity!!.displayToast(MACHINE_OFFLINE, "Error")
        }else{
            Log.d(TAG, "ERROR MAKING DRINK, $message")
        }
    }

    private fun showProgressBar(){
        progress_bar_drink_details.isVisible = true
        btn_make_drink.isVisible = false
    }

    private fun hideProgressBar(){
        progress_bar_drink_details.isVisible = false
        btn_make_drink.isVisible = true
    }
}
