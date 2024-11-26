package com.rwa.tellme.view.login

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.rwa.tellme.R
import com.rwa.tellme.ToastMatcher
import com.rwa.tellme.utils.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {

    @get:Rule
    val activity = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        Intents.init()
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        Intents.release()
    }

    @Test
    fun loadLogin_Success() {
        onView(withId(R.id.ed_login_email)).check(matches(isDisplayed()))
        onView(withId(R.id.ed_login_password)).check(matches(isDisplayed()))
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()))
    }

    @Test
    fun loginWithValidCredentials_Success() {
        onView(withId(R.id.ed_login_email)).perform(ViewActions.typeText("zuma@gmail.com"))
        onView(withId(R.id.ed_login_password)).perform(ViewActions.typeText("password"))

        onView(withId(R.id.ed_login_password)).perform(ViewActions.closeSoftKeyboard())

        onView(withId(R.id.loginButton)).perform(ViewActions.click())

        onView(withText(R.string.welcome_back)).inRoot(ToastMatcher()).check(matches(isDisplayed()))

        activity.scenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun loginWithInvalidCredentials_ShowsError() {
        onView(withId(R.id.ed_login_email)).perform(ViewActions.typeText("salah@gmail.com"))
        onView(withId(R.id.ed_login_password)).perform(ViewActions.typeText("bijiOnta"))

        onView(withId(R.id.ed_login_password)).perform(ViewActions.closeSoftKeyboard())

        onView(withId(R.id.loginButton)).perform(ViewActions.click())

        onView(withText("Failed!")).inRoot(isDialog()).check(matches(isDisplayed()))
    }
}