package com.batodev.sudoku.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.batodev.sudoku.R

/**
 * The standard back-navigation [IconButton] used as `navigationIcon` on top app bars across the
 * app. Extracted to remove the repeated back-arrow-icon boilerplate.
 */
@Composable
fun BackIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_round_arrow_back_24),
            contentDescription = null,
        )
    }
}

/**
 * The standard "more options" overflow menu (a [Icons.Default.MoreVert] icon that opens a
 * rounded [DropdownMenu]) used as `actions` content on several top app bars. [menuItems] receives
 * a `closeMenu` callback that dismisses the menu, for use in each item's `onClick`.
 */
@Composable
fun OverflowMenuButton(
    modifier: Modifier = Modifier,
    menuItems: @Composable ColumnScope.(closeMenu: () -> Unit) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { showMenu = !showMenu }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = null,
            )
        }
        RoundedDropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            menuItems { showMenu = false }
        }
    }
}

/**
 * A [DropdownMenu] with the app's standard rounded corner shape override, used by every dropdown
 * menu in the app (overflow menus, game menus, difficulty pickers, etc).
 */
@Composable
fun RoundedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = MaterialTheme.shapes.large)) {
        DropdownMenu(
            modifier = modifier,
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            content = content,
        )
    }
}
