package com.leonardo.drinkslab.ui.main.OwnDrink.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.leonardo.drinkslab.R
import com.leonardo.drinkslab.data.model.Drink
import com.leonardo.drinkslab.data.model.Ingredient
import kotlinx.android.synthetic.main.layout_banner_item.view.*
import kotlinx.android.synthetic.main.layout_ingredient_item.view.*

class OwnDrinkRecyclerAdapter : ListAdapter<Ingredient, OwnDrinkRecyclerAdapter.IngredientsViewHolder>(DrinksDiffCallback()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientsViewHolder {
        return IngredientsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_ingredient_item, parent, false))
    }

    override fun onBindViewHolder(holder: IngredientsViewHolder, position: Int) {
        val ingredient = getItem(position)
        holder.bind(ingredient)
    }

    inner class IngredientsViewHolder(
        itemView: View
    ): RecyclerView.ViewHolder(itemView){

        val tvIngredientName = itemView.tv_ingredient_name
        val tvIngredientQuantity = itemView.tv_ingredient_quantity

        fun bind(ingredient: Ingredient) = with(itemView){

            tvIngredientName.text = ingredient.name
            tvIngredientQuantity.text = "${ingredient.quantity} oz"
        }
    }
}

class DrinksDiffCallback : DiffUtil.ItemCallback<Ingredient>(){

    override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
        return oldItem.equals(newItem)
    }


}