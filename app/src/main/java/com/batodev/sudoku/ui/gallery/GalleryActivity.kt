package com.batodev.sudoku.ui.gallery

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.batodev.sudoku.R
import com.batodev.sudoku.core.PreferencesConstants
import com.batodev.sudoku.core.utils.AdSupportedActivity
import com.batodev.sudoku.data.datastore.ThemeSettingsManager
import com.batodev.sudoku.data.settings.SettingsHelper
import com.batodev.sudoku.ui.theme.SudokuTheme
import com.batodev.sudoku.ui.theme.resolveAppTheme
import com.batodev.sudoku.ui.theme.resolveDarkTheme
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import nl.birdly.zoombox.zoomable
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

const val TMP_SHARED = "tmp_shared"
const val TMP_IMG_PATH = "$TMP_SHARED/tmp.jpg"
const val PRIZE_IMAGES = "prize-images"
private const val AD_CHECK_INTERVAL_MS = 20000L
private const val COPY_BUFFER_SIZE = 1024

@AndroidEntryPoint
class GalleryActivity : AdSupportedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(GalleryActivity::class.java.simpleName, "onCreate")
        super.onCreate(savedInstanceState)
        handlerAdPosting(AD_CHECK_INTERVAL_MS)

        setContent {
            val mainViewModel: GalleryActivityViewModel = hiltViewModel()

            val dynamicColors by mainViewModel.dc.collectAsState(isSystemInDarkTheme())
            val darkTheme by mainViewModel.darkTheme.collectAsState(PreferencesConstants.DEFAULT_DARK_THEME)
            val amoledBlack by mainViewModel.amoledBlack.collectAsState(PreferencesConstants.DEFAULT_AMOLED_BLACK)
            val currentTheme by mainViewModel.currentTheme.collectAsState(PreferencesConstants.DEFAULT_SELECTED_THEME)

            SudokuTheme(
                darkTheme = resolveDarkTheme(darkTheme),
                dynamicColor = dynamicColors,
                amoled = amoledBlack,
                appTheme = resolveAppTheme(currentTheme),
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ImageViewerScreen(this)
                }
            }
        }
    }
}

@Composable
fun ImageViewerScreen(galleryActivity: GalleryActivity) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "imageList") {
        composable("imageList") { ImageListScreen(navController, galleryActivity) }
        composable(
            "imageDetail/{index}",
            arguments = listOf(navArgument("index") { type = NavType.StringType }),
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index") ?: 0
            ImageDetailScreen(index as String, navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageListScreen(
    navController: NavController,
    galleryActivity: GalleryActivity,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Box(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.uncovered_images)) },
            navigationIcon = {
                IconButton(onClick = { galleryActivity.finish() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            },
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.padding(0.dp, 60.dp, 0.dp, 0.dp)) {
            val items = SettingsHelper(context).preferences.uncoveredPics
            items(items) { imageResId ->
                ImageListItem(imageResId, navController)
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageListItem(
    imageResId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { navController.navigate("imageDetail/$imageResId") },
    ) {
        GlideImage(
            model = "file:///android_asset/$PRIZE_IMAGES/$imageResId",
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .clip(shape = MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop,
        )
    }
}

private fun previousImageUri(
    uncoveredPics: List<String>,
    currentFileName: String,
): String {
    val index = uncoveredPics.indexOf(currentFileName)
    return "file:///android_asset/$PRIZE_IMAGES/${uncoveredPics[(index - 1).coerceAtLeast(0)]}"
}

private fun nextImageUri(
    uncoveredPics: List<String>,
    currentFileName: String,
): String {
    val index = uncoveredPics.indexOf(currentFileName)
    return "file:///android_asset/$PRIZE_IMAGES/${uncoveredPics[(index + 1).coerceAtMost(uncoveredPics.size - 1)]}"
}

private fun shareImage(
    context: android.content.Context,
    currentPicture: String,
) {
    val inputStream: InputStream =
        context.assets.open("$PRIZE_IMAGES/${Uri.parse(currentPicture).lastPathSegment}")

    val file = File(context.filesDir, TMP_IMG_PATH)
    File(context.filesDir, TMP_SHARED).mkdirs()
    file.delete()
    val outputStream: OutputStream = FileOutputStream(file)
    val buffer = ByteArray(COPY_BUFFER_SIZE)
    var bytesRead: Int
    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        outputStream.write(buffer, 0, bytesRead)
    }
    inputStream.close()
    outputStream.close()
    val shareIntent = Intent(Intent.ACTION_SEND)
    val uri =
        Uri.parse("content://com.batodev.sudoku.data.provider.ImagesProvider/$TMP_IMG_PATH")
    shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
    shareIntent.clipData = android.content.ClipData.newRawUri("", uri)
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    shareIntent.type = "image/*"
    ContextCompat.startActivity(context, shareIntent, null)
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ImageDetailNavigationRow(
    context: android.content.Context,
    currentPicture: androidx.compose.runtime.MutableState<String>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        val currentPictureFileName =
            currentPicture.value.substring(currentPicture.value.lastIndexOf("/") + 1)
        val uncoveredPics = SettingsHelper(context).preferences.uncoveredPics
        IconButton(onClick = {
            currentPicture.value = previousImageUri(uncoveredPics, currentPictureFileName)
        }) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
        }
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = {
            currentPicture.value = nextImageUri(uncoveredPics, currentPictureFileName)
        }) {
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
        }
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = {
            shareImage(context, currentPicture.value)
        }) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailScreen(
    resId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val currentPicture =
        remember {
            mutableStateOf("file:///android_asset/$PRIZE_IMAGES/$resId")
        }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
    ) {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.uncovered_images)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        GlideImage(
            model = currentPicture.value,
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .zoomable(),
            contentScale = ContentScale.FillHeight,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ImageDetailNavigationRow(context, currentPicture)
    }
}

@HiltViewModel
class GalleryActivityViewModel
    @Inject
    constructor(
        themeSettingsManager: ThemeSettingsManager,
    ) : ViewModel() {
        val dc = themeSettingsManager.dynamicColors
        val darkTheme = themeSettingsManager.darkTheme
        val amoledBlack = themeSettingsManager.amoledBlack
        val currentTheme = themeSettingsManager.currentTheme
    }
