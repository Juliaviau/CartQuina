package com.example.cartquina

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("game") {
            GameScreen(navController = navController, partida = null)
        }
        composable("partides_guardades") {
            PartidesGuardadesScreen(navController = navController)
        }
        composable("cartrons_guardats") {
            CartroListScreen(navController = navController)
        }
        composable("game/{partidaId}") { backStackEntry ->
            val partidaId = backStackEntry.arguments?.getString("partidaId")?.toIntOrNull()
            val context = LocalContext.current
            val database = remember { DatabaseInstance.getDatabase(context) }

            val partida = remember(partidaId) {
                if (partidaId != null) {
                    runBlocking { database.cartroDao().getPartidaById(partidaId) }
                } else null
            }

            GameScreen(navController, partida)
        }


    }
}


@Composable
fun HomeScreen( navController: NavController) {
    var showGameOptions by remember { mutableStateOf(false) } // Estat per mostrar el diàleg
    var showPartidesOptions by remember { mutableStateOf(false) } // Estat per mostrar el diàleg

    // Fons degradat modern
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFBBDEFB), Color(0xFF2196F3)) // Gradient blau
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Nom de l'aplicació amb efecte
            Spacer(modifier = Modifier.height(20.dp))
            /*Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF)) // Degradat blau
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 8.dp) // Mida del fons
            ) {
                Text(
                    text = "CartQuina",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    ),
                    modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp)) // Ombra al text
                )
            }*/
            AnimatedGradientText(text = "CartQuina")
            Spacer(modifier = Modifier.height(30.dp))
            // Il·lustració d'un bingo o bola
            Icon(
                painter = painterResource(id = R.drawable.baseline_star_24), // Requereix afegir la icona a res/drawable
                contentDescription = "Bola de bingo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(20.dp))
            // Botó "Jugar"
            Button(
                onClick = { showGameOptions = true }, // Obre el diàleg
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(50)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF2196F3)
                )
            ) {
                Text(
                    text = "Jugar",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Botó "Configuració"
            OutlinedButton(
                onClick = { navController.navigate("partides_guardades")},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(50)), // Botó arrodonit
                border = BorderStroke(2.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text(text = "Partides Guardades", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }

            // Botó "Cartrons"
            OutlinedButton(
                onClick = { navController.navigate("cartrons_guardats") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(50)), // Botó arrodonit
                border = BorderStroke(2.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text(text = "Cartrons", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }

    val context = LocalContext.current
    val database = DatabaseInstance.getDatabase(context)

    // Diàleg superposat per seleccionar el tipus de joc
    if (showGameOptions) {
        GameOptionsDialog(
            onDismiss = { showGameOptions = false },
            onOptionSelected = { option ->
                // Accions en seleccionar una opció
                println("Opció seleccionada: $option")
                if (option == "AMB BOLES") {
                    showPartidesOptions = true


                    //navController.navigate("game") // Navegar a la pantalla del joc
                }
                showGameOptions = false
            }
        )
    }
    var selectedOption by remember { mutableStateOf<String?>(null) }


    val partidaViewModel: PartidaViewModel = viewModel()
    val partidas = remember { mutableStateListOf<PartidaEntity>() }

    // Obtener partidas de la base de datos
    LaunchedEffect(Unit) {
        val database = DatabaseInstance.getDatabase(context)
        partidas.addAll(database.cartroDao().getAllPartides())
    }


    if (showPartidesOptions) {
        PartidaOptionsDialog(
            onDismiss = { showPartidesOptions = false },
            onPartidaSelected = { partida ->
                // Lógica para cargar la partida seleccionada
                println("Cargar partida: ${partida.id}")
                Log.d("AAAAAAAAAAAAAAdanlt",partida.id.toString())
                //loadGameToScreen(partida, database, navController,partidaViewModel)
                navController.navigate("game/${partida.id}") // Navegar al juego con el ID de la partida
            },
            onCrearPartida = {
                // Lógica para crear una nueva partida
                println("Crear nueva partida")
                navController.navigate("game/new")
            },
            partidas = partidas
        )
    }
}

@Composable
fun PartidaOptionsDialog(
    onDismiss: () -> Unit,
    onPartidaSelected: (PartidaEntity) -> Unit, // Callback para cargar una partida
    onCrearPartida: () -> Unit, // Callback para crear una nueva partida
    partidas: List<PartidaEntity> // Lista de partidas existentes
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Selecciona una opció") },
        text = {
            Column {
                Button(
                    onClick = { onCrearPartida() }, // Crear nueva partida
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear Nova Partida")
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (partidas.isEmpty()) {
                    Text(
                        text = "No hi ha partides guardades.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp) // Puedes ajustar el tamaño de la lista
                    ) {
                        items(partidas) { partida ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onPartidaSelected(partida) },
                                //elevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        text = "Partida: ${partida.id}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Data: ${partida.data}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Estat: ${partida.estat}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Numeros cantats: ${partida.numerosDit.size}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}


  fun loadGameToScreen(partida: PartidaEntity,database: AppDatabase,navController: NavController,partidaViewModel: PartidaViewModel) {
    val cartones = partida.cartronsAsignats.map { cartroId ->
        database.cartroDao().getCartroEstat(cartroId)
    }
    val numerosLlamados = partida.numerosDit

    partidaViewModel.cargarPartida(partida, numerosLlamados)
      Log.d("AAAAAAAAAAAAAAloadddd",partidaViewModel.partida.toString())
    navController.navigate("game")
}

/*suspend fun loadGameToScreen(partida: PartidaEntity, database: AppDatabase, navController: NavController) {
    // Aquí deberías cargar los cartones asignados y los números llamados para la partida

    // Cargar los cartones de la partida
    val cartones = partida.cartronsAsignats.map { cartroId ->
        database.cartroDao().getCartroEstat(cartroId)  // Cargar el estado de cada cartón
    }

    // Cargar los números llamados
    val numerosLlamados = partida.numerosDit

    // Navegar a la pantalla del juego pasando los datos
    navController.navigate("game") {
        // Pasa los datos de la partida como argumentos, por ejemplo:
      //   arguments = bundleOf("cartones" to cartones, "numerosLlamados" to numerosLlamados)
    }
}*/


/*@Composable
fun loadSavedGame(navController: NavController, database: AppDatabase) {
    // Cargar las partidas guardadas de la base de datos
    val partidesGuardades = remember { mutableStateListOf<PartidaEntity>() }

    LaunchedEffect(key1 = Unit) { // key1 triggers the effect only once
        partidesGuardades.addAll(database.cartroDao().getAllPartides())
    }
    //val partidesGuardades = database.cartroDao().getAllPartides()

    // Mostrar un listado con las partidas guardadas
    // Puedes usar LazyColumn para mostrar las partidas guardadas y permitir la selección
    // Después de seleccionar una partida, navegas a la pantalla del juego con los datos de esa partida

    if (partidesGuardades.isNotEmpty()) {
        // Mostrar las partidas y permitir al usuario seleccionar una
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(partidesGuardades) { partida ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Partida: ${partida.data}")
                        Text("Estado: ${partida.estat}")
                        Button(onClick = {
                            // Cargar esta partida y navegar al GameScreen
                            // Puedes cargar los cartones y los números llamados
                            loadGameToScreen(partida,  database,navController, partidaViewModel = PartidaViewModel())
                        }) {
                            Text("Cargar Partida")
                        }
                    }
                }
            }
        }
    } else {
        // Si no hay partidas guardadas, muestra un mensaje
        Text("No tienes partidas guardadas.")
    }
}*/


@Composable
fun createNewGame(navController: NavController, database: AppDatabase) {
    // Crear un nuevo objeto de partida
    val currentDateTime = LocalDateTime.now() // Obtiene la fecha y hora actual
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // Define el formato
    val formattedDateTime = currentDateTime.format(formatter) // Convierte a string con el formato deseado

    val newPartida = PartidaEntity(
        data = formattedDateTime, // Usa la fecha y hora formateada
        numerosDit = listOf(), // Inicia sin números llamados
        estat = "linia", // Puede ser "linia" o "quines"
        cartronsAsignats = listOf() // No asignamos cartones aún
    )

    // Insertar la nueva partida en la base de datos
    LaunchedEffect(Unit) {
        database.cartroDao().insertPartida(newPartida)
    }

    // Navegar a la pantalla del juego
    navController.navigate("game") // Cambiar "game" por el nombre de la ruta del GameScreen
}

/*@Composable
fun GameOptionsDialog(
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(
                text = "Selecciona el tipus de joc",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2196F3)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    onClick = { onOptionSelected("AMB BOLES") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("AMB BOLES")
                }
                TextButton(
                    onClick = { onOptionSelected("INDIVIDUAL SENSE BOLES") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("INDIVIDUAL SENSE BOLES")
                }
                TextButton(
                    onClick = { onOptionSelected("MULTIPLE SENSE BOLES") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("MULTIPLE SENSE BOLES")
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}*/

/*@Composable
fun PartidaOptionsDialog(
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit
){
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Selecciona una opción") },
        text = {
            Column {
                Button(
                    onClick = { onOptionSelected("Crear Nueva") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear Nueva Partida")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onOptionSelected("Cargar Antigua") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cargar Partida Antigua")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}*/


@Composable
fun PartidesGuardadesScreen(navController: NavController) {
    val partides = remember { mutableStateOf<List<PartidaEntity>>(emptyList()) }
    val context = LocalContext.current
    val database = DatabaseInstance.getDatabase(context)

    // Cargar las partidas de la base de datos
    LaunchedEffect(Unit) {
        partides.value = database.cartroDao().getAllPartides()
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(partides.value) { partida ->
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "Partida: ${partida.data}")
                    Text(text = "Estado: ${partida.estat}")
                    Text(text = "Números llamados: ${partida.numerosDit.joinToString(", ")}")
                    Button(onClick = {
                        // Eliminar la partida
                        /*LaunchedEffect(Unit) {
                            database.cartroDao().deletePartida(partida)
                        }*/
                    }) {
                        Text("Eliminar Partida")
                    }
                }
            }
        }
    }
}

@Composable
fun CartroListScreen(navController: NavController) {
    val cartros = remember { mutableStateOf<List<CartroEntity>>(emptyList()) }
    val context = LocalContext.current
    val database = DatabaseInstance.getDatabase(context)

    // Cargar los cartones
    LaunchedEffect(Unit) {
        cartros.value = database.cartroDao().getAllCartros()
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(cartros.value) { cartro ->
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "Cartón ID: ${cartro.id}")
                    Button(onClick = {
                        // Eliminar cartón
                        /*LaunchedEffect(Unit) {
                            database.cartroDao().deleteCartro(cartro)
                        }*/
                    }) {
                        Text("Eliminar Cartón")
                    }
                }
            }
        }
    }
}

@Composable
fun GameScreen(navController: NavController, partida: PartidaEntity?) {

    val cartones = partida?.cartronsAsignats //llista dels cartrons assignats
    val numerosLlamados = partida?.numerosDit

    var gameMode by remember { mutableStateOf("Línia") } //posar estat de la partida
    var expandedMenu by remember { mutableStateOf(false) }  // Controla l'obertura del menú
    var showAddCartroDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // Per gestionar les corrutines
    val context = LocalContext.current
    val database = DatabaseInstance.getDatabase(context)

    if (partida != null) {
        Log.d("AAAAAAAAAAAAAArasi",partida.id.toString())
        gameMode = partida.estat

    } else {
        Log.d("AAAAAAAAAAAAAArasi","NULL")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically // Alinear verticalment
            ) {
                //PER recular
                IconButton(onClick = { navController.navigate("home") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Menu")
                }

                Text(
                    text = "    Partida " + partida?.id + "   " + partida?.data,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.W100,
                        fontSize = 26.sp,
                        color = Color(0xFF374C60)
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp)
                        .wrapContentSize(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Row {
                        IconButton(
                            onClick = {
                                expandedMenu = true
                            }
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Open Menu"
                            )
                        }

                        // Menú desplegable que apareix just sota del botó de menú
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false },  // Tancar el menú
                            modifier = Modifier
                                .background(Color.White, shape = RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            // Opció 1: Afegir Cartro
                            DropdownMenuItem(
                                text = {Text(text = "AFEGIR CARTRO")},
                                onClick = {
                                    println("Afegir Cartro seleccionat")
                                    showAddCartroDialog = true
                                    expandedMenu = false  // Tancar el menú després de seleccionar l'opció
                                },leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Add,
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.Green
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = {Text(text = "GUARDAR PARTIDA")},
                                onClick = {
                                    println("GUARDAR Partida seleccionat")
                                    // Aquí pots gestionar l'acció per acabar la partida
                                    expandedMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.FavoriteBorder,
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.Magenta
                                    )
                                }
                            )

                            // Opció 2: Acabar Partida
                            DropdownMenuItem(
                                text = {Text(text = "ACABAR PARTIDA")},
                                onClick = {
                                    println("Acabar Partida seleccionat")
                                    // Aquí pots gestionar l'acció per acabar la partida
                                    expandedMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Clear,
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.Red
                                    )
                                }
                            )

                            // Opció 3: Reiniciar
                            DropdownMenuItem(
                                text = {Text(text = "REINICIAR")},
                                onClick = {
                                    println("Reiniciar seleccionat")
                                    // Aquí pots gestionar l'acció per reiniciar la partida
                                    expandedMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Refresh,
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.Blue
                                    )
                                }
                            )
                        }
                    }
                }
            }

            /*Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                // Cada fila té 10 boletes
                for (rowIndex in 0 until 9) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly // Espai igual entre boletes
                    ) {
                        for (columnIndex in 0 until 10) {
                            val ballNumber = rowIndex * 10 + columnIndex + 1 // Numeració de 1 a 90
                            Box(
                                modifier = Modifier
                                    .size(32.dp) // Ajustem la mida de les boletes
                                    .background(Color(0xFFF5F5DC), shape = RoundedCornerShape(50)) // Beige clar
                                    .border(2.dp, Color.Black, shape = RoundedCornerShape(50)) // Contorn negre
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ballNumber.toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black, fontWeight = FontWeight.Bold) // Text negre
                                )
                            }
                        }
                    }
                }
            }*/
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                // Cada fila té 10 boletes
                for (rowIndex in 0 until 9) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly // Espai igual entre boletes
                    ) {
                        for (columnIndex in 0 until 10) {
                            val ballNumber = rowIndex * 10 + columnIndex + 1 // Numeració de 1 a 90
                            Ball(
                                ballNumber = ballNumber
                            )
                        }
                    }
                }
            }

            // Botó Quina/Línia
            Button(
                onClick = {
                    gameMode = if (gameMode == "Quina") "Línia" else "Quina"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Anem per " + gameMode,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
            }

            //CARTRONS
           /* val cartroList = remember {
                listOf(
                    listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, null, null, null, null, null, null, null, null, null, null, null),
                    listOf(16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, null, null, null, null, null, null, null, null, null, null, null),
                    listOf(31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, null, null, null, null, null, null, null, null, null, null, null)
                )
            }

            val cartroList = remember { mutableStateListOf<List<Int?>>() }

            // Función para obtener los cartones desde la base de datos
            LaunchedEffect(Unit) {
                val database = DatabaseInstance.getDatabase(context)
                val cartros = database.cartroDao().getAllCartros()
                cartroList.addAll(cartros.map { it.numeros })
            }*/

            /*
            el de boles:
            va per partides. a cada partida guarda una nota, la data, els numeros que s'han dit , si es va per quina o per linia, i cartrons assignats a la partida. a mes, per a cada cartro sap quins numeros s'han tatxat i quins no
            en clicar jugar, es selecciona nova partida o carregar una partida guardada
            un boto a inici per a veure parides guardades, i es mostra quins numeros s'han dit, i lestat dels cartrons. sense poder modificar, nomes eliminar
            un llistat de cartrons tambe, que es puguin modificar, eliminar i afegir. han de tenir un nom tambe


            els altres no guarda a la base de dades la partida, nomes es crea un cartro o sen carrega un o multiples. i es tatxa en aquell moment individualment
             */

            val cartroList = remember { mutableStateListOf<List<Int?>>() }
            if (partida != null) {
                LaunchedEffect(partida.id) {
                    // Cargar los cartones de la base de datos
                    val cartones = loadCartonesDePartida(partida.id, context)
                    if (cartones != null) {
                        cartroList.addAll(cartones)
                    }  // Agregar los cartones a la lista
                }
            }
            // Mostrar la lista de cartones
            if (cartroList.isEmpty()) {
                // Si no hay cartones asignados, mostrar un mensaje
                Text(
                    text = "No hi ha cartrons assignats. S'han d'afegir per a poder jugar.",
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            } else {
                CartroList(cartroList = cartroList) // Si hay cartones, mostrar la lista
            }

            // Scroll de cartrons
            /*LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(3) { // Exemples amb 3 cartrons
                    Cartro(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }*/
        }
    }
    if (showAddCartroDialog) {
        AddCartroDialog(
            onDismiss = { showAddCartroDialog = false },
            onSave = { numbers ->
                // Desa el cartró a la base de dades
                scope.launch {
                    val cartro = CartroEntity(numeros = numbers)


                    // Guardar cartón en la base de datos y obtener su ID como Long
                    //val newCartroIdlong: Long = database.cartroDao().insertCartro(cartro)

                    val newCartroId: Int = database.cartroDao().insertCartro(cartro).toInt()
                    println("Cartró guardat a la base de dades amb ID: $newCartroId")

                    // Agregar el ID del cartón a la partida actual
                    partida?.let { currentPartida ->
                        val updatedCartons = currentPartida.cartronsAsignats.toMutableList()
                        updatedCartons.add(newCartroId.toInt()) // Si realmente necesitas convertirlo

                        val updatedPartida = currentPartida.copy(cartronsAsignats = updatedCartons)
                        database.cartroDao().updatePartida(updatedPartida)

                        println("Cartró associat a la partida actual amb ID: $newCartroId")
                    }

                    showAddCartroDialog = false

                }
            }
        )
    }
}

