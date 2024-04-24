package com.teamsparta.moamoa.domain.product.service

import com.teamsparta.moamoa.domain.product.dto.ProductRequest
import com.teamsparta.moamoa.domain.product.dto.ProductResponse
import com.teamsparta.moamoa.domain.product.model.Product
import com.teamsparta.moamoa.domain.product.model.ProductStock
import com.teamsparta.moamoa.domain.product.repository.ProductRepository
import com.teamsparta.moamoa.domain.product.repository.ProductStockRepository
import com.teamsparta.moamoa.domain.review.repository.ReviewRepository
import com.teamsparta.moamoa.domain.seller.repository.SellerRepository
import com.teamsparta.moamoa.exception.ModelNotFoundException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val productStockRepository: ProductStockRepository,
    private val sellerRepository: SellerRepository,
    private val reviewRepository: ReviewRepository,
) : ProductService {
    @Transactional
    override fun getAllProducts(): List<ProductResponse> {
        val products = productRepository.findAll().filter { it.deletedAt == null }
        return products.map { product ->

            val reviews = reviewRepository.findAllByProductIdAndDeletedAtIsNull(product.id!!, Pageable.unpaged()).content
            val averageRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 0.0

            product.ratingAverage = averageRating
            productRepository.save(product)

            ProductResponse(product)
        }
    }//- `findAllByProductIdAndDeletedAtIsNull` 를 추가해 리뷰와 연결하고`reviews.isNotEmpty()`를 통해 리뷰가
    // 존재하는지 확인. 만약 리뷰가 하나라도 있다면, `reviews.map { it.rating }`을 통해 각 리뷰의 평점을 추출하고,
    // `.average()`를 사용하여 이 평점들의 평균 값을 계산

    @Transactional
    override fun getProductById(productId: Long): ProductResponse {
        val product =
            productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow { ModelNotFoundException("Product not found or deleted", productId) }

        val reviews = reviewRepository.findAllByProductIdAndDeletedAtIsNull(productId, Pageable.unpaged()).content
        val averageRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 0.0

        product.ratingAverage = averageRating
        productRepository.save(product)

        return ProductResponse(product)
    }

    @Transactional
    override fun getPaginatedProductList(pageable: Pageable): Page<ProductResponse> {
        return productRepository.findAllByDeletedAtIsNull(pageable)
    }

    @Transactional
    override fun createProduct(
        sellerId: Long,
        request: ProductRequest,
    ): Product {
        val seller =
            sellerRepository.findByIdOrNull(sellerId)
                ?: throw ModelNotFoundException("seller", sellerId)

        val product =
            Product(
                title = request.title,
                content = request.content,
                imageUrl = request.imageUrl,
                price = request.price,
                purchaseAble = request.purchaseAble,
                userLimit = request.userLimit,
                discount = request.discount,
                seller = seller,
                likes = 0,
            )

        val productStock =
            ProductStock(
                product = product,
                stock = request.stock,
                productName = request.title,
            )

        // product.productStock = productStock

        productRepository.save(product)
        productStockRepository.save(productStock)

        return product
    }

    @Transactional
    override fun updateProduct(
        productId: Long,
        sellerId: Long,
        request: ProductRequest,
    ): Product {
        val product =
            productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow { ModelNotFoundException("Product not found or deleted", productId) }

        val seller =
            sellerRepository.findByIdOrNull(sellerId)
                ?: throw ModelNotFoundException("seller", sellerId)

        if (seller != product.seller) {
            throw IllegalArgumentException("권한이 없습니다")
        }

        product.apply {
            title = request.title
            content = request.content
            imageUrl = request.imageUrl
            price = request.price
            purchaseAble = request.purchaseAble
        }

        return productRepository.save(product)
    }

    @Transactional
    override fun deleteProduct(
        productId: Long,
        sellerId: Long,
    ): Product {
        val product =
            productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow { ModelNotFoundException("Product not found or deleted", productId) }

        val seller =
            sellerRepository.findByIdOrNull(sellerId)
                ?: throw ModelNotFoundException("seller", sellerId)

        if (seller != product.seller) {
            throw IllegalArgumentException("권한이 없습니다")
        }

        product.deletedAt = LocalDateTime.now()

        return productRepository.save(product)
    }

    @Transactional
    override fun decreaseStock(
        productId: Long,
        quantity: Int,
    ) {
        val product =
            productRepository.findById(productId)
                .orElseThrow { ModelNotFoundException("Product", productId) }

        val productStock =
            productStockRepository.findById(productId)
                .orElseThrow { ModelNotFoundException("Product", productId) }

        if (productStock.stock < quantity) {
            throw IllegalStateException("Not enough stock")
        }

        productStock.stock -= quantity
        productStockRepository.save(productStock)

        if (productStock.stock == 0) {
            product.isSoldOut = true
            productRepository.save(product)
        }
    }
}
