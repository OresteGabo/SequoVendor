package dev.orestegabo.sequovendor.core.orders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

@Composable
actual fun rememberVendorIncomingOrderReceiptStore(): VendorIncomingOrderReceiptStore =
    remember { IosVendorIncomingOrderReceiptStore() }

private class IosVendorIncomingOrderReceiptStore(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : VendorIncomingOrderReceiptStore {
    override fun consumed(scope: String): Set<String> =
        decodeVendorIncomingReceipts(defaults.stringForKey(vendorIncomingReceiptKey(scope))).toSet()

    override fun consume(scope: String, orderId: String) {
        val key = vendorIncomingReceiptKey(scope)
        val encoded = encodeVendorIncomingReceipt(decodeVendorIncomingReceipts(defaults.stringForKey(key)), orderId)
        defaults.setObject(encoded, forKey = key)
        check(defaults.synchronize() && defaults.stringForKey(key) == encoded) {
            "Order alert receipt could not be saved."
        }
    }
}
