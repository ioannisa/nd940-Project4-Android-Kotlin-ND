package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
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
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO COMPLETED: Add testing implementation to the RemindersDao.kt

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
    fun action_insertNewReminder_and_RetrieveItBack() = runBlocking {
        // IF we have a DTO reminder we want to insert...
        val reminderToInsert = FakeDataUsingLondonLandmarks.getNextDTOItem()

        // WHEN we insert the DTO reminder
        localDataSource.saveReminder(reminderToInsert)

        // THEN if we retrieve the inserted reminder from the DB
        val retrievedReminderFromDB = localDataSource.getReminder(reminderToInsert.id)

        // AND that reminder is instance of class Success
        Truth.assertThat(retrievedReminderFromDB).isInstanceOf(Result.Success::class.java)
        retrievedReminderFromDB as Result.Success

        // AND the retrieved reminder data will match the data we used during insert
        assertThat(retrievedReminderFromDB.data.id, `is`(reminderToInsert.id))
        assertThat(retrievedReminderFromDB.data.title, `is`(reminderToInsert.title))
        assertThat(retrievedReminderFromDB.data.description, `is`(reminderToInsert.description))
        assertThat(retrievedReminderFromDB.data.location, `is`(reminderToInsert.location))
        assertThat(retrievedReminderFromDB.data.latitude, `is`(reminderToInsert.latitude))
        assertThat(retrievedReminderFromDB.data.longitude, `is`(reminderToInsert.longitude))
    }

    /**
     * Inserting a reminder and deleting all reminders
     */
    @Test
    fun action_insertNewReminder_and_deleteAllReminders() = runBlocking {
        // IF we have a DTO reminder we want to insert...
        val reminderToInsert = FakeDataUsingLondonLandmarks.getNextDTOItem()

        // WHEN we insert the DTO reminder
        localDataSource.saveReminder(reminderToInsert)

        // THEN if we retrieve the inserted reminder from the DB
        val retrievedReminderFromDB = localDataSource.getReminder(reminderToInsert.id)
        Truth.assertThat(retrievedReminderFromDB).isInstanceOf(Result.Success::class.java)

        // WHEN we delete all reminders (now we know there is at least one reminder in our db...)
        localDataSource.deleteAllReminders()

        // THEN WHEN we get the list of reminders returned (after deletion)
        val retrievedRemindersFromDB = localDataSource.getReminders()
        // AND that list is instance of class Success
        Truth.assertThat(retrievedRemindersFromDB).isInstanceOf(Result.Success::class.java)
        retrievedRemindersFromDB as Result.Success

        // THEN we make sure the retrieved list is empty (since we wanted to remove everything)
        Truth.assertThat(retrievedRemindersFromDB.data).isEmpty()
    }
}