package Practica_Hibernate

import jakarta.persistence.Persistence
import java.util.logging.Level
import java.util.logging.Logger

fun main() {
    // Silencia els logs INFO/WARN d'Hibernate
    Logger.getLogger("org.hibernate").level = Level.SEVERE
    // Crea la factory d'EntityManager segons la unitat 'sanitat' del persistence.xml
    val emf = Persistence.createEntityManagerFactory("sanitat")
    val crud = HospitalCRUD(emf)
    menu(crud)



    // Tanquem la factory per alliberar recursos
    emf.close()
}


// Bucle de menú que crida cada operació CRUD segons l'opció triada
fun menu(crud:HospitalCRUD){
    var sortir = false
    while (!sortir) {
        println("\n--- MENU ---")
        println("a) Inserir 3 hospitals")
        println("b) Mostrar hospitals")
        println("c) Modificar telefon")
        println("d) Eliminar hospital")
        println("0) Sortir")
        print("Opcio: ")
        when (readLine()?.trim()) {
            "a" -> insertarHospitals(crud)
            "b" -> mostrarHospitals(crud)
            "c" -> modificarTelefon(crud)
            "d" -> eliminarHospital(crud)
            "0" -> sortir = true
            else -> println("Opció no valida")
        }
    }
}

// Apartat a) Insereix tres hospitals nous a la BD
fun insertarHospitals(crud: HospitalCRUD) {
    println("=== a) Inserint hospitals ===")
    crud.insert(Hospital(1L, "Hospital Vall", "Carrer Major 1", "931234567", 200))
    crud.insert(Hospital(2L, "Hospital Mar", "Passeig Mar 5", "932345678", 350))
    crud.insert(Hospital(3L, "Hospital Clinic", "Carrer Rossello", "933456789", 500))
    println("Hospitals inserits correctament.")
}

// Apartat b) Mostra totes les files de la taula hospital
fun mostrarHospitals(crud: HospitalCRUD) {
    println("\n=== b) Hospitals existents ===")
    val hospitals = crud.selectAll()
    for (h in hospitals) {
        println("Codi: ${h.hospitalCod}, Nom: ${h.nom}, Adreca: ${h.adreca}, Telefon: ${h.telefon}, Llits: ${h.qtatLlits}")
    }
}

// Apartat c) Modifica el telèfon de l'hospital amb id 1
fun modificarTelefon(crud: HospitalCRUD) {
    println("\n=== c) Modificant telefon del hospital 1 ===")
    // Carreguem l'hospital, canviem el camp i el persistim amb update
    val hospital = crud.selectById(1L)
    if (hospital != null) {
        hospital.telefon = "999999999"
        crud.update(hospital)
        println("Telefon actualitzat: ${hospital.telefon}")
    }
}

// Apartat d) Elimina l'hospital amb id 3
fun eliminarHospital(crud: HospitalCRUD) {
    println("\n=== d) Eliminant hospital 3 ===")
    crud.delete(3L)
    println("Hospital eliminat.")
}
