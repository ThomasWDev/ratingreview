package com.phillipsconnect.app

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.phillipsconnect.ratingreview.RatingService.setRatingPreference

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val feedbackApp = findViewById<Button>(R.id.feedback_app)
        feedbackApp.setOnClickListener {
            setRatingPreference( this, Firebase.database, "", "", "" )
        }
    }
}