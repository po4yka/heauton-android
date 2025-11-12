package com.po4yka.heauton.data.local.database.entities

/**
 * Delivery method for scheduled quotes.
 */
enum class DeliveryMethod {
    /**
     * Deliver via notification only.
     */
    NOTIFICATION,

    /**
     * Deliver via widget only.
     */
    WIDGET,

    /**
     * Deliver via both notification and widget.
     */
    BOTH
}
