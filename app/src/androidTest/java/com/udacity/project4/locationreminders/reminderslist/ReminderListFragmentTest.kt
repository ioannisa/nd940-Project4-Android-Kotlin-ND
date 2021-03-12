package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.shared.FakeDataUsingLondonLandmarks
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorReminderListFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {


//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.
    private lateinit var repository: ReminderDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var context: Application

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        stopKoin()

        // get the application context
        context = ApplicationProvider.getApplicationContext()

        val myModule = module {
            viewModel {  RemindersListViewModel(context, get() as ReminderDataSource ) }
            single    { RemindersLocalRepository(get()) as ReminderDataSource }
            single    { LocalDB.createRemindersDao(context) }
        }

        startKoin {
            modules(listOf(myModule))
            repository = get()
        }
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


//    TODO COMPLETED: test the navigation of the fragments.
    /**
     * When the user clicks on the FAB, is navigated to the "SaveReminderFragment"
     */
    @Test
    fun fab_navigates_to_SaveReminderFragment() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorReminderListFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // Click the FAB to get to the SaveReminderFragment
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // Expect to be navigated to the SaveReminderFragment
        Mockito.verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    /**
     * Adding 5 random items and checking whether the data of these items is reflected on the screen
     */
//    TODO: test the displayed data on the UI.
    @Test
    fun addedItems_displayProperlyOnScreen() {

        val remindersToBeAdded = mutableListOf<ReminderDTO>()

        runBlocking {
            // cleanup reminders to make easier to see when we double check the inserted data values
            repository.deleteAllReminders()

            for (i in 1..5) {
                // add 5 random, fresh reminders
                val reminder = FakeDataUsingLondonLandmarks.nextDTOItem
                remindersToBeAdded.add(reminder)     // keep a copy of the reminder to be added
                repository.saveReminder(reminder)    // then add it at the repository
            }
        }

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorReminderListFragment(scenario)

        // now check that the list of the "reminders to be added" matches the data of the displayed - added - reminders
        remindersToBeAdded.forEach{
            Espresso.onView(withText(it.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Espresso.onView(withText(it.description)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Espresso.onView(withText(it.location)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }

        runBlocking {
            // hold on for 2 seconds to see the outcome with your human eyes
            delay(2000)
        }
    }

    /**
     * Going with empty data, should display the "No Data" message on the screen
     */
    //    TODO: test the displayed data on the UI.
    @Test
    fun noItems_displays_noDataMessage() {

        runBlocking {
            // cleanup reminders again to check "no data" message on an empty reminder list
            repository.deleteAllReminders()
        }

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorReminderListFragment(scenario)

        Espresso.onView(ViewMatchers.withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        runBlocking {
            // hold on for 2 seconds to see the "No Items" message with your human eyes
            delay(2000)
        }
    }
}