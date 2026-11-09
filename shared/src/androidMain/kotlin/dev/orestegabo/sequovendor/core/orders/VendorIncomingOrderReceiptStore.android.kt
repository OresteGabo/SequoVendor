package dev.orestegabo.sequovendor.core.orders

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberVendorIncomingOrderReceiptStore(): VendorIncomingOrderReceiptStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidVendorIncomingOrderReceiptStore(context) }
}

private class AndroidVendorIncomingOrderReceiptStore(context: Context) : VendorIncomingOrderReceiptStore {
    private val preferences = context.getSharedPreferences("vendor_incoming_order_receipts", Context.MODE_PRIVATE)

    override fun consumed(scope: String): Set<String> =
        decodeVendorIncomingReceipts(preferences.getString(vendorIncomingReceiptKey(scope), null)).toSet()

    override fun consume(scope: String, orderId: String) {
        val key = vendorIncomingReceiptKey(scope)
        val encoded = encodeVendorIncomingReceipt(decodeVendorIncomingReceipts(preferences.getString(key, null)), orderId)
        check(preferences.edit().putString(key, encoded).commit()) { "Order alert receipt could not be saved." }
    }
}
