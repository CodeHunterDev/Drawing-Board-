package com.matrix.drawing.domain.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.matrix.drawing.R
import com.matrix.drawing.domain.OnClickAlertDialogTwoButtons

object AppAlertDialog {
    private var clickDialogTwoBtns: OnClickAlertDialogTwoButtons? = null
    fun showAlertMessage(context: Context, message: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, which -> }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    @JvmStatic
    fun showAlertMessageWithTwoButtons(
        context: Context?, clickListener: OnClickAlertDialogTwoButtons?,
        dialog_name: String?, title: String?, message: String?,
        btn_pos_name: String?, btn_neg_name: String?
    ) {
        val builder = AlertDialog.Builder(context!!)
        builder.setMessage(message)
        builder.setTitle(title)
        clickDialogTwoBtns = clickListener
        builder.setPositiveButton(btn_pos_name) { dialog, which ->
            clickDialogTwoBtns!!.clickPositiveDialogButton(
                dialog_name
            )
        }
        builder.setNegativeButton(btn_neg_name) { dialog, which ->
            clickDialogTwoBtns!!.clickNegativeDialogButton(
                dialog_name
            )
        }
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}