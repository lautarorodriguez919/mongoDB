package org.example.reines

import kotlin.math.abs

class Tauler(val mida: Int) {

    //Constructor que envia un mesaje si la condicion es false
    init {
        require(mida > 0) { "La mida del tauler ha de ser positiva" }
    }

    //Variable que guarda la medida del tablero en un array
    private val columnaPerFila = IntArray(mida) { -1 }


    //falta por corregir este bloque
    //funcion que recibe como pararemtro la fila y la columna para verificar si la posicion supera el limite del tablero
    fun esPosicioValida(fila: Int, col: Int): Boolean {
        var filaIt = 0
        while (filaIt < fila) {
            val c = columnaPerFila[filaIt]
            if (c == col) return false
            if (abs(c - col) == fila - filaIt) return false
            filaIt++  // ✅
        }
        return true
    }

    fun colocaReina(fila: Int, col: Int) {
        columnaPerFila[fila] = col
    }

    fun retiraReina(fila: Int) {
        columnaPerFila[fila] = -1
    }

    /**
     * Captura l'estat actual del tauler com una [Solucio] immutable.
     */
    fun aSolucio(): Solucio = Solucio(columnaPerFila.toList())
}
