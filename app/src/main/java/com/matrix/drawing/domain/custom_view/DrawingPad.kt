package com.matrix.drawing.domain.custom_view


import com.matrix.drawing.data.model.Drawing
import android.view.ScaleGestureDetector
import android.view.GestureDetector
import android.graphics.drawable.Drawable
import com.matrix.drawing.domain.OnSelectEraser
import com.matrix.drawing.domain.OnUndoRedoPaths
import com.matrix.drawing.domain.OnSetZoomLevel
import com.matrix.drawing.domain.utils.AppConstant
import android.util.DisplayMetrics
import com.matrix.drawing.presentation.MainActivity
import android.graphics.drawable.BitmapDrawable
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.View
import androidx.core.view.ViewCompat
import java.util.ArrayList
import kotlin.math.*

class DrawingPad : View {
    private var mX = 0f
    private var mY = 0f
    private var mPaint: Paint? = null
    private var mPath: Path? = null
    private var currentColor = 0
    private var strokeWidth = 0
    private var savedBitmap: Bitmap? = null
    var canvasBitmap: Bitmap? = null
        private set
    private var mCanvas: Canvas? = null
    private val mBitmapPaint = Paint(Paint.DITHER_FLAG)
    var drawingList1: MutableList<Drawing>? = ArrayList()
    private val drawingUndoList: MutableList<Drawing> = ArrayList()
    private var drawingType = 0
    private var width1 = 0
    private var height1 = 0
    private var isUndoRedo = false
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mGestureDetector: GestureDetector? = null
    private var clipCanvas: Rect? = null
    private var mMatrix: Matrix? = null
    private var mBoard: Drawable? = null
    private var scaleFactor = 1.5f
    private var mBoardWidth = 0f
    private var mBoardHeight = 0f
    private var selectEraserListener: OnSelectEraser? = null
    private var undoRedoPathsListener: OnUndoRedoPaths? = null
    private var setZoomLevelListener: OnSetZoomLevel? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {

        //Initializing the paint attributes
        mPaint = Paint()
        mPaint!!.color = Color.BLACK
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeCap = Paint.Cap.ROUND
        drawingType = AppConstant.DRAWING_TYPE_PENCIL
    }