@Composable
fun CartroList(cartroList: List<List<Int?>>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()

    ) {
        items(cartroList) { cartroNumbers ->
            Cartro(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                numbers = cartroNumbers
            )
        }
    }
}
suspend fun loadCartonesDePartida(partidaId: Int, context: Context): List<List<Int?>>? {
    val database = DatabaseInstance.getDatabase(context)
    val partida = database.cartroDao().getPartidaById(partidaId)  // Obtiene la partida por ID

    // Obtener los cartones asignados a esta partida
    val cartonesAsignados = partida?.cartronsAsignats  // Lista de IDs de cartones asignados
    val cartroList = cartonesAsignados?.map { cartroId ->
        database.cartroDao().getCartroById(cartroId)?.numeros ?: emptyList()
    }

    return cartroList
}


@Composable
fun AddCartroDialog(
    onDismiss: () -> Unit,
    onSave: (List<Int?>) -> Unit
) {
    val cartroNumbers = remember { mutableStateListOf<Int?>().apply { addAll(List(27) { null }) } }
    var showInputDialog by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(-1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crea el teu cartró") },
        text = {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .border(BorderStroke(2.dp, Color.Black), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    repeat(3) { rowIndex ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(9) { columnIndex ->
                                val index = rowIndex * 9 + columnIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(
                                            color = if (cartroNumbers[index] != null) Color(
                                                0xFFFFF9C4
                                            ) else Color(0xFFF0F0F0),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .border(
                                            BorderStroke(1.dp, Color.Black),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clickable {
                                            selectedIndex = index
                                            showInputDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cartroNumbers[index]?.toString() ?: "⭐",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                val filledCount = cartroNumbers.count { it != null }
                Text(
                    text = "Números emplenats: $filledCount/15",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (filledCount == 15) Color.DarkGray else Color.Red
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(cartroNumbers) },
                enabled = cartroNumbers.count { it != null } == 15
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel·lar")
            }
        }
    )

    if (showInputDialog) {
        var input by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showInputDialog = false },
            title = { Text("Escriu un número") },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Número (1-90)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val number = input.toIntOrNull()
                    if (number != null && number in 1..90 && selectedIndex != -1) {
                        cartroNumbers[selectedIndex] = number
                    }
                    showInputDialog = false
                }) {
                    Text("Escriure")
                }
            },
            dismissButton = {
                Button(onClick = { showInputDialog = false }) {
                    Text("Cancel·lar")
                }
            }
        )
    }
}


