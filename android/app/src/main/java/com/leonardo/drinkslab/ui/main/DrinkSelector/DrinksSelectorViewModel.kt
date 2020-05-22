package com.leonardo.drinkslab.ui.main.DrinkSelector

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import com.leonardo.drinkslab.R
import com.leonardo.drinkslab.data.model.Drink
import com.leonardo.drinkslab.data.repositories.DocumentsRepository
import com.leonardo.drinkslab.domain.IUseCase
import com.leonardo.drinkslab.util.Constants
import com.leonardo.drinkslab.util.vo.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import java.lang.Exception

class DrinksSelectorViewModel(
    application: Application,
    private val useCase: IUseCase,
    private val documentsRepository: DocumentsRepository
): AndroidViewModel(application)
{
    private val TAG: String = "AppDebug"

    var sharedPreference: SharedPreferences
    var idMachine: String? = ""
    var lastDrinksWasAdded: Boolean = false

    private val context = getApplication<Application>().applicationContext

    init {
        sharedPreference = context.getSharedPreferences("com.leonardo.drinkslab", Context.MODE_PRIVATE)
        idMachine = sharedPreference.getString(context.getString(R.string.key_id_machine), "")
        idMachine = Constants.MACHINE_ID_TEST

        updateMachineOnlineStatus(idMachine!!)
    }

    val fetchVersionCode =liveData(Dispatchers.IO) {
        emit(value = Resource.Loading())

        try {
            useCase.getVersionCode().collect {
                emit(it)
            }
        } catch(e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    val fetchDrinksList2 = liveData(Dispatchers.IO){
        emit(value = Resource.Loading())

        try {
            useCase.getDrinksList(idMachine!!).collect {
                emit(it)
            }
        } catch(e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun fetchDrinksList(idDocument: String): LiveData<MutableList<Drink>>{
        val mutableData = MutableLiveData<MutableList<Drink>>()
        documentsRepository.getDrinksList(idDocument).observeForever {
            mutableData.value = it
        }
        return mutableData
    }

    fun updateMachineOnlineStatus(idDocument: String){
        documentsRepository.updateMachineOnlineStatus(idDocument)
    }
}