package com.batodev.sudoku.ui.more.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.batodev.sudoku.R
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.chipColors
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutLibrariesScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier =
            modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.libraries_licenses_title)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painterResource(R.drawable.ic_round_arrow_back_24),
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LibrariesContainer(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            colors =
                LibraryDefaults.libraryColors(
                    libraryBackgroundColor = MaterialTheme.colorScheme.background,
                    libraryContentColor = MaterialTheme.colorScheme.onBackground,
                    versionChipColors =
                        LibraryDefaults.chipColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    licenseChipColors =
                        LibraryDefaults.chipColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    fundingChipColors =
                        LibraryDefaults.chipColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                ),
        )
    }
}
