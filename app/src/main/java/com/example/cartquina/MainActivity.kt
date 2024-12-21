package com.example.cartquina

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.launch
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
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.sql.SQLOutput
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.text.contains
import kotlin.text.toFloat

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
        composable("gameNoBolesIdividual") {
            GameNoBolesIndividualScreen(navController = navController, idsCartronsSeleccionats = null)
        }

        composable("gameNoBolesIdividual/{idsJson}") { backStackEntry ->
            val idsJson = backStackEntry.arguments?.getString("idsJson") ?: "[]"
            val idsCartronsSeleccionats: List<Int> = Gson().fromJson(idsJson, object : TypeToken<List<Int>>() {}.type)

            GameNoBolesIndividualScreen(
                navController = navController,
                idsCartronsSeleccionats = idsCartronsSeleccionats
            )
        }
    }
}

@Composable
fun GameNoBolesIndividualScreen(navController: NavController, idsCartronsSeleccionats: List<Int>?) {
    val numerosSeleccionados = remember { mutableStateListOf<Int>() }
    var gameMode by remember { mutableStateOf("Línia") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val database = DatabaseInstance.getDatabase(context)

    val cartroEntities = remember { mutableStateOf<List<CartroEntity>>(emptyList()) }

    // Controla la rotación de la lista de cartones
    var isRotated by remember { mutableStateOf(false) }

    LaunchedEffect(idsCartronsSeleccionats) {
        val cartros = idsCartronsSeleccionats?.map { id ->
            database.cartroDao().getCartroById(id)
        }
        cartroEntities.value = cartros as List<CartroEntity>
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

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    navController.navigate("home")
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Menu")
                }

                Text(
                    text = "  Cartró ",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.W100,
                        fontSize = 24.sp,
                        color = Color(0xFF374C60)
                    )
                )

                IconButton(onClick = {
                    // Cambiar el estado de rotación
                    isRotated = !isRotated
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Orientacio de cartrons")
                }
            }

            // Botón Quina/Línia
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
                    text = "Anem per $gameMode",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
            }

            Text(
                text = "num seleccionats {${numerosSeleccionados.size}}",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
            )

            // Renderizar la lista de cartones
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isRotated) 0.dp else 0.dp) // Padding horizontal cuando está rotado
                    .graphicsLayer {
                        rotationZ = if (isRotated) 90f else 0f
                        // Ajustar el pivote para centrar la rotación
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    },
                contentPadding = PaddingValues(
                    vertical = if (isRotated) 6.dp else 8.dp // Ajustar padding inicial si está rotado
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Reducido el espacio entre elementos
            ) {
                if (isRotated) {
                    item {
                        Spacer(modifier = Modifier.height(100.dp)) // Espacio extra antes del primer cartón
                    }
                }
                items(cartroEntities.value) { cartro ->
                    Box(
                        modifier = Modifier
                            .background(Color.LightGray)
                            .padding(horizontal = 1.dp, vertical = 1.dp)
                            .aspectRatio(2f)
                            .fillMaxSize(), // Ocupa todo el ancho disponible
                        contentAlignment = Alignment.Center

                    ) {
                        Column(

                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth(), // Reducido el padding del Column
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Cartró: ${cartro.id}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp)
                            )
                            Cartro(
                                modifier = Modifier
                                    .padding(vertical = 4.dp), // Reducido el padding vertical
                                numbers = cartro.numeros,
                                numerosSeleccionados = numerosSeleccionados,
                                estat = gameMode
                            )
                        }
                    }
                }
                if (isRotated) {
                    item {
                        Spacer(modifier = Modifier.height(100.dp)) // Espacio extra antes del primer cartón
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen( navController: NavController) {
    var showGameOptions by remember { mutableStateOf(false) } // Estat per mostrar el diàleg
    var showPartidesOptions by remember { mutableStateOf(false) } // Estat per mostrar el diàleg
    var individualsenseboles by remember { mutableStateOf(false) } // Estat per mostrar el diàleg
    var mostrarCrearCartroNou by remember { mutableStateOf(false) }
    var mostrarCarregarCartroExistent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                } else if (option == "INDIVIDUAL SENSE BOLES") {
                    individualsenseboles = true
                }
                showGameOptions = false
            }
        )
    }


    if (individualsenseboles) {
        //Seleccionar escollir un cartro existent, o crearne un
        OpcionsAfegirCartro (
            onDismiss = { individualsenseboles = false },
            onOptionSelected = { option ->
                // Accions en seleccionar una opció
                println("Opció seleccionada: $option")

                if (option == "CREAR UN CARTRÓ NOU") {
                    mostrarCrearCartroNou = true
                } else if (option == "CARREGAR UN CARTRÓ EXISTENT") {
                    mostrarCarregarCartroExistent = true
                }
                individualsenseboles = false
            }
        )
    }

    if (mostrarCarregarCartroExistent) {
        AddCartroExistentDialog(
            onDismiss = { mostrarCarregarCartroExistent = false },
            onSave = { idsCartronsSeleccionats, numerosCartronsSeleccionats ->
                val idsJson = Gson().toJson(idsCartronsSeleccionats)
                navController.navigate("gameNoBolesIdividual/$idsJson")
            }
        )

    }

    /*if (mostrarCrearCartroNou) {
        AddCartroDialog(
            onDismiss = { mostrarCrearCartroNou = false },
            onSave = { numbers ->
                scope.launch(Dispatchers.IO) {
                    val cartro = CartroEntity(numeros = numbers)
                    val newCartroId = database.cartroDao().insertCartro(cartro).toInt()

                    withContext(Dispatchers.Main) {
                        // Añadir el nuevo ID a la lista mutable
                        cartronsPartidaId.add(newCartroId)

                        // Actualizar la partida con los nuevos IDs de cartones
                        partida?.let { currentPartida ->
                            val updatedPartida = currentPartida.copy(cartronsAsignats = cartronsPartidaId.toList())
                            scope.launch(Dispatchers.IO) {
                                database.cartroDao().updatePartida(updatedPartida)
                            }
                        }

                        // Actualizar la UI
                        cartroList.add(cartro.numeros)
                        mostrarCrearCartroNou = false
                    }
                }
            }
        )
    }*/



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
                createNewGame(navController, database)
                //val partidanova = createNewGame(navController, database)
               // navController.navigate("game/new")
            },
            partidas = partidas
        )
    }
}

