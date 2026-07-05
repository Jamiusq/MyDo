package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.TaskRepository
import com.example.data.TodoDatabase
import com.example.ui.TodoScreen
import com.example.ui.TodoViewModel
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var database: TodoDatabase
  private lateinit var repository: TaskRepository
  private lateinit var viewModel: TodoViewModel

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, TodoDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    repository = TaskRepository(database.taskDao())
    viewModel = TodoViewModel(repository)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun todo_screen_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        TodoScreen(viewModel = viewModel)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
