package com.quickits.zpatch.sample

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.quickits.zpatch.ZPatch
import com.quickits.zpatch.sample.ui.theme.ZpatchTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZpatchTheme {
                ZPatchUI()
            }
        }
    }
}

enum class PatchTab(val title: String, val icon: ImageVector) {
    Create("Create", Icons.Outlined.AddCircle),
    Apply("Apply", Icons.Outlined.Done)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZPatchUI() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Permissions
    var hasPermissions by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) true
            else {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions -> hasPermissions = permissions.values.all { it } }

    // State
    var createOldFile by remember { mutableStateOf<Uri?>(null) }
    var createNewFile by remember { mutableStateOf<Uri?>(null) }
    var createResult by remember { mutableStateOf<String?>(null) }
    var isCreating by remember { mutableStateOf(false) }

    var applyOldFile by remember { mutableStateOf<Uri?>(null) }
    var applyPatchFile by remember { mutableStateOf<Uri?>(null) }
    var applyResult by remember { mutableStateOf<String?>(null) }
    var isApplying by remember { mutableStateOf(false) }

    var version by remember { mutableStateOf("Loading...") }
    var patchOutputPath by remember { mutableStateOf("") }
    var restoredOutputPath by remember { mutableStateOf("") }

    // Pager & Drawer state
    val pagerState = rememberPagerState(pageCount = { PatchTab.entries.size })
    var versionForDrawer by remember { mutableStateOf("Loading...") }
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        val ver = try { "v${ZPatch.getVersion()}" } catch (e: Exception) { "ZPatch" }
        version = ver
        versionForDrawer = ver
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        patchOutputPath = File(downloadDir, "patch_${System.currentTimeMillis()}.zst").absolutePath
        restoredOutputPath = File(downloadDir, "restored_${System.currentTimeMillis()}").absolutePath
    }

    // File pickers
    val pickOldForCreate = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            createOldFile = uri
            createResult = null
        }
    }
    val pickNewForCreate = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            createNewFile = uri
            createResult = null
        }
    }
    val pickOldForApply = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            applyOldFile = uri
            applyResult = null
        }
    }
    val pickPatchFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            applyPatchFile = uri
            applyResult = null
        }
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !hasPermissions) {
        LaunchedEffect(Unit) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    suspend fun uriToTempFile(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}")
            tempFile.outputStream().use { input.copyTo(it) }
            tempFile.absolutePath
        } catch (e: Exception) { null }
    }

    suspend fun tempFileToDownload(tempPath: String, outputPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(tempPath).inputStream().use { input ->
                File(outputPath).outputStream().use { input.copyTo(it) }
            }
            File(tempPath).delete()
            true
        } catch (e: Exception) { false }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppInfoDrawer(version = versionForDrawer)
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "ZPatch",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Outlined.Menu, contentDescription = "Open menu")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.surface
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tab Row
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = when (pagerState.currentPage) {
                                0 -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
                    }
                ) {
                    PatchTab.entries.forEachIndexed { index, tab ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = tab.title,
                                    style = if (pagerState.currentPage == index)
                                        MaterialTheme.typography.titleMedium
                                    else
                                        MaterialTheme.typography.bodyLarge
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = when (index) {
                                        0 -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.secondary
                                    }
                                )
                            }
                        )
                    }
                }

                // Horizontal Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    val tab = PatchTab.entries[pageIndex]
                    val accentColor = when (tab) {
                        PatchTab.Create -> MaterialTheme.colorScheme.primary
                        PatchTab.Apply -> MaterialTheme.colorScheme.secondary
                    }

                    when (tab) {
                        PatchTab.Create -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    PatchOperationCard(
                                        title = "Create Patch",
                                        description = "Generate a differential patch from two files",
                                        icon = Icons.Outlined.AddCircle,
                                        accentColor = accentColor,
                                        isProcessing = isCreating,
                                        result = createResult,
                                        fileSelectors = listOf(
                                            FileInfo("Old file", createOldFile) { pickOldForCreate.launch(arrayOf("*/*")) },
                                            FileInfo("New file", createNewFile) { pickNewForCreate.launch(arrayOf("*/*")) }
                                        ),
                                        outputPath = patchOutputPath,
                                        onExecute = {
                                            scope.launch {
                                                isCreating = true
                                                createResult = null
                                                delay(300)
                                                try {
                                                    val oldPath = uriToTempFile(createOldFile!!)
                                                    val newPath = uriToTempFile(createNewFile!!)
                                                    if (oldPath != null && newPath != null) {
                                                        val tempPatch = File(context.cacheDir, "temp_${System.currentTimeMillis()}")
                                                        val result = ZPatch.createPatch(oldPath, newPath, tempPatch.absolutePath)
                                                        createResult = if (result.isSuccess) {
                                                            if (tempFileToDownload(tempPatch.absolutePath, patchOutputPath)) {
                                                                "OK\n${File(patchOutputPath).name}\n${result.originalSize} bytes"
                                                            } else "ERR\nWrite failed"
                                                        } else "ERR\n${result.message}"
                                                        File(oldPath).delete()
                                                        File(newPath).delete()
                                                    } else createResult = "ERR\nRead failed"
                                                } catch (e: Exception) {
                                                    createResult = "ERR\n${e.message?.take(20) ?: "Unknown error"}"
                                                } finally {
                                                    isCreating = false
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        PatchTab.Apply -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    PatchOperationCard(
                                        title = "Apply Patch",
                                        description = "Restore a file using a differential patch",
                                        icon = Icons.Outlined.Done,
                                        accentColor = accentColor,
                                        isProcessing = isApplying,
                                        result = applyResult,
                                        fileSelectors = listOf(
                                            FileInfo("Base file", applyOldFile) { pickOldForApply.launch(arrayOf("*/*")) },
                                            FileInfo("Patch file", applyPatchFile) { pickPatchFile.launch(arrayOf("*/*")) }
                                        ),
                                        outputPath = restoredOutputPath,
                                        onExecute = {
                                            scope.launch {
                                                isApplying = true
                                                applyResult = null
                                                delay(300)
                                                try {
                                                    val oldPath = uriToTempFile(applyOldFile!!)
                                                    val patchPath = uriToTempFile(applyPatchFile!!)
                                                    if (oldPath != null && patchPath != null) {
                                                        val tempNew = File(context.cacheDir, "temp_${System.currentTimeMillis()}")
                                                        val result = ZPatch.applyPatch(oldPath, patchPath, tempNew.absolutePath)
                                                        applyResult = if (result.isSuccess) {
                                                            if (tempFileToDownload(tempNew.absolutePath, restoredOutputPath)) {
                                                                "OK\n${File(restoredOutputPath).name}\n${result.originalSize} bytes"
                                                            } else "ERR\nWrite failed"
                                                        } else "ERR\n${result.message}"
                                                        File(oldPath).delete()
                                                        File(patchPath).delete()
                                                    } else applyResult = "ERR\nRead failed"
                                                } catch (e: Exception) {
                                                    applyResult = "ERR\n${e.message?.take(20) ?: "Unknown error"}"
                                                } finally {
                                                    isApplying = false
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Data class for file info
data class FileInfo(
    val label: String,
    val uri: Uri?,
    val onClick: () -> Unit
)

@Composable
fun AppInfoDrawer(version: String) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App Icon & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column {
                    Text(
                        text = "ZPatch",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = version,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // App Info
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                InfoRow("Description", "Differential patch library")
                InfoRow("Compression", "Zstd algorithm")
            }

            Spacer(Modifier.weight(1f))

            // Footer
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Open Source • MIT License",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatchOperationCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    isProcessing: Boolean,
    result: String?,
    fileSelectors: List<FileInfo>,
    outputPath: String,
    onExecute: () -> Unit
) {
    var showResult by remember { mutableStateOf(false) }
    LaunchedEffect(result) { showResult = result != null }

    val context = LocalContext.current

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            accentColor.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = accentColor
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // File Selectors
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                fileSelectors.forEach { fileInfo ->
                    FileSelector(
                        label = fileInfo.label,
                        fileName = fileInfo.uri?.let { DocumentFile.fromSingleUri(context, it)?.name },
                        onClick = fileInfo.onClick,
                        accentColor = accentColor
                    )
                }
            }

            // Output Path
            OutputPathDisplay(path = outputPath, accentColor = accentColor)

            // Execute Button
            Button(
                onClick = onExecute,
                modifier = Modifier.fillMaxWidth(),
                enabled = fileSelectors.all { it.uri != null } && !isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    if (isProcessing) "Processing..." else "Execute",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Result
            AnimatedVisibility(
                visible = showResult && result != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                result?.let { ResultCard(it) }
            }
        }
    }
}

@Composable
fun FileSelector(
    label: String,
    fileName: String?,
    onClick: () -> Unit,
    accentColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FilledTonalButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = if (fileName != null)
                    accentColor.copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = if (fileName != null) Icons.Outlined.Check else Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = fileName ?: "Select file",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun OutputPathDisplay(path: String, accentColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(
            1.dp,
            accentColor.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Output location",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Downloads/${File(path).name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ResultCard(result: String) {
    val isSuccess = result.startsWith("OK")
    val color = if (isSuccess)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Outlined.CheckCircle else Icons.Outlined.Close,
                contentDescription = null,
                tint = color
            )
            Column {
                Text(
                    text = if (isSuccess) "Success" else "Failed",
                    style = MaterialTheme.typography.titleSmall,
                    color = color
                )
                result.substringAfter("\n").lines().forEach { line ->
                    Text(
                        text = line.trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