@Composable
fun PartidaOptionsDialog(onDismiss: () -> Unit, onPartidaSelected: (PartidaEntity) -> Unit, onCrearPartida:   () -> Unit, partidas: List<PartidaEntity>) {
    val gradientColors = listOf(
        listOf(Color(0xFFBBDEFB), Color(0xFF2196F3)), // Gradient 1
        listOf(Color(0xFFE91E63), Color(0xFFFFC107)), // Gradient 2
        listOf(Color(0xFF4CAF50), Color(0xFF00BCD4)), // Gradient 3
        // Add more gradient color pairs as needed
    )

    val gradientcol = listOf(Color(0xFFBBDEFB), Color(0xFF65B1EE),Color(0xFF2196F3),Color(0xFF65B1EE),Color(0xFFBBDEFB)) // Gradient 1


    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(
                text = "Selecciona una opció",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2196F3)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botó per crear nova partida amb "+" i contorn marcat
                TextButton(
                    onClick = { /*onCrearPartida()*/onCrearPartida() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .border(2.dp, Color(0xFF2196F3), shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2196F3))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Afegir nova partida",
                            tint = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear Nova Partida")
                    }
                }

                if (partidas.isEmpty()) {
                    // Missatge si no hi ha partides
                    Text(
                        text = "No hi ha partides guardades.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Llista de partides existents amb scroll
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp) // Defineix l'alçada del scroll
                    ) {
                        items(partidas) { partida ->
                                val gradientIndex =
                                    partidas.indexOf(partida) % gradientColors.size // Get index and cycle through colors
                                val currentGradientColors = gradientColors[gradientIndex]

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .border(
                                            2.dp,
                                            Color(0xFF2196F3),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onPartidaSelected(partida) }
                                        .shadow(2.dp, RoundedCornerShape(8.dp)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                brush = Brush.sweepGradient(
                                                    colors = gradientcol
                                                )
                                            )
                                            .fillMaxSize()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Text(
                                                text = "Partida: ${partida.id}",
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.DarkGray
                                                )
                                            )
                                            Text(
                                                text = "Data: ${partida.data}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = Color.White
                                                )
                                            )
                                            Text(
                                                text = "Estat: ${partida.estat}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = Color.White
                                                )
                                            )
                                            Text(
                                                text = "Cartrons: ${partida.cartronsAsignats.size}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = Color.White
                                                )
                                            )
                                            Text(
                                                text = "Numeros cantats: ${partida.numerosDit.size}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
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

 fun createNewGame(navController: NavController, database: AppDatabase) {
    // Crear un nuevo objeto de partida
    val currentDateTime = LocalDateTime.now() // Obtiene la fecha y hora actual
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // Define el formato
    val formattedDateTime = currentDateTime.format(formatter) // Convierte a string con el formato deseado

    val newPartida = PartidaEntity(
        data = formattedDateTime, // Usa la fecha y hora formateada
        numerosDit = listOf(), // Inicia sin números llamados
        estat = "Línia", // Puede ser "linia" o "quines"
        cartronsAsignats = listOf() // No asignamos cartones aún
    )

    Log.d("CREAR NOVA PARTIDA",newPartida.toString())


         // Crear nueva partida
         val executor = Executors.newSingleThreadExecutor()
         executor.execute {
             val idpartida = database.cartroDao().insertPartida(newPartida)
             Log.d("CREAR NOVA PARTIDA ID", idpartida.toString())

             // Navegar al nuevo juego después de crear la partida
             Handler(Looper.getMainLooper()).post {
                 navController.navigate("game/${idpartida}")
             }
         }
}

@Composable
fun PartidesGuardadesScreen(navController: NavController) {
    val partides = remember { mutableStateOf<List<PartidaEntity>>(emptyList()) }
    val context = LocalContext.current
    val database = DatabaseInstance.getDatabase(context)

    // Cargar las partidas de la base de datos usando LaunchedEffect
    LaunchedEffect(Unit) {
        partides.value = database.cartroDao().getAllPartides()
    }

    // Función para eliminar una partida y recargar la lista
    fun eliminarPartida(partida: PartidaEntity) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            database.cartroDao().deletePartida(partida)
            // Recargar la lista de partidas después de eliminar
            // Lanzar una coroutine para cargar las partidas de nuevo
            // Usamos LaunchedEffect para actualizar la UI de manera adecuada.
            partides.value = database.cartroDao().getAllPartidesUn() // Esto también necesita estar dentro de un contexto suspendido
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFBBDEFB), Color(0xFF2196F3)) // Gradient blau
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White),
                horizontalArrangement = Arrangement.Absolute.Left,
                verticalAlignment = Alignment.CenterVertically // Alinear verticalment
            ) {
                //PER recular
                IconButton(onClick = {
                    navController.navigate("home")
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Menu")
                }


                Text(
                    text = "          Partides Guardades",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.W500),
                    color = Color.Black
                )
            }

            // Pantalla con lista de partidas
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(partides.value) { partida ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp)) // Vores arrodonides
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFBBDEFB),
                                        Color(0xFF2196F3)
                                    )
                                )
                            )
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .border(
                                2.dp,
                                Color(0xFF000000),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF93BDE0),
                                            Color(0xFF97C4E8),

                                            Color(0xFFD7E5F1)
                                        )
                                    )
                                )
                                .fillMaxSize()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Datos de la partida
                                Row (
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ){
                                    Text(
                                        text = "Partida: ${partida.id}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF1f77be),
                                        textAlign = TextAlign.Center

                                    )
                                }


                                // Línea debajo del título
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = Color.Black, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Fila con "Data" a la izquierda y su valor a la derecha
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Data
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Data: ",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el texto y el valor
                                        Text(
                                            text = partida.data,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // Estat
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Estat: ",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp)) // Espacio entre el texto y el valor
                                        Text(
                                            text = partida.estat,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))




                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Absolute.Left
                                ) {
                                    Text(
                                        text = "Cartrons assignats: ",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = partida.cartronsAsignats.joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }


                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Números dits: ",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (partida.numerosDit.isEmpty()) {
                                        "No s'han cantat números"
                                    } else {
                                        partida.numerosDit.joinToString(", ")
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )

                                // Botón para eliminar la partida con un diseño atractivo
                                Spacer(modifier = Modifier.height(16.dp))

                                Row (
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Absolute.Right
                                ) {

                                    IconButton(onClick = {     }){
                                        Icon(
                                            imageVector = Icons.Filled.Info, // Icona d'eliminar
                                            contentDescription = "Editar partida",
                                            tint = Color(0xFF2C5CA4)
                                        )
                                    }

                                    IconButton(onClick = { eliminarPartida(partida) }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete, // Icona d'eliminar
                                            contentDescription = "Eliminar partida",
                                            tint = Color(0xFFA1342D)
                                        )
                                    }
                                }


                                /*Button(
                                    onClick = { eliminarPartida(partida) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text(text = "Eliminar Partida", color = MaterialTheme.colorScheme.onError)
                                }*/
                            }
                        }

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

    var cartroToEdit: List<Int?>? by remember { mutableStateOf(null) }

   // val cartroAEditar = C

    val cartroSeleccionat = remember { mutableStateOf<CartroEntity?>(null) }

    var cartroAEditar = remember { mutableStateOf<CartroEntity?>(null) }
    //var cartroAEditar: List<Int?>? by remember { mutableStateOf(null) }
    var modificarcartro by remember { mutableStateOf(false) }
    var noucartro by remember { mutableStateOf(false) }

    // Cargar los cartones
    LaunchedEffect(Unit) {
        cartros.value = database.cartroDao().getAllCartros()
    }

    fun eliminarCartro(cartro: CartroEntity) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            database.cartroDao().deleteCartro(cartro)
            cartros.value = database.cartroDao().getAllCartrosUn()
        }
    }

    if (noucartro) {
        AddCartroDialog(
            onDismiss = { noucartro = false },
            onSave = { numbers ->
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    val cartro = CartroEntity(numeros = numbers)
                    val newCartroId: Int = database.cartroDao().insertCartronosuspend(cartro).toInt()
                    cartros.value = database.cartroDao().getAllCartrosUn()
                    noucartro = false

                    Log.d("NOU CARTRO CREAT",newCartroId.toString())
                }
            }
        )
    }

    if (modificarcartro) {
        AddCartroDialog(
            onDismiss = { modificarcartro = false },
            onSave = { numbers ->
                cartroSeleccionat.value = cartroSeleccionat.value?.copy(numeros = numbers)

                val executor = Executors.newSingleThreadExecutor()
                executor.execute {

                    database.cartroDao().updateCartro(cartroSeleccionat.value!!)




                    //val cartro = CartroEntity(numeros = numbers)
                    //val newCartroId: Int = database.cartroDao().insertCartronosuspend(cartro).toInt()
                    cartros.value = database.cartroDao().getAllCartrosUn()
                    modificarcartro = false

                    Log.d(" CARTRO ACTUALITZAT",cartroSeleccionat.value!!.id.toString())
                }
            },
            cartro = cartroSeleccionat.value!!.numeros
        )
    }

