package carrillo.mariana.tareasapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TaskDao

    // @Before se ejecuta antes de CADA test
    @Before
    fun setUp() {
        val context = ApplicationProvider
            .getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = db.taskDao()
    }

    // @After se ejecuta después de CADA test
    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertarTarea() = runTest {

        // Arrange
        val tarea = TaskEntity(
            titulo = "Comprar pan"
        )

        // Act
        dao.insert(tarea)

        // Assert
        val tareas = dao
            .getAllTasks()
            .first()

        assertEquals(1, tareas.size)
        assertEquals(
            "Comprar pan",
            tareas[0].titulo
        )
    }

    @Test
    fun actualizarTarea() = runTest {

        dao.insert(
            TaskEntity(titulo = "Estudiar")
        )

        val original = dao
            .getAllTasks()
            .first()
            .first()

        assertEquals(
            false,
            original.completado
        )

        dao.update(
            original.copy(
                completado = true
            )
        )

        val actualizada = dao
            .getAllTasks()
            .first()
            .first()

        assertTrue(
            actualizada.completado
        )
    }

    @Test
    fun borrarTarea() = runTest {

        dao.insert(
            TaskEntity(titulo = "Algo")
        )

        val tarea = dao
            .getAllTasks()
            .first()
            .first()

        dao.delete(tarea)

        val tareas = dao
            .getAllTasks()
            .first()

        assertTrue(tareas.isEmpty())
    }
}