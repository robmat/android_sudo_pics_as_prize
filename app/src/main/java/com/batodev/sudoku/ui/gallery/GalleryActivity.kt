package com.batodev.sudoku.ui.gallery

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.batodev.sudoku.core.utils.AdHelper
import com.batodev.sudoku.data.datastore.ThemeSettingsManager
import com.batodev.sudoku.data.settings.SettingsHelper
import com.batodev.sudoku.ui.theme.AppTheme
import com.batodev.sudoku.ui.theme.SudokuTheme
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

const val tmpShared = "tmp_shared"
const val tmpImgPath = "$tmpShared/tmp.jpg"
const val PRIZE_IMAGES = "prize-images"

@AndroidEntryPoint
class GalleryActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private var isActivityVisible = true
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(GalleryActivity::class.java.simpleName, "onCreate")
        super.onCreate(savedInstanceState)
        handlerAdPosting()

        setContent {
            val mainViewModel: GalleryActivityViewModel = hiltViewModel()

            val dynamicColors by mainViewModel.dc.collectAsState(isSystemInDarkTheme())
            val darkTheme by mainViewModel.darkTheme.collectAsState(PreferencesConstants.DEFAULT_DARK_THEME)
            val amoledBlack by mainViewModel.amoledBlack.collectAsState(PreferencesConstants.DEFAULT_AMOLED_BLACK)
            val currentTheme by mainViewModel.currentTheme.collectAsState(PreferencesConstants.DEFAULT_SELECTED_THEME)

            SudokuTheme(
                darkTheme = when (darkTheme) {
                    1 -> false
                    2 -> true
                    else -> isSystemInDarkTheme()
                },
                dynamicColor = dynamicColors,
                amoled = amoledBlack,
                appTheme = when (currentTheme) {
                    PreferencesConstants.GREEN_THEME_KEY -> AppTheme.Green
                    PreferencesConstants.BLUE_THEME_KEY -> AppTheme.Blue
                    PreferencesConstants.PEACH_THEME_KEY -> AppTheme.Peach
                    PreferencesConstants.YELLOW_THEME_KEY -> AppTheme.Yellow
                    PreferencesConstants.LAVENDER_THEME_KEY -> AppTheme.Lavender
                    PreferencesConstants.BLACK_AND_WHITE_THEME_KEY -> AppTheme.BlackAndWhite
                    else -> AppTheme.Green
                }
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ImageViewerScreen(this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
    }

    override fun onPause() {
        super.onPause()
        isActivityVisible = false
    }

    fun isActivityVisible(): Boolean {
        return isActivityVisible
    }

    private fun handlerAdPosting() {
        handler.postDelayed({
            Log.d(GalleryActivity::class.java.simpleName, "Showing ad.")
            if (isActivityVisible()) {
                AdHelper.showAd(this)
            }
            handlerAdPosting()
        }, 20000)
    }
}

@Composable
fun ImageViewerScreen(galleryActivity: GalleryActivity) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "imageList") {
        composable("imageList") { ImageListScreen(navController, galleryActivity) }
        composable(
            "imageDetail/{index}",
            arguments = listOf(navArgument("index") { type = NavType.StringType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index") ?: 0
            ImageDetailScreen(index as String, navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageListScreen(navController: NavController, galleryActivity: GalleryActivity) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(text = stringResource(id = R.string.uncovered_images)) },
            navigationIcon = {
                IconButton(onClick = { galleryActivity.finish() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            })
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
fun ImageListItem(imageResId: String, navController: NavController) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .clickable { navController.navigate("imageDetail/$imageResId") }) {
        GlideImage(
            model = "file:///android_asset/$PRIZE_IMAGES/$imageResId",
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .clip(shape = MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailScreen(resId: String, navController: NavController) {
    val context = LocalContext.current
    val currentPicture = remember {
        mutableStateOf("file:///android_asset/$PRIZE_IMAGES/$resId")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TopAppBar(title = { Text(text = stringResource(id = R.string.uncovered_images)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            })

        Spacer(modifier = Modifier.height(16.dp))

        GlideImage(
            model = currentPicture.value,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .zoomable(),
            contentScale = ContentScale.FillHeight
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {
            val currentPictureFileName =
                currentPicture.value.substring(currentPicture.value.lastIndexOf("/") + 1)
            val uncoveredPics = SettingsHelper(context).preferences.uncoveredPics
            IconButton(onClick = {
                val index = uncoveredPics.indexOf(currentPictureFileName)
                currentPicture.value = "file:///android_asset/$PRIZE_IMAGES/${
                    uncoveredPics[Math.max(
                        0, index - 1
                    )]
                }"
            }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = {
                val index = uncoveredPics.indexOf(currentPictureFileName)
                currentPicture.value = "file:///android_asset/$PRIZE_IMAGES/${
                    uncoveredPics[Math.min(
                        uncoveredPics.size - 1, index + 1
                    )]
                }"
            }) {
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = {
                val inputStream: InputStream =
                    context.assets.open("$PRIZE_IMAGES/${Uri.parse(currentPicture.value).lastPathSegment}")

                val file = File(context.filesDir, tmpImgPath)
                File(context.filesDir, tmpShared).mkdirs()
                file.delete()
                val outputStream: OutputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                inputStream.close()
                outputStream.close()
                val shareIntent = Intent(Intent.ACTION_SEND)
                val uri =
                    Uri.parse("content://com.batodev.sudoku.data.provider.ImagesProvider/$tmpImgPath")
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.type = "image/*"
                ContextCompat.startActivity(context, shareIntent, null)
            }) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null)
            }
        }
    }
}


@HiltViewModel
class GalleryActivityViewModel
@Inject constructor(
    themeSettingsManager: ThemeSettingsManager
) : ViewModel() {
    val dc = themeSettingsManager.dynamicColors
    val darkTheme = themeSettingsManager.darkTheme
    val amoledBlack = themeSettingsManager.amoledBlack
    val currentTheme = themeSettingsManager.currentTheme
}