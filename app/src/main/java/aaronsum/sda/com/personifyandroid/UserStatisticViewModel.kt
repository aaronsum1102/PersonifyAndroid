package aaronsum.sda.com.personifyandroid

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class UserStatisticViewModel : ViewModel() {
    private val repository = UserStatisticRepository()
    val userStatistics: LiveData<UserStatistics> = repository.userStatistics

    fun initUserStatistic(userId: String) {
        repository.initialiseCollection(userId)
    }

    fun updateStatistic(command: String) {
        repository.updateStatistic(command)
    }

    fun updateCompletionStatistic(newStatistic: UserCompletionStatistic) {
        repository.updateCompletionStatistic(newStatistic)
    }

    fun deleteStatistic() {
        repository.deleteStatistic()
        clearStatisticWhenNoUser()
    }

    fun clearStatisticWhenNoUser() = repository.clearStatistic()
}