package com.xenorking.novgpu.domain.usecase

import com.xenorking.novgpu.data.repository.SystemStatsRepository
import com.xenorking.novgpu.domain.model.SystemStats
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSystemStatsUseCase @Inject constructor(
    private val repository: SystemStatsRepository
) {
    operator fun invoke(intervalMs: Long = 1000L): Flow<SystemStats> =
        repository.getStatsFlow(intervalMs)
}
