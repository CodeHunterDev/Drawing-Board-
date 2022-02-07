package com.matrix.drawing.data.model

import android.graphics.Path
import androidx.room.Ignore

class Drawing {
    var id = 0
    var color: Int
    var strokeWidth: Int
    var type: Int
    var startX = 0f
    var startY = 0f
    var stopX = 0f
    var stopY = 0f
    var path: Path? = null

    @Ignore
    constructor(color: Int, strokeWidth: Int, type: Int, path: Path?) {
        this.color = color
        this.strokeWidth = strokeWidth
        this.type = type
        this.path = path
    }

    @Ignore
    constructor(color: Int, strokeWidth: Int, type: Int, startX: Float, startY: Float) : this(
        color,
        strokeWidth,
        type,
        startX,
        startY,
        startX,
        startY
    ) {
    }

    @Ignore
    constructor(color: Int, strokeWidth: Int, type: Int, startX: Float, startY: Float, stopX: Float, stopY: Float) {
        this.color = color
        this.strokeWidth = strokeWidth
        this.type = type
        this.startX = startX
        this.startY = startY
        this.stopX = stopX
        this.stopY = stopY
    }

    //Constructor for saving data locally
    constructor(
        color: Int,
        strokeWidth: Int,
        type: Int,
        startX: Float,
        startY: Float,
        stopX: Float,
        stopY: Float,
        path: Path?
    ) {
        this.color = color
        this.strokeWidth = strokeWidth
        this.type = type
        this.startX = startX
        this.startY = startY
        this.stopX = stopX
        this.stopY = stopY
        this.path = path
    }
}