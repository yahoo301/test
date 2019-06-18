package com.weixintools.weixintools

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.view.View
import android.content.Intent
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity() {

    var autoLikeOn = false
    var totalLike = 5

    var autoChatOn = false
    var minTimeMain = 20000
    var maxTimeMain = 60000
    var minTime = 3000
    var maxTime = 5000
    var totalMsg = 20
    var url = "http://www.tuling123.com/openapi/api"
    var key = "e305b5c621114ef89a61d73f24947a6f"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MyAccessibilityService.mainActivity = this

        if(!isAccessibilitySettingsOn(applicationContext))
            startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))

        val sharedPreference =  getSharedPreferences("WeiXinTools", Context.MODE_PRIVATE)
        totalLike = sharedPreference.getInt("totalLike",5)

        autoLikeSwitch.setOnClickListener {
            autoLikeOn = autoLikeSwitch.isChecked

            if(autoLikeSwitch.isChecked){
                try {
                    val pm = applicationContext.packageManager
                    val intent:Intent? = pm.getLaunchIntentForPackage("com.tencent.mm")
                    intent?.addCategory(Intent.CATEGORY_LAUNCHER)
                    applicationContext.startActivity(intent)
                }catch(e: Exception){
                    Toast.makeText(this, "未安装微信", Toast.LENGTH_SHORT).show()
                }
            }
        }

        totalMsg = sharedPreference.getInt("totalMessage",20)
        minTimeMain = sharedPreference.getInt("minTimeMain",20000)
        maxTimeMain = sharedPreference.getInt("maxTimeMain",60000)
        minTime = sharedPreference.getInt("minTime",3000)
        maxTime = sharedPreference.getInt("maxTime",5000)
        url = sharedPreference.getString("url","http://www.tuling123.com/openapi/api")!!
        key = sharedPreference.getString("key","e305b5c621114ef89a61d73f24947a6f")!!

        autoChatSwitch.setOnClickListener {
            autoChatOn = autoChatSwitch.isChecked

            if(autoChatSwitch.isChecked){
                try {
                    val pm = applicationContext.packageManager
                    val intent:Intent? = pm.getLaunchIntentForPackage("com.tencent.mm")
                    intent?.addCategory(Intent.CATEGORY_LAUNCHER)
                    applicationContext.startActivity(intent)
                }catch(e: Exception){
                    Toast.makeText(this, "未安装微信", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isAccessibilitySettingsOn(mContext:Context):Boolean{
        var accessibilityEnabled = 0
        val service = packageName + "/" + MyAccessibilityService::class.java.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                mContext.applicationContext.contentResolver,
                android.provider.Settings.Secure.ACCESSIBILITY_ENABLED)
            Log.v("KEN", "accessibilityEnabled = $accessibilityEnabled")
        } catch (e: Settings.SettingNotFoundException ) {
            Log.e("KEN", "Error finding setting, default accessibility to not found: "
                    + e.message)
        }
        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')

        if (accessibilityEnabled == 1) {
            Log.v("KEN", "***ACCESSIBILITY IS ENABLED*** -----------------")
            val settingValue = Settings.Secure.getString(
                mContext.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()

                    Log.v("KEN", "-------------- > accessibilityService :: $accessibilityService $service")
                    if (accessibilityService.equals(service, true)) {
                        Log.v("KEN", "We've found the correct setting - accessibility is switched on!")
                        return true
                    }
                }
            }
        } else {
            Log.v("KEN", "***ACCESSIBILITY IS DISABLED***")
        }

        return false
    }

    fun autoChat(view: View){
        if(isAccessibilitySettingsOn(applicationContext)){
            startActivity(Intent(this, AutoChatActivity::class.java))
        }else{
            Toast.makeText(this, "打开无障碍服务", Toast.LENGTH_SHORT).show()
        }
    }

    fun autoLike(view: View){
        if(isAccessibilitySettingsOn(applicationContext)){
            startActivity(Intent(this, AutoLikeActivity::class.java))
        }else{
            Toast.makeText(this, "打开无障碍服务", Toast.LENGTH_SHORT).show()
        }
    }

    fun autoExtract(view: View){
        if(isAccessibilitySettingsOn(applicationContext)){
            startActivity(Intent(this, AutoExtractActivity::class.java))
        }else{
            Toast.makeText(this, "打开无障碍服务", Toast.LENGTH_SHORT).show()
        }
    }

    fun popUp(){
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("通知")
        builder.setMessage("任务完成")
        builder.setPositiveButton("OK"){ dialog, _ -> dialog.dismiss() }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun onBackPressed() {

    }

    override fun onResume() {
        super.onResume()
        Log.e("KEN", "onResume")
        val sharedPreference =  getSharedPreferences("WeiXinTools", Context.MODE_PRIVATE)
        totalLike = sharedPreference.getInt("totalLike",5)
        totalMsg = sharedPreference.getInt("totalMessage",20)
        minTimeMain = sharedPreference.getInt("minTimeMain",20000)
        maxTimeMain = sharedPreference.getInt("maxTimeMain",60000)
        minTime = sharedPreference.getInt("minTime",3000)
        maxTime = sharedPreference.getInt("maxTime",5000)
        url = sharedPreference.getString("url","http://www.tuling123.com/openapi/api")!!
        key = sharedPreference.getString("key","e305b5c621114ef89a61d73f24947a6f")!!
        autoChatOn = false
        autoLikeOn = false
        autoChatSwitch.isChecked = false
        autoLikeSwitch.isChecked = false
    }
}
