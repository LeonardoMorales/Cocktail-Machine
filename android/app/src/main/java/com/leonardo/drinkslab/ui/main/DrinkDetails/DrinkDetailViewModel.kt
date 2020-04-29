package com.leonardo.drinkslab.ui.main.DrinkDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.leonardo.drinkslab.data.repositories.DocumentsRepository
import kotlinx.coroutines.Dispatchers
import java.lang.Exception
import kotlinx.coroutines.flow.collect
import com.leonardo.drinkslab.util.vo.Resource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi

class DrinkDetailViewModel(
    private val documentsRepository: DocumentsRepository,
    idDocument: String
): ViewModel()
{

    private val disposables = CompositeDisposable()

    var machineListener: MachineListener? = null

    @ExperimentalCoroutinesApi
    val fetchMachineState =liveData(Dispatchers.IO){
        emit(value = Resource.Loading())

        try {
            documentsRepository.getMachineStateFirestore(idDocument).collect{
                emit(it)
            }
        }catch (e: Exception){
            emit(Resource.Failure(e))
        }
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