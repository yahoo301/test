package com.weixintools.weixintools

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_auto_chat.*

class AutoChatActivity : AppCompatActivity() {

    private var minTimeMain = 20000
    private var maxTimeMain = 60000
    private var minTime = 3000
    private var maxTime = 5000
    private var totalMsg = 20
    private var url = "http://www.tuling123.com/openapi/api"
    private var key = "e305b5c621114ef89a61d73f24947a6f"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_chat)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        MyAccessibilityService.autoChat = this

        val sharedPreference =  getSharedPreferences("WeiXinTools", Context.MODE_PRIVATE)
        totalMsg = sharedPreference.getInt("totalMessage",20)
        autoSendAmount.setText(totalMsg.toString())
        minTime = sharedPreference.getInt("minTime",3000)
        timeMin.setText((minTime/1000).toString())
        maxTime = sharedPreference.getInt("maxTime",5000)
        timeMax.setText((maxTime/1000).toString())
        minTimeMain = sharedPreference.getInt("minTimeMain",20000)
        timeMinMain.setText((minTimeMain/1000).toString())
        maxTimeMain = sharedPreference.getInt("maxTimeMain",60000)
        timeMaxMain.setText((maxTimeMain/1000).toString())
        url = sharedPreference.getString("url","http://www.tuling123.com/openapi/api")!!
        autoURL.setText(url)
        key = sharedPreference.getString("key","e305b5c621114ef89a61d73f24947a6f")!!
        autoKey.setText(key)
    }

    override fun onSupportNavigateUp(): Boolean {
        MyAccessibilityService.autoChat = null
        finish()
        return true
    }

    override fun onBackPressed() {

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.auto_chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.autoChat_save -> {
                val sharedPreference =  getSharedPreferences("WeiXinTools", Context.MODE_PRIVATE)
                val editor = sharedPreference.edit()

                if(!autoSendAmount.text.isNullOrEmpty()){
                    totalMsg = autoSendAmount.text.toString().toInt()
                    editor.putInt("totalMessage", totalMsg)
                }

                if(!timeMinMain.text.isNullOrEmpty()){
                    minTimeMain = timeMinMain.text.toString().toInt() * 1000
                    editor.putInt("minTimeMain", minTimeMain)
                }

                if(!timeMaxMain.text.isNullOrEmpty()){
                    maxTimeMain = timeMaxMain.text.toString().toInt() * 1000
                    editor.putInt("maxTimeMain", maxTimeMain)
                }

                if(minTimeMain > maxTimeMain){
                    val temp = minTimeMain
                    minTimeMain = maxTimeMain
                    maxTimeMain = temp
                    editor.putInt("minTimeMain", minTimeMain)
                    editor.putInt("maxTimeMain", maxTimeMain)
                }

                if(!timeMin.text.isNullOrEmpty()){
                    minTime = timeMin.text.toString().toInt() * 1000
                    editor.putInt("minTime", minTime)
                }

                if(!timeMax.text.isNullOrEmpty()){
                    maxTime = timeMax.text.toString().toInt() * 1000
                    editor.putInt("maxTime", maxTime)
                }

                if(minTime > maxTime){
                    val temp = minTime
                    minTime = maxTime
                    maxTime = temp
                    editor.putInt("minTime", minTime)
                    editor.putInt("maxTime", maxTime)
                }

                if(!autoURL.text.isNullOrEmpty()){
                    url = autoURL.text.toString()
                    editor.putString("url", url)
                }

                if(!autoKey.text.isNullOrEmpty()){
                    key = autoKey.text.toString()
                    editor.putString("key", key)
                }

                editor.apply()
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
