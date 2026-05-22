package carrillo.mariana.tareasapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query(
        "SELECT * FROM tasks"
    )
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("""
        SELECT * FROM tasks
        WHERE titulo LIKE '%' || :query || '%'
        ORDER BY creado_en DESC
        """)
    fun searchTasks(query: String): Flow<List<TaskEntity>>
}