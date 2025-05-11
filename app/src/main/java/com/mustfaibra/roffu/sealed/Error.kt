package com.mustfaibra.roffu.sealed

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import com.mustfaibra.roffu.R
sealed class Error(@StringRes var title: Int, @StringRes var message: Int) {
    object Network : Error(
        title = R.string.net_conn_err_title,
        message = R.string.net_conn_err_message,
    )

    object Empty : Error(
        title = R.string.no_avail_data_err_title,
        message = R.string.no_avail_data_err_body,
    )

    object Unauthorized : Error(
        title = R.string.unauthorized_err_title,
        message = R.string.unauthorized_err_body,
    )

    object Forbidden : Error(
        title = R.string.forbidden_err_title,
        message = R.string.forbidden_err_body,
    )

    @SuppressLint("ResourceType")
    class Custom(
        title: String,
        message: Int = R.string.unknown_err_body,
    ) : Error(
        title = 404,
        message = message,
    )

    object Unknown : Error(
        title = R.string.unknown_err_title,
        message = R.string.unknown_err_body,
    )
}
