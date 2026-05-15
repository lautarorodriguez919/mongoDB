package Practica_Hibernate

import jakarta.persistence.EntityManagerFactory

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
