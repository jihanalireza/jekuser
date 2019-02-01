package com.udacoding.gitfire.utama.history


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import com.udacoding.gitfire.R
import com.udacoding.gitfire.network.myFirebaseDatabase
import com.udacoding.gitfire.utama.history.adapter.Historydapter
import com.udacoding.gitfire.utama.home.model.Booking
import kotlinx.android.synthetic.main.fragment_history.*


class HistoryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getHistory()

    }

    private fun getHistory() {
        val uid =  FirebaseAuth.getInstance().currentUser?.uid
        myFirebaseDatabase.bookingRef().orderByChild("uid").equalTo(uid).addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
//                showDataHistory(p0)
                val data = ArrayList<Booking>()
                for (issue in p0.children){
                    val itemBooking = issue.getValue(Booking::class.java)
                    data.add(itemBooking ?: Booking())
                }
                showDataHistory(data)
            }


        })
    }

    private fun showDataHistory(dataHistory: ArrayList<Booking>) {
        recycleHistory.adapter = Historydapter(dataHistory as List<Booking>)
        recycleHistory.layoutManager = LinearLayoutManager(context)
    }


}
