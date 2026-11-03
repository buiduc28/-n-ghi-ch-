package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Note
import com.example.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = NoteRepository(database.noteDao())
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tất cả")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _selectedColorHex = MutableStateFlow<String?>(null)
    val selectedColorHex: StateFlow<String?> = _selectedColorHex.asStateFlow()

    val allNotes: StateFlow<List<Note>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredNotes: StateFlow<List<Note>> = combine(
        allNotes,
        _searchQuery,
        _selectedCategory,
        _selectedColorHex
    ) { notes, query, category, colorHex ->
        notes.filter { note ->
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
            val matchesCategory = category == "Tất cả" || note.category == category
            val matchesColor = colorHex == null || note.colorHex.equals(colorHex, ignoreCase = true)
            matchesQuery && matchesCategory && matchesColor
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onColorFilterSelected(colorHex: String?) {
        _selectedColorHex.value = if (_selectedColorHex.value == colorHex) null else colorHex
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun toggleViewMode() {
        _isGridView.value = !_isGridView.value
    }

    fun saveNote(
        id: Long = 0,
        title: String,
        content: String,
        category: String = "Chung",
        colorHex: String = "#FFFDF5",
        isPinned: Boolean = false
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val note = Note(
                id = id,
                title = title.trim(),
                content = content.trim(),
                category = category,
                colorHex = colorHex,
                isPinned = isPinned,
                updatedAt = now,
                createdAt = if (id != 0L) {
                    allNotes.value.find { it.id == id }?.createdAt ?: now
                } else {
                    now
                }
            )
            if (id == 0L) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note)
            }
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.updateNote(
                note.copy(
                    isPinned = !note.isPinned,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun duplicateNote(note: Note) {
        viewModelScope.launch {
            val copyNote = note.copy(
                id = 0,
                title = "${note.title} (Bản sao)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertNote(copyNote)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
                return NotesViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
