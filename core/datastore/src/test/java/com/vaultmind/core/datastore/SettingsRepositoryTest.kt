package com.vaultmind.core.datastore

import app.cash.turbine.test
import com.vaultmind.core.model.ThemeMode
import com.vaultmind.core.testing.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsRepositoryTest {
    @Test fun updatesThemeModeThroughSettingsFlow() = runTest {
        val repository = FakeSettingsRepository()
        repository.settings.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem().themeMode)
            repository.setThemeMode(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, awaitItem().themeMode)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
