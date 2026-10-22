package com.example.pulse.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PulseDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao

    companion object {
        @Volatile
        private var INSTANCE: PulseDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `playlists` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`createdAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `playlist_tracks` (" +
                            "`playlistId` INTEGER NOT NULL, " +
                            "`trackId` TEXT NOT NULL, " +
                            "`addedAt` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`playlistId`, `trackId`), " +
                            "FOREIGN KEY(`playlistId`) REFERENCES `playlists`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                            "FOREIGN KEY(`trackId`) REFERENCES `tracks`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_playlist_tracks_trackId` " +
                            "ON `playlist_tracks` (`trackId`)"
                )
            }
        }

        fun getDatabase(context: Context): PulseDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PulseDatabase::class.java,
                    "pulse_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}