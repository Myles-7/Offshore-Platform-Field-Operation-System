package com.offshore.platform.mobile.data.repository

import com.offshore.platform.mobile.data.remote.NetworkResult
import com.offshore.platform.mobile.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * All repositories extend this to get uniform error handling.
 *
 * Usage:
 *   class MyRepository : BaseRepository() {
 *       suspend fun getData() = safeApiCall {
 *           api.getSomething()
 *       }
 *   }
 */
abstract class BaseRepository {

    /** Tag for log filtering. */
    private val logTag: String = javaClass.simpleName

    /**
     * Execute a Retrofit [Response] call against backend ApiResponse<T> and map it to [NetworkResult].
     *
     * Error mapping:
     *  - Code 200 body → NetworkResult.Success
     *  - Code 401       → NetworkResult.Unauthorized
     *  - Code 403       → NetworkResult.Forbidden
     *  - Code 404       → NetworkResult.NotFound
     *  - Code 4xx       → NetworkResult.BusinessError
     *  - Code 5xx       → NetworkResult.ServerError
     *  - IOException    → NetworkResult.NetworkError
     *  - Other Throwable → NetworkResult.UnknownError
     */
    protected suspend fun <T> safeApiCall(
        block: suspend () -> Response<com.offshore.platform.mobile.data.remote.dto.ApiResponse<T>>
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        try {
            val response = block()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                if (body.code == 200) {
                    @Suppress("UNCHECKED_CAST")
                    val data = body.data ?: (Unit as T)
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.BusinessError(
                        code = body.code,
                        message = body.message.ifBlank { "未知业务错误" },
                        traceId = body.traceId
                    )
                }
            } else {
                when (response.code()) {
                    401 -> NetworkResult.Unauthorized
                    403 -> NetworkResult.Forbidden
                    404 -> NetworkResult.NotFound
                    in 500..599 -> NetworkResult.ServerError(
                        code = response.code(),
                        message = response.message().ifBlank { "服务器内部错误" }
                    )
                    else -> NetworkResult.BusinessError(
                        code = response.code(),
                        message = response.message().ifBlank { "请求失败" }
                    )
                }
            }
        } catch (e: SocketTimeoutException) {
            AppLogger.w("[$logTag] 网络超时: ${e.message}")
            NetworkResult.NetworkError(e)
        } catch (e: ConnectException) {
            AppLogger.w("[$logTag] 连接失败: ${e.message}")
            NetworkResult.NetworkError(e)
        } catch (e: UnknownHostException) {
            AppLogger.w("[$logTag] DNS解析失败: ${e.message}")
            NetworkResult.NetworkError(e)
        } catch (e: IOException) {
            AppLogger.w("[$logTag] 网络IO异常: ${e.message}")
            NetworkResult.NetworkError(e)
        } catch (e: Exception) {
            AppLogger.e("[$logTag] 未知异常", e)
            NetworkResult.UnknownError(e.message ?: "未知错误", e)
        }
    }
}
