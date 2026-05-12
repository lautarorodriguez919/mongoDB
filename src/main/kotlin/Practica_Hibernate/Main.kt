package Practica_Hibernate

import jakarta.persistence.Persistence

fun main() {
    val emf = Persistence.createEntityManagerFactory("sanitat")
    val crud = HospitalCRUD(emf)

    insertarHospitals(crud)
    mostrarHospitals(crud)
    modificarTelefon(crud)
    eliminarHospital(crud)

    emf.close()
}

fun insertarHospitals(crud: HospitalCRUD) {
    println("=== a) Inserint hospitals ===")
    crud.insert(Hospital(1L, "Hospital Vall", "Carrer Major 1", "931234567", 200))
    crud.insert(Hospital(2L, "Hospital Mar", "Passeig Mar 5", "932345678", 350))
    crud.insert(Hospital(3L, "Hospital Clinic", "Carrer Rossello", "933456789", 500))
    println("Hospitals inserits correctament.")
}

fun mostrarHospitals(crud: HospitalCRUD) {
    println("\n=== b) Hospitals existents ===")
    val hospitals = crud.selectAll()
    for (h in hospitals) {
        println("Codi: ${h.hospitalCod}, Nom: ${h.nom}, Adreca: ${h.adreca}, Telefon: ${h.telefon}, Llits: ${h.qtatLlits}")
    }
}

fun modificarTelefon(crud: HospitalCRUD) {
    println("\n=== c) Modificant telefon del hospital 1 ===")
    val hospital = crud.selectById(1L)
    if (hospital != null) {
        hospital.telefon = "999999999"
        crud.update(hospital)
        println("Telefon actualitzat: ${hospital.telefon}")
    }
}

fun eliminarHospital(crud: HospitalCRUD) {
    println("\n=== d) Eliminant hospital 3 ===")
    crud.delete(3L)
    println("Hospital eliminat.")
}
