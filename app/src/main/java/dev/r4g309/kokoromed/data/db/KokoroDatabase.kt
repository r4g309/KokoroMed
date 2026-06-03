package dev.r4g309.kokoromed.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities  = [DeckEntity::class, CardEntity::class, ActivityEntity::class],
    version   = 1,
    exportSchema = false,
)
abstract class KokoroDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao
    abstract fun activityDao(): ActivityDao
}
