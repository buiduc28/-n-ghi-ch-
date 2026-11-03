package com.example

import com.example.data.model.Note
import com.example.data.model.NotePresets
import com.example.ui.components.formatNoteDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun note_creation_default_values() {
        val note = Note(
            title = "Tiêu đề ghi chú",
            content = "Nội dung ghi chú"
        )
        assertEquals("Tiêu đề ghi chú", note.title)
        assertEquals("Nội dung ghi chú", note.content)
        assertEquals("Chung", note.category)
        assertFalse(note.isPinned)
        assertEquals("#FFFDF5", note.colorHex)
    }

    @Test
    fun note_presets_has_valid_categories_and_colors() {
        assertTrue(NotePresets.categories.contains("Tất cả"))
        assertTrue(NotePresets.categories.contains("Công việc"))
        assertTrue(NotePresets.noteColors.isNotEmpty())
    }

    @Test
    fun formatNoteDate_just_now() {
        val now = System.currentTimeMillis()
        val formatted = formatNoteDate(now - 1000)
        assertEquals("Vừa xong", formatted)
    }

    @Test
    fun note_color_filter_matching() {
        val noteAmber = Note(title = "N1", content = "C1", colorHex = "#FEF3C7")
        val noteGreen = Note(title = "N2", content = "C2", colorHex = "#DCFCE7")
        val notes = listOf(noteAmber, noteGreen)
        val filtered = notes.filter { it.colorHex.equals("#FEF3C7", ignoreCase = true) }
        assertEquals(1, filtered.size)
        assertEquals("N1", filtered[0].title)
    }
}
