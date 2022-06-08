package com.example.firestore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
//    Firestore stores data in collection of documents.
//    Means we start with collections .
//    That contains documents.
//    First we will get a reference to collection

    //take out instance of firestore database
    private val fireStoreDb = Firebase.firestore

    //take out particular collection reference
    private val personCollectionRef = fireStoreDb.collection("persons")

        //assign views using findViewByID
        lateinit var  btnUploadData  : Button
    lateinit var  etFirstName : EditText
    lateinit var  etLastName  : EditText
    lateinit var  etAge  : EditText
    lateinit var btnRetieveData : Button
    lateinit var tvPerson : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnUploadData  = findViewById(R.id.btnUploadData)
        etFirstName  = findViewById(R.id.etFirstName)
        etLastName  = findViewById(R.id.etLastName)
        etAge  = findViewById(R.id.etAge)
        btnRetieveData = findViewById(R.id.btnRetrieveData)
        tvPerson = findViewById(R.id.tvPersons)

        btnUploadData.setOnClickListener{
            val person = getOldPerson()
                savePerson(person)
        }
        btnUpdatePerson.setOnClickListener {
            val oldPerson = getOldPerson()
            val newPersonMap = getNewPerson()
            updatePerson(oldPerson, newPersonMap)
        }

       btnRetieveData.setOnClickListener {
            retrievePersons()
        }
    }

    private fun getOldPerson() : Person{
        val firstName = etFirstName.text.toString()
        val lastName =  etLastName.text.toString()
        val age =  etAge.text.toString().toInt()
       return Person(firstName, lastName, age)
    }

    private fun getNewPerson() : Map<String,Any>{
        //we will create a map of type string and any as field can have different type values
        val firstName = etNewFirstName.text.toString()
        val lastName =  etNewLastName.text.toString()
        val age =  etNewAge.text.toString()
        //wont convert age from string to int .
    // If edit text as empty value then conversion from string to int will throw error
        //create mutable map
        val map =  mutableMapOf<String,Any>()
        if(firstName.isNotEmpty()){
            map["firstName"] = firstName
        }
        if(lastName.isNotEmpty()){
            map["firstName"] = lastName
        }
        if(age.isNotEmpty()){
            map["firstName"] = age.toInt()
        }
 return  map
    }

    private fun updatePerson(person: Person, newPersonMap: Map<String, Any>) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personCollectionRef
            .whereEqualTo("firstName", person.firstName)
            .whereEqualTo("lastName", person.lastName)
            .whereEqualTo("age", person.age)
            .get()
            .await()
        if(personQuery.documents.isNotEmpty()) {
            for(document in personQuery) {
                try {
                    //personCollectionRef.document(document.id).update("age", newAge).await()
                    personCollectionRef.document(document.id).set(
                        newPersonMap,
                        SetOptions.merge()
                    ).await()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "No persons matched the query.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun subscribeToRealTimeUpdates(){
        personCollectionRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            querySnapshot?.let {
            val sb = StringBuilder()
                for(document in querySnapshot.documents){
                    val person = document.toObject<Person>()
                    sb.append("$person\n")
                    // we get data from document and convert the data to person class
                }
                tvPerson.text = sb.toString()
            }
        }
    }


    private fun retrievePersons() = CoroutineScope(Dispatchers.IO).launch {
        val fromAge = etFrom.text.toString().toInt()
        val toAge = etTo.text.toString().toInt()

        try {
            //create querySnapShot - it is result of our query to fireStore
           //we will get queries to firestore using collection object
            val querySnapshot = personCollectionRef
                .whereGreaterThan("age",fromAge)
                .whereLessThan("age",toAge)
                .orderBy("age")
                .get()
                .await()
            // now we can use querySnapshot to loop over the documents
            val sb = StringBuilder()
            //querySnapShot contains group of documentSnapshot
            //documentSnapShot contain information about particular document
            for(document in querySnapshot.documents){
                val person = document.toObject<Person>()
                sb.append("$person\n")
                // we get data from document and convert the data to person class
            }
            withContext(Dispatchers.Main){
                tvPerson.text = sb.toString()
            }

        }catch (e:Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
            }


    private fun savePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {

            //await() is used to wait thread till uploading data is finished
            personCollectionRef.add(person).await()
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, "Data Successfully saved", Toast.LENGTH_SHORT).show()
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}