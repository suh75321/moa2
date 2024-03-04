package com.teamsparta.moamoa.review.model

import com.teamsparta.moamoa.infra.BaseTimeEntity
import jakarta.persistence.*

@Entity
class Review (

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "title")
    var title: String,

    @Column(name = "content")
    var content: String,

    @Column(name = "name")
    var name: String,

    @Column(name = "image_path")
    val imagePath: String? = null,

    @Column(name = "is_deleted")
    var isDeleted: Boolean = false


): BaseTimeEntity() {


}