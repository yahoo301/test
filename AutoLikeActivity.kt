package com.weixintools.weixintools

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_auto_like.*

class AutoLikeActivity : AppCompatActivity() {

    private var totalLike = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_like)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        MyAccessibilityService.autoLike = this

        val sharedPreference =  getSharedPreferences("WeiXinTools", Context.MODE_PRIVATE)
        totalLike = sharedPreference.getInt("totalLike",5)
        autoLikeAmount.setText(totalLike.toString())
    }

    override fun onSupportNavigateUp(): Boolean {
        MyAccessibilityService.autoLike = null
        finish()
        return true
    }

    override fun onBackPressed() {

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.auto_like_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.autoLike_save -> {
                val sharedPreference =  getSharedPreferences("WeiXinTools", Context.MODE_PRIVATE)
                val editor = sharedPreference.edit()
                if(!autoLikeAmount.text.isNullOrEmpty()){
                    totalLike = autoLikeAmount.text.toString().toInt()
                    editor.putInt("totalLike", totalLike)
                }
                editor.apply()
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
