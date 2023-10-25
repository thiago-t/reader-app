package com.example.readerapp.screens.update

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.readerapp.R
import com.example.readerapp.components.InputField
import com.example.readerapp.components.LinearLoader
import com.example.readerapp.components.RatingBar
import com.example.readerapp.components.ReaderAppTopBar
import com.example.readerapp.components.RoundedButton
import com.example.readerapp.data.DataOrException
import com.example.readerapp.model.MBook
import com.example.readerapp.navigation.ReaderScreens
import com.example.readerapp.screens.home.ReaderHomeScreenViewModel
import com.example.readerapp.utils.formatted
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ReaderUpdateScreen(
    navController: NavHostController,
    bookItemId: String,
    viewModel: ReaderHomeScreenViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            ReaderAppTopBar(
                title = "Update book",
                icon = Icons.Default.ArrowBack,
                showProfile = false,
                navController = navController
            ) { navController.popBackStack() }
        }
    ) {
        val bookInfo = produceState<DataOrException<List<MBook>, Boolean, Exception>>(
            initialValue = DataOrException(emptyList(), true, Exception())
        ) {
            value = viewModel.data.value
        }.value

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(
                Modifier.padding(top = 3.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (bookInfo.loading == true) {
                    LinearLoader()
                    bookInfo.loading = false
                } else {
                    Surface(
                        modifier = Modifier
                            .padding(2.dp)
                            .fillMaxWidth(),
                        shape = CircleShape,
                        shadowElevation = 4.dp
                    ) {
                        ShowBookUpdate(bookInfo = viewModel.data.value, bookItemId = bookItemId)
                    }
                    ShowSimpleForm(
                        book = viewModel.data.value.data?.firstOrNull { it.id == bookItemId },
                        navController
                    )
                }
            }
        }

    }
}

@ExperimentalComposeUiApi
@Composable
fun ShowSimpleForm(book: MBook?, navController: NavController) {
    val notesText = remember { mutableStateOf("") }
    val isStartedReading = remember { mutableStateOf(false) }
    val isFinishedReading = remember { mutableStateOf(false) }
    val rating = remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    SimpleForm(defaultValue = if (book?.notes?.isNotEmpty() == true) book.notes.toString() else "No thoughts yet.") { note ->
        notesText.value = note
    }

    Row(
        modifier = Modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        TextButton(onClick = {
            isStartedReading.value = true
        }, enabled = book?.startedReading == null) {
            if (book?.startedReading == null) {
                if (!isStartedReading.value) {
                    Text(text = "Start Reading")
                } else {
                    Text(
                        text = "Started Reading!",
                        modifier = Modifier.alpha(0.6f),
                        color = Color.Red.copy(alpha = 0.5f)
                    )
                }
            } else {
                Text(text = "Started on ${book.startedReading?.formatted()}") // TODO: FORMAT DATE
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        TextButton(onClick = {
            isFinishedReading.value = true
        }, enabled = book?.finishedReading == null) {
            if (book?.finishedReading == null) {
                if (!isFinishedReading.value) {
                    Text(text = "Mark as Read")
                } else {
                    Text(text = "Finished Reading!")
                }
            } else {
                Text(text = "Finished on: ${book.finishedReading?.formatted()}") // TODO: FORMAT DATE
            }
        }
    }

    Text(text = "Rating", modifier = Modifier.padding(bottom = 3.dp))
    book?.rating?.toInt()?.let {
        RatingBar(rating = it) { ratingValue ->
            rating.value = ratingValue
        }
    }

    Spacer(modifier = Modifier.padding(bottom = 15.dp))

    Row {
        val changedNotes = book?.notes != notesText.value
        val changedRating = book?.rating?.toInt() != rating.value
        val isFinishedTimeStamp =
            if (isFinishedReading.value) Timestamp.now() else book?.finishedReading
        val isStartedTimeStamp =
            if (isStartedReading.value) Timestamp.now() else book?.startedReading
        val hasToUpdate =
            changedNotes || changedRating || isStartedReading.value || isFinishedReading.value
        val bookToUpdate = hashMapOf(
            "finished_reading_at" to isFinishedTimeStamp,
            "started_reading_at" to isStartedTimeStamp,
            "rating" to rating.value,
            "notes" to notesText.value
        ).toMap()

        RoundedButton(label = "Update") {
            if (hasToUpdate) {
                FirebaseFirestore.getInstance()
                    .collection("books")
                    .document(book?.id.orEmpty())
                    .update(bookToUpdate)
                    .addOnCompleteListener {
                        showToast(context, "Book updated successfully!")
                        navController.navigate(ReaderScreens.ReaderHomeScreen.name)
                    }
                    .addOnFailureListener {
                        Log.d("DEBUG", "Error updating document", it)
                    }
            }
        }

        Spacer(modifier = Modifier.width(100.dp))

        val openDialog = remember { mutableStateOf(false) }
        if (openDialog.value) {
            ShowAlertDialog(openDialog) {
                FirebaseFirestore.getInstance()
                    .collection("books")
                    .document(book?.id.orEmpty())
                    .delete()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            openDialog.value = false
                            navController.navigate(ReaderScreens.ReaderHomeScreen.name)
                        }
                    }
                    .addOnFailureListener {

                    }
            }
        }
        RoundedButton(label = "Delete") {
            openDialog.value = true
        }
    }
}

