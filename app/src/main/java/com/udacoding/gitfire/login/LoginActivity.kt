package com.udacoding.gitfire.login

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.udacoding.gitfire.R
import com.udacoding.gitfire.auth.AutentikasiHpActivity
import com.udacoding.gitfire.network.myFirebaseDatabase
import com.udacoding.gitfire.signup.SignUpActivity
import com.udacoding.gitfire.signup.model.User
import com.udacoding.gitfire.utama.HomeActivity
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    var googleSignInClient: GoogleSignInClient? = null
    var RC_SIGN_IN_GOOGLE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)
        //  End Configure Google Sign In

        loginSignIn.onClick {
            if (loginUsername.text.isEmpty()){
                loginUsername.requestFocus()
                loginUsername.error = "Username tidak boleh kosong"
            }
            else if(loginPassword.text.isEmpty()){
                loginPassword.requestFocus()
                loginPassword.error = "Password tidak boleh kosong"
            }else{
                mAuth?.signInWithEmailAndPassword(loginUsername.text.toString(),loginPassword.text.toString())
                    ?.addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            toast("Login berhasil")
                            startActivity<HomeActivity>()
                        }else{
                            toast(task.exception?.message.toString())
                        }
                    }
            }
        }

        signUpbuttonGmail.onClick {
            signInWithGmail()
        }


        signUplink.onClick {
            startActivity<SignUpActivity>()
        }

    }

    private fun signInWithGmail() {
        val signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN_GOOGLE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                toast("Google Sign In failed")
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    checkDatabase(task.result.user)
                } else {
                    // If sign in fails, display a message to the user.
                    toast("Sign in Gmail Gagal")
                }

                // ...
            }
    }

    private fun checkDatabase(responseUser: FirebaseUser) {
        //  add query
        val query = myFirebaseDatabase.userRef().orderByChild("uid").equalTo(responseUser.uid)

        query.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.value == null){
                    toast("Creating new user")
                    var newUser= User()
                    newUser.uid = responseUser.uid
                    newUser.name = responseUser.displayName
                    newUser.email = responseUser.email

                    if (responseUser.phoneNumber != null){
                        newUser.hp = responseUser.phoneNumber
                    }else{
                        newUser.hp = ""
                    }

                    val key = myFirebaseDatabase.firebaseDatabase().reference.push().key

                    key?.let { myFirebaseDatabase.userRef().child(it).setValue(newUser) }

                    startActivity<AutentikasiHpActivity>(
                        "key" to key.toString()
                    )

                }
                else{
                    startActivity<HomeActivity>()
                }


            }
        })
    }
}
