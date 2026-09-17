package com.safefleet.ai.mobile.di

import com.safefleet.ai.mobile.security.AndroidCredentialVault
import com.safefleet.ai.mobile.security.CredentialVault
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {
    @Binds
    @Singleton
    abstract fun bindCredentialVault(implementation: AndroidCredentialVault): CredentialVault
}
