package com.folioreader.util

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.folioreader.Config
import com.folioreader.util.SharedPreferenceUtil.getSharedPreferencesString
import org.json.JSONException
import org.json.JSONObject
import java.net.ServerSocket
import java.net.URLConnection
import java.util.*

/**
 * Created by mahavir on 5/7/16.
 */
class AppUtil {

    companion object {

        private val LOG_TAG = AppUtil::class.java.simpleName

        @JvmStatic
        fun charsetNameForURLConnection(connection: URLConnection): String {
            val contentType = connection.contentType
            val values = contentType.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var charset: String? = null

            for (_value in values) {
                val value = _value.trim { it <= ' ' }

                if (value.toLowerCase(Locale.ROOT).startsWith("charset=")) {
                    charset = value.substring("charset=".length)
                    break
                }
            }

            if (charset == null || charset.isEmpty()) {
                charset = "UTF-8" //Assumption
            }

            return charset
        }

        fun saveConfig(context: Context?, config: Config) {
            val obj = JSONObject()
            try {
                obj.put(Config.CONFIG_FONT, config.font)
                obj.put(Config.CONFIG_FONT_SIZE, config.fontSize)
                obj.put(Config.CONFIG_FONT_LINE_SPACE, config.fontLineSpace)
                obj.put(Config.CONFIG_FONT_WHITE_SPACE, config.fontWhiteSpace)
                obj.put(Config.CONFIG_IS_ALIGNMENT, config.alignment)
                obj.put(Config.CONFIG_IS_PAGE, config.pageType)
                obj.put(Config.CONFIG_IS_THEME, config.currentTheme)
                obj.put(Config.CONFIG_IS_NIGHT_MODE, config.isNightMode)
                obj.put(Config.CONFIG_THEME_COLOR_INT, config.themeColor)
                obj.put(Config.CONFIG_ALLOWED_DIRECTION, config.allowedDirection.toString())
                obj.put(Config.CONFIG_DIRECTION, config.direction.toString())
                SharedPreferenceUtil.putSharedPreferencesString(
                    context, Config.INTENT_CONFIG,
                    obj.toString()
                )
            } catch (e: JSONException) {
                Log.e(LOG_TAG, e.message)
            }
        }

        @JvmStatic
        fun getSavedConfig(context: Context?): Config? {
            val json = getSharedPreferencesString(context, Config.INTENT_CONFIG, null)
            if (json != null) {
                return try {
                    val jsonObject = JSONObject(json)
                    Config(jsonObject)
                } catch (e: JSONException) {
                    Log.e(LOG_TAG, e.message)
                    null
                }
            } else {
                Config()
            }
            return null
        }

        fun hideKeyboard(activity: Activity) {

            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token
            if (view == null)
                view = View(activity)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }

        fun getAvailablePortNumber(portNumber: Int): Int {

            var serverSocket: ServerSocket? = null
            var portNumberAvailable: Int

            try {
                serverSocket = ServerSocket(portNumber)
                Log.d(LOG_TAG, "-> getAvailablePortNumber -> portNumber $portNumber available")
                portNumberAvailable = portNumber
            } catch (e: Exception) {
                serverSocket = ServerSocket(0)
                portNumberAvailable = serverSocket.localPort
                Log.w(
                    LOG_TAG, "-> getAvailablePortNumber -> portNumber $portNumber not available, " +
                            "$portNumberAvailable is available"
                )
            } finally {
                serverSocket?.close()
            }

            return portNumberAvailable
        }
    }
}







