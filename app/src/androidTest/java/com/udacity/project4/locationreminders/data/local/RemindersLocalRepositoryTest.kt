package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.shared.FakeDataUsingLondonLandmarks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO COMPLETED: Add testing implementation to the RemindersLocalRepository.kt
private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    // Rule 1: (Architecture components background jobs executed synchronously at same thread)
    // Ensure that the test results happen synchronously and in repeatable order
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup(){
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanup() = database.close()

    /**
     * Checking both insert and select
     */
    @Test
    fun insertNewReminder_and_RetrieveItBack() = runBlocking {
        // IF we have a DTO reminder we want to insert...
        val reminderToInsert = FakeDataUsingLondonLandmarks.nextDTOItem

        // GIVEN we insert the DTO reminder
        localDataSource.saveReminder(reminderToInsert)

        // WHEN we retrieve the inserted reminder from the repository
        val retrievedReminderFromRepository = localDataSource.getReminder(reminderToInsert.id)

        // AND that reminder is instance of class Success
        Truth.assertThat(retrievedReminderFromRepository).isInstanceOf(Result.Success::class.java)
        retrievedReminderFromRepository as Result.Success

        // THEN the retrieved reminder data will match the data we used during insert
        assertThat(retrievedReminderFromRepository.data.id, `is`(reminderToInsert.id))
        assertThat(retrievedReminderFromRepository.data.title, `is`(reminderToInsert.title))
        assertThat(retrievedReminderFromRepository.data.description, `is`(reminderToInsert.description))
        assertThat(retrievedReminderFromRepository.data.location, `is`(reminderToInsert.location))
        assertThat(retrievedReminderFromRepository.data.latitude, `is`(reminderToInsert.latitude))
        assertThat(retrievedReminderFromRepository.data.longitude, `is`(reminderToInsert.longitude))
    }

    /**
     * Inserting a reminder and deleting all reminders
     */
    @Test
    fun insertNewReminder_and_deleteAllReminders() = runBlocking {
        // IF we have a DTO reminder we want to insert...
        val reminderToInsert = FakeDataUsingLondonLandmarks.nextDTOItem

        // GIVEN we insert the DTO reminder
        localDataSource.saveReminder(reminderToInsert)

        // WHEN we delete all reminders (now we know there is at least one reminder in our repo...)
        localDataSource.deleteAllReminders()

        // AND we get back the updated list of reminders returned (after deletion)
        val retrievedRemindersFromRepository = localDataSource.getReminders()

        // AND that list is instance of class Success
        Truth.assertThat(retrievedRemindersFromRepository).isInstanceOf(Result.Success::class.java)
        retrievedRemindersFromRepository as Result.Success

        // THEN the retrieved list should be empty
        Truth.assertThat(retrievedRemindersFromRepository.data).isEmpty()
    }
}