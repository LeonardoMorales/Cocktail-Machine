package com.leonardo.drinkslab.ui.main.OwnDrink

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.leonardo.drinkslab.data.model.Drink
import com.leonardo.drinkslab.data.model.Ingredient
import com.leonardo.drinkslab.data.repositories.DocumentsRepository
import com.leonardo.drinkslab.ui.main.DrinkDetails.MachineListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class OwnDrinkViewModel(
    private val documentsRepository: DocumentsRepository
): ViewModel()
{
    var idMachine: String? = null
    var drinkName: String = ""

    private val disposables = CompositeDisposable()
    var processListener: ProcessListener? = null
    var machineListener: MachineListener? = null

    var ingredientsList: MutableList<Ingredient> = ArrayList()
    var ingredientsListName: MutableList<String> = ArrayList()

    var selectedIngredientsList: MutableList<Ingredient> = ArrayList()

    fun fetchIngredientsList(): LiveData<MutableList<Ingredient>> {
        val mutableData = MutableLiveData<MutableList<Ingredient>>()
        documentsRepository.getIngredientsList(idMachine!!).observeForever {
            mutableData.value = it
        }
        return mutableData
    }

    fun drinksListSize(): Long {
        var drinksListSize = -1L
        documentsRepository.getDrinksList(idMachine!!).observeForever {
            drinksListSize = it.size.toLong()
        }
        return drinksListSize
    }


    fun fillSpinner(){
        for (item in ingredientsList){
            ingredientsListName.add(item.name!!)
        }
    }

    fun shareDrink(){
        processListener?.onProcessStarted()
        val ingredients = ArrayList<String>()
        for(item in selectedIngredientsList){
            ingredients.add(item.name!!)
        }

        val processList = ArrayList<HashMap<String, Long>>()

        for (item in selectedIngredientsList){
            val process = HashMap<String, Long>()
            process.put("a_posicion", item.position!!)
            process.put("cantidad", item.quantity!!.toLong())

            processList.add(process)
        }

        val drink = Drink(drinksListSize()+1, drinkName, "", ingredients, processList)

        val disposable = documentsRepository.addDrink(idMachine!!, drink)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( {
                processListener?.onProcessSuccess()
            },{
                processListener?.onProcessFailure(it.message!!)
            })

        disposables.add(disposable)
    }

    fun updateProcess(idDocument: String, process: MutableList<HashMap<String, Long>>){
        machineListener?.onStarted()

        val disposable = documentsRepository.updateProcess(idDocument, process)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( {
                machineListener?.onSuccess()
            },{
                machineListener?.onFailure(it.message!!)
            })

        disposables.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}