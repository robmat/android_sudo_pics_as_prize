package com.batodev.sudoku.ui.learn.learnapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.batodev.sudoku.R
import com.batodev.sudoku.ui.learn.components.TutorialBase
import com.batodev.sudoku.ui.onboarding.FirstGameScreen

@Composable
fun ToolbarTutorialScreen(helpNavController: NavController) {
    TutorialBase(
        title = stringResource(R.string.learn_app_toolbar),
        helpNavController = helpNavController
    ) {
        FirstGameScreen()
        Text(
            text = stringResource(R.string.learn_app_toolbar_notes_menu),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}