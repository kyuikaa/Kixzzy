/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * AppDetectionService.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

@file:Suppress("DEPRECATION")

package com.my.kizzy.feature_rpc_base.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.IBinder
import com.blankj.utilcode.util.AppUtils
<<<<<<< HEAD
import com.my.kizzy.data.rpc.Constants
=======
>>>>>>> dev
import com.my.kizzy.data.rpc.KizzyRPC
import com.my.kizzy.data.rpc.RpcImage
import com.my.kizzy.domain.model.rpc.RpcButtons
import com.my.kizzy.feature_rpc_base.Constants
import com.my.kizzy.feature_rpc_base.setLargeIcon
import com.my.kizzy.preference.Prefs
import com.my.kizzy.resources.R
import dagger.hilt.android.AndroidEntryPoint
<<<<<<< HEAD
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*
=======
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.SortedMap
import java.util.TreeMap
>>>>>>> dev
import javax.inject.Inject

@AndroidEntryPoint
class AppDetectionService : Service() {

    @Inject
    lateinit var kizzyRPC: KizzyRPC

    @Inject
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var notificationBuilder: Notification.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var pendingIntent: PendingIntent

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
<<<<<<< HEAD
        if (intent?.action.equals(ACTION_STOP_SERVICE)) stopSelf()
        else {
            context = this
            notifset = false
            val apps = Prefs[Prefs.ENABLED_APPS, "[]"]
            val enabledPackages: ArrayList<String> = Json.decodeFromString(apps)
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Background Service which notifies the Current Running app"
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            val stopIntent = Intent(this, AppDetectionService::class.java)
            stopIntent.action = ACTION_STOP_SERVICE
            val pendingIntent: PendingIntent = PendingIntent.getService(this,
                0,stopIntent,PendingIntent.FLAG_IMMUTABLE)
            val rpcButtonsString = Prefs[Prefs.RPC_BUTTONS_DATA,"{}"]
            val rpcButtons = Json.decodeFromString<RpcButtons>(rpcButtonsString)
            scope.launch {
                while (isActive) {
                    val usageStatsManager =
                        (this@AppDetectionService).getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
                    val currentTimeMillis = System.currentTimeMillis()
                    val queryUsageStats = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        currentTimeMillis - 10000,
                        currentTimeMillis
                    )
                    if (queryUsageStats != null && queryUsageStats.size > 1) {
                        val treeMap: SortedMap<Long, UsageStats> = TreeMap()
                        for (usageStats in queryUsageStats) {
                            treeMap[usageStats.lastTimeUsed] = usageStats
                        }
                        if (!(treeMap.isEmpty() ||
                                    treeMap[treeMap.lastKey()]?.packageName == "com.my.kizzy" ||
                                    treeMap[treeMap.lastKey()]?.packageName == "com.discord")
                        ) {
                            val packageName = treeMap[treeMap.lastKey()]!!.packageName
                            Objects.requireNonNull(packageName)
                            if (enabledPackages.contains(packageName)) {
                                if (!kizzyRPC.isRpcRunning()) {
                                    kizzyRPC.apply {
                                        setName(AppUtils.getAppName(packageName))
                                        setStartTimestamps(System.currentTimeMillis())
                                        setStatus(Constants.DND)
                                        setLargeImage(RpcImage.ApplicationIcon(packageName, this@AppDetectionService))
                                        if (Prefs[Prefs.USE_RPC_BUTTONS,false]){
                                            with(rpcButtons){
                                                setButton1(button1.takeIf { it.isNotEmpty() })
                                                setButton1URL(button1Url.takeIf { it.isNotEmpty() })
                                                setButton2(button2.takeIf { it.isNotEmpty() })
                                                setButton2URL(button2Url.takeIf { it.isNotEmpty() })
                                            }
                                        }
                                        build()
                                    }
                                }
                                startForeground(
                                    1111,
                                    Notification.Builder(context, CHANNEL_ID)
                                        .setContentText(packageName)
                                        .setSmallIcon(R.drawable.ic_apps)
                                        .setContentTitle("Service enabled")
                                        .addAction(R.drawable.ic_apps,"Exit",pendingIntent)
                                        .build()
                                )
                                notifset = true
                            } else {
                                if (kizzyRPC.isRpcRunning()) {
                                    kizzyRPC.closeRPC()
                                }
                                notifset = false
                            }
                        }
                    }
                    if (!notifset) {
                        startForeground(
                            1111,
                            Notification.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_apps)
                                .setContentTitle("Service enabled")
                                .addAction(R.drawable.ic_apps,"Exit",pendingIntent)
                                .build()
                        )
                    }
                    delay(5000)
                }
            }
