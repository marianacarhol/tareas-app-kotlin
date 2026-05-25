package carrillo.mariana.tareasapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TasksScreen(
    viewModel: TaskViewModel = viewModel(
        factory = TaskViewModel.Factory
    )
) {
// Observa la lista de tareas del ViewModel.
// collectAsStateWithLifecycle deja de escuchar
// cuando la pantalla no está visible.
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val searchInput by viewModel.searchInput
        .collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
// Estado local: texto del campo de nueva tarea.
    var nuevaTareaTexto by remember { mutableStateOf("") }
    var tareaPendienteDeEliminar by remember { mutableStateOf<TaskEntity?>(null) }

    LaunchedEffect(sortOrder, tasks.size) {
        if (tasks.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    Scaffold { paddingValues ->
        if (tareaPendienteDeEliminar != null) {
            AlertDialog(
                onDismissRequest = {
                    tareaPendienteDeEliminar = null
                },
                title = {
                    Text(text = "Eliminar tarea")
                },
                text = {
                    Text(text = "¿Seguro que quieres eliminar esta tarea?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            tareaPendienteDeEliminar?.let { task ->
                                viewModel.deleteTask(task)
                            }
                            tareaPendienteDeEliminar = null
                        }
                    ) {
                        Text(text = "Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            tareaPendienteDeEliminar = null
                        }
                    ) {
                        Text(text = "Cancelar")
                    }
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
// ----- Titulo -----
            Text(
                text = stringResource(R.string.app_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // ----- Barra de busqueda -----
            SearchBar(
                searchInput = searchInput,
                onSearchInputChanged = { texto ->
                    viewModel.onSearchInputChanged(texto)
                },
                onSearchClicked = {
                    viewModel.executeSearch()
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SortDropdown(
                selectedOrder = sortOrder,
                onSortOrderSelected = { order ->
                    viewModel.onSortOrderChanged(order)
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )

// ----- Lista de tareas -----
            Box(modifier = Modifier.weight(1f)) {
                if (tasks.isEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.empty_list_message
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(
                            items = tasks,
                            key = { task -> task.id }
                        ) { task ->
                            TaskItem(
                                task = task,
                                onToggleCompleted = {
                                    viewModel.toggleCompleted(task)
                                },
                                onDelete = {
                                    tareaPendienteDeEliminar = task
                                }
                            )
                        }
                    }
                }
            }
// ----- Campo para agregar nueva tarea -----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nuevaTareaTexto,
                    onValueChange = { nuevaTareaTexto = it },
                    placeholder = {
                        Text(
                            text = stringResource(
                                R.string.new_task_placeholder
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        viewModel.addTask(nuevaTareaTexto)
                        nuevaTareaTexto = ""
                    }
                ) {
                    Text(
                        text = stringResource(R.string.add_button)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortDropdown(
    selectedOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOrder.label,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(text = "Ordenar por")
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier
                .menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            SortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = {
                        Text(text = order.label)
                    },
                    onClick = {
                        onSortOrderSelected(order)
                        expanded = false
                    }
                )
            }
        }
    }
}