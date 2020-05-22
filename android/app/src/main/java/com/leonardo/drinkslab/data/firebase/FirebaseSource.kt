package com.leonardo.drinkslab.data.firebase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.leonardo.drinkslab.data.model.Drink
import com.leonardo.drinkslab.data.model.Ingredient
import com.leonardo.drinkslab.data.model.Machine
import com.leonardo.drinkslab.util.ErrorHandling
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class FirebaseSource {

    private val TAG = "AppDebug"

    private val firebaseCloudFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val firebaseRealTimeDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    private val machineReference by lazy {
        firebaseRealTimeDatabase.getReference("Machines")
    }

    fun getVersionCode() = firebaseCloudFirestore.collection("AppSettings")
        .document("Configuration")

    fun getMachineStateFirestore(idDocument: String) = firebaseCloudFirestore.collection("Machines")
        .document(idDocument)

    fun getMachineStateRealTimeDB(idDocument: String) = machineReference.child(idDocument)
        .child("isWorking")

    fun getMachine(idDocument: String): LiveData<Machine> {
        val mutableData = MutableLiveData<Machine>()

        firebaseCloudFirestore.collection("Machines")
            .document(idDocument)
            .get()
            .addOnSuccessListener { document ->

                val id = document.getLong("id")
                val QR = document.getString("QR")
                val dispenserCount = document.getDouble("dispenserCount")?.toInt()
                val isWorking = document.getBoolean("isWorking")
                val lightSecuence = document.getDouble("lightSecuence")?.toInt()
                val password = document.getString("password")

                val machine = Machine(id, QR, dispenserCount, isWorking, lightSecuence, password)


                mutableData.value = machine
            }
        return mutableData
    }

    fun getDrinksList2(idDocument: String) = firebaseCloudFirestore.collection("Machines")
        .document(idDocument)
        .collection("Drinks")

    fun getDrinksList(idDocument: String): LiveData<MutableList<Drink>> {
        val mutableData = MutableLiveData<MutableList<Drink>>()

        firebaseCloudFirestore.collection("Machines")
            .document(idDocument)
            .collection("Drinks")
            .get()
            .addOnSuccessListener { documentSnapshot ->
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
            }
        return mutableData
    }

    fun getIngredentsList(idDocument: String): LiveData<MutableList<Ingredient>>{

        val mutableData = MutableLiveData<MutableList<Ingredient>>()

        firebaseCloudFirestore.collection("Machines")
            .document(idDocument)
            .collection("Ingredients")
            .orderBy("position", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val list = mutableListOf<Ingredient>()

                for (document in documentSnapshot) {

                    val id = document.getLong("id")
                    val name = document.getString("name")
                    val category = document.getString("category")
                    val position = document.getLong("position")

                    val ingredient = Ingredient(category, id, name, position)

                    list.add(ingredient)
                }
                mutableData.value = list
            }
        return mutableData
    }

    fun updateProcessRealTimeDB(idDocument: String, process: MutableList<HashMap<String, Long>>) =
        Completable.create { emitter ->

            val onLineSubscription = object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val isOnline: Int = dataSnapshot.value.toString().toInt()
                    Log.d(TAG, "isMachineOnLine: $isOnline")

                    if (isOnline == 1) {
                        proceedToValidateState(idDocument, emitter, process)
                    } else {
                        emitter.onError(Throwable(ErrorHandling.MACHINE_OFFLINE))
                    }
                }

            }

            machineReference.child(idDocument).child("isOnline").addListenerForSingleValueEvent(onLineSubscription)
        }

    private fun proceedToValidateState(idDocument: String, emitter: CompletableEmitter, process: MutableList<HashMap<String, Long>>) {
        val isWorkingSubscription = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val isWorking = dataSnapshot.value.toString().toInt()
                Log.d(TAG, "isMachineWorking: $isWorking")

                if(isWorking == 0){
                    Log.d(TAG, "UPDATING MACHINE STATE...")

                    machineReference.child(idDocument).child("currentProcess").child("process").setValue(process)
                        .addOnCompleteListener { task ->
                            if (!emitter.isDisposed) {
                                if (task.isSuccessful) {
                                    Log.d(TAG, "DocumentSnapshot added")
                                    emitter.onComplete()
                                } else {
                                    emitter.onError(task.exception!!)
                                }
                            }
                        }

                    machineReference.child(idDocument).child("isWorking").setValue(1)
                        .addOnCompleteListener { task ->
                            if (!emitter.isDisposed) {
                                if (task.isSuccessful) {
                                    Log.d(TAG, "DocumentSnapshot added")
                                    emitter.onComplete()
                                } else {
                                    emitter.onError(task.exception!!)
                                }
                            }
                        }
                }else {
                    emitter.onError(Throwable(ErrorHandling.MACHINE_WORKING))
                }
            }
        }

        getMachineStateRealTimeDB(idDocument).addListenerForSingleValueEvent(isWorkingSubscription)
    }

    fun updateMachineOnlineStatusRealTimeDB(idDocument: String){
        Log.d(TAG, "UPDATING MACHINE ONLINE STATUS...")

        machineReference.child(idDocument).child("isOnline").setValue(0)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "DocumentSnapshot added")
                } else {
                    Log.e(TAG, "ERROR UPDATING MACHINE STATUS, ${task.exception}")
                }
            }

    }

    fun updateProcessFirestore(idDocument: String, process: MutableList<Int>) =
        Completable.create { emitter ->

            val infoUpdated: MutableMap<String, Any> = HashMap()
            infoUpdated["currentProcess"] = process
            infoUpdated["isWorking"] = true

            firebaseCloudFirestore.collection("Machines")
                .document(idDocument)
                .update(infoUpdated)
                .addOnCompleteListener { task ->
                    if (!emitter.isDisposed) {
                        if (task.isSuccessful) {
                            Log.d(TAG, "DocumentSnapshot added")
                            emitter.onComplete()
                        } else {
                            emitter.onError(task.exception!!)
                        }
                    }
                }
        }

    fun addDrink(idDocument: String, drink: Drink) =
        Completable.create { emitter ->

            firebaseCloudFirestore.collection("Machines")
                .document(idDocument)
                .collection("Drinks")
                .add(drink)
                .addOnCompleteListener{task ->
                    if(!emitter.isDisposed){
                        if(task.isSuccessful){
                            Log.d(TAG, "DocumentSnapshot added")
                            emitter.onComplete()
                        } else {
                            Log.d(TAG, "Error adding new drink")
                            emitter.onError(task.exception!!)
                        }
                    }
                }
        }
}