=======
        if (intent?.action == Constants.ACTION_STOP_SERVICE) {
            stopSelf()
        } else {
            handleAppDetection()
>>>>>>> dev
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        scope.cancel()
        kizzyRPC.closeRPC()
        super.onDestroy()
    }

    private fun handleAppDetection() {
        val enabledPackages = getEnabledPackages()

        val stopIntent = createStopIntent()
        pendingIntent = createPendingIntent(stopIntent)
        // Adding action to notification builder here to avoid having multiple Exit buttons
        // https://github.com/dead8309/Kizzy/issues/197
        notificationBuilder
            .setSmallIcon(R.drawable.ic_apps)
            .addAction(R.drawable.ic_apps, "Exit", pendingIntent)

        startForeground(Constants.NOTIFICATION_ID, createDefaultNotification())

        val rpcButtons = getRpcButtons()

        scope.launch {
            while (isActive) {
                val queryUsageStats = getUsageStats()

                if (queryUsageStats != null && queryUsageStats.size > 1) {
                    val packageName = getLatestPackageName(queryUsageStats)
                    if (packageName != null && packageName !in EXCLUDED_APPS) {
                        handleValidPackage(packageName, enabledPackages, rpcButtons)
                    }
                }
                delay(5000)
            }
        }
    }

    private fun getEnabledPackages(): List<String> {
        val apps = Prefs[Prefs.ENABLED_APPS, "[]"]
        return Json.decodeFromString(apps)
    }

    private fun getRpcButtons(): RpcButtons {
        val rpcButtonsString = Prefs[Prefs.RPC_BUTTONS_DATA, "{}"]
        return Json.decodeFromString(rpcButtonsString)
    }

    private fun getUsageStats(): List<UsageStats>? {
        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTimeMillis = System.currentTimeMillis()
        return usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            currentTimeMillis - 10000,
            currentTimeMillis
        )
    }

    private fun getLatestPackageName(usageStats: List<UsageStats>): String? {
        val treeMap: SortedMap<Long, UsageStats> = TreeMap()
        for (usageStatsItem in usageStats) {
            treeMap[usageStatsItem.lastTimeUsed] = usageStatsItem
        }
        return treeMap.lastKey()?.let { treeMap[it]?.packageName }
    }

    private suspend fun handleValidPackage(
        packageName: String,
        enabledPackages: List<String>,
        rpcButtons: RpcButtons
    ) {
        if (packageName in enabledPackages) {
            handleEnabledPackage(packageName, rpcButtons)
        } else {
            handleDisabledPackage()
        }
    }

    private suspend fun handleEnabledPackage(packageName: String, rpcButtons: RpcButtons) {
        if (!kizzyRPC.isRpcRunning()) {
            kizzyRPC.apply {
                setName(AppUtils.getAppName(packageName))
                setStartTimestamps(System.currentTimeMillis())
                setStatus(Prefs[Prefs.CUSTOM_ACTIVITY_STATUS,"dnd"])
                setLargeImage(RpcImage.ApplicationIcon(packageName, this@AppDetectionService))
                if (Prefs[Prefs.USE_RPC_BUTTONS, false]) {
                    with(rpcButtons) {
                        setButton1(button1.takeIf { it.isNotEmpty() })
                        setButton1URL(button1Url.takeIf { it.isNotEmpty() })
                        setButton2(button2.takeIf { it.isNotEmpty() })
                        setButton2URL(button2Url.takeIf { it.isNotEmpty() })
                    }
                }
                build()
            }
        }
        notificationManager.notify(
            Constants.NOTIFICATION_ID, notificationBuilder
                .setContentText(packageName)
                .setLargeIcon(
                    rpcImage = RpcImage.ApplicationIcon(packageName, this@AppDetectionService),
                    context = this@AppDetectionService
                )
                .build()
        )
    }

    private fun handleDisabledPackage() {
        if (kizzyRPC.isRpcRunning()) {
            kizzyRPC.closeRPC()
        }
        notificationManager.notify(Constants.NOTIFICATION_ID, createDefaultNotification())
    }

    private fun createDefaultNotification(): Notification {
        return Notification.Builder(this,Constants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_apps)
            .setContentTitle("Service enabled")
            .addAction(R.drawable.ic_apps, "Exit", pendingIntent)
            .build()
    }

    private fun createStopIntent(): Intent {
        val stopIntent = Intent(this, AppDetectionService::class.java)
        stopIntent.action = Constants.ACTION_STOP_SERVICE
        return stopIntent
    }

    private fun createPendingIntent(stopIntent: Intent): PendingIntent {
        return PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        val EXCLUDED_APPS = listOf("com.my.kizzy", "com.discord")
    }
}
