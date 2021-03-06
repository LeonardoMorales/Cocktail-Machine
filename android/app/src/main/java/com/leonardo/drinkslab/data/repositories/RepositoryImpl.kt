package com.leonardo.drinkslab.data.repositories

import androidx.lifecycle.MutableLiveData
import com.leonardo.drinkslab.data.firebase.FirebaseSource
import com.leonardo.drinkslab.data.model.Drink
import com.leonardo.drinkslab.util.vo.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

class RepositoryImpl(
    private val firebase: FirebaseSource
): IRepository{

    @ExperimentalCoroutinesApi
    override suspend fun getVersionCode(): Flow<Resource<Int>> = callbackFlow{

        val eventDocument = firebase.getVersionCode()

        val suscription = eventDocument.addSnapshotListener{ documentSnapshot, firebaseFirestoreException ->  
            if(documentSnapshot!!.exists()){
                val versionCode = documentSnapshot.getDouble("version")!!.toInt()
                offer(Resource.Success(versionCode))
            } else {
                channel.close(firebaseFirestoreException?.cause)
            }
        }

        awaitClose{
            suscription.remove()
        }

    }

    @ExperimentalCoroutinesApi
    override suspend fun getMachineState(idDocument: String): Flow<Resource<Boolean>> = callbackFlow {

        val eventDocument = firebase.getMachineStateFirestore(idDocument)

        val suscription = eventDocument.addSnapshotListener{ documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot!!.exists()){
                val machineState = documentSnapshot.getBoolean("isWorking")!!
                offer(Resource.Success(machineState))
            } else {
                channel.close(firebaseFirestoreException?.cause)
            }
        }

        awaitClose {
            suscription.remove()
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun getDrinksList(idDocument: String): Flow<Resource<MutableList<Drink>>> = callbackFlow {

        val eventDocument = firebase.getDrinksList2(idDocument)

        val suscription = eventDocument.addSnapshotListener{ documentSnapshot, firebaseFirestoreException ->
            if(!documentSnapshot!!.isEmpty){

                val mutableData = MutableLiveData<MutableList<Drink>>()
                val list = mutableListOf<Drink>()

                for (document in documentSnapshot) {

                    val id = document.getLong("id")
                    val image = document.getString("image")
                    val name = document.getString("name")
                    val ingredients = document.get("ingredients") as ArrayList<String>
                    val process = document.get("process") as ArrayList<HashMap<String, Long>>

                    val drink = Drink(id, name, image, ingredients, process)

                    list.add(drink)
                }
                mutableData.value = list

                offer(Resource.Success(mutableData.value!!))
            } else {
                channel.close(firebaseFirestoreException?.cause)
            }
        }

        awaitClose {
            suscription.remove()
        }
    }
}