package com.matrix.drawing.domain.utils

import android.app.AlertDialog
import android.content.Context
import com.matrix.drawing.domain.OnSelectColor
import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import com.matrix.drawing.R
import androidx.core.content.ContextCompat
import com.matrix.drawing.databinding.DialogSelectColorBinding

object AppSelectionColor {
    private var selectColorListener: OnSelectColor? = null
    @JvmStatic
    fun show(context: Context?, color: Int, selectColor: OnSelectColor) {
        val builder = AlertDialog.Builder(context)
        val viewBinding = DataBindingUtil.inflate<DialogSelectColorBinding>(
            LayoutInflater.from(context),
            R.layout.dialog_select_color,
            null,
            false
        )
        builder.setView(viewBinding.root)
        val alertDialog = builder.create()
        alertDialog.show()
        selectColorListener = selectColor
        when (color) {
            AppConstant.colorBlack -> viewBinding.rlBlack.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_circle_black)
            AppConstant.colorBlue -> viewBinding.rlBlue.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_circle_blue)
            AppConstant.colorRed -> viewBinding.rlRed.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_circle_red)
            AppConstant.colorGreen -> viewBinding.rlGreen.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_circle_green)
            AppConstant.colorYellow -> viewBinding.rlYellow.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_circle_yellow)
        }
        viewBinding.rlContainerBlack.setOnClickListener {
            selectColor.selectColor(AppConstant.colorBlack)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerBlue.setOnClickListener {
            selectColor.selectColor(AppConstant.colorBlue)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerRed.setOnClickListener {
            selectColor.selectColor(AppConstant.colorRed)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerGreen.setOnClickListener {
            selectColor.selectColor(AppConstant.colorGreen)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerYellow.setOnClickListener {
            selectColor.selectColor(AppConstant.colorYellow)
            alertDialog.dismiss()
        }
    }
}