package org.example.reines

class Reines {

    private var tauler: Tauler = Tauler(1)
    private var numMaxim: Int? = null
    private val solucions: MutableList<Solucio> = mutableListOf()

    //Funcion que devuelve las soluciones posibles dependiendo del numero maximo del tablero
    fun cercaSolucions(nFiles: Int, numMaxim: Int? = null) {
        //metodo de condicion si es false devuelve mensaje de error si es true pasa a lo siguiente
        require(numMaxim == null || numMaxim > 0) { "numMaxim ha de ser positiu" }
        //se lla a
        this.tauler = Tauler(nFiles)
        this.numMaxim = numMaxim
        solucions.clear()
        backtracking(fila = 0)
    }


    private fun backtracking(fila: Int): Boolean {
        if (limitAssolit()) return true

        if (fila == tauler.mida) {
            solucions.add(tauler.aSolucio())
            return limitAssolit()
        }

        var col = 0
        while (col < tauler.mida) {
            if (tauler.esPosicioValida(fila, col)) {
                tauler.colocaReina(fila, col)
                if (backtracking(fila + 1)) return true
                tauler.retiraReina(fila)
            }
            col++
        }
        return false
    }

    private fun limitAssolit(): Boolean =
        numMaxim != null && solucions.size >= numMaxim!!

    fun numSolucions(): Int = solucions.size

    fun visualitzaSolucio(numSolucioAVisualitzar: Int? = null): List<Solucio> {
        if (numSolucioAVisualitzar == null) {
            println("Total de solucions: ${solucions.size}")
            solucions.forEachIndexed { i, sol -> imprimeix(i + 1, sol) }
            return solucions.toList()
        }

        require(numSolucioAVisualitzar in 1..solucions.size) {
            "numSolucioAVisualitzar fora de rang (1..${solucions.size})"
        }
        val solucio = solucions[numSolucioAVisualitzar - 1]
        imprimeix(numSolucioAVisualitzar, solucio)
        return listOf(solucio)
    }

    private fun imprimeix(num: Int, solucio: Solucio) {
        println("\nSolució #$num: $solucio")
        print(solucio.dibuixa())
    }
}
