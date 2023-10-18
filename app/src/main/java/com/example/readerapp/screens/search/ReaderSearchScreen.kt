package com.example.readerapp.screens.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.readerapp.components.InputField
import com.example.readerapp.components.ReaderAppTopBar
import com.example.readerapp.model.MBook
import com.example.readerapp.navigation.ReaderScreens

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReaderSearchScreen(navController: NavController) {
    Scaffold(topBar = {
        ReaderAppTopBar(
            title = "Search Books",
            icon = Icons.Default.ArrowBack,
            navController = navController,
            showProfile = false
        ) {
            navController.popBackStack()
        }
    }) {
        Surface(
            modifier = Modifier
                .padding(it)
                .consumeWindowInsets(it)
        ) {
            Column {
                SearchForm(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                }
                Spacer(modifier = Modifier.height(8.dp))
                val books = listOf(
                    MBook(title = "Hello again", authors = "All of us"),
                    MBook(title = "Hello 2", authors = "All of us"),
                    MBook(title = "Hello Very good", authors = "All of us"),
                    MBook(title = "Hello AMIGOS da rede gLoBo", authors = "All of us")
                )
                SearchBooksList(books, navController)
            }
        }
    }
}

@Composable
fun SearchBooksList(books: List<MBook>, navController: NavController) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        items(items = books) { book ->
            BooksRow(book, navController)
        }
    }
}

@Composable
fun BooksRow(book: MBook, navController: NavController) {
    Card(
        modifier = Modifier
            .clickable { }
            .fillMaxWidth()
            .height(100.dp)
            .padding(3.dp),
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(7.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Row(modifier = Modifier.padding(5.dp), verticalAlignment = Alignment.Top) {
            val imageUrl =
                "http://books.google.com/books/content?id=mmx3CgAAQBAJ&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api"
            Image(
                painter = rememberAsyncImagePainter(model = imageUrl),
                contentDescription = "Book Image",
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .padding(end = 4.dp)
            )
            Column {
                Text(text = book.title.toString(), overflow = TextOverflow.Ellipsis)
                Text(
                    text = "Authors: ${book.authors.toString()}",
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchForm(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    hint: String = "Search",
    onSearch: (String) -> Unit = {}
) {
    Column {
        val searchQueryState = rememberSaveable { mutableStateOf("") }
        val keyboardController = LocalSoftwareKeyboardController.current
        val valid = remember(searchQueryState.value) { searchQueryState.value.trim().isNotEmpty() }

        InputField(
            modifier = modifier,
            valueState = searchQueryState,
            labelId = "Search",
            enabled = true,
            onAction = KeyboardActions {
                if (!valid) return@KeyboardActions
                onSearch.invoke(searchQueryState.value.trim())
                searchQueryState.value = ""
                keyboardController?.hide()
            })
    }
}
