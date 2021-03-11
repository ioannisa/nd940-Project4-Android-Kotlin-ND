package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.shared.FakeDataUsingLondonLandmarks
import com.udacity.project4.shared.getOrAwaitValue
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorSaveReminderFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito


// UI Testing
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class SaveReminderFragmentTest : AutoCloseKoinTest() {

    private lateinit var viewModel: SaveReminderViewModel
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        // Stop the original app koin.
        stopKoin()

        val myModule = module {
            single {
                SaveReminderViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }
        }

        // Declare a new koin module.
        startKoin {
            modules(listOf(myModule))
            viewModel = get()
        }
    }

    @Test
    fun incompleteData() = runBlocking {
        val navController = Mockito.mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.AppTheme)

        dataBindingIdlingResource.monitorSaveReminderFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())
        Truth.assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)

        // Take time to see the SnackBar with the message "Please enter title"
        delay(2000)

        val reminderDataItem = FakeDataUsingLondonLandmarks.nextDataItem


        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription)).perform(ViewActions.typeText(reminderDataItem.description))
        Espresso.closeSoftKeyboard()

        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())
        Truth.assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)

        delay(2000)
    }
}