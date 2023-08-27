package com.batodev.sudoku.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.batodev.sudoku.R
import com.batodev.sudoku.ui.theme.SudokuTheme
import com.batodev.sudoku.ui.util.LightDarkPreview

@Composable
fun HelpCard(
    modifier: Modifier = Modifier,
    title: String,
    details: String,
    painter: Painter?,
    onCloseClicked: () -> Unit
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (painter != null) {
                        Icon(
                            painter = painter,
                            contentDescription = null
                        )
                    }
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                IconButton(onClick = onCloseClicked) {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_close_24),
                        contentDescription = null
                    )
                }
            }
            Text(
                text = details,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@LightDarkPreview
@Composable
fun HelpCardPreview() {
    SudokuTheme {
        HelpCard(
            title = "This is the title",
            details = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam tempus arcu vitae elit congue scelerisque. Sed a vestibulum tellus. Suspendisse tristique dui eget nisi dictum tempus",
            painter = painterResource(R.drawable.ic_outline_verified_24),
            onCloseClicked = {}
        )
    }
}