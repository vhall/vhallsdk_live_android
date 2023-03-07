package com.vhall.uimodule.dao

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*


/**
 * @author hkl
 *Date: 2022/12/5 13:56
 */

val Context.userStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class UserDataStore {
    companion object {
        private val accountKey = stringPreferencesKey("account")
        private val passwordKey = stringPreferencesKey("password")
        private val thirdIdKey = stringPreferencesKey("thirdId")
        private val thirdNameKey = stringPreferencesKey("thirdName")
        private val thirdPicKey = stringPreferencesKey("thirdPicName")
        private val appKey = stringPreferencesKey("appKey")
        private val appSecretKey = stringPreferencesKey("appSecretKey")
        private val watchIdKey = stringPreferencesKey("watchId")
        private val watchKKey = stringPreferencesKey("watchK")
        private val watchKIDKey = stringPreferencesKey("watchKId")

        suspend fun saveAccount(account: String, password: String, context: Context) {
            context.userStore.edit { setting ->
                setting[accountKey] = account
                setting[passwordKey] = password
            }
        }

        suspend fun saveThirdId(
            thirdId: String,
            thirdName: String,
            thirdPic: String,
            context: Context
        ) {
            context.userStore.edit { setting ->
                setting[thirdIdKey] = thirdId
                setting[thirdNameKey] = thirdName
                setting[thirdPicKey] = thirdPic
            }
        }

        suspend fun saveToAppKey(app: String, appSecret: String, context: Context) {
            context.userStore.edit { setting ->
                setting[appKey] = app
                setting[appSecretKey] = appSecret
            }
        }

        suspend fun saveWatch(watchId: String, watchK: String, watchKId: String, context: Context) {
            context.userStore.edit { setting ->
                setting[watchIdKey] = watchId
                setting[watchKKey] = watchK
                setting[watchKIDKey] = watchKId
            }
        }

        suspend fun getValue(
            context: Context,
            stringPreferencesKey: Preferences.Key<String>,
            def: String = ""
        ): String {
            val nameFlow: Flow<String> = context.userStore.data.map { settings ->
                settings[stringPreferencesKey] ?: def
            }
            return nameFlow.first()
        }


        suspend fun getAccount(context: Context): String {
            return getValue(context, accountKey)
        }

        suspend fun getPassword(context: Context): String {
            return getValue(context, passwordKey)
        }

        suspend fun getThirdId(context: Context): String {
            return getValue(context, thirdIdKey)
        }

        suspend fun getThirdName(context: Context): String {
            return getValue(context, thirdNameKey)
        }

        suspend fun getThirdPic(context: Context): String {
            return getValue(
                context,
                thirdPicKey,
                "https://t-alistatic01.e.vhall.com/upload/users/face-imgs/67/cf/67cf18a4250bc48ec9d1eb3ed82b741d.gif"
            )
        }

        suspend fun getAppKey(context: Context): String {
            return getValue(context, appKey)
        }

        suspend fun getAppSecret(context: Context): String {
            return getValue(context, appSecretKey)
        }

        suspend fun getWatchId(context: Context): String {
            return getValue(context, watchIdKey)
        }

        suspend fun getWatchK(context: Context): String {
            return getValue(context, watchKKey)
        }

        suspend fun getWatchKId(context: Context): String {
            return getValue(context, watchKIDKey)
        }
    }

}