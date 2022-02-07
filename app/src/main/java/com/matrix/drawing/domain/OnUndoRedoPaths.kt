package com.matrix.drawing.domain

interface OnUndoRedoPaths {
    fun enableDisableUndo(isEnable: Boolean)
    fun enableDisableRedo(isEnable: Boolean)
}