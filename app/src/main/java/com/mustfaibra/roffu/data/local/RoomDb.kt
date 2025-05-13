package com.mustfaibra.roffu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mustfaibra.roffu.R
import com.mustfaibra.roffu.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(
    entities = [
        Advertisement::class,
        Manufacturer::class,
        Review::class,
        User::class,
        PaymentProvider::class,
        UserPaymentProvider::class,
        Product::class,
        BookmarkItem::class,
        Location::class,
        CartItem::class,
        Order::class,
        OrderItem::class,
        OrderPayment::class,
        Notification::class,
        ProductColor::class,
        ProductSize::class,
        VirtualCard::class
    ],
    version = 9, exportSchema = false)
abstract class RoomDb : RoomDatabase() {

    /** A function that used to retrieve Room's related dao instance */
    abstract fun getDao(): RoomDao

    class PopulateDataClass @Inject constructor(
        private val client: Provider<RoomDb>,
        private val scope: CoroutineScope,
    ) : RoomDatabase.Callback() {
        private val description =
            "This is the description text that is supposed to be long enough to show how the UI looks, so it's not a real text.\n"
        private val manufacturers = listOf(
            Manufacturer(id = 1, name = "Nike", icon = R.drawable.ic_nike),
            Manufacturer(id = 2, name = "Adidas", icon = R.drawable.adidas_48),
        )
        private val advertisements = listOf(
            Advertisement(1, R.drawable.air_huarache_gold_black_ads, 1, 0),
            Advertisement(2, R.drawable.pegasus_trail_gortex_ads, 2, 0),
            Advertisement(3, R.drawable.blazer_low_black_ads, 3, 0),
        )

        private val nikeProducts = listOf(
            Product(
                id = 1,
                name = "Pegasus Trail Gortex Green",
                image = R.drawable.pegasus_trail_3_gore_tex_dark_green,
                price = 149.0,
                description = description,
                manufacturerId = 1,
                basicColorName = "dark-green",
                barcode = "9788492808274"
            ).also {
                it.colors = mutableListOf(
                    ProductColor(productId = it.id,
                        colorName = it.basicColorName,
                        image = it.image),
                    ProductColor(productId = it.id,
                        colorName = "lemon",
                        image = R.drawable.pegasus_trail_3_gore_tex_lemon),
                )
            },
            Product(
                id = 3,
                name = "Air Huarache Gold",
                image = R.drawable.air_huarache_le_gold_black,
                price = 159.0,
                description = description,
                manufacturerId = 1,
                basicColorName = "gold",
                barcode = "12345"
            ).also {
                it.colors = mutableListOf(
                    ProductColor(productId = it.id,
                        colorName = it.basicColorName,
                        image = it.image),
                    ProductColor(productId = it.id,
                        colorName = "gray",
                        image = R.drawable.air_huarache_le_gray_dark),
                    ProductColor(productId = it.id,
                        colorName = "pink",
                        image = R.drawable.air_huarache_le_pink_black),
                    ProductColor(productId = it.id,
                        colorName = "red",
                        image = R.drawable.air_huarache_le_red_black),
                )
            },
            Product(
                id = 7,
                name = "Blazer Low Black",
                image = R.drawable.blazer_low_black,
                price = 120.0,
                description = description,
                manufacturerId = 1,
                basicColorName = "black",
                barcode = "12345"
            ).also {
                it.colors = mutableListOf(
                    ProductColor(productId = it.id,
                        colorName = it.basicColorName,
                        image = it.image),
                    ProductColor(productId = it.id,
                        colorName = "pink",
                        image = R.drawable.blazer_low_pink),
                    ProductColor(productId = it.id,
                        colorName = "lemon",
                        image = R.drawable.blazer_low_light_green),
                )
            },
        )
        private val adidasProducts = listOf(
            Product(
                id = 10,
                name = "Defiant Generation Green",
                image = R.drawable.defiant_generation_green,
                price = 149.0,
                description = description,
                manufacturerId = 2,
                basicColorName = "green",
                barcode = "12345"
            ).also {
                it.colors = mutableListOf(
                    ProductColor(productId = it.id,
                        colorName = it.basicColorName,
                        image = it.image),
                    ProductColor(productId = it.id,
                        colorName = "red",
                        image = R.drawable.defiant_generation_red),
                )
            },

            Product(
                id = 12,
                name = "Solarthon Primegreen Gray",
                image = R.drawable.solarthon_primegreen_gray,
                price = 159.0,
                description = description,
                manufacturerId = 2,
                basicColorName = "gray",
                barcode = "12345"
            ).also {
                it.colors = mutableListOf(
                    ProductColor(productId = it.id,
                        colorName = it.basicColorName,
                        image = it.image),
                    ProductColor(productId = it.id,
                        colorName = "black",
                        image = R.drawable.solarthon_primegreen_black),
                    ProductColor(productId = it.id,
                        colorName = "red",
                        image = R.drawable.solarthon_primegreen_red),
                )
            },
        )
        private val paymentProviders = listOf(
            PaymentProvider(
                id = "apple",
                title = R.string.apple_pay,
                icon = R.drawable.ic_apple,
            ),
            PaymentProvider(
                id = "master",
                title = R.string.master_card,
                icon = R.drawable.ic_master_card,
            ),
            PaymentProvider(
                id = "visa",
                title = R.string.visa,
                icon = R.drawable.ic_visa,
            ),
        )
        private val userPaymentAccounts = listOf(
            UserPaymentProvider(
                providerId = "apple",
                cardNumber = "8402-5739-2039-5784"
            ),
            UserPaymentProvider(
                providerId = "master",
                cardNumber = "3323-8202-4748-2009"
            ),
            UserPaymentProvider(
                providerId = "visa",
                cardNumber = "7483-02836-4839-2833"
            ),
        )
        private val userLocation = Location(
            address = "AlTaif 51, st 5",
            city = "Khartoum",
            country = "Sudan",
        )

        init {
            nikeProducts.onEach {
                it.sizes = mutableListOf(
                    ProductSize(it.id, 38),
                    ProductSize(it.id, 40),
                    ProductSize(it.id, 42),
                    ProductSize(it.id, 44),
                )
            }
            adidasProducts.onEach {
                it.sizes = mutableListOf(
                    ProductSize(it.id, 38),
                    ProductSize(it.id, 40),
                    ProductSize(it.id, 42),
                    ProductSize(it.id, 44),
                )
            }

            scope.launch {
                populateDatabase(dao = client.get().getDao(), scope = scope)
            }
        }

        private suspend fun populateDatabase(dao: RoomDao, scope: CoroutineScope) {
            /** Save users */
            scope.launch {
                dao.saveUser(
                    User(
                        userId = 1,
                        name = "Phuc admin",
                        profile = R.drawable.mustapha_profile,
                        phone = "0945396023",
                        email = "phucadmin@gmail.com",
                        password = "Anhnhoem456@",
                        token = "1234567",
                        role = "admin"
                    )
                )
            }
            /** insert manufacturers */
            scope.launch {
                manufacturers.forEach {
                    dao.insertManufacturer(it)
                }
            }
            /** insert advertisements */
            scope.launch {
                advertisements.forEach {
                    dao.insertAdvertisement(it)
                }
            }
            /** Insert products */
            scope.launch {
                nikeProducts.plus(adidasProducts).forEach {
                    /** Insert the product itself */
                    dao.insertProduct(product = it)
                    /** Insert colors */
                    it.colors?.forEach { productColor ->
                        dao.insertOtherProductCopy(productColor)
                    }
                    /** Insert size */
                    it.sizes?.forEach { productSize ->
                        dao.insertSize(productSize)
                    }
                }
            }
            /** Insert payment providers */
            scope.launch {
                paymentProviders.forEach {
                    dao.savePaymentProvider(paymentProvider = it)
                }
            }
            /** Insert user's payment providers */
            scope.launch {
                userPaymentAccounts.forEach {
                    dao.saveUserPaymentProvider(it)
                }
            }
            /** Insert user's location */
            scope.launch {
                dao.saveLocation(location = userLocation)
            }
            /** Insert test orders with various statuses */
            scope.launch {
                val now = System.currentTimeMillis()
                val orderList = listOf(
                    Order(
                        orderId = "test1",
                        userId = 1,
                        total = 174.0,
                        createdAt = "2025-05-03 20:11",
                        modifiedAt = "2025-05-03 20:11",
                        status = "Chờ xác nhận",
                        locationId = 1,
                    ),
                    Order(
                        orderId = "test2",
                        userId = 1,
                        total = 443.0,
                        createdAt = "2025-05-03 20:12",
                        modifiedAt = "2025-05-03 20:12",
                        status = "Chờ lấy hàng",
                        locationId = 1,
                    ),
                    Order(
                        orderId = "test3",
                        userId = 1,
                        total = 255.0,
                        createdAt = "2025-05-03 20:16",
                        modifiedAt = "2025-05-03 20:16",
                        status = "Đang giao",
                        locationId = 1,
                    ),
                    Order(
                        orderId = "test4",
                        userId = 1,
                        total = 399.0,
                        createdAt = "2025-05-03 20:20",
                        modifiedAt = "2025-05-03 20:20",
                        status = "Đã giao",
                        locationId = 1,
                    ),
                    Order(
                        orderId = "test5",
                        userId = 1,
                        total = 123.0,
                        createdAt = "2025-05-03 20:25",
                        modifiedAt = "2025-05-03 20:25",
                        status = "Đã hủy",
                        locationId = 1,
                    )
                )
                orderList.forEach { dao.insertOrder(it) }
                val orderItems = listOf(
                    OrderItem(orderId = "test1", quantity = 2, productId = 1, userId = 1),
                    OrderItem(orderId = "test1", quantity = 1, productId = 3, userId = 1),
                    OrderItem(orderId = "test2", quantity = 3, productId = 7, userId = 1),
                    OrderItem(orderId = "test3", quantity = 1, productId = 10, userId = 1),
                    OrderItem(orderId = "test4", quantity = 2, productId = 12, userId = 1),
                    OrderItem(orderId = "test5", quantity = 1, productId = 1, userId = 1)
                )
                dao.insertOrderItems(orderItems)
                val payments = listOf(
                    OrderPayment(orderId = "test1", providerId = "apple"),
                    OrderPayment(orderId = "test2", providerId = "master"),
                    OrderPayment(orderId = "test3", providerId = "visa"),
                    OrderPayment(orderId = "test4", providerId = "apple"),
                    OrderPayment(orderId = "test5", providerId = "master")
                )
                payments.forEach { dao.insertOrderPayment(it) }
            }
        }
    }

}
val MIGRATION_1_2 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //database.execSQL("ALTER TABLE User ADD COLUMN role TEXT NOT NULL DEFAULT 'user'")
//        database.execSQL("ALTER TABLE User ADD COLUMN city TEXT")
//        database.execSQL("ALTER TABLE User ADD COLUMN district TEXT")
//        database.execSQL("ALTER TABLE User ADD COLUMN address TEXT")
        database.execSQL("ALTER TABLE Product ADD COLUMN imagePath TEXT")
        // Thêm trường status vào migration nếu cần
        database.execSQL("ALTER TABLE orders ADD COLUMN status TEXT NOT NULL DEFAULT 'Chờ xác nhận'")
    }
}