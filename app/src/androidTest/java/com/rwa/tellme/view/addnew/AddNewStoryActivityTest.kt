package com.rwa.tellme.view.addnew

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.rwa.tellme.R
import com.rwa.tellme.ToastMatcher
import com.rwa.tellme.data.pref.dataStore
import com.rwa.tellme.view.main.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class AddNewStoryActivityTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext

        val appContext = ApplicationProvider.getApplicationContext<Context>()

        runBlocking {
            appContext.dataStore.edit { preferences ->
                preferences[IS_LOGIN_KEY] = true
                preferences[EMAIL_KEY] = "zm1@gmail.com"
                preferences[TOKEN_KEY] = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLUZQMzZPbUZEZldqY3pUeXEiLCJpYXQiOjE3MzI1MTQ0ODB9.8WdDNOaTuP4wKzaNZyemCr8ryXK-FcQbaE18y-tEQ3g"
            }
        }

        Intents.init()
        mockMediaSelection()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    private fun mockMediaSelection() {
        val assetManager = InstrumentationRegistry.getInstrumentation().context
        val inputStream = assetManager.assets.open("test_image.jpg")
        val tempFile = File(context.getExternalFilesDir(null), "temp_image.jpg")
        val outputStream = FileOutputStream(tempFile)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        val uri = Uri.fromFile(tempFile)
        val bundle = Bundle()
        val parcels = arrayListOf<Parcelable>(uri)
        bundle.putParcelableArrayList(Intent.EXTRA_STREAM, parcels)

        val resultData = Intent().apply { putExtras(bundle) }

        intending(IntentMatchers.hasAction(Intent.ACTION_PICK))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, resultData))
    }

    @Test
    fun testUploadStoryWithMockMedia() {
        Thread.sleep(1000L)
        onView(withId(R.id.button_add)).perform(click())

        Thread.sleep(1000L)
        onView(withId(R.id.btn_gallery)).perform(click())

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            Toast.makeText(context, "Pick image manually! hehe. Then when in uCrop test can auto crop, but need time longer!", Toast.LENGTH_LONG).show()
        }

        onView(withId(com.yalantis.ucrop.R.id.menu_crop)).perform(click())

        onView(withId(R.id.ed_add_description)).perform(typeText("INI END-TO-END TEST DENGAN ESPRESSO, UPLOAD GAMBAR"))
        onView(withId(R.id.ed_add_description)).perform(ViewActions.closeSoftKeyboard())

        onView(withId(R.id.button_add)).perform(click())

        onView(withText(R.string.upload_story_success)).inRoot(ToastMatcher()).check(matches(isDisplayed()))

        intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
    }

    companion object {
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val IS_LOGIN_KEY = booleanPreferencesKey("isLogin")
    }
}