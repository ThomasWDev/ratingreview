/**
 * @author Thomas Woodfin
 */
package com.phillipsconnect.ratingreview

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.phillipsconnect.ratingreview.RatingReview.Companion.showRatingReview
import java.util.Calendar

@SuppressLint("StaticFieldLeak")
object RatingService{
    private var firebaseSecretKey = ""
    internal var firebaseUsername = ""
    internal var firebaseChildName = ""
    internal lateinit var firebaseDatabase: FirebaseDatabase
    internal lateinit var ratingActivity: AppCompatActivity
    internal var showRatingDialog: Boolean = false

    fun setRatingPreference(
        activity: AppCompatActivity,
        database: FirebaseDatabase,
        childName: String,
        secretKey: String,
        username: String
    ) {
        ratingActivity = activity
        firebaseDatabase = database
        firebaseChildName = childName
        firebaseSecretKey = secretKey
        firebaseUsername = username

        initRemoteConfig()
    }

    private fun initRemoteConfig() {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 10
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(ratingActivity) { task ->
                if (task.isSuccessful) {
                    showRatingDialog = remoteConfig.getBoolean("showRatingDialog")
                    val showRatingAfter = remoteConfig.getLong("showRatingAfterNDays")

                    val calendar = Calendar.getInstance()
                    val currentTime = calendar.time.time
                    calendar.add(Calendar.DATE, showRatingAfter.toInt())
                    val delayTime = calendar.time.time - currentTime

                    Handler(Looper.myLooper()!!).postDelayed(
                        {
                            if (showRatingDialog) {
                                showRatingReview(ratingActivity.supportFragmentManager)
                            }
                        }, delayTime
                    )
                }
            }
    }

    private fun getUserRef(): String {
        return "${firebaseChildName}_$firebaseSecretKey/$firebaseUsername"
    }

    private fun getUserNeverShowRef(): String {
        return "${getUserRef()}/neverShowAgainAndroid"
    }

    internal fun saveRatingNeverShowInFirebase() {
        val values: MutableMap<String, Boolean> = HashMap()
        values["neverShowAgain"] = true
        firebaseDatabase.reference.child(getUserNeverShowRef()).setValue(values)
    }
}