@Composable
fun ShowAlertDialog(openDialog: MutableState<Boolean>, onConfirmPressed: () -> Unit) {
    val message =
        stringResource(id = R.string.msg_are_you_sure) + "\n" + stringResource(id = R.string.msg_cannot_undone)

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = { Text(text = "Delete book") },
            text = { Text(text = message) },
            dismissButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text(text = "Cancel")
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirmPressed.invoke() }) {
                    Text(text = "Confirm")
                }
            }
        )
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SimpleForm(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    defaultValue: String = "Great Book!",
    onSearch: (String) -> Unit
) {
    Column {
        val textFieldValue = rememberSaveable { mutableStateOf(defaultValue) }
        val keyboardController = LocalSoftwareKeyboardController.current
        val valid = remember(textFieldValue.value) { textFieldValue.value.trim().isNotEmpty() }
        InputField(modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(3.dp)
            .background(Color.White, CircleShape)
            .padding(horizontal = 20.dp, vertical = 12.dp),
            valueState = textFieldValue,
            labelId = "Enter your thoughts",
            enabled = true,
            onAction = KeyboardActions {
                if (!valid) return@KeyboardActions
                onSearch.invoke(textFieldValue.value.trim())
                keyboardController?.hide()
            })
    }
}

@Composable
fun ShowBookUpdate(bookInfo: DataOrException<List<MBook>, Boolean, Exception>, bookItemId: String) {
    Row {
        Spacer(modifier = Modifier.width(43.dp))
        if (bookInfo.data != null) {
            Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.Center) {
                CardListItem(book = bookInfo.data?.firstOrNull { it.id == bookItemId }) {

                }
            }
        }
    }
}

@Composable
fun CardListItem(
    book: MBook?,
    onPressDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onPressDetails.invoke() },
        elevation = CardDefaults.cardElevation(8.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(horizontalArrangement = Arrangement.Start) {
            Image(
                painter = rememberAsyncImagePainter(model = book?.photoUrl.toString()),
                contentDescription = null,
                modifier = Modifier
                    .height(100.dp)
                    .width(120.dp)
                    .padding(4.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 120.dp,
                            topEnd = 20.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 0.dp
                        )
                    )
            )
            Column {
                Text(
                    text = book?.title.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .width(120.dp),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book?.authors.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 0.dp)
                )
                Text(
                    text = book?.publishedDate.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 8.dp)
                )
            }
        }
    }
}
