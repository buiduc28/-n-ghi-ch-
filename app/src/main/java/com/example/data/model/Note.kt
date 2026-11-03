package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an offline note in Room database.
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "Chung",
    val colorHex: String = "#FFFDF5",
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object NotePresets {
    val categories = listOf("Tất cả", "Chung", "Cá nhân", "Công việc", "Học tập", "Ý tưởng")
    
    // Palette of gentle pastel tones suited for card backgrounds in light & dark mode
    val noteColors = listOf(
        NoteColorItem(hex = "#FFFDF5", darkHex = "#23262F", label = "Mặc định"),
        NoteColorItem(hex = "#FEF3C7", darkHex = "#372B10", label = "Vàng nhạt"),
        NoteColorItem(hex = "#DCFCE7", darkHex = "#132D1E", label = "Xanh lá"),
        NoteColorItem(hex = "#E0F2FE", darkHex = "#13273D", label = "Xanh dương"),
        NoteColorItem(hex = "#F3E8FF", darkHex = "#2D1D3E", label = "Tím nhạt"),
        NoteColorItem(hex = "#FFE4E6", darkHex = "#3B1A22", label = "Hồng phấn"),
        NoteColorItem(hex = "#FFEDD5", darkHex = "#382312", label = "Cam đào")
    )
}

data class NoteColorItem(
    val hex: String,
    val darkHex: String,
    val label: String
)
