package com.matrix.drawing.domain.utils

import android.app.AlertDialog
import android.content.Context
import com.matrix.drawing.domain.OnSelectDrawing
import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import com.matrix.drawing.R
import androidx.core.content.ContextCompat
import com.matrix.drawing.databinding.DialogSelectDrawingBinding

object AppSelectionDrawing {
    private var selectDrawingListener: OnSelectDrawing? = null
    @JvmStatic
    fun show(context: Context?, drawing: Int, selectDrawing: OnSelectDrawing?) {
        val builder = AlertDialog.Builder(context)
        val viewBinding = DataBindingUtil.inflate<DialogSelectDrawingBinding>(
            LayoutInflater.from(context),
            R.layout.dialog_select_drawing,
            null,
            false
        )
        builder.setView(viewBinding.root)
        val alertDialog = builder.create()
        alertDialog.show()

        selectDrawingListener = selectDrawing
        when (drawing) {
            AppConstant.DRAWING_TYPE_PENCIL -> viewBinding.rlPencil.background = ContextCompat.getDrawable(
                context!!, R.drawable.style_border_rect_width_3
            )
            AppConstant.DRAWING_TYPE_ERASER -> viewBinding.rlEraser.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_rect_width_3)
            AppConstant.DRAWING_TYPE_LINE -> viewBinding.rlLine.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_rect_width_3)
            AppConstant.DRAWING_TYPE_LINE_ARROW -> viewBinding.rlLineArrow.background = ContextCompat.getDrawable(
                context!!, R.drawable.style_border_rect_width_3
            )
            AppConstant.DRAWING_TYPE_RECTANGLE -> viewBinding.rlRect.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_rect_width_3)
            AppConstant.DRAWING_TYPE_RECTANGLE_FILLED -> viewBinding.rlRectFilled.background =
                ContextCompat.getDrawable(
                    context!!, R.drawable.style_border_rect_width_3
                )
            AppConstant.DRAWING_TYPE_CIRCLE -> viewBinding.rlCircle.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_rect_width_3)
            AppConstant.DRAWING_TYPE_CIRCLE_FILLED -> viewBinding.rlCircleFilled.background = ContextCompat.getDrawable(
                context!!, R.drawable.style_border_rect_width_3
            )
            AppConstant.DRAWING_TYPE_OVAL -> viewBinding.rlOval.background = ContextCompat.getDrawable(
                context!!, R.drawable.style_border_rect_width_3
            )

            AppConstant.DRAWING_TYPE_ZOOMING -> viewBinding.rlZooming.background =
                ContextCompat.getDrawable(context!!, R.drawable.style_border_rect_width_3)
        }
        viewBinding.rlContainerPencil.setOnClickListener {
            selectDrawingListener!!.selectDrawing(AppConstant.DRAWING_TYPE_PENCIL)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerEraser.setOnClickListener {
            selectDrawingListener!!.selectDrawing(AppConstant.DRAWING_TYPE_ERASER)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerLine.setOnClickListener {
            selectDrawingListener!!.selectDrawing(AppConstant.DRAWING_TYPE_LINE)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerLineArrow.setOnClickListener {
            selectDrawingListener!!.selectDrawing(AppConstant.DRAWING_TYPE_LINE_ARROW)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerRect.setOnClickListener {
            selectDrawingListener!!.selectDrawing(AppConstant.DRAWING_TYPE_RECTANGLE)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerRectFilled.setOnClickListener {
            selectDrawingListener!!.selectDrawing(AppConstant.DRAWING_TYPE_RECTANGLE_FILLED)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerCircle.setOnClickListener {
            selectDrawingListener!!.selectDrawing(AppConstant.DRAWING_TYPE_CIRCLE)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerCircleFilled.setOnClickListener {
            selectDrawingListener!!.selectDrawing(AppConstant.DRAWING_TYPE_CIRCLE_FILLED)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerOval.setOnClickListener {
            selectDrawingListener!!.selectDrawing(AppConstant.DRAWING_TYPE_OVAL)
            alertDialog.dismiss()
        }
        viewBinding.rlContainerZooming.setOnClickListener {
            selectDrawingListener!!.selectDrawing(AppConstant.DRAWING_TYPE_ZOOMING)
            alertDialog.dismiss()
        }
    }
}