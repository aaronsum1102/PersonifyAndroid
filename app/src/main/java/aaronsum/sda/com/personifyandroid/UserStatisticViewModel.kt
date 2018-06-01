package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel

class UserStatisticViewModel : ViewModel() {
    private val repository = UserStatisticRepository()
    val userStatistics: LiveData<UserStatistics> = repository.userStatistics

    fun loadUserStatistic(userId: String) {
        repository.loadStatistic(userId)
    }

    fun updateStatistic(command: String) {
        repository.updateStatistic(command)
    }

    fun updateCompletionStatistic(newStatistic: UserCompletionStatistic) {
        repository.updateCompletionStatistic(newStatistic)
    }
}