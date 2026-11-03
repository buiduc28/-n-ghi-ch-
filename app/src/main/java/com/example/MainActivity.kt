package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Note
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NotesViewModel

sealed interface Screen {
    data object Home : Screen
    data class Editor(val noteId: Long? = null) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NotesApp()
            }
        }
    }
}

@Composable
fun NotesApp(
    viewModel: NotesViewModel = viewModel(factory = NotesViewModel.Factory(androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application))
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedColorHex by viewModel.selectedColorHex.collectAsStateWithLifecycle()
    val isGridView by viewModel.isGridView.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
            when (screen) {
                is Screen.Home -> {
                    HomeScreen(
                        notes = filteredNotes,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        selectedColorHex = selectedColorHex,
                        isGridView = isGridView,
                        onSearchChange = viewModel::onSearchQueryChanged,
                        onCategoryChange = viewModel::onCategorySelected,
                        onColorFilterChange = viewModel::onColorFilterSelected,
                        onToggleViewMode = viewModel::toggleViewMode,
                        onNoteClick = { note ->
                            currentScreen = Screen.Editor(note.id)
                        },
                        onCreateNote = {
                            currentScreen = Screen.Editor(null)
                        },
                        onTogglePin = viewModel::togglePin,
                        onDuplicateNote = viewModel::duplicateNote,
                        onDeleteNote = viewModel::deleteNote
                    )
                }
                is Screen.Editor -> {
                    val editingNote = remember(screen.noteId, allNotes) {
                        screen.noteId?.let { id -> allNotes.find { it.id == id } }
                    }
                    NoteEditorScreen(
                        note = editingNote,
                        onSaveNote = { title, content, category, colorHex, isPinned ->
                            viewModel.saveNote(
                                id = screen.noteId ?: 0L,
                                title = title,
                                content = content,
                                category = category,
                                colorHex = colorHex,
                                isPinned = isPinned
                            )
                        },
                        onDeleteNote = { note ->
                            viewModel.deleteNote(note)
                        },
                        onBack = {
                            currentScreen = Screen.Home
                        }
                    )
                }
            }
        }
    }
}
