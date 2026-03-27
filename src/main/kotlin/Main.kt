package org.example

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.runBlocking
import org.bson.Document

//hacer algo con parse string relacionado con la base de datos en mongoDB
//contrasenya dvus@Gvhv6FhcyP

fun main() = runBlocking {
    val connectionString = "mongodb+srv://lautarorodriguez7e9_db_user:<dvus@Gvhv6FhcyP>@cluster0.bykrvj1.mongodb.net/dmuKgXpnFp7bz6ZT"
    val client = MongoClient.create(connectionString)
    val database = client.getDatabase("sample_training")
    val collection = database.getCollection<Document>("grades")

    ejercicio1(collection)

    client.close()
}

suspend fun ejercicio1(collection: MongoCollection<Document>) {
    val estudiant1 = Document()
        .append("student_id", 111333444)
        .append("name", "Lautaro")
        .append("surname", "Rodriguez")
        .append("class_id", "DAM")
        .append("group", "el teu grup")
        .append("scores", listOf(
            Document("type", "exam").append("score", 100),
            Document("type", "teamWork").append("score", 50)
        ))

    val estudiant2 = Document()
        .append("student_id", 111222333)
        .append("name", "Iago")
        .append("surname", "Zahonero")
        .append("class_id", "Undefined")
        .append("group", "el teu grup")
        .append("interests", listOf("music", "gym", "code", "electronics"))

    collection.insertOne(estudiant1)
    collection.insertOne(estudiant2)
    println("Estudiants inserits correctament!")
}

suspend fun ejercicio2(collection: MongoCollection<Document>) {
    // aquí les consultes
}