package org.example

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.bson.Document
import java.io.File

// Punt d'entrada del programa. Estableix la connexió i inicia la importació.
fun main() {
    val collection = conectar()
    importarRestaurants(collection)
}

// Estableix la connexió amb el clúster de MongoDB Atlas i retorna la col·lecció "restaurants" de la base de dades "ITB".
fun conectar(): MongoCollection<Document> {
    val uri =
        "mongodb+srv://lautarorodriguez7e9_db_user:Password123@cluster0.bykrvj1.mongodb.net/?retryWrites=true&writeConcern=majority"
    val mongoClient = MongoClients.create(uri)
    val db = mongoClient.getDatabase("ITB")
    println("Connexió feta!")
    return db.getCollection("restaurants")
}

// Importa els documents del fitxer restaurants.json a la col·lecció de MongoDB.
// El fitxer té un document JSON per línia (format NDJSON).
// Rep per paràmetre la col·lecció on s'han d'inserir els documents.
fun importarRestaurants(coll: MongoCollection<Document>) {
    try {
        // Obre el fitxer JSON en mode lectura seqüencial línia per línia
        val reader = File("restaurants.json").bufferedReader()
        // Comptador per fer el seguiment del nombre de documents inserits
        var count = 0

        // Llegeix la primera línia abans d'entrar al bucle
        var linia = reader.readLine()
        // Recorre el fitxer fins que no hi ha més línies (readLine retorna null)
        while (linia != null) {
            // Descarta les línies buides per evitar errors de parsing
            if (linia.isNotEmpty()) {
                try {
                    // Converteix la línia JSON en un Document BSON per poder-lo inserir a MongoDB
                    val doc = Document.parse(linia)
                    // Insereix el document a la col·lecció
                    coll.insertOne(doc)
                    count++
                    // Mostra el progrés indicant el número de registre i el nom del restaurant inserit
                    println("Restaurant $count inserit: ${doc.getString("name")}")
                } catch (e: Exception) {
                    // Captura errors de parsing en una línia concreta sense aturar la importació
                    println("Error parsejant línia: ${e.message}")
                }
            }
            // Llegeix la següent línia per continuar el bucle
            linia = reader.readLine()
        }

        reader.close()
        println("\n$count restaurants importats correctament!")

    } catch (e: Exception) {
        // Captura errors generals com fitxer no trobat o problemes de connexió
        println("Error: ${e.message}")
    }
}