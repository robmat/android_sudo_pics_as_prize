package com.batodev.sudoku.di

import android.app.Application
import com.batodev.sudoku.data.database.AppDatabase
import com.batodev.sudoku.data.database.dao.BoardDao
import com.batodev.sudoku.data.database.dao.FolderDao
import com.batodev.sudoku.data.database.dao.RecordDao
import com.batodev.sudoku.data.database.dao.SavedGameDao
import com.batodev.sudoku.data.database.repository.BoardRepositoryImpl
import com.batodev.sudoku.data.database.repository.FolderRepositoryImpl
import com.batodev.sudoku.data.database.repository.RecordRepositoryImpl
import com.batodev.sudoku.data.database.repository.SavedGameRepositoryImpl
import com.batodev.sudoku.domain.repository.BoardRepository
import com.batodev.sudoku.domain.repository.FolderRepository
import com.batodev.sudoku.domain.repository.RecordRepository
import com.batodev.sudoku.domain.repository.SavedGameRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(app: Application): AppDatabase = AppDatabase.getInstance(context = app)

    @Provides
    @Singleton
    fun provideFolderRepository(folderDao: FolderDao): FolderRepository = FolderRepositoryImpl(folderDao)

    @Provides
    @Singleton
    fun provideFolderDao(appDatabase: AppDatabase): FolderDao = appDatabase.folderDao()

    // records
    @Singleton
    @Provides
    fun provideRecordRepository(recordDao: RecordDao): RecordRepository =
        RecordRepositoryImpl(recordDao)

    @Singleton
    @Provides
    fun provideRecordDao(appDatabase: AppDatabase): RecordDao = appDatabase.recordDao()

    // boards
    @Singleton
    @Provides
    fun provideBoardRepository(boardDao: BoardDao): BoardRepository = BoardRepositoryImpl(boardDao)

    @Singleton
    @Provides
    fun provideBoardDao(appDatabase: AppDatabase): BoardDao = appDatabase.boardDao()

    // saved games
    @Singleton
    @Provides
    fun provideSavedGameRepository(savedGameDao: SavedGameDao): SavedGameRepository =
        SavedGameRepositoryImpl(savedGameDao)

    @Singleton
    @Provides
    fun provideSavedGameDao(appDatabase: AppDatabase): SavedGameDao = appDatabase.savedGameDao()
}
