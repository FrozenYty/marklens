package com.example.marklens.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.marklens.data.MarkLensDatabase
import com.example.marklens.data.entity.RegionTemplate
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class RegionTemplateDaoTest {

    private lateinit var db: MarkLensDatabase
    private lateinit var dao: RegionTemplateDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MarkLensDatabase::class.java
        ).build()
        dao = db.regionTemplateDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_shouldReturnNonZeroId() = runTest {
        val id = dao.insert(RegionTemplate(name = "Midterm Math", regionsJson = "[]"))
        assertTrue(id > 0)
    }

    @Test
    fun getAll_shouldEmitAllTemplates() = runTest {
        dao.insert(RegionTemplate(name = "A", regionsJson = "[]"))
        dao.insert(RegionTemplate(name = "B", regionsJson = "[]"))

        dao.getAll().test {
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun delete_shouldRemoveTemplate() = runTest {
        val id = dao.insert(RegionTemplate(name = "Temp", regionsJson = "[]"))
        dao.delete(RegionTemplate(id = id, name = "Temp", regionsJson = "[]"))

        dao.getAll().test {
            assertEquals(0, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
