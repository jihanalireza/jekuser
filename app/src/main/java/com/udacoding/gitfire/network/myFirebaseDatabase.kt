package com.udacoding.gitfire.network

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object myFirebaseDatabase {
    fun firebaseDatabase(): FirebaseDatabase {
        val database = FirebaseDatabase.getInstance()
        return database
    }

    fun userRef(): DatabaseReference {
        val userRef = firebaseDatabase().getReference("User")
        return userRef
    }

    fun bookingRef(): DatabaseReference {
        val bookingRef = firebaseDatabase().getReference("Booking")
        return bookingRef
    }

    fun driverRef(): DatabaseReference {
        val driverRef = firebaseDatabase().getReference("Driver")
        return driverRef
    }
}