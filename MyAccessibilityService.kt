package com.weixintools.weixintools

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityEvent
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_auto_extract.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.timerTask


class MyAccessibilityService : AccessibilityService() {

    var obtainText = ""
    var receiveText = ""
    //var newMessage = false
    //var testLock = false
    //var testChatLock = false
    private var currentMsg = 0
    private var currentLike = 0

    var name = ""
    var realName = ""
    var id = ""
    var region = ""
    private var momentMenu = false
    private var momentList = false
    private var contactMenu = false
    private var contactData = false
    //var chatMode = false
    //var postMessage = true
    private var listViewIndexAuto = 0
    lateinit var info: AccessibilityServiceInfo
    private lateinit var accessibilityService:MyAccessibilityService
    private var autoChatLock = false
    private var processingLock = false
    private var loopingFunctionFirst = true
    private var autoLikeLock = false
    private var autoExtractLock = false

    companion object {
        @JvmStatic var autoChat: AutoChatActivity? = null
        @JvmStatic var autoLike: AutoLikeActivity? = null
        @JvmStatic var autoExtract: AutoExtractActivity? = null
        @JvmStatic var mainActivity: MainActivity? = null
    }

    override fun onServiceConnected() {
        Log.e("KEN", "onServiceConnected")

        //WechatUtils.utils = this

        accessibilityService = this

        info = serviceInfo
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if(autoExtract != null){
            if(autoExtract!!.autoExtractOn){
                if(info.notificationTimeout.toInt() != 0){
                    Log.e("KEN", "Change timeout 0")
                    info.notificationTimeout = 0
                }

                if(!autoExtractLock){
                    autoExtractLock = true
                    loopingFunctionExtract()
                }
            }
        }else if(mainActivity != null){
            if(mainActivity!!.autoChatOn){
                if(info.notificationTimeout.toInt() != 0){
                    Log.e("KEN", "Change timeout 1000")
                    info.notificationTimeout = 0
                }

                if(!autoChatLock){
                    autoChatLock = true
                    loopingFunction()
                }
            }else if(mainActivity!!.autoLikeOn){
                if(info.notificationTimeout.toInt() != 0){
                    Log.e("KEN", "Change timeout 0")
                    info.notificationTimeout = 0
                }

                if(!autoLikeLock){
                    autoLikeLock = true
                    loopingFunctionLike()
                }
            }
        }
    }

