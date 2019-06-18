package com.weixintools.weixintools

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class LVAdapter(context: Context) : BaseAdapter() {

    //private var mContext: Context = context
    private var mlist: List<DetailBean>? = null
    private var mInflater: LayoutInflater = LayoutInflater.from(context)

    fun addItem(list: List<DetailBean>) {
        this.mlist = list
    }

    override fun getCount(): Int {
        return if (mlist == null) 0 else mlist!!.size
    }

    override fun getItem(position: Int): Any {
        return mlist!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = mInflater.inflate(R.layout.lv_item_layout, parent, false)

        val nameTV = rowView.findViewById(R.id.name) as TextView
        val idTV = rowView.findViewById(R.id.id) as TextView
        val realNameTV = rowView.findViewById(R.id.realName) as TextView
        val regionTV = rowView.findViewById(R.id.region) as TextView

        val detailBean = mlist?.get(position)

        nameTV.text = detailBean?.name
        idTV.text = detailBean?.id
        realNameTV.text = detailBean?.realName
        regionTV.text = detailBean?.region

        return rowView
    }

}