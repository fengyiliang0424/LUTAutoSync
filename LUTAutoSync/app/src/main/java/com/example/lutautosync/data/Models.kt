package com.example.lutautosync.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "luts")
data class LutEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val path: String, val format: String, val favorite: Boolean = false, val isDefault: Boolean = false)

@Entity(tableName = "processed_files", indices = [Index(value = ["path", "md5"], unique = true)])
data class ProcessedFileEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val path: String, val md5: String, val processedAt: Long, val lutId: Long)

@Dao interface LutDao {
    @Query("SELECT * FROM luts ORDER BY favorite DESC, name") fun observeAll(): Flow<List<LutEntity>>
    @Query("SELECT * FROM luts WHERE isDefault = 1 LIMIT 1") suspend fun default(): LutEntity?
    @Insert suspend fun insert(item: LutEntity): Long
    @Update suspend fun update(item: LutEntity)
    @Delete suspend fun delete(item: LutEntity)
    @Query("UPDATE luts SET isDefault = 0") suspend fun clearDefault()
}
@Dao interface ProcessedDao {
    @Query("SELECT * FROM processed_files ORDER BY processedAt DESC LIMIT 20") fun recent(): Flow<List<ProcessedFileEntity>>
    @Query("SELECT COUNT(*) FROM processed_files") fun count(): Flow<Int>
    @Query("SELECT EXISTS(SELECT 1 FROM processed_files WHERE path = :path AND md5 = :md5 AND lutId = :lutId)") suspend fun exists(path: String, md5: String, lutId: Long): Boolean
    @Insert suspend fun insert(item: ProcessedFileEntity)
}
@Database(entities = [LutEntity::class, ProcessedFileEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() { abstract fun lutDao(): LutDao; abstract fun processedDao(): ProcessedDao }
