package com.marketpos.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

object SettingKeys {
    const val PIN_HASH = "auth.pin_hash"
    const val PIN_SALT = "auth.pin_salt"
    const val APP_MODE = "app.mode"
    const val APP_SALE_MODE = "app.sale_mode"
    const val THEME_MODE = "ui.theme_mode"
    const val CAMERA_ENABLED_ADMIN = "scan.camera_enabled.admin"
    const val CAMERA_ENABLED_CASHIER = "scan.camera_enabled.cashier"
    const val CAMERA_ENABLED_STOCK_COUNT = "scan.camera_enabled.stock_count"
    const val SERIAL_SCAN_COOLDOWN = "scan.serial.cooldown"
    const val SCAN_BOX_SIZE = "scan.box_size"
    const val ROUNDING_MODE = "pricing.rounding_mode"
    const val DEFAULT_PERCENT_STEP = "pricing.default_percent_step"
    const val PREMIUM_TIER = "premium.tier"
    const val PREMIUM_SOURCE = "premium.source"
    const val PREMIUM_LICENSE_CODE = "premium.license_code"
    const val PREMIUM_ACTIVATED_AT = "premium.activated_at"
    const val PREMIUM_EXPIRES_AT = "premium.expires_at"
    const val PREMIUM_TRIAL_USED = "premium.trial_used"
    const val STOCK_COUNT_SESSION = "stock.count.session"
    const val STOCK_COUNT_STARTED_AT = "stock.count.started_at"
    const val HELD_CARTS = "cart.held.sessions"
    const val COMPANY_ID = "sync.company.id"
    const val COMPANY_NAME = "sync.company.name"
    const val COMPANY_CODE = "sync.company.code"
    const val SYNC_DEVICE_UID = "sync.device.uid"
    const val SYNC_DEVICE_NAME = "sync.device.name"
    const val SYNC_ACTIVATION_TOKEN = "sync.activation.token"
    const val SYNC_LAST_SUCCESS_AT = "sync.last_success_at"
    const val SYNC_LAST_ERROR = "sync.last_error"
    const val SYNC_CATALOG_CURSOR = "sync.catalog.cursor"
    const val SYNC_CATALOG_CURSOR_COMPANY = "sync.catalog.cursor.company"
    const val SESSION_TYPE = "session.type"
    const val SESSION_USER_ID = "session.user.id"
    const val SESSION_USER_NAME = "session.user.name"
    const val SESSION_USER_EMAIL = "session.user.email"
    const val SESSION_AUTH_TOKEN = "session.auth.token"
    const val LEGAL_CONSENT_VERSION = "legal.consent.version"
    const val LEGAL_CONSENT_ACCEPTED_AT = "legal.consent.accepted_at"
}
