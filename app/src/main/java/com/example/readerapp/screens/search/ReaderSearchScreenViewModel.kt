package com.example.readerapp.screens.search

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readerapp.data.Resource
import com.example.readerapp.model.Item
import com.example.readerapp.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderSearchScreenViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {
    var list: List<Item> by mutableStateOf(listOf())
    var isLoading: Boolean by mutableStateOf(true)

    init {
        searchBooks("android")
    }

    fun searchBooks(query: String) {
        viewModelScope.launch(Dispatchers.Default) {
            if (query.isEmpty()) return@launch
            try {
                when (val response = repository.getBooks(query)) {
                    is Resource.Success -> list = response.data!!
                    is Resource.Error -> Log.d("Network", response.message!!)
                    else -> {}
                }
            } catch (exception: Exception) {
                Log.d("Network", exception.message.toString())
            }
            isLoading = false
        }
    }

}