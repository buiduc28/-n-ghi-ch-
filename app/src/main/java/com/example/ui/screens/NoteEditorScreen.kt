package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.data.model.NotePresets
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.formatNoteDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    note: Note?,
    onSaveNote: (title: String, content: String, category: String, colorHex: String, isPinned: Boolean) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var category by remember { mutableStateOf(note?.category ?: "Chung") }
    var colorHex by remember { mutableStateOf(note?.colorHex ?: "#FFFDF5") }
    var isPinned by remember { mutableStateOf(note?.isPinned ?: false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun handleSaveAndBack() {
        if (title.isNotBlank() || content.isNotBlank()) {
            onSaveNote(title, content, category, colorHex, isPinned)
        }
        onBack()
    }

    // Auto save on system back
    BackHandler {
        handleSaveAndBack()
    }

    val defaultBg = MaterialTheme.colorScheme.background
    val defaultSurface = MaterialTheme.colorScheme.surface
    val editorBgColor = remember(colorHex, isDark, defaultBg) {
        val preset = NotePresets.noteColors.find { it.hex.equals(colorHex, ignoreCase = true) }
        val hex = if (isDark && preset != null) preset.darkHex else colorHex
        try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: Exception) {
            defaultBg
        }
    }

    val textColor = if (isDark) MaterialTheme.colorScheme.onBackground else Color(0xFF1E293B)
    val hintColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF94A3B8)

    val wordCount = remember(content) {
        if (content.isBlank()) 0 else content.trim().split("\\s+".toRegex()).size
    }
    val charCount = remember(content) { content.length }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(editorBgColor),
        containerColor = editorBgColor,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { handleSaveAndBack() },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    // Pin Button
                    IconButton(
                        onClick = { isPinned = !isPinned },
                        modifier = Modifier.testTag("editor_pin_button")
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (isPinned) "Bỏ ghim" else "Ghim ghi chú",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else textColor
                        )
                    }

                    // Share Button
                    IconButton(
                        onClick = {
                            val shareText = buildString {
                                if (title.isNotBlank()) append(title).append("\n\n")
                                append(content)
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, title)
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Chia sẻ ghi chú"))
                        },
                        modifier = Modifier.testTag("editor_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Chia sẻ ghi chú",
                            tint = textColor
                        )
                    }

                    // Delete Button (only if editing an existing note)
                    if (note != null) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("editor_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa ghi chú",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Save Button
                    Button(
                        onClick = { handleSaveAndBack() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("editor_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lưu", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = editorBgColor
                )
            )
        },
        bottomBar = {
            Surface(
                color = editorBgColor,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (note != null) "Đã sửa: ${formatNoteDate(note.updatedAt)}" else "Ghi chú mới",
                        style = MaterialTheme.typography.labelMedium,
                        color = hintColor
                    )
                    Text(
                        text = "$wordCount từ • $charCount ký tự",
                        style = MaterialTheme.typography.labelMedium,
                        color = hintColor
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // Category Chips Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotePresets.categories.filter { it != "Tất cả" }.forEach { cat ->
                    val isSelected = category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { category = cat },
                        label = { Text(cat, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Note Color Palette Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Màu sắc:",
                    style = MaterialTheme.typography.labelMedium,
                    color = hintColor,
                    modifier = Modifier.padding(end = 4.dp)
                )

                NotePresets.noteColors.forEach { colorItem ->
                    val itemColor = try {
                        val hex = if (isDark) colorItem.darkHex else colorItem.hex
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (_: Exception) {
                        defaultSurface
                    }
                    val isSelected = colorHex.equals(colorItem.hex, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(itemColor)
                            .border(
                                BorderStroke(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
                                ),
                                CircleShape
                            )
                            .clickable { colorHex = colorItem.hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Đã chọn",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Title Field
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        lineHeight = 30.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) {
                            Text(
                                text = "Tiêu đề ghi chú...",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = hintColor,
                                    lineHeight = 30.sp
                                )
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Content Field
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    textStyle = TextStyle(
                        fontSize = 17.sp,
                        color = textColor,
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (content.isEmpty()) {
                            Text(
                                text = "Bắt đầu viết nội dung ở đây...\n\n• Nhập việc cần làm\n• Ý tưởng hoặc tài liệu cá nhân",
                                style = TextStyle(
                                    fontSize = 17.sp,
                                    color = hintColor,
                                    lineHeight = 24.sp
                                )
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .testTag("note_content_input")
                )
            }
        }
    }

    if (showDeleteDialog && note != null) {
        DeleteConfirmDialog(
            title = "Xóa ghi chú này?",
            message = "Bạn có chắc muốn xóa ghi chú này không?",
            onConfirm = {
                showDeleteDialog = false
                onDeleteNote(note)
                onBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
