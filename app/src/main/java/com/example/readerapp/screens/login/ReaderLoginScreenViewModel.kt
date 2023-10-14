package com.example.readerapp.screens.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readerapp.model.MUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ReaderLoginScreenViewModel : ViewModel() {

    companion object {
        private const val USERS_COLLECTION_NAME = "users"
    }

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun signInWithEmailAndPassword(email: String, password: String, home: () -> Unit) =
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("LOGIN", "Successful signedIn")
                            home.invoke()
                        } else {
                            Log.d("LOGIN", "Failed to signIn")
                        }
                    }
            } catch (e: Exception) {
                Log.d("LOGIN", e.message.toString())
            }
        }

    fun createUserWithEmailAndPassword(email: String, password: String, home: () -> Unit) {
        if (_loading.value == false) {
            _loading.value = true
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val displayName = task.result.user?.email?.split('@')?.get(0)
                        createUser(displayName)
                        home.invoke()
                    } else {
                        Log.d("LOGIN", "Failed to createUser")
                    }
                }
            _loading.value = false
        }
    }

    private fun createUser(displayName: String?) {
        val userId = auth.currentUser?.uid
        val user = MUser(
            id = null,
            userId = userId.toString(),
            displayName = displayName.toString(),
            avatarUrl = "",
            quote = "Life is good",
            profession = "Android Developer"
        ).toMap()
        db.collection(USERS_COLLECTION_NAME).add(user)
    }

}