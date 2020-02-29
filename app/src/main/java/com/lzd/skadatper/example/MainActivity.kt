package com.lzd.skadatper.example

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun jumpHolder(view: View) {
        startActivity(Intent(this, HolderActivity::class.java))
    }

    fun jumpNormal(view: View) {
        startActivity(Intent(this, ListActivity::class.java))
    }
}
