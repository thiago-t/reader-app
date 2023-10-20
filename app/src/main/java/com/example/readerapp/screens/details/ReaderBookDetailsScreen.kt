package com.example.readerapp.screens.details

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.readerapp.components.LinearLoader
import com.example.readerapp.components.ReaderAppTopBar
import com.example.readerapp.components.RoundedButton
import com.example.readerapp.data.Resource
import com.example.readerapp.model.Item
import com.example.readerapp.model.MBook
import com.example.readerapp.navigation.ReaderScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    navController: NavController,
    bookId: String,
    viewModel: ReaderBookDetailsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            ReaderAppTopBar(
                title = "Book details",
                navController = navController,
                icon = Icons.Default.ArrowBack,
                showProfile = false
            ) {
                navController.navigate(ReaderScreens.SearchScreen.name)
            }
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .consumeWindowInsets(it)
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val bookInfo = produceState<Resource<Item>>(initialValue = Resource.Loading()) {
                    value = viewModel.getBookInfo(bookId)
                }.value
                if (bookInfo.data == null) {
                    LinearLoader()
                } else {
                    BookDetails(book = bookInfo, navController = navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BookDetails(book: Resource<Item>? = null, navController: NavController? = null) {
    val bookInfo = book?.data?.volumeInfo
    val bookImage = bookInfo?.imageLinks?.thumbnail
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        bookImage?.let {
            Image(
                painter = rememberAsyncImagePainter(model = it),
                contentDescription = "Book image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(80.dp)
                    .height(80.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.padding(4.dp))

        Text(
            text = bookInfo?.title.toString(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Authors: ${bookInfo?.authors.toString()}",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Page count: ${bookInfo?.pageCount.toString()}",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Categories: ${bookInfo?.categories.toString()}",
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 3
        )

        Text(
            text = "Published date: ${bookInfo?.publishedDate.toString()}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.padding(4.dp))

        val cleanDescription = HtmlCompat.fromHtml(
            bookInfo?.description.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        TextField(
            value = cleanDescription.toString(),
            onValueChange = {},
            shape = TextFieldDefaults.outlinedShape,
            modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            RoundedButton(label = "Save") {
                val mBook = MBook(
                    title = bookInfo?.title,
                    authors = bookInfo?.authors.toString(),
                    description = bookInfo?.description,
                    categories = bookInfo?.categories.toString(),
                    notes = "",
                    photoUrl = bookInfo?.imageLinks?.thumbnail,
                    publishedDate = bookInfo?.publishedDate,
                    pageCount = bookInfo?.pageCount.toString(),
                    rating = bookInfo?.ratingsCount?.toDouble() ?: 0.0,
                    googleBookId = book?.data?.id,
                    userId = FirebaseAuth.getInstance().currentUser?.uid
                )
                saveToFirebase(mBook, navController)
            }
            Spacer(modifier = Modifier.width(25.dp))
            RoundedButton(label = "Cancel") {
                navController?.popBackStack()
            }
        }
    }
}

fun saveToFirebase(book: MBook?, navController: NavController?) {
    val db = FirebaseFirestore.getInstance()
    val collection = db.collection("books")
    if (book != null) {
        collection.add(book)
            .addOnSuccessListener {
                val docId = it.id
                collection.document(docId)
                    .update(hashMapOf("id" to docId) as Map<String, Any>)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            navController?.popBackStack()
                        }
                    }
                    .addOnFailureListener {
                        Log.d("DEBUG", "Error updating doc")
                    }
            }
    }
}
