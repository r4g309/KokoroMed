package dev.r4g309.kokoromed.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.r4g309.kokoromed.data.db.ActivityDao
import dev.r4g309.kokoromed.data.db.CardDao
import dev.r4g309.kokoromed.data.db.DeckDao
import dev.r4g309.kokoromed.data.db.KokoroDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): KokoroDatabase =
        Room.databaseBuilder(ctx, KokoroDatabase::class.java, "kokoro.db")
            .build()

    @Provides fun provideDeckDao(db: KokoroDatabase): DeckDao = db.deckDao()
    @Provides fun provideCardDao(db: KokoroDatabase): CardDao = db.cardDao()
    @Provides fun provideActivityDao(db: KokoroDatabase): ActivityDao = db.activityDao()
}
