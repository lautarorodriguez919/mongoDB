package org.example

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.*
import org.bson.Document
import org.bson.conversions.Bson

fun main() {
    val collection = conectar()
    ejercicio1(collection)
    ejercicio2(collection)
}

fun conectar(): MongoCollection<Document> {
    val uri = "mongodb+srv://lautarorodriguez7e9_db_user:Password123@cluster0.bykrvj1.mongodb.net/?retryWrites=true&writeConcern=majority"
    val mongoClient = MongoClients.create(uri)
    val db = mongoClient.getDatabase("sample_training")
    return db.getCollection("grades")
}

fun ejercicio1(coll: MongoCollection<Document>) {
    val estudiant1 = Document("student_id", 111333444)
        .append("name", "Lautaro")
        .append("surname", "Rodriguez")
        .append("class_id", "DAM")
        .append("group", "el teu grup")
        .append("scores", listOf(
            Document("type", "exam").append("score", 100),
            Document("type", "teamWork").append("score", 50)
        ))

    val estudiant2 = Document("student_id", 111222333)
        .append("name", "Iago")
        .append("surname", "Zahonero")
        .append("class_id", "Undefined")
        .append("group", "el teu grup")
        .append("interests", listOf("music", "gym", "code", "electronics"))

    coll.insertOne(estudiant1)
    coll.insertOne(estudiant2)
    println("Estudiants inserits correctament!")
}

fun ejercicio2(coll: MongoCollection<Document>) {

    println("\n=== Estudiants del grup ===")
    var filter: Bson = eq("group", "el teu grup")
    var cursor = coll.find(filter).iterator()
    while (cursor.hasNext()) {
        println(cursor.next().toJson())
    }


    println("\n=== Estudiants amb 100 a l'exam ===")
    filter = elemMatch("scores", and(eq("type", "exam"), eq("score", 100)))
    cursor = coll.find(filter).iterator()
    while (cursor.hasNext()) {
        println(cursor.next().toJson())
    }


    println("\n=== Estudiants amb menys de 50 a l'exam ===")
    filter = elemMatch("scores", and(eq("type", "exam"), lt("score", 50)))
    cursor = coll.find(filter).iterator()
    while (cursor.hasNext()) {
        println(cursor.next().toJson())
    }


    println("\n=== Interessos de l'estudiant 111222333 ===")
    filter = eq("student_id", 111222333)
    cursor = coll.find(filter).iterator()
    while (cursor.hasNext()) {
        val doc = cursor.next()
        println(doc.getList("interests", String::class.java))
    }
}