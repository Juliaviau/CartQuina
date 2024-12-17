package com.example.cartquina

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class PartidaViewModel : ViewModel() {
    var partida: PartidaEntity? by mutableStateOf(null)
    var cartones: List<CartroEstatEntity> by mutableStateOf(emptyList())
    var numerosLlamados: List<Int> by mutableStateOf(emptyList())

    fun cargarPartida(partida: PartidaEntity,/* cartones: List<CartroEstatEntity?>,*/ numerosLlamados: List<Int>) {
        this.partida = partida
       // this.cartones = cartones
        this.numerosLlamados = numerosLlamados
    }
}
