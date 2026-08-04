package com.yage.opencode_client

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.yage.opencode_client.data.model.FileNode
import com.yage.opencode_client.ui.files.FileRow
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FileBrowserCopyInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun longPressingFileCopiesFullPathWithoutOpeningIt() {
        val file = FileNode(name = "MainActivity.kt", path = "app/src/main/MainActivity.kt", type = "file")
        val selected = AtomicInteger(0)
        val copied = AtomicReference<String?>(null)

        composeRule.setContent {
            MaterialTheme {
                FileRow(file = file, status = null, onClick = selected::incrementAndGet, onLongClick = copied::set)
            }
        }

        composeRule.onNodeWithTag("files.row.${file.path}").performTouchInput { longClick() }

        assertEquals(file.path, copied.get())
        assertEquals(0, selected.get())
    }

    @Test
    fun longPressingDirectoryCopiesFullPathWithoutOpeningIt() {
        val directory = FileNode(name = "files", path = "app/src/main/files", type = "directory")
        val selected = AtomicInteger(0)
        val copied = AtomicReference<String?>(null)

        composeRule.setContent {
            MaterialTheme {
                FileRow(file = directory, status = null, onClick = selected::incrementAndGet, onLongClick = copied::set)
            }
        }

        composeRule.onNodeWithTag("files.row.${directory.path}").performTouchInput { longClick() }

        assertEquals(directory.path, copied.get())
        assertEquals(0, selected.get())
    }

    @Test
    fun tappingFileRetainsExistingOpenBehavior() {
        val file = FileNode(name = "MainActivity.kt", path = "app/src/main/MainActivity.kt", type = "file")
        val selected = AtomicInteger(0)
        val copied = AtomicReference<String?>(null)

        composeRule.setContent {
            MaterialTheme {
                FileRow(file = file, status = null, onClick = selected::incrementAndGet, onLongClick = copied::set)
            }
        }

        composeRule.onNodeWithTag("files.row.${file.path}").performClick()

        assertEquals(1, selected.get())
        assertEquals(null, copied.get())
    }
}
