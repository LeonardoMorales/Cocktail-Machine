package com.leonardo.drinkslab.ui.main.DrinkSelector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.leonardo.drinkslab.R
import com.leonardo.drinkslab.data.model.Drink
import com.leonardo.drinkslab.ui.kodeinViewModel
import com.leonardo.drinkslab.ui.main.DrinkSelector.adapter.DrinkSelectorRecyclerAdapter
import com.leonardo.drinkslab.ui.main.OwnDrink.OwnDrink
import com.leonardo.drinkslab.util.vo.Resource
import kotlinx.android.synthetic.main.fragment_drink_selector.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein

class DrinkSelectorFragment : Fragment(), KodeinAware, DrinkSelectorRecyclerAdapter.Interaction {

    private val TAG: String = "AppDebug"

    override val kodein by kodein()
    private val viewModel: DrinksSelectorViewModel by kodeinViewModel()

    private lateinit var drinksAdapter: DrinkSelectorRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_drink_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        suscribeObservers()
    }

    private fun initRecyclerView() {
        drinksAdapter = DrinkSelectorRecyclerAdapter(this@DrinkSelectorFragment)
        recyclerViewDrinksSelector.apply {
            fitsSystemWindows = true
            adapter = drinksAdapter
        }
    }

    private fun suscribeObservers() {
        shimmer_view_container.startShimmer()
        viewModel.fetchVersionCode.observe(viewLifecycleOwner, Observer {result ->
            when(result){
                is Resource.Loading -> {

                    Log.d(TAG, "LOADING...")
                }

                is Resource.Success -> {
                    Log.d(TAG, "Version Code: ${result.data}")
                }

                is Resource.Failure -> {
                    Log.d(TAG, "ERROR: ${result.exception}")
                }
            }
        })

        /*viewModel.fetchDrinksList2.observe(viewLifecycleOwner, Observer { result ->
            when(result){
                is Resource.Loading -> {
                    Log.d(TAG, "LOADING...")
                }

                is Resource.Success -> {
                    if(!viewModel.lastDrinksWasAdded){
                        val lastDrink = Drink((result.data.size+1).toLong(), "Yourn Own Drink", "https://the-peak.ca/wp-content/uploads/2018/06/Photo-courtesy-of-delicious.com-.-au.jpg", null, null)
                        result.data.add(result.data.size, lastDrink)
                        viewModel.lastDrinksWasAdded = true
                    }

                    shimmer_view_container.stopShimmer()
                    shimmer_view_container.isVisible = false
                    drinksAdapter.submitList(result.data)
                }

                is Resource.Failure -> {
                    Log.d(TAG, "ERROR: ${result.exception}")
                }
            }
        })*/
    }

    override fun onItemSelected(position: Int, drink: Drink) {
        val action: NavDirections?
        if(drink.id!!.toInt() == drinksAdapter.itemCount){
            Log.d(TAG, "LAST DRINK...CREATE YOUR OWN")
            val intent = Intent(requireActivity(), OwnDrink::class.java)
            intent.putExtra("idMachine", viewModel.idMachine!!)
            startActivity(intent)
            return
        }else{
            action = DrinkSelectorFragmentDirections.actionDrinkSelectorFragmentToDrinkDetailsFragment(drink, viewModel.idMachine!!)
            findNavController().navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchDrinksList(viewModel.idMachine!!).observe(viewLifecycleOwner, Observer {
            val lastDrink = Drink((it.size+1).toLong(), "Yourn Own Drink", "https://the-peak.ca/wp-content/uploads/2018/06/Photo-courtesy-of-delicious.com-.-au.jpg", null, null)
            it.add(it.size, lastDrink)
            shimmer_view_container.stopShimmer()
            shimmer_view_container.isVisible = false
            drinksAdapter.submitList(it)
        })
    }


}
