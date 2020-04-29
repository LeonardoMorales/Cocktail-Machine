package com.leonardo.drinkslab.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.leonardo.drinkslab.data.firebase.FirebaseSource
import com.leonardo.drinkslab.data.model.Drink
import com.leonardo.drinkslab.util.vo.Resource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DocumentsRepository(
    private val firebase: FirebaseSource
) {
    private val TAG: String = "AppDebug"

    @ExperimentalCoroutinesApi
    suspend fun getMachineStateRealTimeDB(idDocument: String): Flow<Resource<Boolean>> = callbackFlow {

        val eventDocument = firebase.getMachineStateRealTimeDB(idDocument)

        val suscription = object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(isClosedForSend){
                    return
                }
                try {
                    val machineState = dataSnapshot.value as Boolean
                    offer(Resource.Success(machineState))
                } catch(exception: CancellationException){
                    Log.d(TAG, "ERROR, CancelationException")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                channel.close(error.toException())
            }
        }
        eventDocument.addValueEventListener(suscription)

        awaitClose {
            suscription.onCancelled(error("Error en la base de datos"))
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getMachineStateFirestore(idDocument: String): Flow<Resource<Boolean>> = callbackFlow {

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

    fun getMachine(idDocument: String) = firebase.getMachine(idDocument)

    fun getDrinksList(idDocument: String): LiveData<MutableList<Drink>> = firebase.getDrinksList(idDocument)

    fun updateProcess(idDocument: String, process: MutableList<HashMap<String, Long>>) = firebase.updateProcessRealTimeDB(idDocument, process)

    fun updateMachineOnlineStatus(idDocument: String) = firebase.updateMachineOnlineStatusRealTimeDB(idDocument)

}