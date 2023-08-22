package com.batodev.sudoku.ui.learn.learnapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.batodev.sudoku.R
import com.batodev.sudoku.ui.learn.components.LearnRowItem

@Composable
fun LearnAppScreen(
    helpNavController: NavController
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyColumn {
            item {
                LearnRowItem(
                    title = stringResource(R.string.learn_app_toolbar),
                    subtitle = stringResource(R.string.learn_app_toolbar_desc),
                    onClick = { helpNavController.navigate("app_toolbar") }
                )
            }
        }
    }
}