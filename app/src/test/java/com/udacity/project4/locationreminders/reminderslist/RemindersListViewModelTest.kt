package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.shared.FakeDataUsingLondonLandmarks
import com.udacity.project4.shared.MainCoroutineRule
import com.udacity.project4.shared.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

//@Config(sdk = [Build.VERSION_CODES.O])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO COMPLETED: provide testing to the RemindersListViewModel and its live data objects
    private lateinit var viewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    // Rule 1: (Architecture components background jobs executed synchronously at same thread)
    // Ensure that the test results happen synchronously and in repeatable order
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Rule 2: (coroutines rule in a ViewModel)
    // use this custom rule to swap Dispatchers.Main to TestCoroutineDispatcher to avoid
    // IllegalStateException due to attempting to use the non-existent main looper in a local test
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    /**
     * Initialize the RemindersListViewModel and pass it the fake locations on its constructor
     */
    @Before
    fun setupViewModel() = mainCoroutineRule.runBlockingTest {
        stopKoin()

        fakeDataSource = FakeDataSource().apply {
            // add 5 random landmark item reminders
            for (i in 1..5){
                saveReminder(FakeDataUsingLondonLandmarks.nextDTOItem) // runBlockingTest because saveReminder() is suspended function
            }
        }

        // create a ViewModel populated by the fake dataSource's random landmark items
        //
        // utilize AndroidX's ApplicationProvider to provide an Application Context for local tests,
        // and pass the fake DataSource
        viewModel =  RemindersListViewModel(
            app =         ApplicationProvider.getApplicationContext(),
            dataSource =  fakeDataSource
        )
    }


    /**
     * Loading the RemindersList we get an animating ProgressBar until the items are loaded in the RecyclerView
     *
     * This test will ensure that the during this loading, the animation shows,
     * and when done loading the animation goes away
     */
    @Test
    fun progressBar_AppearDisappear() {
        // loading animation show until items loaded
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        // loading hidden when done loading items
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    /**
     * When the reminders are loaded on the screen we should have the following conditions met
     * 1) The animating ProgressBar should be invisible
     * 2) The remindersList should not be empty
     * 3) the showNoData should be false (as there is data)
     */
    @Test
    fun conditions_withDataLoaded()  {
        viewModel.loadReminders()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(viewModel.remindersList.getOrAwaitValue().isEmpty(), `is`(false))
        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    /**
     * When no reminders are loaded on the screen we should have the following conditions met
     * 1) The animating ProgressBar should be invisible
     * 2) The remindersList should be empty
     * 3) the showNoData should be true (as there is no data)
     */
    @Test
    fun conditions_withoutDataLoaded() = mainCoroutineRule.runBlockingTest {
        fakeDataSource.deleteAllReminders() // runBlockingTest because deleteAllReminders() is suspended function
        viewModel.loadReminders()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(viewModel.remindersList.getOrAwaitValue().isEmpty(), `is`(true))
        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(true))
    }
}