//*******************************************************************************************************

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFBBDEFB), Color(0xFF2196F3)) // Gradient blau
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {

        Column {

            //Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White),horizontalArrangement = Arrangement.Absolute.Left,
                verticalAlignment = Alignment.CenterVertically // Alinear verticalment
            ) {
                //PER recular
                IconButton(onClick = {
                    navController.navigate("home")
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Menu")
                }

                Text(
                    text = "             Cartrons Guardats",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.W500),
                    color = Color.Black
                )
            }


            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Absolute.Right
            ) {
                TextButton(
                    onClick = {  noucartro = true  },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .border(2.dp, Color(0xFF2196F3), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2196F3))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Afegir nou cartro",
                            tint = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Crear un nou Cartró",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }


            // Llista de cartrons
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(cartros.value) { cartro ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp)) // Vores arrodonides
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFBBDEFB),
                                        Color(0xFF2196F3)
                                    )
                                )
                            )
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .border(
                                2.dp,
                                Color(0xFF000000),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF93BDE0),
                                            Color(0xFF97C4E8),
                                            Color(0xFFD7E5F1)
                                        )
                                    )
                                )
                                .fillMaxSize()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Datos de la partida
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically // Centra verticalmente los elementos
                                ) {
                                    // Texto a la izquierda
                                    Text(
                                        text = "Cartró ${cartro.id}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF1f77be),
                                        modifier = Modifier.weight(1f), // Hace que el texto ocupe el espacio disponible a la izquierda
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis // Manejo elegante si el texto es demasiado largo
                                    )

                                    // Iconos a la derecha
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp), // Espacio entre iconos
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = {
                                            //cartroToEdit = cartro.numeros
                                            cartroSeleccionat.value = cartro
                                            //cartroAEditar = cartro
                                            Log.d("CARTRO EDITAR",cartro.id.toString() + cartroSeleccionat.value!!.id.toString())
                                            modificarcartro = true
                                        }) {
                                            Icon(
                                                imageVector = Icons.Filled.Edit, // Icono de editar
                                                contentDescription = "Editar cartro",
                                                tint = Color(0xFF1A9A20)
                                            )
                                        }
                                        IconButton(onClick = { eliminarCartro(cartro) }) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete, // Icono de eliminar
                                                contentDescription = "Eliminar cartro",
                                                tint = Color(0xFFA1342D)
                                            )
                                        }
                                    }
                                }


                                // Línea debajo del título
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = Color.Black, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                VeureCartro(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    numbers = cartro.numeros
                                )

                                // Botón para eliminar la partida con un diseño atractivo
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VeureCartro(modifier: Modifier = Modifier, numbers: List<Int?>) {
    // Colores de las columnas, basados en el bingo tradicional
    val columnColors = listOf(
        Color(0xFFFFCDD2), // Rosa
        Color(0xFFFFF59D), // Amarillo
        Color(0xFFA5D6A7), // Verde
        Color(0xFF90CAF9), // Azul
        Color(0xFFCE93D8)  // Lila
    )

    // Estructura del cartón
    Column(
        modifier = modifier
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(BorderStroke(2.dp, Color.Black), shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { rowIndex -> // 3 filas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(9) { columnIndex -> // 9 columnas
                    val index = rowIndex * 9 + columnIndex
                    val number = numbers.getOrNull(index)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                color = if (number == null) Color.Gray else columnColors[columnIndex % columnColors.size],
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                BorderStroke(2.dp, Color.Black),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                number == null -> "⭐"
                                else -> number.toString()
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight =  FontWeight.Bold,
                                color =Color.Black,
                                textDecoration = TextDecoration.None
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
fun CustomToast(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize() // Ocupar toda la pantalla
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter // Alinear el contenido en la parte inferior central
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF323232),
            contentColor = Color.White,
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.Green,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun GameScreen(navController: NavController, partida: PartidaEntity?) {

    //var cartons = partida?.cartronsAsignats
    val cartronsPartidaId = remember {
        partida?.cartronsAsignats?.toMutableStateList() ?: mutableStateListOf()
    }

    // val numerosSeleccionados = remember { mutableStateListOf<Int>() }

    val numerosSeleccionados = remember { partida?.numerosDit?.toMutableStateList() ?: mutableStateListOf<Int>()}
    var guardarPartida by remember { mutableStateOf(false) }
    var guardarPartidaToast by remember { mutableStateOf(false) }

    var gameMode by remember { mutableStateOf(if (partida?.estat?.isEmpty() == true) {"Linia"} else {partida?.estat})} //posar estat de la partida
    var expandedMenu by remember { mutableStateOf(false) }  // Controla l'obertura del menú
    var showAddCartroDialog by remember { mutableStateOf(false) }

    var reiniciarPartida by remember { mutableStateOf(false) }


    var mostrarCarregarCartroExistent by remember { mutableStateOf(false) }
    var mostrarCrearCartroNou by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope() // Per gestionar les corrutines
    val context = LocalContext.current
    val database = DatabaseInstance.getDatabase(context)

    val cartroList = remember { mutableStateListOf<List<Int?>>() }

    if (partida != null) {
        LaunchedEffect(partida.id) {
            // Cargar los cartones de la base de datos
            val cartones = loadCartonesDePartida(partida.id, context)
            if (cartones != null) {
                cartroList.clear()
                cartroList.addAll(cartones)
            }  // Agregar los cartones a la lista
        }
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

            //header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically // Alinear verticalment
            ) {
                //PER recular
                IconButton(onClick = {
                    guardarPartida=true
                    //showSavedGameToast(context)
                    navController.navigate("home")
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Menu")
                }

                Text(
                    text = "  Partida " + partida?.id + "   " + partida?.data,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.W100,
                        fontSize = 24.sp,
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
                                    // Guardar la partida con los números seleccionados

                                    guardarPartida = true
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
                                    guardarPartida = true
                                    expandedMenu = false
                                    navController.navigate("home")
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
                                    reiniciarPartida = true
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



            //Boles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                for (rowIndex in 0 until 9) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (columnIndex in 0 until 10) {
                            val ballNumber = rowIndex * 10 + columnIndex + 1

                            Ball(
                                ballNumber = ballNumber,
                                numerosSeleccionados = numerosSeleccionados,
                                onBallClicked = { selectedNumber ->
                                    if (numerosSeleccionados.contains(selectedNumber)) {
                                        // Si el número ya está seleccionado, quitarlo de la lista
                                        numerosSeleccionados.remove(selectedNumber)

                                        // Restaurar los números en los cartones
                                        for (cartro in cartroList) {
                                            cartro.forEachIndexed { index, numero ->
                                                if (numero == -selectedNumber) {
                                                    cartroList[cartroList.indexOf(cartro)] =
                                                        cartro.toMutableList().apply { set(index, selectedNumber) }
                                                }
                                            }
                                        }
                                    } else {
                                        // Si no está seleccionado, añadirlo a la lista
                                        numerosSeleccionados.add(selectedNumber)

                                        // Marcar el número como tachado en los cartones
                                        /*for (cartro in cartroList) {
                                            cartro.forEachIndexed { index, numero ->
                                                if (numero == selectedNumber) {
                                                    cartroList[cartroList.indexOf(cartro)] =
                                                        cartro.toMutableList().apply { set(index, -numero!!) }
                                                }
                                            }
                                        }*/
                                    }
                                }
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

            /*
            el de boles:
            va per partides. a cada partida guarda una nota, la data, els numeros que s'han dit , si es va per quina o per linia, i cartrons assignats a la partida. a mes, per a cada cartro sap quins numeros s'han tatxat i quins no
            en clicar jugar, es selecciona nova partida o carregar una partida guardada
            un boto a inici per a veure parides guardades, i es mostra quins numeros s'han dit, i lestat dels cartrons. sense poder modificar, nomes eliminar
            un llistat de cartrons tambe, que es puguin modificar, eliminar i afegir. han de tenir un nom tambe

            es guarda la partida en tornar enrere per boto, en donarli a acabar partida, i en guardar partida

            els altres no guarda a la base de dades la partida, nomes es crea un cartro o sen carrega un o multiples. i es tatxa en aquell moment individualment
             */

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
                gameMode?.let { CartroList(cartroList = cartroList,numerosSeleccionados, it) } // Si hay cartones, mostrar la lista
            }
        }
    }


    if (guardarPartida) {
        partida?.let { currentPartida ->
            val updatedPartida = currentPartida.copy(
                numerosDit = numerosSeleccionados,
                estat = gameMode.toString(),
                cartronsAsignats = cartronsPartidaId.toList()
            )

            scope.launch(Dispatchers.IO) { // Assegura't d'usar Dispatcher.IO per a operacions de base de dades
                try {
                    Log.d("GUARDAR", "Actualitzant partida amb ID: ${updatedPartida.id}")
                    Log.d("GUARDAR", "Números dits: ${numerosSeleccionados}")
                    Log.d("GUARDAR", "Estat: ${gameMode.toString()}")
                    Log.d("GUARDAR", "Cartrons assignats: ${currentPartida.cartronsAsignats}")
                    Log.d("GUARDAR", "Cartrons assignats updated: ${updatedPartida.cartronsAsignats}")
                    Log.d("GUARDAR", "Cartrons assignats cartolist: ${cartronsPartidaId}")


                    database.cartroDao().updatePartida(updatedPartida)
                    withContext(Dispatchers.Main) {
                        guardarPartidaToast = true
                        guardarPartida = false
                    }
                } catch (e: Exception) {
                    Log.e("ERROR", "Error guardant la partida: ${e.message}")
                }
            }
        }
    }

        //*******************************************************************************************
        /*val updatedPartida = gameMode?.let {
            partida?.copy(
                numerosDit = numerosSeleccionados,
                estat = it // Cambiar el estado si es necesario
            )
        } ?: partida?.let {
            gameMode?.let { it1 ->
                PartidaEntity(
                    data = partida?.data ?: "", // Usar la fecha de la partida existente
                    numerosDit = numerosSeleccionados,
                    estat = it1, // Estado inicial
                    cartronsAsignats = it.cartronsAsignats // Los cartones asignados
                )
            }
        }
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            if (updatedPartida != null) {
                database.cartroDao().updatePartida(updatedPartida)
                Log.d("AAAAAGUUUUARDDDADAAA",updatedPartida.id.toString())
            }

        }

        guardarPartidaToast = true
        guardarPartida = false*/
  //  }

    if (guardarPartidaToast) {
        CustomToast(message = "Partida guardada correctament 🎉")
        LaunchedEffect(Unit) {
            delay(3000) // Mostrar el Toast durante 2 segundos
            guardarPartidaToast = false
        }

    }

    if (showAddCartroDialog) {
        OpcionsAfegirCartro (
            onDismiss = { showAddCartroDialog = false },
            onOptionSelected = { option ->
                // Accions en seleccionar una opció
                println("Opció seleccionada: $option")

                if (option == "CREAR UN CARTRÓ NOU") {
                    mostrarCrearCartroNou = true
                } else if (option == "CARREGAR UN CARTRÓ EXISTENT") {
                    mostrarCarregarCartroExistent = true
                }
                showAddCartroDialog = false
            }
        )
    }

    if (mostrarCrearCartroNou) {
        AddCartroDialog(
            onDismiss = { mostrarCrearCartroNou = false },
            onSave = { numbers ->
                scope.launch(Dispatchers.IO) {
                    val cartro = CartroEntity(numeros = numbers)
                    val newCartroId = database.cartroDao().insertCartro(cartro).toInt()

                    withContext(Dispatchers.Main) {
                        // Añadir el nuevo ID a la lista mutable
                        cartronsPartidaId.add(newCartroId)

                        // Actualizar la partida con los nuevos IDs de cartones
                        partida?.let { currentPartida ->
                            val updatedPartida = currentPartida.copy(cartronsAsignats = cartronsPartidaId.toList())
                            scope.launch(Dispatchers.IO) {
                                database.cartroDao().updatePartida(updatedPartida)
                            }
                        }

                        // Actualizar la UI
                        cartroList.add(cartro.numeros)
                        mostrarCrearCartroNou = false
                    }
                }
            }
        )
    }

    if (reiniciarPartida) {
        LaunchedEffect(Unit) {
            partida?.let { currentPartida ->
                val updatedPartida = currentPartida.copy(estat = "Línia", numerosDit = emptyList())
                scope.launch(Dispatchers.IO) {
                    database.cartroDao().updatePartida(updatedPartida)
                    numerosSeleccionados.clear()
                    reiniciarPartida = false
                }
            }
        }
    }

    if (mostrarCarregarCartroExistent) {
        AddCartroExistentDialog(
            onDismiss = { mostrarCarregarCartroExistent = false },
            onSave = { idsCartronsSeleccionats, numerosCartronsSeleccionats ->
                scope.launch(Dispatchers.IO) {
                    Log.d("CARTRONS EXISTENTS SELECCIONATS", idsCartronsSeleccionats.toString())
                    Log.d("NÚMEROS CARTRONS SELECCIONATS", numerosCartronsSeleccionats.toString())

                    withContext(Dispatchers.Main) {
                        // Afegir els IDs seleccionats a cartronsPartidaId
                        cartronsPartidaId.addAll(idsCartronsSeleccionats)

                        // Actualitzar la partida amb els nous IDs
                        partida?.let { currentPartida ->
                            val updatedPartida = currentPartida.copy(cartronsAsignats = cartronsPartidaId.toList())
                            scope.launch(Dispatchers.IO) {
                                database.cartroDao().updatePartida(updatedPartida)
                            }
                        }

                        // Afegir les llistes de números seleccionats a cartroList
                        cartroList.addAll(numerosCartronsSeleccionats)

                        // Tanca el diàleg
                        mostrarCarregarCartroExistent = false
                    }
                }
            }
        )
    }
}

@Composable
fun AddCartroExistentDialog(onDismiss: () -> Unit, onSave: (List<Int>, List<List<Int?>>) -> Unit) {
    val selectedCartros = remember { mutableStateOf(mutableSetOf<Int>()) }
    val selectedNumbers = remember { mutableStateListOf<List<Int?>>() } // Llista de números seleccionats
    val cartros = remember { mutableStateOf<List<CartroEntity>>(emptyList()) }
    val context = LocalContext.current
    val database = DatabaseInstance.getDatabase(context)

    // Carreguem els cartrons de la base de dades
    LaunchedEffect(Unit) {
        cartros.value = database.cartroDao().getAllCartros()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escull un cartró", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartros.value) { cartro ->
                    val isSelected = selectedCartros.value.contains(cartro.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) Color.Black else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedCartros.value = selectedCartros.value
                                    .toMutableSet()
                                    .apply {
                                        if (isSelected) {
                                            remove(cartro.id)
                                            selectedNumbers.remove(cartro.numeros) // Elimina els números
                                        } else {
                                            add(cartro.id)
                                            selectedNumbers.add(cartro.numeros) // Afegeix els números
                                        }
                                    }
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFBBDEFB), Color(0xFF2196F3))
                                    )
                                )
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Cartró ${cartro.id}",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                                VeureCartro(
                                    modifier = Modifier.fillMaxWidth(),
                                    numbers = cartro.numeros
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val ids = selectedCartros.value.toList()
                val numbers = selectedNumbers.toList()
                onSave(ids, numbers) // Retorna les dues llistes
                onDismiss()
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·lar")
            }
        }
    )
}

