package com.leonardo.drinkslab.ui.main.DrinkSelector.adapter

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
import kotlinx.android.synthetic.main.layout_banner_item.view.*

class DrinkSelectorRecyclerAdapter(val interaction: Interaction? = null) : ListAdapter<Drink, DrinkSelectorRecyclerAdapter.DrinksViewHolder>(DrinksDiffCallback()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinksViewHolder {
        return DrinksViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_banner_item, parent, false))
    }

    override fun onBindViewHolder(holder: DrinksViewHolder, position: Int) {
        val drink = getItem(position)
        holder.bind(drink)
    }

    inner class DrinksViewHolder(
        itemView: View
    ): RecyclerView.ViewHolder(itemView){

        val tvDrinkName = itemView.textViewDrink
        val ivDrinkImage = itemView.imageViewDrink

        fun bind(drink: Drink) = with(itemView){

            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, drink)
            }

            tvDrinkName.text = drink.name

            Glide.with(itemView.context)
                .load(drink.image)
                .into(ivDrinkImage)
        }
    }

    interface Interaction{
        fun onItemSelected(position: Int, drink: Drink)
    }
}

class DrinksDiffCallback : DiffUtil.ItemCallback<Drink>(){

    override fun areItemsTheSame(oldItem: Drink, newItem: Drink): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Drink, newItem: Drink): Boolean {
        return oldItem.equals(newItem)
    }


}