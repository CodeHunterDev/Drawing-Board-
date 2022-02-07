package com.matrix.drawing.domain.utils

import android.content.Context
import android.widget.Toast

object AppToastMessage {
    @JvmStatic
    fun showMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun showShortMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}