@Composable
fun OpcionsAfegirCartro(onDismiss: () -> Unit, onOptionSelected: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(
                text = "Selecciona una opció",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2196F3)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Opció 1: Crear un cartro
                TextButton(
                    onClick = { onOptionSelected("CREAR UN CARTRÓ NOU") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFBBDEFB), shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("CREAR UN CARTRÓ NOU")
                }

                // Opció 2: Carregar un cartro existent
                TextButton(
                    onClick = { onOptionSelected("CARREGAR UN CARTRÓ EXISTENT") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF90CAF9), shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("CARREGAR UN CARTRÓ EXISTENT")
                }

                // Opció 3:
                /*TextButton(
                    onClick = { onOptionSelected("MULTIPLE SENSE BOLES") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF64B5F6), shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text("MULTIPLE SENSE BOLES")
                }*/
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CartroList(cartroList: List<List<Int?>>, numerosSeleccionados: MutableList<Int>, gameMode: String) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(cartroList) { cartroNumbers ->
            Cartro(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                numbers = cartroNumbers,
                numerosSeleccionados = numerosSeleccionados,
                estat = gameMode
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
fun AddCartroDialog(onDismiss: () -> Unit,onSave: (List<Int?>) -> Unit, cartro: List<Int?>? = null) {

    val columnColors = listOf(
        Color(0xFFFFCDD2), // Rosa
        Color(0xFFFFF59D), // Amarillo
        Color(0xFFA5D6A7), // Verde
        Color(0xFF90CAF9), // Azul
        Color(0xFFCE93D8)  // Lila
    )

    //*************************************************************************************************************************************************
    val cartroNumbers = remember {
        mutableStateListOf<Int?>().apply {
            if (cartro != null) {
                addAll(cartro)
            } else {
                addAll(List(27) { null })
            }
        }
    }
    var showInputDialog by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(-1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crea el teu cartró") },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                //cartro ns
                Column(
                    modifier = Modifier
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .border(BorderStroke(2.dp, Color.Black), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(3) { rowIndex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(9) { columnIndex ->
                                val index = rowIndex * 9 + columnIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(
                                            color = if (cartroNumbers[index] != null) columnColors[columnIndex % columnColors.size]
                                            else Color.Gray,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .border(
                                            BorderStroke(2.dp, Color.Black),
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
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color =Color.Black,
                                            textDecoration = TextDecoration.None
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                val filledCount = cartroNumbers.count { it != null }
                Text(
                    text = "Números emplenats: $filledCount/2",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (filledCount == 2) Color.DarkGray else Color.Red
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(cartroNumbers) },
                enabled = cartroNumbers.count { it != null } == 2
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
fun Ball(ballNumber: Int, numerosSeleccionados: MutableList<Int>, onBallClicked: (Int) -> Unit) {
    val isSelected = remember { derivedStateOf { numerosSeleccionados.contains(ballNumber) } }

    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                if (isSelected.value) Color.Black else Color(0xFFF5F5DC), // Fondo negro si está seleccionado
                shape = RoundedCornerShape(50)
            )
            .border(
                2.dp,
                if (isSelected.value) Color.Gray else Color.Black, // Borde gris si está seleccionado
                shape = RoundedCornerShape(50)
            )
            .padding(6.dp)
            .clickable { onBallClicked(ballNumber) }, // Llama a la función pasada
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ballNumber.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isSelected.value) Color.White else Color.Black, // Texto blanco si está seleccionado
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun Cartro(
    modifier: Modifier = Modifier,
    numbers: List<Int?>,
    numerosSeleccionados: MutableList<Int>,estat: String
) {
    val columnColors = listOf(
        Color(0xFFF2D7D9),
        Color(0xFFD6EAF8),
        Color(0xFFD4EFDF),
        Color(0xFFFCF3CF),
        Color(0xFFF5EEF8),
        Color(0xFFE8DAEF),
        Color(0xFFD0ECE7),
        Color(0xFFE5E7E9),
        Color(0xFFFADBD8)
    )

    val density = LocalDensity.current
    val confettiParticles = remember { mutableStateListOf<ConfettiParticle>() }
    val scope = rememberCoroutineScope()
    var showLineText by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(
                androidx.compose.foundation.BorderStroke(2.dp, Color.Black),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { rowIndex -> // 3 filas
            val isRowComplete = (0 until 9).count { columnIndex -> // Cuenta los números seleccionados en la fila
                val index = rowIndex * 9 + columnIndex
                val number = numbers.getOrNull(index)
                number != null && numerosSeleccionados.contains(number)
            } == 5 // Comprueba si hay 5 números seleccionados

            var isScaled by remember { mutableStateOf(false) }
            var isGlowing by remember { mutableStateOf(false) }

            LaunchedEffect(isRowComplete) {
                if (isRowComplete && estat == "Línia") {
                    isScaled = true
                    isGlowing = true
                    showLineText = true
                    scope.launch {
                        // Generar partículas de confeti
                        val rowHeight = with(density) { 40.dp.toPx() }
                        val rowWidth = with(density) { 360.dp.toPx() }
                        val rowY = rowIndex * rowHeight + rowHeight / 2
                        val rowX = rowWidth / 2
                        repeat(50) {
                            confettiParticles.add(
                                ConfettiParticle(
                                    x = rowX + Random.nextInt(-100, 100),
                                    y = rowY,
                                    color = Color(Random.nextLong(0xFF000000, 0xFFFFFFFF)),
                                    size = Random.nextInt(5, 15).toFloat(),
                                    rotation = Random.nextInt(0, 360).toFloat(),
                                    xVelocity = Random.nextInt(-10, 10).toFloat(),
                                    yVelocity = Random.nextInt(-20, -5).toFloat()
                                )
                            )
                        }
                    }
                    delay(200) // Espera 2 segundos
                    isScaled = false
                    isGlowing = false
                    showLineText = false
                }
            }

            val scale by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isScaled) 1.2f else 1f, // Escala aumentada cuando la fila está completa
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 500)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .drawBehind {
                        if (isGlowing) {
                            drawCircle(
                                color = Color.Yellow.copy(alpha = 0.5f),
                                radius = size.minDimension * 0.6f,
                                center = Offset(size.width / 2, size.height / 2)
                            )
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(9) { columnIndex -> // 9 columnas
                    val index = rowIndex * 9 + columnIndex
                    val number = numbers.getOrNull(index) // Obtiene el número o `null` si es una casilla vacía
                    val isSeleccionado = number != null && numerosSeleccionados.contains(number)

                    Box(
                        modifier = Modifier
                            .weight(1f) // Todas las celdas tienen el mismo ancho
                            .aspectRatio(1f) // Mantener proporción cuadrada
                            .background(
                                color = if (number == null) Color.Gray else columnColors[columnIndex % columnColors.size],
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                androidx.compose.foundation.BorderStroke(2.dp, Color.Black),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                number?.let {
                                    if (numerosSeleccionados.contains(it)) {
                                        numerosSeleccionados.remove(it)
                                    } else {
                                        numerosSeleccionados.add(it)
                                        //Log.d("NUMEROS SELECCIONADOS", numerosSeleccionados.toString() + isRowComplete)
                                    }
                                }
                            }
                            .drawBehind {
                                if (isSeleccionado) {
                                    drawCircle(
                                        color = Color.Black,
                                        style = Stroke(width = 2.dp.toPx()),
                                        radius = size.minDimension / 2 - 4.dp.toPx()
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                number == null -> "⭐" // Casillas vacías con estrella
                                else -> number.toString() // Mostrar número
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
    if (showLineText) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "LÍNIA!",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp
                )
            )
        }
    }

    ConfettiAnimation(confettiParticles)
}

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    val color: Color,
    val size: Float,
    var rotation: Float,
    var xVelocity: Float,
    var yVelocity: Float
)

@Composable
fun ConfettiAnimation(particles: MutableList<ConfettiParticle>) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(particles.size) {
        if (particles.isNotEmpty()) {
            scope.launch {
                while (particles.isNotEmpty()) {
                    particles.forEach { particle ->
                        // Actualizar posición y rotación inmediatamente
                        particle.x += particle.xVelocity
                        particle.y += particle.yVelocity
                        particle.rotation += 5f
                        particle.yVelocity += 0.5f // Simula gravedad
                    }
                    particles.removeAll { it.y > 1000f } // Elimina partículas fuera de la pantalla
                    delay(16L) // Aproximadamente 60 FPS
                }
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            with(particle) {
                // Dibuja las partículas con su posición y tamaño actuales
                drawCircle(
                    color = color,
                    radius = size,
                    center = Offset(x, y),
                    style = Fill
                )
            }
        }
    }
}



@Composable
fun GameOptionsDialog(onDismiss: () -> Unit, onOptionSelected: (String) -> Unit) {
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