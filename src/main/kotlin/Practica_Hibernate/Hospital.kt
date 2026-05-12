package Practica_Hibernate

import jakarta.persistence.*

@Entity
@Table(name = "hospital")
class Hospital(

    @Id
    @Column(name = "hospital_cod")
    var hospitalCod: Long? = null,

    @Column(name = "nom", nullable = false, length = 15)
    var nom: String = "",

    @Column(name = "adreca", nullable = false, length = 25)
    var adreca: String = "",

    @Column(name = "telefon", nullable = false, length = 9)
    var telefon: String = "",

    @Column(name = "qtat_llits", nullable = false)
    var qtatLlits: Int = 0
)
