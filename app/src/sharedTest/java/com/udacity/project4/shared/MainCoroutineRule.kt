package com.udacity.project4.shared

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Custom JUnit Rule to avoid boilerplate code @Before and @After to allow for testing coroutines
 * that use the Dispatchers.Main (like in the ViewModelScope) and run as Local Tests,
 * because the Dispatchers.Main uses android's main looper, and in local tests is not available.
 *
 * Thus we swap out Dispatchers.Main to the TestCoroutineDispatcher by using setMain
 * from kotlinx-coroutines-test module so there are no IllegalState Exceptions in Local Tests.
 */
@ExperimentalCoroutinesApi
class MainCoroutineRule(private val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()):
    TestWatcher(),
    TestCoroutineScope by TestCoroutineScope(dispatcher) {
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }
}