package com.udacoding.gitfire.signup

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.udacoding.gitfire.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.udacoding.gitfire.login.LoginActivity
import com.udacoding.gitfire.signup.model.User
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*


class SignUpActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        //  inisialisasi
        mAuth =  FirebaseAuth.getInstance()

        //  listener button

        signUpbutton.onClick {
//            email required
            if(signUpEmail.text.isEmpty()){
                signUpEmail.requestFocus()
                signUpEmail.error = "Email tidak boleh kosong"
            }
//            password required
            else if(signUpPassword.text.isEmpty()){
                signUpPassword.requestFocus()
                signUpPassword.error = "Password tidak boleh kosong"
            }
//            password min 6 character
            else if(signUpPassword.text.length < 6){
                signUpPassword.requestFocus()
                signUpPassword.error = "Password minimal 6 karakter"
            }
//            validation password confirmation
            else  if(signUpPassword.text.toString() != signUpPasswordConfirm.text.toString()){
                signUpPasswordConfirm.requestFocus()
                signUpPasswordConfirm.error = "Password tidak cocok"
            }
            else{
                mAuth?.createUserWithEmailAndPassword(signUpEmail.text.toString(),
                    signUpPassword.text.toString())
                    ?.addOnCompleteListener {result ->
//                        check response firebase signup
                        if (result.isSuccessful){
                            tesInsertDatabase(result.result.user.uid)
                            toast("Sign up berhasil")
                            startActivity<LoginActivity>()
                        }else{
                            toast(result.exception?.message.toString())
                        }
                    }
            }

        }

        alreadyAccount.onClick {
            startActivity<LoginActivity>()
        }
    }

    fun tesInsertDatabase(uid: String){
        var database = FirebaseDatabase.getInstance()
        var CobaRef = database.getReference("User")
        var user = User()
        user.name = signUpName.text.toString()
        user.hp = signUpHp.text.toString()
        user.email = signUpEmail.text.toString()
        user.uid = uid

        var key = database.reference.push().key

        key?.let { CobaRef.child(it).setValue(user) }

    }


}
