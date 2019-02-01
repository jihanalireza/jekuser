package com.udacoding.gitfire.auth

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.udacoding.gitfire.R
import com.udacoding.gitfire.network.myFirebaseDatabase
import com.udacoding.gitfire.utama.HomeActivity
import kotlinx.android.synthetic.main.activity_autentikasi_hp.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class AutentikasiHpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autentikasi_hp)
        var key = intent.getStringExtra("key")

        authentikasisubmit.onClick {
            val userRef = myFirebaseDatabase.userRef()
            userRef.child(key).child("hp").setValue(authentikasinomorhp.text.toString())
            startActivity<HomeActivity>()
        }


    }
}
