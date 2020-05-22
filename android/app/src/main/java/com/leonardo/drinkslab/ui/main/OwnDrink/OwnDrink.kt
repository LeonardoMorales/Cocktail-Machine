package com.leonardo.drinkslab.ui.main.OwnDrink

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.textfield.TextInputEditText
import com.leonardo.drinkslab.R
import com.leonardo.drinkslab.data.model.Ingredient
import com.leonardo.drinkslab.ui.kodeinViewModel
import com.leonardo.drinkslab.ui.main.DrinkDetails.MachineListener
import com.leonardo.drinkslab.ui.main.OwnDrink.adapter.OwnDrinkRecyclerAdapter
import com.leonardo.drinkslab.util.displayToast
import kotlinx.android.synthetic.main.activity_own_drink.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import java.util.*
import kotlin.collections.ArrayList

class OwnDrink : AppCompatActivity(), KodeinAware, ProcessListener, MachineListener {

    private val TAG: String = "AppDebug"

    override val kodein by kodein()
    private val viewModel: OwnDrinkViewModel by kodeinViewModel()

    private lateinit var ownDrinkAdapter: OwnDrinkRecyclerAdapter

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
        object: ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, START or END){

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val adapter = recyclerView.adapter as OwnDrinkRecyclerAdapter
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                if (from < to) {
                    for (i in from until to) {
                        Collections.swap(viewModel.selectedIngredientsList, i, i + 1)
                    }
                } else {
                    for (i in from downTo to + 1) {
                        Collections.swap(viewModel.selectedIngredientsList, i, i - 1)
                    }
                }

                adapter.notifyItemMoved(from, to)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.selectedIngredientsList.removeAt(viewHolder.adapterPosition)
                ownDrinkAdapter.notifyItemRemoved(viewHolder.adapterPosition)
            }

        }

        ItemTouchHelper(simpleItemTouchCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_own_drink)

        viewModel.processListener = this
        viewModel.machineListener = this

        initRecyclerView()

        subscribeObservers()

        btnAddIngredient.setOnClickListener {
            addIngredient()
        }

        btnCreateDrink.setOnClickListener {
            createDrink()
        }

    }

    private fun initRecyclerView() {
        ownDrinkAdapter = OwnDrinkRecyclerAdapter()
        recyclerViewOwnDrink.apply {
            fitsSystemWindows = true
            adapter = ownDrinkAdapter
        }

        itemTouchHelper.attachToRecyclerView(recyclerViewOwnDrink)
    }

    private fun subscribeObservers() {
        viewModel.idMachine = intent.getStringExtra("idMachine")
        viewModel.fetchIngredientsList().observe(this, Observer {
            btnAddIngredient.isEnabled = true
            viewModel.ingredientsList = it
            viewModel.fillSpinner()
        })
    }

    private fun addIngredient() {
        var ingredientSelected: Ingredient? = null
        val materialDialog = MaterialDialog(this)
            .noAutoDismiss()
            .cancelOnTouchOutside(false)
            .customView(R.layout.layout_add_ingredient)

        val spinner = materialDialog.findViewById<Spinner>(R.id.spinner_ingredient)
        if (spinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                viewModel.ingredientsListName
            )
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    ingredientSelected = viewModel.ingredientsList[position]
                }
            }
        }

        materialDialog.findViewById<TextView>(R.id.negative_button_layout_amount)
            .setOnClickListener {
                materialDialog.dismiss()
            }

        materialDialog.findViewById<TextView>(R.id.positive_button_layout_amount)
            .setOnClickListener {
                displayIngredientsList(ingredientSelected, materialDialog.findViewById<TextInputEditText>(R.id.textInputEditTextQuantity).text.toString(), materialDialog)
            }

        materialDialog.show()
    }

    private fun displayIngredientsList(ingredient: Ingredient?, quantity: String, dialog: MaterialDialog) {
        if (quantity.isBlank() || quantity.toInt() <= 0) {
            this.displayToast("Please fill out the quantity field", "Error")
        } else {
            ingredient!!.quantity = quantity.toInt()
            viewModel.selectedIngredientsList.add(ingredient)
            ownDrinkAdapter.submitList(viewModel.selectedIngredientsList)
            ownDrinkAdapter.notifyItemInserted(viewModel.selectedIngredientsList.size)
            dialog.dismiss()
        }
    }

    private fun createDrink() {
        val drinkName = textInputEditTextDrinkName.text.toString()

        if (drinkName.isBlank()) {
            this.displayToast("Please fill out the Drink Name field", "Error")
        }else if(viewModel.selectedIngredientsList.size <= 0){
            this.displayToast("Please add an ingredient", "Error")
        } else {
            viewModel.drinkName = drinkName
            shareDrinkDialog()
        }
    }

    private fun shareDrinkDialog() {
        val shareDrinkDialog = MaterialDialog(this)
            .noAutoDismiss()
            .title(text = "Share Drink")
            .message(text = "Do you want to share the drink with other people?")
            .positiveButton(text = "Agree") {
                viewModel.shareDrink()
                it.dismiss()
            }
            .negativeButton(text = "Disagree") {
                makeDrink()
                it.dismiss()
            }

        shareDrinkDialog.show()
    }

    private fun makeDrink() {
        val processList = ArrayList<HashMap<String, Long>>()

        for (item in viewModel.selectedIngredientsList){
            val process = HashMap<String, Long>()
            process.put("a_posicion", item.position!!)
            process.put("cantidad", item.quantity!!.toLong())

            processList.add(process)
        }
        viewModel.updateProcess(viewModel.idMachine!!, processList)
    }

    override fun onProcessStarted() {
        showProgressBar()
    }

    override fun onProcessSuccess() {
        hideProgressBar()
        makeDrink()
    }

    override fun onProcessFailure(message: String) {
        hideProgressBar()
        this.displayToast(message, "Error")
    }

    private fun showProgressBar(){
        progress_bar_own_drink.isVisible = true
        btnCreateDrink.isVisible = false
    }

    private fun hideProgressBar(){
        progress_bar_own_drink.isVisible = false
        btnCreateDrink.isVisible = true
    }

    override fun onStarted() {
        showProgressBar()
    }

    override fun onSuccess() {
        hideProgressBar()
    }

    override fun onFailure(message: String) {
        hideProgressBar()
        this.displayToast(message, "Error")
    }
}
