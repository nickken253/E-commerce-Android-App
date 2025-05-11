package com.mustfaibra.roffu.repositories

import com.mustfaibra.roffu.data.local.RoomDao
import com.mustfaibra.roffu.models.User
import com.mustfaibra.roffu.models.VirtualCard
import com.mustfaibra.roffu.sealed.DataResponse
import com.mustfaibra.roffu.sealed.Error
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val dao: RoomDao,
) {
    /** Fake login operation with email and password */
    suspend fun signInUser(email: String, password: String): DataResponse<User> {
        return dao.fakeSignIn(email = email, password = password)?.let {
            DataResponse.Success(data = it)
        } ?: DataResponse.Error(error = Error.Empty)
    }

    /** Save the user to local storage */
    suspend fun saveUserLocally(user: User) {
        dao.saveUser(user = user)
    }

    /** Get current logged user from local */
    suspend fun getLoggedUser(userId: Int) = dao.getLoggedUser(userId = userId)

    /** Get the available payment providers for current user */
    suspend fun getUserPaymentProviders() =
        dao.getUserPaymentProviders()

    /** Register a new user */
    suspend fun registerUser(user: User): DataResponse<User> {
        return try {
            // Check if user already exists (e.g., by email)
            val existingUser = user.email?.let { dao.getUserByEmail(email = it) }
            if (existingUser != null) {
                DataResponse.Error(error = Error.Custom("User with this email already exists"))
            } else {
                // Simulate saving the user to local storage
                dao.saveUser(user = user)
                // Retrieve the saved user to confirm (assuming saveUser assigns an ID)
                val savedUser = user.email?.let { dao.getUserByEmail(email = it) }
                savedUser?.let {
                    DataResponse.Success(data = it)
                } ?: DataResponse.Error(error = Error.Custom("Failed to retrieve registered user"))
            }
        } catch (e: Exception) {
            DataResponse.Error(error = Error.Network)
        }
    }

    /** Check if an email exists in the database */
    suspend fun checkEmailExists(email: String): DataResponse<Boolean> {
        return try {
            val user = dao.getUserByEmail(email = email)
            DataResponse.Success(data = user != null)
        } catch (e: Exception) {
            DataResponse.Error(error = Error.Network)
        }
    }

    /** Reset password for a user */
    suspend fun resetPassword(email: String, newPassword: String): DataResponse<User?> {
        return try {
            // Check if user exists by email
            val existingUser = dao.getUserByEmail(email = email)
            if (existingUser == null) {
                DataResponse.Error(error = Error.Custom("Email không tồn tại"))
            } else {
                // Update the user's password
                dao.updateUserPassword(email = email, newPassword = newPassword)
                // Retrieve the updated user to confirm
                val updatedUser = dao.getUserByEmail(email = email)
                updatedUser?.let {
                    DataResponse.Success(data = it)
                } ?: DataResponse.Error(error = Error.Custom("Failed to retrieve updated user"))
            }
        } catch (e: Exception) {
            DataResponse.Error(error = Error.Network)
        }
    }

    /** Get the available locations for current user */
    suspend fun getUserLocations() = dao.getUserLocations()

    suspend fun addVirtualCard(card: VirtualCard) = dao.insertVirtualCard(card)
    suspend fun getVirtualCardByUser(userId: Int) = dao.getVirtualCardByUser(userId)
    suspend fun deleteVirtualCard(card: VirtualCard) = dao.deleteVirtualCard(card)
}