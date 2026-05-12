package org.example.reines

data class Solucio(val posicions: List<Int>) {

    val mida: Int get() = posicions.size

    fun dibuixa(): String {
        val sb = StringBuilder()
        var fila = 0
        while (fila < mida) {
            var col = 0
            while (col < mida) {
                sb.append(if (posicions[fila] == col) "Q " else ". ")
                col++
            }
            sb.append('\n')
            fila++
        }
        return sb.toString()
    }

    override fun toString(): String = posicions.toString()
}
