package com.pandora.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pandora.database.dao.*
import com.pandora.database.entity.*

@Database(
    entities = [
        CeoProfile::class, Device::class, Role::class, ConsentRecord::class,
        Product::class, Payment::class, Receipt::class, ChatMessage::class,
        ScanSession::class, Building::class, Floor::class, Room::class, Zone::class,
        MeshNode::class, MeshRoute::class, MeshMessage::class,
        SecurityEvent::class, LogEntry::class, NetworkStatus::class,
        ComputeNode::class, ComputeTask::class, JayJayTask::class,
        CsiData::class, OverlayData3D::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class PandoraDatabase : RoomDatabase() {

    abstract fun ceoDao(): CeoDao
    abstract fun deviceDao(): DeviceDao
    abstract fun productDao(): ProductDao
    abstract fun paymentDao(): PaymentDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun chatDao(): ChatDao
    abstract fun meshDao(): MeshDao
    abstract fun scanDao(): ScanDao
    abstract fun buildingDao(): BuildingDao
    abstract fun securityDao(): SecurityDao
    abstract fun computeDao(): ComputeDao
    abstract fun jayJayDao(): JayJayDao
    abstract fun networkDao(): NetworkDao

    companion object {
        @Volatile private var INSTANCE: PandoraDatabase? = null

        fun getInstance(context: Context, dbPath: String? = null): PandoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = if (dbPath != null) {
                    Room.databaseBuilder(context.applicationContext, PandoraDatabase::class.java, dbPath)
                } else {
                    Room.databaseBuilder(context.applicationContext, PandoraDatabase::class.java, "pandora.db")
                }
                builder
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

class Converters {
    @TypeConverter fun fromList(list: List<String>?): String = list?.joinToString(",") ?: ""
    @TypeConverter fun toList(s: String?): List<String> = if (s.isNullOrBlank()) emptyList() else s.split(",")
}