    fun initialize(metrics: DisplayMetrics, oldBitmap: Bitmap?, view: View, mainScreen: MainActivity) {
        height1 = metrics.heightPixels
        width1 = metrics.widthPixels

        //If old bitmap is null them it means there is saved bitmap before.
        if (oldBitmap != null) {
            savedBitmap = oldBitmap
            val loadedBitmap = Bitmap.createBitmap(savedBitmap!!)
            canvasBitmap = loadedBitmap.copy(Bitmap.Config.ARGB_8888, true)
            mCanvas = Canvas(canvasBitmap!!)
        } else {
            canvasBitmap = Bitmap.createBitmap(width1, height1, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(canvasBitmap!!)
        }

        currentColor = Color.BLACK
        strokeWidth = AppConstant.STROKE_WIDTH_1
        selectEraserListener = mainScreen
        undoRedoPathsListener = mainScreen
        setZoomLevelListener = mainScreen
        mBoard = BitmapDrawable(mainScreen.resources, canvasBitmap)
        mBoardWidth = mBoard!!.intrinsicWidth.toFloat()
        mBoardHeight = mBoard!!.intrinsicHeight.toFloat()

        mBoard!!.setBounds(0, 0, mBoardWidth.toInt(), mBoardHeight.toInt())
        mMatrix = Matrix()
        val centreX = view.x + width1 / 2
        val centreY = view.y + height1 / 2
        mMatrix!!.setScale(scaleFactor, scaleFactor, centreX, centreY)
        mScaleDetector = ScaleGestureDetector(mainScreen, scaleListener)
        mGestureDetector = GestureDetector(mainScreen, listener)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.concat(mMatrix)
        clipCanvas = canvas.clipBounds
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, null)
        for (drawing in drawingList1!!) {
            mPaint!!.color = drawing.color
            mPaint!!.strokeWidth = drawing.strokeWidth.toFloat()
            when (drawing.type) {
                AppConstant.DRAWING_TYPE_PENCIL -> {
                    mPaint!!.style = Paint.Style.STROKE
                    if (!isUndoRedo) {
                        canvas.drawPath(drawing.path!!, mPaint!!)
                    } else {
                        mCanvas!!.drawPath(drawing.path!!, mPaint!!)
                    }
                    Log.d("check_drawing", "Pencil")
                }
                AppConstant.DRAWING_TYPE_ERASER -> {
                    //mPaint.setMaskFilter(null);
                    mPaint!!.style = Paint.Style.STROKE
                    mPaint!!.color = Color.WHITE
                    mPaint!!.strokeWidth = AppConstant.STROKE_WIDTH_5.toFloat()
                    if (!isUndoRedo) {
                        canvas.drawPath(drawing.path!!, mPaint!!)
                    } else {
                        mCanvas!!.drawPath(drawing.path!!, mPaint!!)
                    }
                    Log.d("check_drawing", "Eraser")
                }
                AppConstant.DRAWING_TYPE_LINE -> {
                    mPaint!!.style = Paint.Style.STROKE
                    if (!isUndoRedo) {
                        canvas.drawLine(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY, mPaint!!)
                    } else {
                        mCanvas!!.drawLine(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY, mPaint!!)
                    }
                    Log.d("check_drawing", "Line")
                }
                AppConstant.DRAWING_TYPE_LINE_ARROW -> {
                    mPaint!!.style = Paint.Style.STROKE
                    val x = drawing.startX
                    val y = drawing.startY
                    val x1 = drawing.stopX
                    val y1 = drawing.stopY
                    if (!isUndoRedo) {
                        canvas.drawLine(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY, mPaint!!)
                    } else {
                        mCanvas!!.drawLine(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY, mPaint!!)
                    }
                    val degree = calculateDegree(x, x1, y, y1)
                    val endX1 = (x1 + 50 * cos(Math.toRadians(degree - 30 + 90))).toFloat()
                    val endY1 = (y1 + 50 * sin(Math.toRadians(degree - 30 + 90))).toFloat()
                    val endX2 = (x1 + 50 * cos(Math.toRadians(degree - 60 + 180))).toFloat()
                    val endY2 = (y1 + 50 * sin(Math.toRadians(degree - 60 + 180))).toFloat()
                    if (!isUndoRedo) {
                        canvas.drawLine(x1, y1, endX1, endY1, mPaint!!)
                        canvas.drawLine(x1, y1, endX2, endY2, mPaint!!)
                    } else {
                        mCanvas!!.drawLine(x1, y1, endX1, endY1, mPaint!!)
                        mCanvas!!.drawLine(x1, y1, endX2, endY2, mPaint!!)
                    }
                }
                AppConstant.DRAWING_TYPE_RECTANGLE -> {
                    mPaint!!.style = Paint.Style.STROKE
                    if (!isUndoRedo) {
                        canvas.drawRect(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY, mPaint!!)
                    } else {
                        mCanvas!!.drawRect(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY, mPaint!!)
                    }
                    Log.d("check_drawing", "Rectangle")
                }
                AppConstant.DRAWING_TYPE_RECTANGLE_FILLED -> {
                    mPaint!!.style = Paint.Style.FILL
                    if (!isUndoRedo) {
                        canvas.drawRect(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY, mPaint!!)
                    } else {
                        mCanvas!!.drawRect(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY, mPaint!!)
                    }
                    Log.d("check_drawing", "Rectangle Filled")
                }
                AppConstant.DRAWING_TYPE_CIRCLE -> {
                    mPaint!!.style = Paint.Style.STROKE
                    if (!isUndoRedo) {
                        val radius = sqrt((drawing.stopX-drawing.startX).pow(2) + (drawing.stopY-drawing.startY).pow(2))
                        canvas.drawCircle((drawing.startX+drawing.stopX)/2, (drawing.startY+drawing.stopY)/2, radius, mPaint!! )
                    } else {
                        val radius = sqrt((drawing.stopX-drawing.startX).pow(2) + (drawing.stopY-drawing.startY).pow(2))
                        mCanvas!!.drawCircle((drawing.startX+drawing.stopX)/2, (drawing.startY+drawing.stopY)/2, radius, mPaint!! )
                    }
                    Log.d("check_drawing", "Circle")
                }
                AppConstant.DRAWING_TYPE_CIRCLE_FILLED -> {
                    mPaint!!.style = Paint.Style.FILL
                    if (!isUndoRedo) {
                        val radius = sqrt((drawing.stopX-drawing.startX).pow(2) + (drawing.stopY-drawing.startY).pow(2))
                        canvas.drawCircle((drawing.startX+drawing.stopX)/2, (drawing.startY+drawing.stopY)/2, radius, mPaint!! )
                    } else {
                        val radius = sqrt((drawing.stopX-drawing.startX).pow(2) + (drawing.stopY-drawing.startY).pow(2))
                        mCanvas!!.drawCircle((drawing.startX+drawing.stopX)/2, (drawing.startY+drawing.stopY)/2, radius, mPaint!! )
                    }
                    Log.d("check_drawing", "Circle Filled")
                }

                AppConstant.DRAWING_TYPE_OVAL -> {
                    mPaint!!.style = Paint.Style.STROKE
                    if (!isUndoRedo) {
                        canvas.drawOval(RectF(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY), mPaint!!)
                    } else {
                        mCanvas!!.drawOval(
                            RectF(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY),
                            mPaint!!
                        )
                    }
                    Log.d("check_drawing", "Oval")
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        isUndoRedo = false
        //In order to get the exact position after scaling and translate, divide the scale factor into event's X and Y
        val x = event.x / scaleFactor + clipCanvas!!.left
        val y = event.y / scaleFactor + clipCanvas!!.top

        //onTouch listener for Pencil and Eraser
        if (drawingType == AppConstant.DRAWING_TYPE_PENCIL || drawingType == AppConstant.DRAWING_TYPE_ERASER) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //This action is called when the user first touch the screen.
                    touchStart(x, y)
                    invalidate()
                    Log.d("check_process", "onTouchEvent: Action Down X: $x Y: $y")
                }
                MotionEvent.ACTION_MOVE -> {
                    //This action is called when the user move the finger on the screen.
                    touchMove(x, y)
                    invalidate()
                    Log.d("check_process", "onTouchEvent: Action Move X: $x Y: $y")
                }
                MotionEvent.ACTION_UP -> {
                    //This action is called when the user un-touch the screen
                    touchUp()
                    invalidate()
                    Log.d("check_process", "onTouchEvent: Action Up")
                }
            }
            val currentDrawing = drawingList1!![drawingList1!!.size - 1]
            drawCanvasBitmap(currentDrawing)
            return true
        }

        //onTouch listener for shapes
        if (drawingType == AppConstant.DRAWING_TYPE_LINE ||
            drawingType == AppConstant.DRAWING_TYPE_LINE_ARROW ||
            drawingType == AppConstant.DRAWING_TYPE_RECTANGLE ||
            drawingType == AppConstant.DRAWING_TYPE_RECTANGLE_FILLED ||
            drawingType == AppConstant.DRAWING_TYPE_CIRCLE ||
            drawingType == AppConstant.DRAWING_TYPE_CIRCLE_FILLED ||
            drawingType == AppConstant.DRAWING_TYPE_OVAL) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                val currentDrawing = Drawing(currentColor, strokeWidth, drawingType, x, y)
                currentDrawing.color = currentColor
                drawingList1!!.add(currentDrawing)
                undoRedoPathsListener!!.enableDisableUndo(true)
                return true
            } else if ((event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_UP) && drawingList1!!.size > 0) {
                val currentDrawing = drawingList1!![drawingList1!!.size - 1]
                currentDrawing.stopX = x
                currentDrawing.stopY = y

                //drawing the line, rect, circle etc inside the canvas bitmap directly
                if (event.action == MotionEvent.ACTION_UP) {
                    drawCanvasBitmap(currentDrawing)
                }
                invalidate()
                return true
            }
        }

        //onTouch listener For zooming
        if (drawingType == AppConstant.DRAWING_TYPE_ZOOMING) {
            mGestureDetector!!.onTouchEvent(event)
            mScaleDetector!!.onTouchEvent(event)
            return true
        }
        return false
    }

    private fun touchStart(x: Float, y: Float) {
        mPath = Path()
        //adding the drawn path into the path list array.
        val fp = Drawing(currentColor, strokeWidth, drawingType, mPath)
        drawingList1!!.add(fp)
        mPath!!.reset()
        mPath!!.moveTo(x, y)
        mX = x
        mY = y
        undoRedoPathsListener!!.enableDisableUndo(true)
        Log.d("check_process", "touchStart: mX: $mX mY: $mY")
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = abs(x - mX)
        val dy = abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath!!.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
        Log.d("check_process", "touchMove: mX: $mX mY: $mY")
    }

    private fun touchUp() {
        mPath!!.lineTo(mX, mY)
        Log.d("check_process", "touchUp")
    }

    private fun drawCanvasBitmap(drawing: Drawing) {
        when (drawing.type) {
            AppConstant.DRAWING_TYPE_PENCIL -> {
                mPaint!!.style = Paint.Style.STROKE
                mCanvas!!.drawPath(drawing.path!!, mPaint!!)
            }
            AppConstant.DRAWING_TYPE_ERASER -> {
                mPaint!!.style = Paint.Style.STROKE
                mPaint!!.color = Color.WHITE
                mPaint!!.strokeWidth = AppConstant.STROKE_WIDTH_5.toFloat()
                mCanvas!!.drawPath(drawing.path!!, mPaint!!)
            }
            AppConstant.DRAWING_TYPE_LINE -> {
                mPaint!!.style = Paint.Style.STROKE
                mCanvas!!.drawLine(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY, mPaint!!)
            }
            AppConstant.DRAWING_TYPE_LINE_ARROW -> {
                mPaint!!.style = Paint.Style.STROKE
                val x = drawing.startX
                val y = drawing.startY
                val x1 = drawing.stopX
                val y1 = drawing.stopY
                mCanvas!!.drawLine(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY, mPaint!!)
                val degree = calculateDegree(x, x1, y, y1)
                val endX1 = (x1 + 50 * cos(Math.toRadians(degree - 30 + 90))).toFloat()
                val endY1 = (y1 + 50 * sin(Math.toRadians(degree - 30 + 90))).toFloat()
                val endX2 = (x1 + 50 * cos(Math.toRadians(degree - 60 + 180))).toFloat()
                val endY2 = (y1 + 50 * sin(Math.toRadians(degree - 60 + 180))).toFloat()
                mCanvas!!.drawLine(x1, y1, endX1, endY1, mPaint!!)
                mCanvas!!.drawLine(x1, y1, endX2, endY2, mPaint!!)
            }
            AppConstant.DRAWING_TYPE_RECTANGLE -> {
                mPaint!!.style = Paint.Style.STROKE
                mCanvas!!.drawRect(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY, mPaint!!)
            }
            AppConstant.DRAWING_TYPE_RECTANGLE_FILLED -> {
                mPaint!!.style = Paint.Style.FILL
                mCanvas!!.drawRect(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY, mPaint!!)
            }
            AppConstant.DRAWING_TYPE_CIRCLE -> {
                mPaint!!.style = Paint.Style.STROKE
                val radius = sqrt((drawing.stopX-drawing.startX).pow(2) + (drawing.stopY-drawing.startY).pow(2))
                mCanvas!!.drawCircle((drawing.startX+drawing.stopX)/2, (drawing.startY+drawing.stopY)/2, radius, mPaint!! )
            }
            AppConstant.DRAWING_TYPE_CIRCLE_FILLED -> {
                mPaint!!.style = Paint.Style.FILL
                val radius = sqrt((drawing.stopX-drawing.startX).pow(2) + (drawing.stopY-drawing.startY).pow(2))
                mCanvas!!.drawCircle((drawing.startX+drawing.stopX)/2, (drawing.startY+drawing.stopY)/2, radius, mPaint!! )
            }
            AppConstant.DRAWING_TYPE_OVAL -> {
                mPaint!!.style = Paint.Style.STROKE
                mCanvas!!.drawOval(RectF(drawing.startX, drawing.startY, drawing.stopX, drawing.stopY), mPaint!!)
            }
        }
    }

    fun setDrawing(drawingType: Int) {
        this.drawingType = drawingType
    }

    //setting the paint color
    fun setPaintColor(color: Int) {
        currentColor = color
    }

    //setting the stroke width
    fun setPaintStroke(stroke: Int) {
        strokeWidth = stroke
    }

    //setting the stroke and width for eraser
    fun setEraserMode(color: Int, stroke: Int) {
        currentColor = color
        strokeWidth = stroke
    }

    val drawingCount: Int
        get() = if (drawingList1 == null) 0 else drawingList1!!.size

    fun enableDisableEraser() {}
    fun setUndo() {
        if (savedBitmap != null) {
            val loadedBitmap = Bitmap.createBitmap(savedBitmap!!)
            canvasBitmap = loadedBitmap.copy(Bitmap.Config.ARGB_8888, true)
            mCanvas = Canvas(canvasBitmap!!)
        } else {
            canvasBitmap = Bitmap.createBitmap(width1, height1, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(canvasBitmap!!)
        }
        isUndoRedo = true

        /*
         * First checking if the pencilList array contains path or not. If path is available then removing the last path from pencilList array
         * and adding that removed path into another array pencilUndoList.
         * */if (drawingList1!!.size > 0) {
            drawingUndoList.add(drawingList1!!.removeAt(drawingList1!!.size - 1))
            invalidate()
        }

        //If pencilList is less than or equal to zero then make the undo disable
        if (drawingList1!!.size <= 0) {
            undoRedoPathsListener!!.enableDisableUndo(false)
        }

        //If pencilUndoList is greater than zero then make the redo enable
        if (drawingUndoList.size > 0) {
            undoRedoPathsListener!!.enableDisableRedo(true)
        }
    }

    fun setRedo() {
        /*
         * First checking if the pencilUndoList array contains path or not. If path is available then removing the last path from pencilUndoList array
         * and adding that removed path into array pencilList.
         * */
        if (drawingUndoList.size > 0) {
            drawingList1!!.add(drawingUndoList.removeAt(drawingUndoList.size - 1))
            invalidate()
        }

        //If pencilUndoList is less than or equal to zero then make the redo disable
        if (drawingUndoList.size <= 0) {
            undoRedoPathsListener!!.enableDisableRedo(false)
        }

        //If pencilList is greater than zero then make the undo enable otherwise disable
        if (drawingList1!!.size > 0) {
            undoRedoPathsListener!!.enableDisableUndo(true)
        } else {
            undoRedoPathsListener!!.enableDisableUndo(false)
        }
    }

    fun deleteDrawing() {
        savedBitmap = null
        canvasBitmap = Bitmap.createBitmap(width1, height1, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(canvasBitmap!!)
        drawingList1!!.clear()
        drawingUndoList.clear()
        invalidate()
    }

    //Removing all drawn path
    fun clear() {
        if (drawingList1!!.size > 0 || drawingUndoList.size > 0) {
            if (savedBitmap != null) {
                val loadedBitmap = Bitmap.createBitmap(savedBitmap!!)
                canvasBitmap = loadedBitmap.copy(Bitmap.Config.ARGB_8888, true)
                mCanvas = Canvas(canvasBitmap!!)
            } else {
                canvasBitmap = Bitmap.createBitmap(width1, height1, Bitmap.Config.ARGB_8888)
                mCanvas = Canvas(canvasBitmap!!)
            }
            isUndoRedo = true
            drawingList1!!.clear()
            drawingUndoList.clear()
            invalidate()
        }
    }

    //This is used to calculate the degree for showing arrow head
    private fun calculateDegree(x1: Float, x2: Float, y1: Float, y2: Float): Double {
        var startRadians = atan(((y2 - y1) / (x2 - x1)).toDouble()).toFloat()
        startRadians += ((if (x2 >= x1) 90 else -90) * Math.PI / 180).toFloat()
        return Math.toDegrees(startRadians.toDouble())
    }

    //This method is used to reset the zooming on canvas.
    private fun resetScaling() {
        mMatrix = Matrix()
        scaleFactor = 1f
        invalidate()
        setZoomLevelListener!!.setZoomLevel(scaleFactor)
    }

    //This listener is used to measure the scale factor for Zoom In/Out
    private var scaleListener: OnScaleGestureListener = object : SimpleOnScaleGestureListener() {
        override fun onScale(scaleDetector: ScaleGestureDetector): Boolean {
            scaleFactor *= scaleDetector.scaleFactor
            if (scaleFactor > 2.2) {
                scaleFactor = 2.2f
            } else if (scaleFactor < 0.3f) {
                scaleFactor = 0.3f
            }
            scaleFactor = (scaleFactor * 100).toInt().toFloat() / 100 //jitter-protection
            mMatrix!!.setScale(scaleFactor, scaleFactor, scaleDetector.focusX, scaleDetector.focusY)

            /*scaleFactor = scaleDetector.getScaleFactor();
            mMatrix.postScale(scaleFactor, scaleFactor, getWidth(), getHeight());
            ViewCompat.postInvalidateOnAnimation(DrawingPad.this);*/Log.d("check_zooming", "Value: $scaleFactor")
            setZoomLevelListener!!.setZoomLevel(scaleFactor)
            return true
        }
    }

    //This listener is use to detect gesture of scrolling into the screen. This listener is also used to handle double touch on screen.
    private var listener: GestureDetector.OnGestureListener = object : SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, dX: Float, dY: Float): Boolean {
            if (scaleFactor <= 1.0f) {
                return true
            }
            mMatrix!!.postTranslate(-dX, -dY)
            ViewCompat.postInvalidateOnAnimation(this@DrawingPad)
            Log.d("check_touch", "SimpleOnGestureListener")
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            resetScaling()
            Log.d("check_touch", "onDoubleTap")
            return super.onDoubleTap(e)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val scale = (w / mBoardWidth).coerceAtLeast(h / mBoardHeight)
        //scaleFactor = ((float)((int)(scaleFactor * 100))) / 100;//jitter-protection
        //mMatrix.setScale(scaleFactor, scaleFactor);
        //mMatrix.postTranslate((w - scale * mBoardWidth) / 2f, (h - scale * mBoardHeight) / 2f);
        Log.d("check_touch", "OnSizeChange")
    }

    companion object {
        private const val TOUCH_TOLERANCE = 4f
    }
}