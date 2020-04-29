package com.leonardo.drinkslab.ui.main.OwnDrink

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.leonardo.drinkslab.R
import kotlinx.android.synthetic.main.fragment_own_drink.*


class OwnDrink : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_own_drink, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnAddIngredient.setOnClickListener {
           showAddIngredientDialog()
        }

        btnCreateDrink.setOnClickListener {
            showAddDrinkDialog()
        }

    }

    private fun showAddIngredientDialog() {
        val dialog = MaterialDialog(activity!!)
            .noAutoDismiss()
            .customView(R.layout.add_ingredient)

        dialog.findViewById<TextView>(R.id.positive_button_layout_amount).setOnClickListener{
            //TODO("ADD INGREDIENT TO THE LIST")
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.negative_button_layout_amount).setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAddDrinkDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity!!)
        builder.setTitle("Share Drink")
        builder.setMessage("Do you want to share the drink with other people?")
        builder.setCancelable(false)
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                addDrinkToMachineList()
                dialog.dismiss()
            })
            .setNegativeButton("No",DialogInterface.OnClickListener { dialog, id ->
                dialog.dismiss()
            })

        val alertdialog: AlertDialog = builder.create()
        alertdialog.show()
    }

    private fun addDrinkToMachineList() {

    }

}
