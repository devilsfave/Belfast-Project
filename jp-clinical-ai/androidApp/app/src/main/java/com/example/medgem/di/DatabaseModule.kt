package com.example.medgem

import android.content.Context
import com.example.medgem.data.ConversationEntity
import com.example.medgem.data.KnowledgeEntity
import com.example.medgem.data.MessageEntity
import com.example.medgem.data.ObjectBox
import com.example.medgem.data.PatientEntity
import com.example.medgem.data.VisitEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.objectbox.Box
import io.objectbox.BoxStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideBoxStore(@ApplicationContext context: Context): BoxStore {
        ObjectBox.init(context)
        return ObjectBox.store
    }

    @Provides
    @Singleton
    fun provideConversationBox(store: BoxStore): Box<ConversationEntity> {
        return store.boxFor(ConversationEntity::class.java)
    }

    @Provides
    @Singleton
    fun provideMessageBox(store: BoxStore): Box<MessageEntity> {
        return store.boxFor(MessageEntity::class.java)
    }

    @Provides
    @Singleton
    fun provideKnowledgeBox(store: BoxStore): Box<KnowledgeEntity> {
        return store.boxFor(KnowledgeEntity::class.java)
    }

    @Provides
    @Singleton
    fun providePatientBox(store: BoxStore): Box<PatientEntity> {
        return store.boxFor(PatientEntity::class.java)
    }

    @Provides
    @Singleton
    fun provideVisitBox(store: BoxStore): Box<VisitEntity> {
        return store.boxFor(VisitEntity::class.java)
    }
}
