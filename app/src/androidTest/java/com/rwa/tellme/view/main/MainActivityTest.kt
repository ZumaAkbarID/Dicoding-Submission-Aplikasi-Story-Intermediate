package com.rwa.tellme.view.main

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.rwa.tellme.R
import com.rwa.tellme.data.pref.dataStore
import com.rwa.tellme.utils.EspressoIdlingResource
import com.rwa.tellme.view.welcome.WelcomeActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[IS_LOGIN_KEY] = true
                preferences[EMAIL_KEY] = "mockEmail@domain.com"
                preferences[TOKEN_KEY] = "mockToken"
            }
        }

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        Intents.init()
    }

    @Test
    fun testLogoutFunctionality() {
        onView(withId(R.id.action_logout)).perform(ViewActions.click())

        Intents.intended(hasComponent(WelcomeActivity::class.java.name))

        onView(withId(R.id.activity_main)).check(doesNotExist())
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        Intents.release()
    }

    companion object {
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val IS_LOGIN_KEY = booleanPreferencesKey("isLogin")
    }
}
