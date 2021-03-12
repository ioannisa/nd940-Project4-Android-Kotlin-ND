package com.udacity.project4.locationreminders.savereminder

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
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import com.udacity.project4.shared.FakeDataUsingLondonLandmarks
import com.udacity.project4.shared.getOrAwaitValue
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorSaveReminderFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
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
    private lateinit var context: Application

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        stopKoin()

        // get the application context
        context = ApplicationProvider.getApplicationContext()

        val myModule = module {
            single {  SaveReminderViewModel(context, get() as ReminderDataSource ) }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(context) }
        }

        startKoin {
            modules(listOf(myModule))
        }

        viewModel = get()
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

//    TODO COMPLETE: add testing for the error messages.
    /**
     * In this test we simply try to save a completely empty reminder
     * The first error check is the title, thus we will get an error message asking to fill in the title
     */
    @Test
    fun saveReminder_NoTitle_Error() {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorSaveReminderFragment(scenario)

        // Click the Save button
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        // Expect an error message about the title not being set
        Truth.assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)

        runBlocking {
            // hold on for 2 seconds to see the error message with your human eyes
            delay(2000)
        }
    }

    /**
     * In this test we will setup both Title and Detail texts
     * However the location will be unset, we should get an location_error shown in the SnackBar
     */
    @Test
    fun saveReminder_NoLocation_Error() {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorSaveReminderFragment(scenario)

        // Get a random reminder (reminderDataItem) from the pool of fake data
        val reminderDataItem = FakeDataUsingLondonLandmarks.nextDataItem

        // Based on that reminderDataItem...
        // Set the Title
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitleData)).perform(ViewActions.typeText(reminderDataItem.title))
        Espresso.closeSoftKeyboard()

        // Set Description
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription)).perform(ViewActions.typeText(reminderDataItem.description))
        Espresso.closeSoftKeyboard()

        // Click the Save button
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        // Expect an error message about location not being set
        Truth.assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)

        runBlocking {
            // hold on for 2 seconds to see the error message with your human eyes
            delay(2000)
        }
    }

//    TODO COMPLETE: test the displayed data on the UI.
//    TODO COMPLETED: test the navigation of the fragments.
    /**
     * In this test we will get all the info (Title, Detail Text, Location)
     * and we expect to successfully add a reminder
     */
    @Test
    fun saveReminder_FullData_Success() {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorSaveReminderFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // Get a random reminder (reminderDataItem) from the pool of fake data
        val reminderDataItem = FakeDataUsingLondonLandmarks.nextDataItem

        // Based on that reminderDataItem...
        // Set the Title
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitleData)).perform(ViewActions.typeText(reminderDataItem.title))
        Espresso.closeSoftKeyboard()

        // Set Description
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription)).perform(ViewActions.typeText(reminderDataItem.description))
        Espresso.closeSoftKeyboard()

        // Set the location
        viewModel.updatePOI(LatLng(reminderDataItem.latitude?:0.0, reminderDataItem.longitude?:0.0), reminderDataItem.location?:"")

        // Now we have all the data required available...

        // Click the Save button
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        // If all aok, then expect to be navigated back to the ReminderListFragment (pop at the navigation BackStack)
        Mockito.verify(navController).popBackStack()

        // When all ok, a Toast with success message should also appear
        ViewMatchers.assertThat(viewModel.showToast.getOrAwaitValue(), `is`(context.getString(R.string.reminder_saved)))

        runBlocking {
            // hold on for 2 seconds to see the success message with your human eyes
            delay(2000)
        }
    }
}