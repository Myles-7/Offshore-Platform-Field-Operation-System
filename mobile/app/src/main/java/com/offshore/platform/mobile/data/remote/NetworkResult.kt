package com.offshore.platform.mobile.data.remote

/**
 * Sealed result type wrapping all possible API call outcomes.
 *
 * Usage:
 *   when (result) {
 *     is NetworkResult.Success -> use(result.data)
 *     is NetworkResult.BusinessError -> show(result.message)
 *     is NetworkResult.Unauthorized -> navigateToLogin()
 *     is NetworkResult.Forbidden -> showForbidden()
 *     is NetworkResult.NetworkError -> showOffline()
 *     is NetworkResult.ServerError -> showServerError()
 *     is NetworkResult.UnknownError -> showGenericError(result.message)
 *   }
 */
sealed class NetworkResult<out T> {

    /** 200 with valid data. */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /** 4xx / 5xx with a backend error code. */
    data class BusinessError(val code: Int, val message: String, val traceId: String? = null) :
        NetworkResult<Nothing>()

    /** 401 — token expired or missing. */
    data object Unauthorized : NetworkResult<Nothing>()

    /** 403 — role/permission denied. */
    data object Forbidden : NetworkResult<Nothing>()

    /** 404 — resource not found. */
    data object NotFound : NetworkResult<Nothing>()

    /** Network timeout, DNS failure, no connectivity. */
    data class NetworkError(val throwable: Throwable) : NetworkResult<Nothing>()

    /** HTTP 500 or similar server fault. */
    data class ServerError(val code: Int, val message: String) :
        NetworkResult<Nothing>()

    /** Uncategorised failure. */
    data class UnknownError(val message: String, val throwable: Throwable? = null) :
        NetworkResult<Nothing>()

    // ---- convenience ----

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this !is Success

    fun getOrNull(): T? = (this as? Success)?.data

    fun getOrDefault(default: @UnsafeVariance T): T =
        (this as? Success)?.data ?: default

    fun <R> map(transform: (T) -> R): NetworkResult<R> = when (this) {
        is Success -> Success(transform(data))
        is BusinessError -> BusinessError(code, message, traceId)
        is Unauthorized -> Unauthorized
        is Forbidden -> Forbidden
        is NotFound -> NotFound
        is NetworkError -> NetworkError(throwable)
        is ServerError -> ServerError(code, message)
        is UnknownError -> UnknownError(message, throwable)
    }

    companion object {
        fun <T> success(data: T): NetworkResult<T> = Success(data)
    }
}
