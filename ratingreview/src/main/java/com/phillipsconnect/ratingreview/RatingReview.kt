package com.phillipsconnect.ratingreview

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.review.ReviewManagerFactory
import com.phillipsconnect.ratingreview.RatingService.ratingActivity
import com.phillipsconnect.ratingreview.RatingService.saveRatingNeverShowInFirebase
import com.phillipsconnect.ratingreview.RatingService.showRatingDialog

class RatingReview: DialogFragment(), OnRatingBarChangeListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity())
        val dialogView = onCreateView(layoutInflater, null, savedInstanceState)
        builder.setCancelable(false).setView(dialogView)
            .setBackground(ColorDrawable(Color.TRANSPARENT))
        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.lib_material_rate_dialog, container, false)
        val neverButton = view.findViewById<TextView>(R.id.bt_maybeLater)
        neverButton.setOnClickListener { dismiss() }
        val ratingBar = view.findViewById<RatingBar>(R.id.bt_ratingBar)
        ratingBar.onRatingBarChangeListener = this
        return view
    }

    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
        ratingActions(rating)
//        getRatingNeverShowInFirebase(rating)
    }

    private fun ratingActions(rating: Float) {
        try {
            if (rating >= 5) {
//                saveRatingNeverShowInFirebase()
                openPlayStoreForRating()
            }
            dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openPlayStoreForRating() {
        val manager = ReviewManagerFactory.create(ratingActivity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(ratingActivity, reviewInfo)
                flow.addOnCompleteListener { }
            } else {
                try {
                    val packageName = requireActivity().packageName
                    var intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                        )
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (dialog != null) {
            dialog!!.setDismissMessage(null)
        }
    }

    private fun getRatingNeverShowInFirebase(rating: Float) {
        RatingService.firebaseDatabase.reference.child(RatingService.firebaseChildName).child(
            RatingService.firebaseUsername
        ).get().addOnSuccessListener {
            if ( (it.exists() && it.value as Boolean) || !it.exists()) {
                ratingActions(rating)
            }
        }.addOnFailureListener{ e->
            e.printStackTrace()
        }
    }

    companion object {
        @SuppressLint("UseRequireInsteadOfGet")
        @JvmStatic
        internal fun showRatingReview(fragmentManager: FragmentManager?) {
            if (showRatingDialog) {
                try {
                    val ratingDialog = RatingReview()
                    ratingDialog.show(fragmentManager!!, "RatingReview")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
