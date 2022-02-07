package com.matrix.drawing.domain

import com.matrix.drawing.data.model.Drawing

interface OnSelectEraser {
    fun selectEraser(drawingList: List<Drawing?>?)
}