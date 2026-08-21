package com.batodev.sudoku.ui.learn.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.batodev.sudoku.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialBase(
    title: String,
    helpNavController: NavController,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {
        IconButton(onClick = {
            helpNavController.popBackStack()
        }) {
            Icon(
                painter = painterResource(R.drawable.ic_round_arrow_back_24),
                contentDescription = null,
            )
        }
    },
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = navigationIcon,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            content()
        }
    }
}
