package com.roman.mars.data.local.database
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.roman.mars.data.local.dao.MessageDao
import com.roman.mars.data.local.entity.MessageEntity
@Database(
    entities = [MessageEntity::class],
    version = 2,  // УВЕЛИЧИЛИ ВЕРСИЮ
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
// МИГРАЦИЯ 1 → 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Добавляем новые колонки
        database.execSQL("ALTER TABLE messages ADD COLUMN client_id TEXT")
        database.execSQL("ALTER TABLE messages ADD COLUMN read_at INTEGER")
        database.execSQL("ALTER TABLE messages ADD COLUMN is_sent INTEGER NOT NULL DEFAULT 1")
        database.execSQL("ALTER TABLE messages ADD COLUMN synced INTEGER NOT NULL DEFAULT 0")

        // Создаём индексы для быстрого поиска
        database.execSQL("CREATE INDEX IF NOT EXISTS index_messages_client_id ON messages(client_id)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_messages_chat_id ON messages(chat_id)")
    }
}