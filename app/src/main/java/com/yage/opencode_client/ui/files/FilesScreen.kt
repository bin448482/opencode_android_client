package com.yage.opencode_client.ui.files

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yage.opencode_client.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    viewModel: FilesViewModel = hiltViewModel(),
    pathToShow: String? = null,
    sessionDirectory: String? = null,
    onCloseFile: () -> Unit = {},
    onFileClick: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var copiedPath by remember { mutableStateOf<String?>(null) }

    fun copyPath(path: String) {
        clipboard.setText(AnnotatedString(path))
        copiedPath = path
    }

    LaunchedEffect(copiedPath) {
        if (copiedPath != null) {
            delay(2_000)
            copiedPath = null
        }
    }

    LaunchedEffect(pathToShow, sessionDirectory) {
        viewModel.syncPathToShow(pathToShow, sessionDirectory)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (state.selectedFilePath == null) {
            TopAppBar(
                title = {
                    val currentPath = state.currentPath
                    Text(
                        text = currentPath.ifEmpty { stringResource(R.string.files_title) },
                        modifier = if (currentPath.isEmpty()) Modifier else Modifier.pointerInput(currentPath) {
                            detectTapGestures(onLongPress = { copyPath(currentPath) })
                        }
                    )
                },
                navigationIcon = {
                    if (state.currentPath.isNotEmpty()) {
                        IconButton(onClick = viewModel::navigateUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.common_refresh))
                    }
                }
            )
        }

        state.error?.let { message ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = viewModel::clearError) {
                        Text(stringResource(R.string.common_dismiss))
                    }
                }
            ) {
                Text(message)
            }
        }

        copiedPath?.let {
            Snackbar(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.files_path_copied))
            }
        }

        when {
            state.selectedFilePath != null && state.selectedFileContent != null -> {
                FilePreviewPane(
                    path = state.selectedFilePath!!,
                    fileContent = state.selectedFileContent!!,
                    repository = viewModel.repository,
                    sessionDirectory = sessionDirectory,
                    isRefreshing = state.isPreviewRefreshing,
                    onRefresh = { viewModel.refreshPreview(sessionDirectory) },
                    onMarkdownLinkClick = { href, sourcePath ->
                        when (val resolution = WorkspaceMarkdownLinkResolver.resolve(href, sessionDirectory, sourcePath)) {
                            is WorkspaceMarkdownLinkResolver.Resolution.External -> {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resolution.url))
                                runCatching { context.startActivity(intent) }
                                    .onFailure { viewModel.showError(it.message ?: "Could not open link") }
                            }
                            is WorkspaceMarkdownLinkResolver.Resolution.Preview -> viewModel.openPreviewPath(resolution.path, sessionDirectory)
                            WorkspaceMarkdownLinkResolver.Resolution.Ignored -> Unit
                            is WorkspaceMarkdownLinkResolver.Resolution.Rejected -> viewModel.showError(resolution.message)
                        }
                    },
                    onClose = {
                        viewModel.closePreview()
                        onCloseFile()
                    }
                )
            }

            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }

            else -> {
                FileBrowserPane(
                    files = state.files,
                    fileStatuses = state.fileStatuses,
                    onFileSelected = { file -> viewModel.selectFile(file, onFileClick) },
                    onPathCopied = ::copyPath
                )
            }
        }
    }
}
