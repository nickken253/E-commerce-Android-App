package dev.vstd.shoppingcart.setting

object AddressFormValidator {
    fun validate(city: String?, district: String?, address: String?): Result {
        return when {
            city == null -> Result("Vui lòng chọn thành phố")
            district == null -> Result("Vui lòng chọn quận/huyện")
            address.isNullOrBlank() -> Result("Vui lòng nhập địa chỉ")
            else -> return Result("Success", true)
        }
    }

    data class Result(val errorMessage: String, val success: Boolean = false)
}