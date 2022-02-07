package com.matrix.drawing.domain.utils

import android.app.AlertDialog
import android.content.Context
import com.matrix.drawing.domain.OnSelectStroke
import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import com.matrix.drawing.R
import androidx.core.content.ContextCompat
import com.matrix.drawing.databinding.DialogSelectStrokeBinding

object AppSelectionStroke {
    private var selectStrokeListener: OnSelectStroke? = null
    @JvmStatic
    fun show(context: Context?, width: Int, selectStroke: OnSelectStroke) {
        val builder = AlertDialog.Builder(context)
        val viewBinding = DataBindingUtil.inflate<DialogSelectStrokeBinding>(
            LayoutInflater.from(context),
            R.layout.dialog_select_stroke,
            null,
            false
        )
        builder.setView(viewBinding.root)
        val alertDialog = builder.create()
        alertDialog.show()
        selectStrokeListener = selectStroke
        when (width) {
            AppConstant.STROKE_WIDTH_1 -> viewBinding.rlWidth1.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_rect_width_1)
            AppConstant.STROKE_WIDTH_2 -> viewBinding.rlWidth2.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_rect_width_2)
            AppConstant.STROKE_WIDTH_3 -> viewBinding.rlWidth3.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_rect_width_3)
            AppConstant.STROKE_WIDTH_4 -> viewBinding.rlWidth4.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_rect_width_4)
            AppConstant.STROKE_WIDTH_5 -> viewBinding.rlWidth5.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_rect_width_5)
        }
        viewBinding.rlContainerWidth1.setOnClickListener {
            selectStroke.selectStroke(AppConstant.STROKE_WIDTH_1)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerWidth2.setOnClickListener {
            selectStroke.selectStroke(AppConstant.STROKE_WIDTH_2)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerWidth3.setOnClickListener {
            selectStroke.selectStroke(AppConstant.STROKE_WIDTH_3)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerWidth4.setOnClickListener {
            selectStroke.selectStroke(AppConstant.STROKE_WIDTH_4)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerWidth5.setOnClickListener {
            selectStroke.selectStroke(AppConstant.STROKE_WIDTH_5)
            alertDialog.dismiss()
        }
    }
}