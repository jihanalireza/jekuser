package com.udacoding.gitfire.utama.profile


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


import com.udacoding.gitfire.R
import com.udacoding.gitfire.login.LoginActivity
import com.udacoding.gitfire.network.myFirebaseDatabase
import com.udacoding.gitfire.signup.model.User
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.startActivity


class ProfileFragment : Fragment() {
    private var mAuth: FirebaseAuth? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mAuth = FirebaseAuth.getInstance()
        super.onViewCreated(view, savedInstanceState)
        // get auth firebase
        val uid = mAuth?.currentUser?.uid
        val query = myFirebaseDatabase.userRef().orderByChild("uid")
            .equalTo(uid)

        query.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                for(issue in p0.children) {
                    val data = issue.getValue(User::class.java)
                    ShowDataProfile(data)
                }

            }

        })
    }

    private fun ShowDataProfile(data: User?) {
        profileName.text = data?.name
        profilEmail.text = data?.email
        profilhp.text = data?.hp

        profileSignout.onClick {
            //            Sign out from firebase auth
            mAuth?.signOut()
            startActivity<LoginActivity>()
        }
    }
}
