package carrillo.mariana.tareasapp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val dao: TaskDao
) : ViewModel() {
    // Exponemos la lista de tareas como StateFlow.
    // stateIn convierte el Flow del DAO en un StateFlow
    // que Compose puede observar fácilmente.
        // Texto que el usuario esta escribiendo en el campo
    // de busqueda. Cambia con cada tecla, pero NO dispara
    // la consulta a la base de datos.
    private val _searchInput = MutableStateFlow("")
    val searchInput: StateFlow<String> = _searchInput.asStateFlow()

    // Texto con el que se esta filtrando efectivamente.
    // Solo cambia cuando el usuario pulsa el boton de buscar.
    private val _activeQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<TaskEntity>> = _activeQuery
        .flatMapLatest { query ->
            dao.searchTasks(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    fun addTask(title: String) {
    // Evitamos insertar tareas con titulo vacio.
        if (title.isBlank()) return
        viewModelScope.launch {
            dao.insert(TaskEntity(titulo = title.trim()))
        }
    }
    fun toggleCompleted(task: TaskEntity) {
        viewModelScope.launch {
            dao.update(task.copy(completado = !task.completado))
        }
    }
    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            dao.delete(task)
        }
    }

    fun onSearchInputChanged(text: String) {
    // Solo actualizamos lo que se ve en el campo.
    // La busqueda real NO se dispara aqui.
        _searchInput.value = text
    }
    fun executeSearch() {
    // Aqui SI se dispara la consulta: copiamos el
    // texto del campo a _activeQuery, que es el que
    // observa el Flow de tasks.
        _activeQuery.value = _searchInput.value.trim()
    }
    // ----- Factory -----
    // El companion object guarda una Factory que sabe
    // como construir TaskViewModel con sus parámetros.
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val dao = AppDatabase
                    .getInstance(application)
                    .taskDao()
                TaskViewModel(dao)
            }
        }
    }
}