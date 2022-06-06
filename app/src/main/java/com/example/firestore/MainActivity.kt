package com.example.firestore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

      val  btnUploadData  = findViewById<Button>(R.id.btnUploadData)
        val  etFirstName  = findViewById<EditText>(R.id.etFirstName)
        val  etLastName  = findViewById<EditText>(R.id.etLastName)
        val  etAge  = findViewById<EditText>(R.id.etAge)

        btnUploadData.setOnClickListener{
            val firstName = etFirstName.text.toString()
            val lastName =  etLastName.text.toString()
            val age =  etAge.text.toString()
            val person = Person(firstName, lastName, age)
            savePerson(person)
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