@Composable
fun Ball(ballNumber: Int) {
    var isSelected by remember { mutableStateOf(false) } // Estat de selecció

    Box(
        modifier = Modifier
            .size(32.dp) // Mida de les boletes
            .background(
                if (isSelected) Color.Black else Color(0xFFF5F5DC), // Fons negre quan seleccionada, beige clar en cas contrari
                shape = RoundedCornerShape(50)
            )
            .border(
                2.dp,
                if (isSelected) Color.Gray else Color.Black, // Contorn gris quan seleccionada, negre per defecte
                shape = RoundedCornerShape(50)
            )
            .padding(6.dp)
            .clickable { isSelected = !isSelected }, // Canvia estat quan es fa clic
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ballNumber.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isSelected) Color.White else Color.Black, // Text blanc quan seleccionada, negre per defecte
                fontWeight = FontWeight.Bold
            )
        )
    }
}


@Composable
fun Cartro(
    modifier: Modifier = Modifier,
    numbers: List<Int?> // Lista de números que representa el cartón
) {
    // Colors per columnes, basat en els colors típics del bingo
    val columnColors = listOf(
        Color(0xFFFFCDD2), // Rosa
        Color(0xFFFFF59D), // Groc
        Color(0xFFA5D6A7), // Verd
        Color(0xFF90CAF9), // Blau
        Color(0xFFCE93D8)  // Lila
    )

    // Estructura del cartró
    Column(
        modifier = modifier
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(BorderStroke(2.dp, Color.Black), shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { rowIndex -> // 3 files
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(9) { columnIndex -> // 9 columnes
                    val index = rowIndex * 9 + columnIndex
                    val number = numbers.getOrNull(index) // Obtiene el número si existe
                    Box(
                        modifier = Modifier
                            .weight(1f) // Totes les cel·les tenen la mateixa amplada
                            .aspectRatio(1f) // Quadrat perfecte
                            .background(
                                color = if (number != null) columnColors[columnIndex % columnColors.size] else Color.Gray,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                BorderStroke(2.dp, Color.Black),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number?.toString() ?: "⭐", // Si el número no es null, muestra el número
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun GameOptionsDialog(
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(
                text = "Selecciona el tipus de joc",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2196F3)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Opció 1: AMB BOLES
                TextButton(
                    onClick = { onOptionSelected("AMB BOLES") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFBBDEFB), shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("AMB BOLES")
                }

                // Opció 2: INDIVIDUAL SENSE BOLES
                TextButton(
                    onClick = { onOptionSelected("INDIVIDUAL SENSE BOLES") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF90CAF9), shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("INDIVIDUAL SENSE BOLES")
                }

                // Opció 3: MULTIPLE SENSE BOLES
                TextButton(
                    onClick = { onOptionSelected("MULTIPLE SENSE BOLES") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF64B5F6), shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("MULTIPLE SENSE BOLES")
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AnimatedGradientText(text: String) {
    // Animació infinita que canvia l'offset del gradient
    val infiniteTransition = rememberInfiniteTransition()
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 300f, // Amplada del desplaçament
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF), Color(0xFF00C6FF)),
                    start = Offset(offsetX, 0f),
                    end = Offset(offsetX + 300f, 300f)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 24.dp, vertical = 8.dp) // Ajustar fons del text
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )
    }
}