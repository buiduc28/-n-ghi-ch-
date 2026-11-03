package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.NoteDao
import com.example.data.model.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notes_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial welcoming notes for great first-run experience
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).noteDao()
                            dao.insertNote(
                                Note(
                                    title = "Chào mừng bạn đến với Ghi Chú! 📝",
                                    content = "Đây là ứng dụng ghi chú đơn giản, tiện lợi và lưu trữ an toàn trên thiết bị của bạn.\n\n" +
                                            "✨ Một số tính năng nổi bật:\n" +
                                            "• Nhấn dấu '+' bên dưới để tạo ghi chú mới\n" +
                                            "• Lọc nhanh theo màu sắc và danh mục ở thanh trên\n" +
                                            "• Ghim ghi chú quan trọng lên đầu\n" +
                                            "• Chọn màu sắc sinh động theo sở thích\n" +
                                            "• Phân loại theo Công việc, Cá nhân, Học tập, Ý tưởng\n" +
                                            "• Tìm kiếm nhanh mọi lúc",
                                    category = "Chung",
                                    colorHex = "#FEF3C7",
                                    isPinned = true,
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                            dao.insertNote(
                                Note(
                                    title = "Danh sách việc cần làm mẫu 📋",
                                    content = "1. Viết ghi chú đầu tiên của bạn\n" +
                                            "2. Thử đổi màu cho ghi chú này\n" +
                                            "3. Tìm kiếm thử bằng ô tìm kiếm phía trên\n" +
                                            "4. Chia sẻ ghi chú cho bạn bè khi cần",
                                    category = "Công việc",
                                    colorHex = "#DCFCE7",
                                    isPinned = false,
                                    createdAt = System.currentTimeMillis() - 3600000,
                                    updatedAt = System.currentTimeMillis() - 3600000
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
