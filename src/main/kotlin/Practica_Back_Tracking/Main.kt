package org.example

import org.example.reines.Reines

fun main() {
    //se llama a la funcion que pide datos al usuario pasando por parametros condiciones y sring de la funcion
    val nFiles = demanaEnter(
        missatge = "Mida del tauler (N): ",
        validador = { it > 0 },
        errorMsg = "Has d'introduir un enter positiu."
    )

    //se le asigna a la variable reines un objeto de tipo reines para despues poder llamar a las funcinoes de esa clase
    val reines = Reines()

    //dentro de la variable reines que tiene un objeto se llama a la funcion que busca soluciones pasandole como parametro la respuesta del usuario antes dada
    reines.cercaSolucions(nFiles = nFiles)
    println("Nombre de solucions per a $nFiles reines: ${reines.numSolucions()}")


    if (reines.numSolucions() > 0) reines.visualitzaSolucio(1)

    println("\n--- Cerca amb numMaxim = 1000 ---")
    //se vuelve a llamar a la funcion cercaSolucions pasandole como pametro el numero maximo de soluciones que puede encontrar
    reines.cercaSolucions(nFiles = nFiles, numMaxim = 1000)
    reines.visualitzaSolucio()
}


//funcion que hace un bucle donde pide los datos al usuario y guarda la respuesta del usuario
private fun demanaEnter(missatge: String, validador: (Int) -> Boolean, errorMsg: String): Int {
    while (true) {
        print(missatge)
        val entrada = readLine()?.trim()?.toIntOrNull()
        if (entrada != null && validador(entrada)) return entrada
        println(errorMsg)
    }
}
