package com.roman.mars.data.local.database
import androidx.room.Database
import androidx.room.RoomDatabase
import com.roman.mars.data.local.dao.ChatDao
import com.roman.mars.data.local.dao.MessageDao
import com.roman.mars.data.local.entity.ChatEntity
import com.roman.mars.data.local.entity.MessageEntity
@Database(
    entities = [ChatEntity::class, MessageEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}