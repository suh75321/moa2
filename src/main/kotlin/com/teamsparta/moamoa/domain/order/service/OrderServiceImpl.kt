package com.teamsparta.moamoa.domain.order.service

import com.teamsparta.moamoa.domain.order.dto.CancelResponseDto
import com.teamsparta.moamoa.domain.order.dto.CreateOrderDto
import com.teamsparta.moamoa.domain.order.dto.ResponseOrderDto
import com.teamsparta.moamoa.domain.order.dto.UpdateOrderDto
import com.teamsparta.moamoa.domain.order.model.OrdersEntity
import com.teamsparta.moamoa.domain.order.model.OrdesStatus
import com.teamsparta.moamoa.domain.order.model.toResponse
import com.teamsparta.moamoa.domain.order.repository.OrderRepository
import com.teamsparta.moamoa.domain.product.model.StockEntity.Companion.discount
import com.teamsparta.moamoa.domain.product.repository.ProductRepository
import com.teamsparta.moamoa.domain.product.repository.StockRepository
import com.teamsparta.moamoa.domain.user.repository.UserRepository
import com.teamsparta.moamoa.exception.ModelNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository,
    private val userRepository: UserRepository
):OrderService {
    @Transactional
    override fun creatOrder(userId: Long, productId: Long, createOrderDto: CreateOrderDto): ResponseOrderDto {
        val findUser = userRepository.findByIdOrNull(userId) ?: throw ModelNotFoundException("user", userId)
        val findProduct =
            productRepository.findByIdOrNull(productId) ?: throw ModelNotFoundException("product", productId)
        val stockCheck = stockRepository.findByProductId(findProduct) ?: throw Exception("없어~")

        return if (stockCheck.stock > 0 && stockCheck.stock > createOrderDto.quantity) {
            stockRepository.save(stockCheck.discount(createOrderDto.quantity))// 재고 날리고 저장
            orderRepository.save(
                OrdersEntity(
                    productName = findProduct.title,
                    totalPrice = findProduct.price * createOrderDto.quantity,
                    address = createOrderDto.address,
                    createdAt = LocalDateTime.now(),
                    discount = 0.0,
                    productId = findProduct,
                    quantity = createOrderDto.quantity,
                    userId = findUser
                )
            ).toResponse()
        } else {
            throw Exception("재고가 모자랍니다 다시 시도")
        }

    }

    @Transactional
    override fun updateOrder( userId: Long,ordersId: Long,updateOrderDto: UpdateOrderDto): ResponseOrderDto {
        val findUser = userRepository.findByIdOrNull(userId)?: throw ModelNotFoundException("user",userId)
        val findOrders = orderRepository.findByIdOrNull(ordersId)?:throw ModelNotFoundException("orders",ordersId)
            if(findOrders.userId == findUser){
                findOrders.address = updateOrderDto.address
                return orderRepository.save(findOrders).toResponse()
            }else{
                throw Exception("도둑 검거 완료")
            }

    }

    @Transactional
    override fun cancelOrder(userId: Long, ordersId: Long):CancelResponseDto {
        val findUser = userRepository.findByIdOrNull(userId)?: throw ModelNotFoundException("user",userId)
        val findOrder = orderRepository.findByIdOrNull(ordersId) ?: throw ModelNotFoundException("orders",ordersId)

        // 취소 여부를 먼저 확인 하고 유저랑 주문정보가 일치하는지를 확인한다 정책상 뭐가 우위인지 몰겄음
        return if(findOrder.status == OrdesStatus.CANCELLED || findOrder.deletedAt != null){
           throw Exception("이미 취소된 주문 입니다")
        }else if (findOrder.userId != findUser){
            throw Exception("유저와 주문정보가 일치 하지 않습니다")
        }else{
            findOrder.deletedAt = LocalDateTime.now()
            findOrder.status = OrdesStatus.CANCELLED
            orderRepository.save(findOrder)
            CancelResponseDto(
                message = "주문이 취소 되었습니다"
            )
        }



    }
}

