package com.answersolutions.runandread.di

import android.app.Application
import android.content.Context
import com.answersolutions.runandread.data.datasource.EBookDataSource
import com.answersolutions.runandread.data.datasource.LibraryAssetDataSource
import com.answersolutions.runandread.data.datasource.LibraryDiskDataSource
import com.answersolutions.runandread.data.datasource.VoiceDataSource
import com.answersolutions.runandread.data.repository.EBookRepository
import com.answersolutions.runandread.data.repository.LibraryRepository
import com.answersolutions.runandread.data.repository.LibraryRepositoryImpl
import com.answersolutions.runandread.data.repository.VoiceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLibraryAssetDataSource(@ApplicationContext context: Context): LibraryAssetDataSource {
        return LibraryAssetDataSource(context)
    }

    @Provides
    @Singleton
    fun provideLibraryDiskDataSource(@ApplicationContext context: Context): LibraryDiskDataSource {
        return LibraryDiskDataSource(context)
    }

    @Provides
    @Singleton
    fun provideLibraryRepository(
        diskDataSource: LibraryDiskDataSource,
        assetDataSource: LibraryAssetDataSource
    ): LibraryRepository {
        return LibraryRepositoryImpl(diskDataSource, assetDataSource)
    }

    @Provides
    @Singleton
    fun provideApplication(@ApplicationContext context: Context): Application {
        return context as Application
    }

    @Provides
    @Singleton
    fun provideEBookDataSource(@ApplicationContext context: Context): EBookDataSource {
        return EBookDataSource(context)
    }

    @Provides
    @Singleton
    fun provideEBookRepository(dataSource: EBookDataSource): EBookRepository {
        return EBookRepository(dataSource)
    }


    @Provides
    @Singleton
    fun provideVoiceDataSource(@ApplicationContext context: Context): VoiceDataSource {
        return VoiceDataSource(context)
    }

    @Provides
    @Singleton
    fun provideVoiceRepository(voiceDataSource: VoiceDataSource): VoiceRepository {
        return VoiceRepository(voiceDataSource)
    }


}