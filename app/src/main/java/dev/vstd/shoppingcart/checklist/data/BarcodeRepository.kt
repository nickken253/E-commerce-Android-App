package dev.vstd.shoppingcart.checklist.data

class BarcodeRepository(private val barcodeDao: BarcodeItemDao) {
    suspend fun insert(barcodeItem: BarcodeItem) = barcodeDao.insert(barcodeItem)
    suspend fun findByBarcode(barcode: String) = barcodeDao.findByBarcode(barcode)
    suspend fun getRecents(limit: Int = 10) = barcodeDao.getRecents(limit)
}