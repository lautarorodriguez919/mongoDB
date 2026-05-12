package Practica_Hibernate

import jakarta.persistence.EntityManagerFactory

class HospitalCRUD(private val emf: EntityManagerFactory) {

    fun selectById(id: Long): Hospital? {
        val em = emf.createEntityManager()
        return try {
            em.find(Hospital::class.java, id)
        } finally {
            em.close()
        }
    }

    fun selectAll(): List<Hospital> {
        val em = emf.createEntityManager()
        return try {
            em.createQuery("FROM Hospital", Hospital::class.java).resultList
        } finally {
            em.close()
        }
    }

    fun insert(hospital: Hospital) {
        val em = emf.createEntityManager()
        try {
            em.transaction.begin()
            em.persist(hospital)
            em.transaction.commit()
        } catch (e: Exception) {
            em.transaction.rollback()
            throw e
        } finally {
            em.close()
        }
    }

    fun update(hospital: Hospital) {
        val em = emf.createEntityManager()
        try {
            em.transaction.begin()
            em.merge(hospital)
            em.transaction.commit()
        } catch (e: Exception) {
            em.transaction.rollback()
            throw e
        } finally {
            em.close()
        }
    }

    fun delete(id: Long) {
        val em = emf.createEntityManager()
        try {
            em.transaction.begin()
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
