package com.mustfaibra.roffu.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.mustfaibra.roffu.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {

    /** Manufacturer operations */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManufacturer(manufacturer: Manufacturer)

    @Transaction
    @Query("SELECT * FROM manufacturer")
    suspend fun getManufacturersWithProducts(): List<LocalManufacturer>

    @Query("SELECT * FROM user WHERE role = 'user'")
    fun getAllUsers(): Flow<List<User>>

    /** User operations */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: User)

    @Insert
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
    @Query("SELECT * FROM cart")
    suspend fun getCartItemsNow(): List<CartItem>

    @Query("SELECT * FROM user WHERE userId = :userId LIMIT 1")
    suspend fun getLoggedUser(userId: Int): User?

    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM user WHERE email = :email AND password = :password LIMIT 1")
    suspend fun fakeSignIn(email: String, password: String): User?

    @Query("UPDATE user SET password = :newPassword WHERE email = :email")
    suspend fun updateUserPassword(email: String, newPassword: String)

    /** Products operations */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtherProductCopy(productColor: ProductColor)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSize(productSize: ProductSize)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE productId = :productId)")
    suspend fun isProductBookmarked(productId: Int): Boolean

    @Query("SELECT * FROM Product WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM Product WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Int): Product?

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM Product")
    fun getAllProducts(): Flow<List<Product>>

    @Transaction
    @Query("SELECT * FROM product WHERE id = :productId")
    suspend fun getProductDetails(productId: Int): ProductDetails?

    /** Advertisements operations */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvertisement(advertisement: Advertisement)

    @Query("SELECT * FROM advertisement")
    suspend fun getAdvertisements(): List<Advertisement>

    /** All Cart operations */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Transaction
    @Query("SELECT * from cart")
    fun getCartItems(): Flow<MutableList<CartItemWithProduct>>

    @Query("SELECT productId FROM cart")
    fun getCartProductsIds(): Flow<List<Int>>

    @Update
    suspend fun updateCartItem(cartItem: CartItem)

    @Query("UPDATE cart SET quantity = :quantity WHERE productId = :productId ")
    suspend fun updateCartItemQuantity(productId: Int, quantity: Int)

    @Query("DELETE FROM cart WHERE productId = :productId ")
    suspend fun deleteCartItem(productId: Int)

    @Query("DELETE FROM cart")
    suspend fun clearCart()

    /** Payment providers operations */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePaymentProvider(paymentProvider: PaymentProvider)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserPaymentProvider(userPaymentProvider: UserPaymentProvider)

    @Transaction
    @Query("SELECT * FROM userPaymentProviders")
    suspend fun getUserPaymentProviders(): List<UserPaymentProviderDetails>

    /** Order operations */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderPayment(payment: OrderPayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>)

    @Transaction
    @Query("SELECT * FROM orders")
    suspend fun getLocalOrders(): List<OrderDetails>

    /** Bookmarks operations */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmarkItem(bookmarkItem: BookmarkItem)

    @Transaction
    @Query("SELECT * from bookmarks")
    fun getBookmarkItems(): Flow<MutableList<BookmarkItemWithProduct>>

    @Query("SELECT productId FROM bookmarks")
    fun getBookmarkProductsIds(): Flow<List<Int>>

    @Query("DELETE FROM bookmarks WHERE productId = :productId ")
    suspend fun deleteBookmarkItem(productId: Int)

    @Query("DELETE FROM bookmarks")
    suspend fun clearBookmark()

    /** Product reviews */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    /** Locations operations */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocation(location: Location)

    @Query("SELECT * FROM location")
    suspend fun getUserLocations(): List<Location>

    @Query("UPDATE orders SET isDelivered = :delivered")
    suspend fun updateOrdersAsDelivered(delivered: Boolean = true)

    /** Manufacturer operations */
    @Query("SELECT * FROM Manufacturer")
    fun getAllManufacturers(): Flow<List<Manufacturer>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVirtualCard(card: VirtualCard)

    @Query("SELECT * FROM virtual_cards WHERE userId = :userId LIMIT 1")
    suspend fun getVirtualCardByUser(userId: Int): VirtualCard?
}