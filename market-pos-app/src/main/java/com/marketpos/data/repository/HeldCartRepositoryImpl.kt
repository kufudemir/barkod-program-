package com.marketpos.data.repository

import com.marketpos.core.util.DateUtils
import com.marketpos.data.db.dao.AppSettingDao
import com.marketpos.data.db.entity.AppSettingEntity
import com.marketpos.data.db.entity.SettingKeys
import com.marketpos.domain.model.CartItem
import com.marketpos.domain.model.HeldCart
import com.marketpos.domain.repository.HeldCartRepository
import java.util.Base64
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class HeldCartRepositoryImpl @Inject constructor(
    private val appSettingDao: AppSettingDao
) : HeldCartRepository {

    override fun observeHeldCarts(): Flow<List<HeldCart>> {
        return appSettingDao.observe(SettingKeys.HELD_CARTS).map { entity ->
            parseHeldCarts(entity?.value)
        }
    }

    override suspend fun getHeldCart(cartId: String): HeldCart? {
        return parseHeldCarts(appSettingDao.get(SettingKeys.HELD_CARTS)?.value)
            .firstOrNull { it.cartId == cartId }
    }

    override suspend fun saveHeldCart(label: String?, items: List<CartItem>): Result<HeldCart> {
        return runCatching {
            require(items.isNotEmpty()) { "Bekletilecek sepet bos olamaz" }
            val carts = parseHeldCarts(appSettingDao.get(SettingKeys.HELD_CARTS)?.value).toMutableList()
            val heldCart = HeldCart(
                cartId = UUID.randomUUID().toString(),
                label = label?.trim().takeUnless { it.isNullOrBlank() } ?: defaultLabel(),
                createdAt = DateUtils.now(),
                items = items
            )
            carts.add(0, heldCart)
            persist(carts)
            heldCart
        }
    }

    override suspend fun deleteHeldCart(cartId: String): Result<Unit> {
        return runCatching {
            val carts = parseHeldCarts(appSettingDao.get(SettingKeys.HELD_CARTS)?.value)
                .filterNot { it.cartId == cartId }
            persist(carts)
        }
    }

    private suspend fun persist(carts: List<HeldCart>) {
        if (carts.isEmpty()) {
            appSettingDao.delete(SettingKeys.HELD_CARTS)
            return
        }
        val raw = carts.joinToString(separator = ";;") { cart ->
            val encodedLabel = encode(cart.label)
            val itemsRaw = cart.items.joinToString(separator = ",,") { item ->
                listOf(
                    item.barcode,
                    item.quantity.toString(),
                    item.baseSalePriceKurus.toString(),
                    item.salePriceKurus.toString(),
                    item.costPriceKurus.toString(),
                    item.stockQty.toString(),
                    encode(item.name)
                ).joinToString("|")
            }
            "${cart.cartId}~${cart.createdAt}~$encodedLabel~$itemsRaw"
        }
        appSettingDao.set(AppSettingEntity(SettingKeys.HELD_CARTS, raw))
    }

    private fun parseHeldCarts(raw: String?): List<HeldCart> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(";;")
            .mapNotNull(::parseHeldCart)
            .sortedByDescending { it.createdAt }
    }

    private fun parseHeldCart(token: String): HeldCart? {
        val parts = token.split("~", limit = 4)
        val cartId = parts.getOrNull(0)?.trim().orEmpty()
        val createdAt = parts.getOrNull(1)?.toLongOrNull()
        val label = parts.getOrNull(2)?.let(::decode).orEmpty()
        val itemsRaw = parts.getOrNull(3).orEmpty()
        if (cartId.isBlank() || createdAt == null || label.isBlank()) return null
        val items = itemsRaw.split(",,")
            .mapNotNull(::parseItem)
        if (items.isEmpty()) return null
        return HeldCart(
            cartId = cartId,
            label = label,
            createdAt = createdAt,
            items = items
        )
    }

    private fun parseItem(token: String): CartItem? {
        val parts = token.split("|")
        val barcode = parts.getOrNull(0)?.trim().orEmpty()
        val quantity = parts.getOrNull(1)?.toIntOrNull()
        val basePrice = parts.getOrNull(2)?.toLongOrNull()
        val salePrice = parts.getOrNull(3)?.toLongOrNull()
        val costPrice = parts.getOrNull(4)?.toLongOrNull()
        val stockQty = parts.getOrNull(5)?.toIntOrNull()
        val name = parts.getOrNull(6)?.let(::decode).orEmpty()
        if (barcode.isBlank() || quantity == null || quantity <= 0 || basePrice == null || salePrice == null || costPrice == null || stockQty == null || name.isBlank()) {
            return null
        }
        return CartItem(
            barcode = barcode,
            name = name,
            baseSalePriceKurus = basePrice,
            salePriceKurus = salePrice,
            costPriceKurus = costPrice,
            quantity = quantity,
            stockQty = stockQty
        )
    }

    private fun defaultLabel(): String {
        return "Bekleyen Sepet ${DateUtils.formatDateTime(DateUtils.now())}"
    }

    private fun encode(value: String): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray())
    }

    private fun decode(value: String): String {
        return runCatching { String(Base64.getUrlDecoder().decode(value)) }.getOrDefault(value)
    }
}
