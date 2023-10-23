package com.example.readerapp.screens.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.readerapp.components.LinearLoader
import com.example.readerapp.components.ReaderAppTopBar
import com.example.readerapp.data.DataOrException
import com.example.readerapp.model.MBook
import com.example.readerapp.screens.home.ReaderHomeScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
                    Text(text = viewModel.data.value.data.toString())
                }
            }
        }

    }
}