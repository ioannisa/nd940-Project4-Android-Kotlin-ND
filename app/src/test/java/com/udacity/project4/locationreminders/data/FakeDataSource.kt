package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

//    TODO COMPLETED: Create a fake data source to act as a double to the real data source
    private var shouldReturnError = false

    fun setReturnError(shouldReturnError: Boolean) {
        this.shouldReturnError = shouldReturnError
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //TODO COMPLETED: ("Return the reminders")
        return  if (reminders == null)
            Result.Error("Reminders is Null")
        else {
            if (shouldReturnError) {
                Result.Error("Data Error")
            } else {
                Result.Success(reminders)
            }
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        //TODO COMPLETED: ("save the reminder")
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //TODO COMPLETED: ("return the reminder with the id")
        val reminder = reminders?.firstOrNull { reminderFound ->
            reminderFound.id == id
        }

        reminder?.let {
            return Result.Success(it)
        }
        return Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
        //TODO COMPLETED: ("delete all the reminders")
        reminders?.clear()
    }
}