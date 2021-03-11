package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth
import com.udacity.project4.shared.FakeDataUsingLondonLandmarks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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
            .build()
    }

    @After
    fun cleanup() = database.close()

    /**
     * Checking both insert and select
     */
    @Test
    fun insertNewReminder_and_RetrieveItBack() = runBlockingTest {
        // IF we have a DTO reminder we want to insert...
        val reminderToInsert = FakeDataUsingLondonLandmarks.nextDTOItem

        // GIVEN we insert the DTO reminder using the database
        database.reminderDao().saveReminder(reminderToInsert)

        // WHEN we retrieve from the DB a stored reminder with the id of the reminder we just inserted
        val retrievedReminderFromDB = database.reminderDao().getReminderById(reminderToInsert.id)

        // THEN the retrieved reminder data will match the data we used during insert
        assertThat(retrievedReminderFromDB?.id, `is`(reminderToInsert.id))
        assertThat(retrievedReminderFromDB?.title, `is`(reminderToInsert.title))
        assertThat(retrievedReminderFromDB?.description, `is`(reminderToInsert.description))
        assertThat(retrievedReminderFromDB?.location, `is`(reminderToInsert.location))
        assertThat(retrievedReminderFromDB?.latitude, `is`(reminderToInsert.latitude))
        assertThat(retrievedReminderFromDB?.longitude, `is`(reminderToInsert.longitude))
    }

    /**
     * Inserting a reminder and deleting all reminders
     */
    @Test
    fun insertNewReminder_and_deleteAllReminders() = runBlockingTest {
        // IF we have a DTO reminder we want to insert...
        val reminderToInsert = FakeDataUsingLondonLandmarks.nextDTOItem

        // GIVEN we insert the DTO reminder to the database
        database.reminderDao().saveReminder(reminderToInsert)

        // AND we delete all reminders (now we know there is at least one reminder in our db...)
        database.reminderDao().deleteAllReminders()

        // AND we get back the updated list of reminders returned (after deletion)
        val retrievedRemindersFromDB = database.reminderDao().getReminders()

        // THEN we make sure the retrieved list is empty (since we wanted to remove everything)
        Truth.assertThat(retrievedRemindersFromDB).isEmpty()
    }
}