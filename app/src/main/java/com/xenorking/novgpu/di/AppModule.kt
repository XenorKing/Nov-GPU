package com.xenorking.novgpu.di

import android.content.Context
import com.xenorking.novgpu.data.monitor.CpuMonitor
import com.xenorking.novgpu.data.monitor.GpuMonitor
import com.xenorking.novgpu.data.monitor.NetworkMonitor
import com.xenorking.novgpu.data.monitor.RamMonitor
import com.xenorking.novgpu.data.monitor.TemperatureMonitor
import com.xenorking.novgpu.data.repository.SystemStatsRepository
import com.xenorking.novgpu.domain.usecase.GetSystemStatsUseCase
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
    fun provideSystemStatsRepository(
        cpuMonitor: CpuMonitor,
        gpuMonitor: GpuMonitor,
        ramMonitor: RamMonitor,
        temperatureMonitor: TemperatureMonitor,
        networkMonitor: NetworkMonitor
    ): SystemStatsRepository = SystemStatsRepository(
        cpuMonitor, gpuMonitor, ramMonitor, temperatureMonitor, networkMonitor
    )

    @Provides
    @Singleton
    fun provideGetSystemStatsUseCase(
        repository: SystemStatsRepository
    ): GetSystemStatsUseCase = GetSystemStatsUseCase(repository)
}
