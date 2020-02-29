package com.lzd.skadatper.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.lzd.skadapter.SKRecyclerViewAdapter
import com.lzd.skadapter.ofSKHolderFactory
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.item_one.view.*

class ListActivity : AppCompatActivity() {

    private val adapter = SKRecyclerViewAdapter<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = adapter

        adapter.register(ofSKHolderFactory<EntityOne>(layoutRes = R.layout.item_one) {
            itemView.text1.text = it.content
        })
        adapter.register(ofSKHolderFactory<EntityTwo>(layoutRes = R.layout.item_two) {})

        setDatas()
    }

    private fun setDatas() {
        val list = arrayListOf<Any>()

        repeat(50) {
            if (it % 2 == 0) {
                list.add(EntityOne(it.toString()))
            } else {
                list.add(EntityTwo())
            }
        }

        adapter.setItems(list)
    }

}

class EntityOne(val content: String)

class EntityTwo
