package com.example.locationapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Photo::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE photos ADD COLUMN area TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN landCategory TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN frontage TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN roadWidth TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN roadDirection TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN structure TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN builtYear TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN floors TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN layout TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN parking TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN waterSupply TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN sewage TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE photos ADD COLUMN sessionId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN isMainPhoto INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE photos ADD COLUMN memo TEXT NOT NULL DEFAULT ''")
                database.execSQL("UPDATE photos SET sessionId = CAST(id AS TEXT)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE photos ADD COLUMN floorArea1 TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN floorArea2 TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN floorArea3 TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE photos ADD COLUMN floorAreaTotal TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "photo_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