    private fun loopingFunctionExtract(){
        Log.e("KEN", "loopingFunctionExtract")

        while(autoExtractLock){

            if(!contactMenu && !contactData){
                try {
                    val accessibilityNodeInfo = accessibilityService.rootInActiveWindow ?: return

                    val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_MOMENTS_TEXT_ID)

                    Log.e("KEN", "findContactsText ${nodeInfoList.size}")
                    if(nodeInfoList.size > 0){
                        Log.e("KEN", "nodeInfoList.size > 0")
                        for(i in 0 until nodeInfoList.size){
                            if(nodeInfoList[i].text == "通讯录" || nodeInfoList[i].text == "Contacts" || nodeInfoList[i].text == "通訊錄"){
                                nodeInfoList[i].parent.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                contactMenu = true
                                Log.e("KEN", "Contact list click")
                                Thread.sleep(1000)
                            }
                        }
                    }
                }catch(e:Exception){
                    Log.e("KEN", "findMomentsText Exception: $e")
                }
            }

            if(contactMenu && !contactData){
                try {
                    val accessibilityNodeInfo = accessibilityService.rootInActiveWindow ?: return

                    val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_CONTACTUI_TEXTVIEW_NAME_ID)

                    Log.e("KEN", "findContactsNameTextView ${nodeInfoList.size}")
                    if(nodeInfoList.size > 0){
                        Log.e("KEN", "nodeInfoList.size > 0")
                        if(listViewIndexAuto < nodeInfoList.size){
                            Log.e("KEN", "listViewIndexAuto: $listViewIndexAuto")
                            name = nodeInfoList[listViewIndexAuto].text.toString()
                            Log.e("KEN", "findContactsNameTextView name: $name")
                            nodeInfoList[listViewIndexAuto].parent.parent.parent.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            contactData = true

                            Thread.sleep(500)
                        }else{

                            Log.e("KEN", "Reach bottom of list")
                            //check if reach bottom of listview
                            val nodeInfoList2 = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_CONTACTUI_TOTALLIST_ID)
                            if(nodeInfoList2.size > 0){
                                Log.e("KEN", "nodeInfoList2.size > 0")
                                Log.e("KEN", "Back to auto_chat_menu app")
                                if(nodeInfoList2[0].text.contains("位联系人") || nodeInfoList2[0].text.contains("contact") || nodeInfoList2[0].text.contains("位聯絡人")){
                                    Log.e("KEN", "REACH THE FINAL DESTINATION 111")

                                    contactMenu = false
                                    contactData = false
                                    autoExtractLock = false
                                    listViewIndexAuto = 0
                                    val notificationIntent = Intent(applicationContext, AutoExtractActivity::class.java)
                                    notificationIntent.action = Intent.ACTION_MAIN
                                    notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    applicationContext.startActivity(notificationIntent)
                                    Log.e("KEN", "REACH THE FINAL DESTINATION 222")

                                    //Thread.sleep(1000)
                                    autoExtract!!.autoExtractSwitch.isChecked = false
                                    autoExtract!!.autoExtractOn = false
                                }
                            }else{
                                val nodeInfoList3 = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_CONTACTUI_LISTVIEW_ID)

                                if(nodeInfoList3.size > 0){
                                    Log.e("KEN", "nodeInfoList3.size > 0")
                                    Log.e("KEN", "perform Scroll")

                                    listViewIndexAuto = 1
                                    nodeInfoList3[0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                                    Thread.sleep(1000)
                                }
                            }
                        }
                    }
                }catch(e:Exception){
                    Log.e("KEN", "findMomentsText Exception: $e")
                }
            }

            if(contactMenu && contactData){

                Log.e("KEN", "Retrieve Data Portion")

                val accessibilityNodeInfo = accessibilityService.rootInActiveWindow ?: return

                try {

                    val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_CONTACTUI_TEXTVIEW_WECHATID_ID)

                    Log.e("KEN", "findContactsDetailsTextView ${nodeInfoList.size}")
                    if(nodeInfoList.size > 0){
                        Log.e("KEN", "nodeInfoList.size > 0")
                        id = nodeInfoList[0].text.toString().removePrefix("微信号:  ").removePrefix("WeChat ID:  ").removePrefix("WeChat ID：  ")
                        Log.e("KEN", "findContactsDetailsTextView id: $id")
                    }

                }catch(e:Exception){
                    Log.e("KEN", "findContactsDetailsTextView id Exception: $e")
                }

                try {
                    val nodeInfoList2 = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_CONTACTUI_TEXTVIEW_REGION_ID)

                    if(nodeInfoList2.size > 0){
                        Log.e("KEN", "nodeInfoList2.size > 0")
                        region = nodeInfoList2[0].text.toString().removePrefix("地区:  ").removePrefix("Region:  ").removePrefix("地區：     ")
                        Log.e("KEN", "findContactsDetailsTextView region: $region")
                    }
                }catch (e:Exception){
                    Log.e("KEN", "findContactsDetailsTextView region Exception: $e")
                }

                try {
                    val nodeInfoList3 = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_CONTACTUI_TEXTVIEW_REALNAME_ID)

                    if(nodeInfoList3.size > 0){
                        Log.e("KEN", "nodeInfoList3.size > 0")
                        realName = nodeInfoList3[0].text.toString().removePrefix("昵称:  ").removePrefix("Name:  ").removePrefix("暱稱：     ")
                        Log.e("KEN", "findContactsDetailsTextView realName: $realName")
                    }
                }catch (e:Exception){
                    Log.e("KEN", "findContactsDetailsTextView realName Exception: $e")
                }

                Log.e("KEN", "\nName: $name\nID: $id\nReal Name: $realName\nRegion: $region")
                if(id.isNotEmpty() && id != "filehelper")
                    autoExtract!!.saveData(name, id, realName, region)
                name = ""
                id = ""
                realName = ""
                region = ""
                listViewIndexAuto++

                try {
                    val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_CONTACTUI_BACK_ID)
                    Log.e("KEN", "findBackLinearLayout ${nodeInfoList.size}")
                    if(nodeInfoList.size > 0){
                        Log.e("KEN", "nodeInfoList.size > 0")
                        nodeInfoList[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        contactData = false

                        Thread.sleep(500)
                    }
                }catch(e:Exception){
                    Log.e("KEN", "findContactsDetailsTextView id Exception: $e")
                }

            }
        }
    }

    private fun loopingFunctionLike(){
        Log.e("KEN", "loopingFunctionLike")

        while(currentLike < mainActivity!!.totalLike){

            if(!momentMenu && !momentList){
                try {
                    val accessibilityNodeInfo = accessibilityService.rootInActiveWindow ?: return

                    val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_MOMENTS_TEXT_ID)

                    Log.e("KEN", "findMomentsText ${nodeInfoList.size}")
                    if(nodeInfoList.size > 0){
                        Log.e("KEN", "nodeInfoList.size > 0")
                        for(i in 0 until nodeInfoList.size){
                            if(nodeInfoList[i].text == "发现" || nodeInfoList[i].text == "Discover" || nodeInfoList[i].text == "發現"){
                                nodeInfoList[i].parent.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                momentMenu = true
                                Thread.sleep(1000)
                            }
                        }
                    }
                }catch(e:Exception){
                    Log.e("KEN", "findMomentsText Exception: $e")
                }
            }

            if(momentMenu && !momentList){

                try {
                    val accessibilityNodeInfo = accessibilityService.rootInActiveWindow ?: return

                    val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_MOMENTS_LISTVIEW_LINEARLAYOUT_ID)

                    Log.e("KEN", "findMomentsListView ${nodeInfoList.size}")
                    if(nodeInfoList.size > 0){
                        Log.e("KEN", "nodeInfoList.size > 0")
                        nodeInfoList[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        momentList = true
                        Thread.sleep(1000)
                    }
                }catch(e:Exception){
                    Log.e("KEN", "findMomentsDot Exception: $e")
                }
            }

            if(momentMenu && momentList){
                try {
                    val randomSwipe = (1..5).shuffled().first()

                    Log.e("KEN", "randomSwipe Number: $randomSwipe")

                    for(i in 1..randomSwipe){
                        Thread.sleep(2500)
                        Log.e("KEN", "randomSwipe i: $i")

                        val accessibilityNodeInfo = accessibilityService.rootInActiveWindow ?: return

                        val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_MOMENTS_POSTING_LISTVIEW_ID)

                        try {
                            Log.e("KEN", "findMomentListView ${nodeInfoList.size}")
                            if(nodeInfoList.size > 0){
                                Log.e("KEN", "nodeInfoList.size > 0")
                                val random = Random()
                                val randomUpDown = random.nextBoolean()
                                Log.e("KEN", "randomUpDown: $randomUpDown")
                                if(randomUpDown){
                                    Log.e("KEN", "SWIPE UP")
                                    nodeInfoList[0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                                }
                                else{
                                    Log.e("KEN", "SWIPE DOWN")
                                    nodeInfoList[0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
                                }
                            }
                        }catch(e:Exception){
                            Log.e("KEN", "findMomentListView Exception: $e")
                        }
                    }

                    try {
                        val accessibilityNodeInfo = accessibilityService.rootInActiveWindow ?: return

                        val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_MOMENTS_POSTING_IMAGEVIEW_ID)

                        Log.e("KEN", "findMomentsImageView ${nodeInfoList.size}")
                        if(nodeInfoList.size > 0){
                            Log.e("KEN", "nodeInfoList.size > 0")
                            val posting = (0 until nodeInfoList.size).shuffled().first()
                            Log.e("KEN", "posting: $posting")
                            nodeInfoList[posting].performAction(AccessibilityNodeInfo.ACTION_CLICK)

                            Thread.sleep(500)

                            val nodeInfoList2 = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_MOMENTS_POSTING_TEXTVIEW_ID)

                            Log.e("KEN", "findMomentsImageView if nodeInfoList2 found size: ${nodeInfoList2.size}")
                            if(nodeInfoList2.size > 0){
                                Log.e("KEN", "nodeInfoList2.size > 0")
                                Log.e("KEN", "nodeInfoList2.text: ${nodeInfoList2[0].text}")
                                if(nodeInfoList2[0].text == "赞" || nodeInfoList2[0].text == "Like" || nodeInfoList2[0].text == "讚"){
                                    Log.e("KEN", "nodeInfoList2 赞")

                                    Thread.sleep(500)
                                    val nodeInfoList3 = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_MOMENTS_POSTING_LINEARLAYOUT_ID)

                                    if(nodeInfoList3.size > 0){
                                        Log.e("KEN", "nodeInfoList3.size > 0")
                                        Thread.sleep(500)
                                        nodeInfoList3[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                        currentLike++
                                    }
                                }else{
                                    Thread.sleep(500)
                                    nodeInfoList[posting].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                }
                            }
                        }
                    }catch(e:Exception){
                        Log.e("KEN", "findMomentsImageView Exception: $e")
                    }

                }catch(e:Exception){
                    Log.e("KEN", "moments3 Exception: $e")
                }
            }
        }

        Log.e("KEN", "loopingFunctionLike totalLike reached")
        accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)

        Thread.sleep(1000)

        momentMenu = false
        momentList = false
        currentLike = 0
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        notificationIntent.action = Intent.ACTION_MAIN
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(notificationIntent)
        autoLikeLock = false
        mainActivity!!.autoLikeSwitch.isChecked = false
        mainActivity!!.autoLikeOn = false
        mainActivity!!.popUp()

    }

    private fun loopingFunction(){
        Log.e("KEN", "loopingFunction")

        while(currentMsg < mainActivity!!.totalMsg){
            Log.e("KEN", "INSIDE While")

            if(!processingLock){
                Log.e("KEN", "INSIDE PROCESSING LOCK")

                if(!loopingFunctionFirst){
                    val delay:Long = (mainActivity!!.minTimeMain..mainActivity!!.maxTimeMain).shuffled().first().toLong()

                    Log.e("KEN", "delay: $delay")
                    Log.e("KEN", "Threading waiting period start")
                    Thread.sleep(delay)
                    Log.e("KEN", "Threading waiting period end")
                }else{
                    try {
                        val accessibilityNodeInfo = accessibilityService.rootInActiveWindow ?: return

                        val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_MOMENTS_TEXT_ID)

                        Log.e("KEN", "findContactsText ${nodeInfoList.size}")
                        if(nodeInfoList.size > 0){
                            Log.e("KEN", "nodeInfoList.size > 0")
                            for(i in 0 until nodeInfoList.size){
                                if(nodeInfoList[i].text == "微信" || nodeInfoList[i].text == "Chats" || nodeInfoList[i].text == "聊天"){
                                    nodeInfoList[i].parent.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                    contactMenu = true
                                    Log.e("KEN", "Contact list click")
                                    Thread.sleep(1000)
                                }
                            }
                        }
                    }catch(e:Exception){
                        Log.e("KEN", "findMomentsText Exception: $e")
                    }

                    loopingFunctionFirst = false
                }

                val accessibilityNodeInfo = accessibilityService.rootInActiveWindow ?: return

                val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_LISTVIEW_LINEARLAYOUT_ID)

                try {
                    for(i in 0 until nodeInfoList.size){

                        var nodeInfoList2 = nodeInfoList[i].findAccessibilityNodeInfosByViewId(Constant.WECHATID_LISTVIEW_DOT_ID)

                        if(nodeInfoList2.size == 0)
                            nodeInfoList2 = nodeInfoList[i].findAccessibilityNodeInfosByViewId(Constant.WECHATID_LISTVIEW_SPECIAL_DOT_ID)

                        if(nodeInfoList2.size > 0){
                            val nodeInfoList3 = nodeInfoList[i].findAccessibilityNodeInfosByViewId(Constant.WECHATID_LISTVIEW_MESSAGE_ID)
                            Log.e("KEN","nodeInfoList3[0].text: ${nodeInfoList3[0].text}")

                            obtainText = nodeInfoList3[0].text.toString()
                            Thread.sleep(1000)
                            nodeInfoList[i].performAction(AccessibilityNodeInfo.ACTION_CLICK)

                            Log.e("KEN", "LOCKING PROCESS")
                            processingLock = true
                            TuLing(this).execute()
                        }
                    }
                }catch(e:Exception){
                    Log.e("KEN", "findAllListViewTest Exception: $e")
                }
            }else{
                Thread.sleep(5000)
            }
        }

        if(!processingLock){
            Log.e("KEN", "loopingFunction totalMsg reached")
            currentMsg = 0
            val notificationIntent = Intent(applicationContext, MainActivity::class.java)
            notificationIntent.action = Intent.ACTION_MAIN
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(notificationIntent)
            autoChatLock = false
            loopingFunctionFirst = true
            processingLock = false
            mainActivity!!.autoChatSwitch.isChecked = false
            mainActivity!!.autoChatOn = false
            mainActivity!!.popUp()
        }else{
            Thread.sleep(5000)
            loopingFunction()
        }


    }

    private fun loopingFunction2(){
        Log.e("KEN", "loopingFunction2")

        val accessibilityNodeInfo = accessibilityService.rootInActiveWindow ?: return

        try {
            Log.e("KEN", "findEditText try")

            val nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_CHATUI_EDITTEXT_ID)

            if(nodeInfoList != null && nodeInfoList.isNotEmpty()){
                Log.e("KEN", "findEditText try if")
                val arguments = Bundle()
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, receiveText)
                nodeInfoList[0].performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

                val sleep:Long = (mainActivity!!.minTime..mainActivity!!.maxTime).shuffled().first().toLong()

                Log.e("KEN", "top sleep: $sleep")

                Thread.sleep(sleep)

                val nodeInfoList2 = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_CHATUI_SEND_ID)

                nodeInfoList2[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)

                currentMsg++

            }
        }catch (e:Exception){
            Log.e("KEN", "Exception 1: $e")
        }

        receiveText = ""
        obtainText = ""

        try {

            val sleep:Long = (mainActivity!!.minTime..mainActivity!!.maxTime).shuffled().first().toLong()

            Log.e("KEN", "bottom sleep: $sleep")

            Thread.sleep(sleep)

            val nodeInfoList2 = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_CHATUI_BACK_ID)

            Log.e("KEN", "BACK ICON")
            if(nodeInfoList2.size > 0){
                nodeInfoList2[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                processingLock = false
                Log.e("KEN", "BACK ICON if")
            }
            else{
                Log.e("KEN", "BACK ICON else")
                val nodeInfoList3 = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(Constant.WECHATID_NEWSUI_BACK_ID)
                Log.e("KEN", "BACK ICON else nodeInfoList3.size: ${nodeInfoList3.size}")
                processingLock = if(nodeInfoList3.size > 0){
                    nodeInfoList3[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    false
                }else{
                    false
                }
            }
        }catch (e:Exception){
            processingLock = false
            Log.e("KEN", "Exception 2: $e")
        }
    }

    private class TuLing internal constructor(activityReference: MyAccessibilityService) : AsyncTask<String, Void, String>() {

        private val activityReference: WeakReference<MyAccessibilityService> = WeakReference(activityReference)

        override fun doInBackground(vararg params: String): String {

            Log.e("KEN", "doInBackground")
            try {
                return URL(mainActivity!!.url)
                    .openConnection()
                    .let {
                        it as HttpURLConnection
                    }.apply {
                        setRequestProperty("Content-Type", "application/json; charset=utf-8")
                        requestMethod = "POST"

                        doOutput = true
                        val outputWriter = OutputStreamWriter(outputStream)
                        val jsonParam = JSONObject()
                        jsonParam.put("key", mainActivity!!.key)
                        jsonParam.put("info", activityReference.get()!!.obtainText)
                        jsonParam.put("userid", "1")

                        outputWriter.write(jsonParam.toString())
                        outputWriter.flush()
                        Log.e("KEN", "Breakpoint 1")
                    }.let {
                        Log.e("KEN", "Breakpoint 2")
                        if (it.responseCode == 200) it.inputStream else it.errorStream
                    }.let { streamToRead ->
                        BufferedReader(InputStreamReader(streamToRead)).use {
                            val response = StringBuffer()

                            var inputLine = it.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = it.readLine()
                            }
                            val resultJson = JSONObject(response.toString())
                            if(resultJson.has("text"))
                                activityReference.get()!!.receiveText = resultJson.getString("text")
                            it.close()
                            Log.e("KEN", "Breakpoint 3")
                            if(activityReference.get()!!.receiveText.isNotEmpty()){Log.e("KEN", "Going into function"); activityReference.get()!!.loopingFunction2()}
                            response.toString()

                        }
                    }

            }catch (e:Exception){
                Log.e("KEN", "TuLing Exception: $e")
                activityReference.get()!!.reconnect()
            }
            return "Executed"
        }

        /*override fun onPostExecute(result: String) {
            Log.e("KEN", "TuLing onPostExecute")
            if(activityReference.get()!!.receiveText.isNotEmpty())
                activityReference.get()!!.loopingFunction2()
                //WechatUtils.findEditTextTest(activityReference.get()!!, Constant.WECHATID_CHATUI_EDITTEXT_ID, mainActivity!!.minTime, mainActivity!!.maxTime)
        }*/
    }

    fun reconnect(){
        val timer = Timer()
        timer.schedule(timerTask { TuLing(accessibilityService).execute() }, 5000)
    }

    override fun onInterrupt() {

    }
}