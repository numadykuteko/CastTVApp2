package com.cast.tv.screen.mirroring.iptv.utils.chromecast

import android.content.Context
import android.net.wifi.WifiManager
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IpUtils {
    companion object {
        fun findIPAddress(context: Context): String? {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            try {
                return if (wifiManager.connectionInfo != null) {
                    val wifiInfo = wifiManager.connectionInfo
                    InetAddress.getByAddress(
                            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                                    .putInt(wifiInfo.ipAddress)
                                    .array()
                    ).hostAddress
                } else
                    null
            } catch (e: Exception) {

            }
            return null
        }
    }
}