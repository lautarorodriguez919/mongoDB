package Practica_Hibernate

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
