package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.FakeDataUsingLondonLandmarks
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //TODO: provide testing to the SaveReminderView and its live data objects
    private lateinit var viewModel: SaveReminderViewModel

    // Rule 1: (Architecture components background jobs in the same thread)
    // Ensure that the test results happen synchronously and in repeatable order
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Rule 2: (coroutines rule in a ViewModel)
    // use this custom rule to swap Dispatchers.Main to TestCoroutineDispatcher to avoid
    // IllegalStateException due to attempting to use the non-existent main looper in a local test
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    /**
     * Initialize a SaveReminderViewModel, by passing the ApplicationContext from AndroidX,
     * and an empty FakeDataSource
     */
    @Before
    fun setupViewModel() {
        stopKoin()
        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeDataSource()
        )
    }

    /**
     * When saving an item a loading animation should appear, and when done or failed it should disappear
     */
    @Test
    fun saveReminder_checkLoading() = mainCoroutineRule.runBlockingTest {
        // generate some random items in a list
        FakeDataUsingLondonLandmarks.generateShuffledData()

        // the loading anim appeared
        mainCoroutineRule.pauseDispatcher()
        viewModel.validateAndSaveReminder(FakeDataUsingLondonLandmarks.getNextDataItem())
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        // the loading anim disappeared
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }


}