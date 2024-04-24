package com.teamsparta.moamoa.domain.search.service

import com.teamsparta.moamoa.domain.product.repository.ProductRepository
import com.teamsparta.moamoa.domain.review.repository.ReviewRepository
import com.teamsparta.moamoa.domain.search.dto.ProductSearchResponse
import com.teamsparta.moamoa.domain.search.dto.ReviewSearchResponse
import com.teamsparta.moamoa.domain.search.model.SearchHistory
import com.teamsparta.moamoa.domain.search.repository.SearchHistoryRepository
import com.teamsparta.moamoa.domain.search.repository.SearchProductRepository
import com.teamsparta.moamoa.domain.search.repository.SearchReviewRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class SearchServiceImpl(
    private val searchProductRepository: SearchProductRepository,
    private val searchReviewRepository: SearchReviewRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val productRepository: ProductRepository,
    private val reviewRepository: ReviewRepository,
) : SearchService {
    @Transactional
    override fun searchProductsByLikes(pageable: Pageable): List<ProductSearchResponse> {
        val products = searchProductRepository.findAllByDeletedAtIsNullOrderByLikesDesc(pageable)
        return products.map { product -> // map는 product를 ProductSearchResponse로 변환하겠다는 뜻
            ProductSearchResponse(
                productId = product.id!!,
                title = product.title,
                content = product.content,
                imageUrl = product.imageUrl,
                price = product.price,
                ratingAverage = product.ratingAverage,
                likes = product.likes,
            )//map로 변환을 하는 이유는 검색을 했을때 그걸 안하면 Product가 전부 보여지니까 ProductSearchResponse랑 형태가 같아야 해서
        }
    }

    @Transactional
    override fun searchReviewsByLikes(pageable: Pageable): List<ReviewSearchResponse> {
        val reviews = searchReviewRepository.findAllByDeletedAtIsNullOrderByLikesDesc(pageable)
        return reviews.map { review ->
            ReviewSearchResponse(
                reviewId = review.id!!,
                productId = review.product.id!!,
                title = review.title,
                content = review.content,
                imageUrl = review.imageUrl,
                name = review.socialUser.nickname,
                likes = review.likes,
            )
        }
    }

    @Transactional
    override fun searchProducts(
        keyword: String,
        pageable: Pageable,
    ): Page<ProductSearchResponse> {
        saveSearchHistory(keyword) // 검색하면 히스토리에 저장된다는 뜻

        val productPage = searchProductRepository.findByTitleContainingAndDeletedAtIsNull(keyword, pageable)
//findByTitleContainingAndDeletedAtIsNull 는 제목에 키워드가 포함된 상품중 삭제되지 않은것들 중에서 검색
        return productPage.map { product ->
            ProductSearchResponse(
                productId = product.id!!,
                title = product.title,
                content = product.content,
                imageUrl = product.imageUrl,
                price = product.price,
                ratingAverage = product.ratingAverage,
                likes = product.likes,
            )
        }
    }
//    상품 검색: searchProductRepository.findAllByDeletedAtIsNullOrderByLikesDesc(pageable) 메소드를 사용하여 삭제되지 않은(deletedAt이 null인) 모든 상품을 '좋아요' 수가 많은 순서로 정렬하여 검색합니다. pageable 파라미터를 통해 페이징 처리가 가능하며, 이는 요청한 페이지 번호, 페이지 당 항목 수, 정렬 순서 등을 포함할 수 있습니다.
//
//    응답 객체 변환: 검색된 상품 목록(products)을 반복하면서 각 상품을 ProductSearchResponse 객체로 변환합니다. 이 과정에서 상품의 ID, 제목, 내용, 이미지 URL, 가격, 평균 평점, 좋아요 수 등을 ProductSearchResponse 객체의 속성으로 설정합니다.
//
//    결과 반환: 변환된 ProductSearchResponse 객체 리스트를 반환합니다.

    @Transactional
    override fun searchReviews(
        keyword: String,
        pageable: Pageable,
    ): Page<ReviewSearchResponse> {
        saveSearchHistory(keyword)

        val reviewsPage = searchReviewRepository.findReviewsByTitleContaining(keyword, pageable)

        return reviewsPage.map { review ->
            ReviewSearchResponse(
                reviewId = review.id!!,
                productId = review.product.id!!,
                title = review.title,
                content = review.content,
                imageUrl = review.imageUrl,
                name = review.socialUser.nickname,
                likes = review.likes,
            )
        }
    }

    @Transactional
    override fun saveSearchHistory(keyword: String) {
        var searchHistory = searchHistoryRepository.findByKeyword(keyword)
        if (searchHistory != null) {
// 키워드를 누르면 서치 히스토리에 가서 확인하는데 이미 존재하는 키워드라면,즉 널이 아니면 count를 증가
            searchHistory.count++
        } else {
            // 널이라면, 즉 새로운 키워드라면 SearchHistory 에 1을 추가
            searchHistory = SearchHistory(id = null, keyword = keyword, count = 1)
        }
        searchHistoryRepository.save(searchHistory)
    }
//    키워드 검색: searchHistoryRepository.findByKeyword(keyword)를 통해 데이터베이스에서 입력된 키워드에 해당하는 검색 기록(searchHistory)을 찾습니다.
//
//    기록 확인 및 업데이트:
//
//    만약 해당 키워드의 검색 기록이 이미 존재한다면(searchHistory != null), 기록의 count 값을 1 증가시킵니다. 이는 해당 키워드가 다시 검색되었음을 나타냅니다.
//    해당 키워드의 검색 기록이 존재하지 않는다면(else), 새로운 SearchHistory 객체를 생성하고, 키워드와 함께 검색 횟수(count)를 1로 설정합니다. 이는 새로운 검색어에 대한 기록을 시작하는 것입니다.
//    검색 기록 저장: 변경된 searchHistory 객체(기존 검색 기록의 업데이트 또는 새로운 검색 기록)를 searchHistoryRepository.save(searchHistory)를 통해 데이터베이스에 저장합니다. 이로써 검색 기록이 업데이트되거나 새로운 기록이 추가됩니다.
//
//    이 기능은 검색어의 인기도나 사용 빈도를 파악하는 데 유용하며, 이를 통해 사용자들이 자주 찾는 검색어를 기반으로 추천 시스템을 개선하거나 마케팅 전략을 수립하는 등 다양한 용도로 활용될 수 있습니다.

    @Transactional
    override fun getPopularKeywords(): List<SearchHistory> {
        return searchHistoryRepository.findAll(Sort.by(Sort.Direction.DESC, "count"))
    }

    @Transactional
    override fun searchProductsByRating(pageable: Pageable): List<ProductSearchResponse> {
        val products = searchProductRepository.findAllByDeletedAtIsNullOrderByRatingAverageDesc(pageable)

        products.forEach { product ->
            val reviews = reviewRepository.findAllByProductIdAndDeletedAtIsNull(product.id!!, Pageable.unpaged()).content
            val averageRating = reviews.map { it.rating }.average()
            product.ratingAverage = if (reviews.isNotEmpty()) averageRating else 0.0 // 평가가 없는 경우 평균 별점을 0으로 설정. 이거 없으면 nan이라고 나오고 맨 위에 나와서 방해함
            productRepository.save(product)
        }

        return products.map { product ->
            ProductSearchResponse(
                productId = product.id!!,
                title = product.title,
                content = product.content,
                imageUrl = product.imageUrl,
                price = product.price,
                ratingAverage = product.ratingAverage,
                likes = product.likes,
            )
        }
    }
}
