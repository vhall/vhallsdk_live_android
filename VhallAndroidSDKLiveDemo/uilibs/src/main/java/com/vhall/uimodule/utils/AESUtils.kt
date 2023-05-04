package com.vhall.uimodule.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AESUtils {
    companion object {
        //加密
        fun encrypt(inpute: String, passworld: String): String {
            //创建cipher对象
            val cipher = Cipher.getInstance("AES")
            //初始化cipher
            //通过秘钥工厂生产秘钥
            val keySpec: SecretKeySpec = SecretKeySpec(passworld.toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            //加密、解密
            val encrypt = cipher.doFinal(inpute.toByteArray())
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getEncoder().encode(encrypt).toString()
            } else {
                TODO("VERSION.SDK_INT < O")
            }
        }

        //解密
        fun decrypt(input: String, passworld: String): String {
            //创建cipher对象
            val cipher = Cipher.getInstance("AES")
            //初始化cipher
            //通过秘钥工厂生产秘钥
            val keySpec: SecretKeySpec = SecretKeySpec(passworld.toByteArray(), "AES")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            //加密、解密
            val encrypt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                cipher.doFinal(Base64.getDecoder().decode(input))
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            return String(encrypt)
        }
    }
}