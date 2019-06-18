package com.weixintools.weixintools

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_auto_extract.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

@Suppress("UNUSED_PARAMETER")
@SuppressLint("ObsoleteSdkInt")
class AutoExtractActivity : AppCompatActivity() {

    private lateinit var lvItemAdapter:LVAdapter
    var autoExtractOn = false

    companion object {
        @JvmStatic private var details:MutableList<DetailBean> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_extract)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        MyAccessibilityService.autoExtract = this

        lvItemAdapter = LVAdapter(this)
        lvItemAdapter.addItem(details)
        listViewAuto.adapter = lvItemAdapter

        autoExtractSwitch.setOnClickListener {
            Log.e("KEN", "autoExtractSwitch setOnClickListener")
            autoExtractOn = autoExtractSwitch.isChecked

            if(autoExtractSwitch.isChecked){
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

        checkNewestFile()
    }

    fun checkNewestFile(){
        val sharedPreference =  getSharedPreferences("WeiXinTools", Context.MODE_PRIVATE)
        if(!sharedPreference.getString("latestFile","").isNullOrEmpty()){
            openFileAuto.visibility = View.VISIBLE
            openFileAuto.text = getString(R.string.auto_extract_path, sharedPreference.getString("latestFile",""))
        }
    }

    fun openFileAuto(view: View){
        val intent = Intent(Intent.ACTION_VIEW)
        val sharedPreference =  getSharedPreferences("WeiXinTools",Context.MODE_PRIVATE)
        if(!sharedPreference.getString("latestFile","").isNullOrEmpty()){
            val file = File(sharedPreference.getString("latestFile",""))
            if(file.exists()){
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                        intent.setDataAndType(FileProvider.getUriForFile(applicationContext, applicationContext.packageName, file), "application/vnd.ms-excel")
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        startActivity(intent)
                    }else{
                        intent.setDataAndType(Uri.fromFile(file),"application/vnd.ms-excel")
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        startActivity(intent)
                    }
                }catch(e:Exception){

                }
            }else{
                Toast.makeText(this, "文件未找到", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        details.clear()
        lvItemAdapter.notifyDataSetChanged()
        autoExtractOn = false
        MyAccessibilityService.autoExtract = null
        finish()
        return true
    }

    override fun onBackPressed() {

    }

    fun exportExcelAuto(view: View){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkPermission()){
                ExportDatabaseXLSXTask(this).execute()
            }else{
                requestPermission()
            }
        }else{
            ExportDatabaseXLSXTask(this).execute()
        }
    }

    private fun checkPermission():Boolean{
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),100)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            100 -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "请允许权限", Toast.LENGTH_LONG).show()
                } else {
                    ExportDatabaseXLSXTask(this).execute()
                }
            }
        }
    }

    fun saveData(name:String, id:String, realName:String, region:String){
        Log.e("KEN", "saveData function")
        val temp = DetailBean(name, id, realName, region)
        if(!details.any { x -> x.id == id}){
            Log.e("KEN", "saveData function added")
            details.add(temp)
            Snackbar.make(findViewById(R.id.mainLL), "信息成功加入", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        lvItemAdapter.notifyDataSetChanged()
        totalRecordsAuto.text = getString(R.string.auto_extract_total, details.size)
        autoExtractOn = false
        autoExtractSwitch.isChecked = false
    }

    private class ExportDatabaseXLSXTask internal constructor(activityReference: AutoExtractActivity) : AsyncTask<String, Void, String>() {

        private val activityReference: WeakReference<AutoExtractActivity> = WeakReference(activityReference)

        override fun doInBackground(vararg params: String?): String {
            activityReference.get()!!.runOnUiThread {
                Toast.makeText(activityReference.get(), "保存文件中", Toast.LENGTH_SHORT).show()
            }

            val workbook = HSSFWorkbook()

            val sheet = workbook.createSheet("微信信息")

            val headerFont = workbook.createFont()
            headerFont.boldweight = 700

            val headerCellStyle = workbook.createCellStyle()
            headerCellStyle.setFont(headerFont)

            val headerRow = sheet.createRow(0)

            val titleStr = arrayOf("序号", "昵称", "微信账户", "备注信息", "所在地区")

            for(i in 0 until titleStr.size){
                val cell = headerRow.createCell(i)
                cell.setCellValue(titleStr[i])
                cell.setCellStyle(headerCellStyle)
            }

            for(i in 0 until details.size){
                val row = sheet.createRow(i+1)
                row.createCell(0).setCellValue((i+1).toString())
                row.createCell(1).setCellValue(details[i].name)
                row.createCell(2).setCellValue(details[i].id)
                row.createCell(3).setCellValue(details[i].realName)
                row.createCell(4).setCellValue(details[i].region)
            }

            val exportDir = File(Environment.getExternalStorageDirectory(), "WeiXinTools")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val date = Date()
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE)
            val timeStr:String = format.format(date)

            val file = File(exportDir, "WeiXinTools-$timeStr.xls")

            file.createNewFile()

            val fileOut = FileOutputStream(file)
            workbook.write(fileOut)
            fileOut.close()

            activityReference.get()!!.runOnUiThread {
                Toast.makeText(activityReference.get(), "保存文件成功", Toast.LENGTH_SHORT).show()
            }

            val sharedPreference =  activityReference.get()!!.getSharedPreferences("WeiXinTools",Context.MODE_PRIVATE)
            val editor = sharedPreference.edit()
            editor.putString("latestFile", file.absolutePath)
            editor.apply()

            activityReference.get()!!.runOnUiThread {
                activityReference.get()!!.checkNewestFile()
            }

            return ""
        }
    }
}
