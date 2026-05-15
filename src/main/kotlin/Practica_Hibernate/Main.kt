package Practica_Hibernate

import jakarta.persistence.Persistence
import java.util.logging.Level
import java.util.logging.Logger
import jakarta.persistence.*

// Marca la classe com entitat JPA mapejada a la taula 'hospital'
@Entity
@Table(name = "hospital")
class Hospital(

    // Clau primària de la taula
    @Id
    @Column(name = "hospital_cod")
    var hospitalCod: Long? = null,

    // Mapeig de columnes amb restriccions NOT NULL i longitud màxima
    @Column(name = "nom", nullable = false, length = 15)
    var nom: String = "",

    @Column(name = "adreca", nullable = false, length = 25)
    var adreca: String = "",

    @Column(name = "telefon", nullable = false, length = 9)
    var telefon: String = "",

    @Column(name = "qtat_llits", nullable = false)
    var qtatLlits: Int = 0
)




// Classe CRUD que rep la factory (EntityManagerFactory) per crear sessions amb la BD
class HospitalCRUD(private val emf: EntityManagerFactory) {

    // Cerca un hospital per la seva clau primària
    fun selectById(id: Long): Hospital? {
        val em = emf.createEntityManager()
        return try {
            // find() retorna l'entitat o null si no existeix
            em.find(Hospital::class.java, id)
        } finally {
            em.close()
        }
    }

    // Retorna tots els hospitals de la taula
    fun selectAll(): List<Hospital> {
        val em = emf.createEntityManager()
        return try {
            // Consulta JPQL sobre la entitat Hospital (no sobre la taula SQL)
            em.createQuery("FROM Hospital", Hospital::class.java).resultList
        } finally {
            em.close()
        }
    }

    // Insereix un nou hospital
    fun insert(hospital: Hospital) {
        val em = emf.createEntityManager()
        try {
            em.transaction.begin()
            // persist() guarda l'entitat nova a la BD
            em.persist(hospital)
            em.transaction.commit()
        } catch (e: Exception) {
            // Si falla, revertim els canvis
            em.transaction.rollback()
            throw e
        } finally {
            em.close()
        }
    }

    // Actualitza un hospital existent
    fun update(hospital: Hospital) {
        val em = emf.createEntityManager()
        try {
            em.transaction.begin()
            // merge() actualitza l'entitat (o la insereix si no existeix)
            em.merge(hospital)
            em.transaction.commit()
        } catch (e: Exception) {
            em.transaction.rollback()
            throw e
        } finally {
            em.close()
        }
    }

    // Elimina un hospital pel seu id
    fun delete(id: Long) {
        val em = emf.createEntityManager()
        try {
            em.transaction.begin()
            // Cal carregar l'entitat abans d'eliminar-la
            val hospital = em.find(Hospital::class.java, id)
            if (hospital != null) em.remove(hospital)
            em.transaction.commit()
        } catch (e: Exception) {
            em.transaction.rollback()
            throw e
        } finally {
            em.close()
        }
    }
}


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
fun menu(crud: HospitalCRUD) {
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
