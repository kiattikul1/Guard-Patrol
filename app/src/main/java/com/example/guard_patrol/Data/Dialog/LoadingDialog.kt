package com.example.guard_patrol.Data.Dialog

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.widget.ProgressBar
import com.example.guard_patrol.R

class LoadingDialog(context: Context) {
    private var progressBar: ProgressBar? = null
    private lateinit var dialog : Dialog
    fun showLoadingDialog(context: Context) {
        Log.d("TestDialog", "Dialog open")
        if (progressBar == null) {
            dialog = Dialog(context)
            dialog.apply {
                setCanceledOnTouchOutside(false)
                setCancelable(false)
                setContentView(R.layout.custom_dialog_loading)
                window?.setBackgroundDrawableResource(android.R.color.transparent);
                show()
            }
        }
        progressBar?.visibility = ProgressBar.VISIBLE
    }

    fun dismissLoadingDialog() {
        Log.d("TestDialog", "Dialog close")
        progressBar?.visibility = ProgressBar.GONE
        dialog.hide()
    }
}
