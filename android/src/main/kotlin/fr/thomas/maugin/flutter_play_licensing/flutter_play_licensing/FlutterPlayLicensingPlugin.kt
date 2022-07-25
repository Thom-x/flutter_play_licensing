package fr.thomas.maugin.flutter_play_licensing.flutter_play_licensing

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.google.android.vending.licensing.AESObfuscator
import com.google.android.vending.licensing.LicenseChecker
import com.google.android.vending.licensing.LicenseCheckerCallback
import com.google.android.vending.licensing.LicenseCheckerCallback.ERROR_CHECK_IN_PROGRESS
import com.google.android.vending.licensing.LicenseCheckerCallback.ERROR_MISSING_PERMISSION
import com.google.android.vending.licensing.ServerManagedPolicy
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


object PlayLicensingConfig {
    val salt: ByteArray = byteArrayOf(
        -46, 65, 30, -128, -103, -57, 74, -64, 51, 88,
        -95, -45, 77, -117, -36, -113, -11, 32, -64, 89
    )

    // in base64, BASE64_PUBLIC_KEY
    val publicKey: String = ""
}

/** FlutterPlayLicensingPlugin */
class FlutterPlayLicensingPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var context: Context? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_play_licensing")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "check" -> {
                check(call, result)
            }
            "isAllowed" -> {
                isAllowed(call, result)
            }
            "serverSideCheck" -> {
                serverSideCheck(call, result)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun Context.checker(
        salt: ByteArray? = null,
        publicKey: String? = null
    ): LicenseChecker {
        return LicenseChecker(
            this,
            ServerManagedPolicy(
                this,
                AESObfuscator(
                    salt ?: PlayLicensingConfig.salt,
                    packageName,
                    Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                )
            ),
            publicKey ?: PlayLicensingConfig.publicKey
        )
    }

    private fun Context.serverSideChecker(
        publicKey: String? = null
    ): LicenseCheckerService {
        return LicenseCheckerService(
            this,
            publicKey ?: PlayLicensingConfig.publicKey
        )
    }

    private fun isAllowed(call: MethodCall, result: Result) {
        context?.let { context ->
            val checker = context.checker(
                call.argument<String>("salt")?.toHexByteArray,
                call.argument<String>("publicKey")
            )
            checker.checkAccess(
                onAllow = {
                    result.onMain().success(true)
                },
                onDontAllow = {
                    result.onMain().success(false)
                },
                onApplicationError = { errorCode ->
                    when (errorCode) {
                        ERROR_MISSING_PERMISSION -> {
                            result.onMain().errors(
                                errorCode.toString(),
                                "ERROR_MISSING_PERMISSION",
                                details = errorCode
                            )
                        }
                        ERROR_CHECK_IN_PROGRESS -> {
                            result.onMain().errors(
                                errorCode.toString(),
                                "ERROR_CHECK_IN_PROGRESS",
                                details = errorCode
                            )
                        }
                        //ERROR_NON_MATCHING_UID -> {
                        //  result.onMain().errors(errorCode.toString(),
                        //          "ERROR_NON_MATCHING_UID",
                        //          details = errorCode)
                        //}
                        //ERROR_INVALID_PACKAGE_NAME -> {
                        //  result.onMain().errors(errorCode.toString(),
                        //          "ERROR_INVALID_PACKAGE_NAME",
                        //          details = errorCode)
                        //}
                        //ERROR_INVALID_PUBLIC_KEY -> {
                        //  result.onMain().errors(errorCode.toString(),
                        //          "ERROR_INVALID_PUBLIC_KEY",
                        //          details = errorCode)
                        //}
                        else -> {
                            result.onMain().success(false)
                        }
                    }
                }
            )
        } ?: result.notImplemented()
    }

    private fun check(call: MethodCall, result: Result) {
        context?.let { context ->
            val checker = context.checker(
                call.argument<String>("salt")?.toHexByteArray,
                call.argument<String>("publicKey")
            )
            checker.checkAccess(
                onAllow = { reason ->
                    result.onMain().success(reason)
                },
                onDontAllow = { reason ->
                    result.onMain().errors(reason.toString(), details = reason)
                },
                onApplicationError = { errorCode ->
                    result.onMain().errors(errorCode.toString(), details = errorCode)
                }
            )
        } ?: result.notImplemented()
    }

    private fun serverSideCheck(call: MethodCall, result: Result) {
        context?.let { context ->
            val checker = context.serverSideChecker(
                call.argument<String>("publicKey")
            )
            checker.serverSideCheck(object : ServerSideLicenseCheckerCallback {
                override fun response(reason: Int, rawData: String) {
                    result.onMain().success(object : HashMap<String?, Any?>() {
                        init {
                            put("reason", reason)
                            put("rawData", rawData)
                        }
                    })
                }

                override fun applicationError(errorCode: Int) {
                    result.onMain().errors(
                        errorCode.toString(),
                        errorCode.toString(),
                        details = errorCode
                    )
                }
            })
        } ?: result.notImplemented()
    }
}

fun LicenseChecker.checkAccess(
    onAllow: (Int) -> Unit = { _ -> },
    onDontAllow: (Int) -> Unit = { _ -> },
    onApplicationError: (Int) -> Unit = { _ -> }
) {
    checkAccess(object : LicenseCheckerCallback {
        override fun allow(reason: Int) {
            onAllow(reason)
        }

        override fun dontAllow(reason: Int) {
            onDontAllow(reason)
        }

        override fun applicationError(errorCode: Int) {
            onApplicationError(errorCode)
        }
    })
}

fun Result.onMain(): ResultOnMain {
    return if (this is ResultOnMain) {
        this
    } else {
        ResultOnMain(this)
    }
}

class ResultOnMain(private val result: Result) : Result {
    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun success(res: Any?) {
        handler.post { result.success(res) }
    }

    override fun error(
        errorCode: String, errorMessage: String?, errorDetails: Any?
    ) {
        handler.post { result.error(errorCode, errorMessage, errorDetails) }
    }

    override fun notImplemented() {
        handler.post { result.notImplemented() }
    }
}

fun <T> MethodCall.argumentOrNull(key: String): T? = try {
    argument(key)
} catch (e: Throwable) {
    null
}

fun <T> MethodCall.argumentsOrNull(): T? = arguments() as? T?

//fun <T> MethodCall.argument(key: String): T? = try { argument(key) } catch (e: Throwable) { null }
//fun <T> MethodCall.arguments(): T? = arguments() as? T?
//fun Result.success(result: Any? = null): Unit = success(result)
fun Result.success(): Unit = success(null) // avoid shadow
fun Result.errors(code: String, message: String? = null, details: Any? = null): Unit =
    error(code, message, details)

fun Result.error(e: Throwable): Unit = errors(e.cause.toString(), e.message, e.stackTrace)

val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        val max = 23
        return if (tag.length <= max) tag else tag.substring(0, max)
    }

/// ref. https://gist.github.com/fabiomsr/845664a9c7e92bafb6fb0ca70d4e44fd#gistcomment-2836766
val ByteArray.toHex
    inline get() = joinToString(separator = "") {
        String.format("%02x", (it.toInt() and 0xFF))
    }

val String.toHexByteArray
    inline get(): ByteArray? = try {
        chunked(2).map {
            it.toUpperCase().toInt(16).toByte()
        }.toByteArray()
    } catch (e: Throwable) {
        null
    }
