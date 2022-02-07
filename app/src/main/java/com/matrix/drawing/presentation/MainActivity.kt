package com.matrix.drawing.presentation

import android.Manifest
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.matrix.drawing.R
import com.matrix.drawing.data.model.Drawing
import com.matrix.drawing.databinding.ActivityMainBinding
import com.matrix.drawing.domain.*
import com.matrix.drawing.domain.custom_view.DrawingPad
import com.matrix.drawing.domain.utils.AppAlertDialog.showAlertMessageWithTwoButtons
import com.matrix.drawing.domain.utils.AppCalculateZoomLevel.getZoomPercentage
import com.matrix.drawing.domain.utils.AppConstant
import com.matrix.drawing.domain.utils.AppSelectionColor.show
import com.matrix.drawing.domain.utils.AppSelectionDrawing.show
import com.matrix.drawing.domain.utils.AppSelectionStroke.show
import com.matrix.drawing.domain.utils.AppSharedPreference.addIntegerPreference
import com.matrix.drawing.domain.utils.AppSharedPreference.addStringPreference
import com.matrix.drawing.domain.utils.AppSharedPreference.getIntegerPreference
import com.matrix.drawing.domain.utils.AppSharedPreference.getStringPreference
import com.matrix.drawing.domain.utils.AppToastMessage.showMessage
import java.io.*


class MainActivity : AppCompatActivity(), View.OnClickListener, OnSelectEraser, OnUndoRedoPaths, OnSetZoomLevel {

    private lateinit var viewBinding: ActivityMainBinding
    private var drawingPad: DrawingPad? = null
    private var selectedColor = 0
    private var selectedStrokeWidth = 0
    private var isEraserModeActive = false

    private val permissions = arrayOf<String>(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val MULTIPLE_PERMISSIONS = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

//        setContentView(viewBinding.root)
        initializations()

        if (checkPermissions()) {
            //  permissions  granted.
        } else {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialog.setMessage("Give storage permission to save drawings.")
                .setTitle("Storage permissions needed.")
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog, which ->
                    if (checkPermissions()) {
                        dialog.dismiss()
                    } else {
                        finishAndRemoveTask()
                    }
                }
            val dialog: AlertDialog = alertDialog.create()
            dialog.show()
        }

        //Initialing the height and width of the custom drawing pad view
        drawingPad = viewBinding.drawingPad
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        var oldBitmap: Bitmap? = null
        val filePath = getStringPreference(this, AppConstant.PREFERENCE_FILE_PATH, "")
        if (!filePath!!.isEmpty()) {
            oldBitmap = loadImageFromStorage(filePath)
            Log.d("check_file", "" + filePath)
            addIntegerPreference(this, AppConstant.PREFERENCE_SELECTED_DRAWING_TYPE, AppConstant.DRAWING_TYPE_ZOOMING)
            updateDrawingType(AppConstant.DRAWING_TYPE_ZOOMING)
        }
        drawingPad!!.initialize(metrics, oldBitmap, viewBinding.drawingPad, this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_drawing -> {
                val defaultDrawing = getIntegerPreference(
                    this,
                    AppConstant.PREFERENCE_SELECTED_DRAWING_TYPE,
                    AppConstant.DRAWING_TYPE_PENCIL
                )
                show(this, defaultDrawing, object : OnSelectDrawing {
                    override fun selectDrawing(drawing: Int) {
                        if (drawing == AppConstant.DRAWING_TYPE_ERASER) {
                            //isEraserModeActive = true;
                            if (drawingPad!!.drawingCount > 0) {
                                updateDrawingType(drawing)
                            } else {
                                showMessage(this@MainActivity, getString(R.string.no_path_drawn_yet))
                            }
                        } else {
                            updateDrawingType(drawing)
                        }
                    }
                })
            }
            R.id.tv_paint_color -> {
                val defaultColor =
                    getIntegerPreference(this, AppConstant.PREFERENCE_SELECTED_PAINT_COLOR, AppConstant.colorBlack)
                show(this, defaultColor, object : OnSelectColor {
                    override fun selectColor(color: Int) {
                        drawingPad!!.setPaintColor(color)
                        addIntegerPreference(this@MainActivity, AppConstant.PREFERENCE_SELECTED_PAINT_COLOR, color)
                        changeSelectionColor(color)
                        selectedColor = color
                    }
                })
                disableEraser()
            }
            R.id.iv_paint_width -> {
                val defaultStrokeWidth =
                    getIntegerPreference(this, AppConstant.PREFERENCE_SELECTED_PAINT_STROKE, AppConstant.STROKE_WIDTH_3)
                show(this, defaultStrokeWidth, object : OnSelectStroke {
                    override fun selectStroke(width: Int) {
                        drawingPad!!.setPaintStroke(width)
                        addIntegerPreference(this@MainActivity, AppConstant.PREFERENCE_SELECTED_PAINT_STROKE, width)
                        selectedStrokeWidth = width
                    }
                })
                disableEraser()
            }
            R.id.iv_eraser -> drawingPad!!.enableDisableEraser()
            R.id.iv_undo -> {
                drawingPad!!.setUndo()
                disableEraser()
            }
            R.id.iv_redo -> {
                drawingPad!!.setRedo()
                disableEraser()
            }
            R.id.iv_clear_all -> {
                drawingPad!!.clear()
                disableEraser()
                viewBinding.ivUndo.alpha = 0.4f
                viewBinding.ivRedo.alpha = 0.4f
            }
            R.id.iv_save -> {
                if (drawingPad!!.drawingList1!!.size <= 0) {
                    showMessage(this@MainActivity, "No Drawing Available.")
                    return
                }
                saveDrawing(drawingPad!!.canvasBitmap)
            }
            R.id.iv_delete -> showAlertMessageWithTwoButtons(this, object : OnClickAlertDialogTwoButtons {
                override fun clickPositiveDialogButton(dialog_name: String?) {
                    addStringPreference(this@MainActivity, AppConstant.PREFERENCE_FILE_PATH, "")
                    drawingPad!!.deleteDrawing()
                    updateDrawingType(AppConstant.DRAWING_TYPE_PENCIL)
                }

                override fun clickNegativeDialogButton(dialog_name: String?) {}
            }, "", "", "Are you sure to delete?", getString(R.string.yes), getString(R.string.no))
        }
    }

    override fun enableDisableUndo(isEnable: Boolean) {
        if (isEnable) {
            viewBinding.ivUndo.alpha = 1.0f
        } else {
            viewBinding.ivUndo.alpha = 0.4f
        }
    }

    override fun enableDisableRedo(isEnable: Boolean) {
        if (isEnable) {
            viewBinding.ivRedo.alpha = 1.0f
        } else {
            viewBinding.ivRedo.alpha = 0.4f
        }
    }

    override fun selectEraser(drawingList: List<Drawing?>?) {
        if (drawingList!!.isNotEmpty()) {
            if (!isEraserModeActive) {
                enableEraser()
            } else {
                disableEraser()
            }
        } else {
            showMessage(this@MainActivity, getString(R.string.no_path_drawn_yet))
        }
    }

    override fun setZoomLevel(scaleFactor: Float) {
        Log.d("check_scale", "" + scaleFactor)
        viewBinding.tvToolbarTitle.text =
            getText(R.string.app_name).toString() + " (" + getZoomPercentage(scaleFactor) + "%)"
    }

    private fun initializations() {
        setSupportActionBar(viewBinding.toolbarScreen)
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }
        viewBinding.ivDrawing.setOnClickListener(this)
        viewBinding.tvPaintColor.setOnClickListener(this)
        viewBinding.ivPaintWidth.setOnClickListener(this)
        viewBinding.ivEraser.setOnClickListener(this)
        viewBinding.ivUndo.setOnClickListener(this)
        viewBinding.ivRedo.setOnClickListener(this)
        viewBinding.ivClearAll.setOnClickListener(this)
        viewBinding.ivSave.setOnClickListener(this)
        viewBinding.ivDelete.setOnClickListener(this)

        //Setting the undo/redo buttons not selectable.
        viewBinding.ivUndo.alpha = 0.4f
        viewBinding.ivRedo.alpha = 0.4f

        //Setting the default values for paint color and stroke width
        selectedColor = AppConstant.colorBlack
        selectedStrokeWidth = AppConstant.STROKE_WIDTH_1
        addIntegerPreference(this, AppConstant.PREFERENCE_SELECTED_DRAWING_TYPE, AppConstant.DRAWING_TYPE_PENCIL)
        addIntegerPreference(this, AppConstant.PREFERENCE_SELECTED_PAINT_COLOR, selectedColor)
        addIntegerPreference(this, AppConstant.PREFERENCE_SELECTED_PAINT_STROKE, selectedStrokeWidth)

        //setting default zoom level
        setZoomLevel(1.5f)
    }

    private fun saveDrawing(canvasBitmap: Bitmap?) {
        if (canvasBitmap != null) {
            val filePath = saveToInternalStorage(canvasBitmap)
            addStringPreference(this, AppConstant.PREFERENCE_FILE_PATH, filePath)
            showMessage(this@MainActivity, getString(R.string.drawing_save))
        }
    }

    private fun setSelectedDrawing(drawing: Int) {
        when (drawing) {
            AppConstant.DRAWING_TYPE_PENCIL -> {
                drawingPad!!.setPaintStroke(AppConstant.STROKE_WIDTH_1)
                addIntegerPreference(
                    this@MainActivity,
                    AppConstant.PREFERENCE_SELECTED_PAINT_STROKE,
                    AppConstant.STROKE_WIDTH_1
                )
                viewBinding.ivDrawing.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_pencil
                    )
                )
            }
            AppConstant.DRAWING_TYPE_ERASER ->                 //drawingPad.setPaintStroke(AppConstant.STROKE_WIDTH_5);
                viewBinding.ivDrawing.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.ic_eraser
                    )
                )
            AppConstant.DRAWING_TYPE_LINE -> viewBinding.ivDrawing.setImageDrawable(
                ContextCompat.getDrawable(
                    this@MainActivity,
                    R.drawable.ic_line
                )
            )
            AppConstant.DRAWING_TYPE_LINE_ARROW -> viewBinding.ivDrawing.setImageDrawable(
                ContextCompat.getDrawable(
                    this@MainActivity,
                    R.drawable.ic_line_arrow
                )
            )
            AppConstant.DRAWING_TYPE_RECTANGLE -> viewBinding.ivDrawing.setImageDrawable(
                ContextCompat.getDrawable(
                    this@MainActivity,
                    R.drawable.ic_rect
                )
            )
            AppConstant.DRAWING_TYPE_RECTANGLE_FILLED -> viewBinding.ivDrawing.setImageDrawable(
                ContextCompat.getDrawable(
                    this@MainActivity,
                    R.drawable.ic_rect_filled
                )
            )
            AppConstant.DRAWING_TYPE_CIRCLE -> viewBinding.ivDrawing.setImageDrawable(
                ContextCompat.getDrawable(
                    this@MainActivity,
                    R.drawable.ic_circle
                )
            )
            AppConstant.DRAWING_TYPE_CIRCLE_FILLED -> viewBinding.ivDrawing.setImageDrawable(
                ContextCompat.getDrawable(
                    this@MainActivity,
                    R.drawable.ic_circle_filled
                )
            )
            AppConstant.DRAWING_TYPE_OVAL-> viewBinding.ivDrawing.setImageDrawable(
                ContextCompat.getDrawable(
                    this@MainActivity,
                    R.drawable.ic_circle
                )
            )
            AppConstant.DRAWING_TYPE_ZOOMING -> viewBinding.ivDrawing.setImageDrawable(
                ContextCompat.getDrawable(
                    this@MainActivity,
                    R.drawable.ic_hand
                )
            )
        }
    }

    private fun changeSelectionColor(color: Int) {
        when (color) {
            AppConstant.colorBlack -> viewBinding.tvPaintColor.background =
                ContextCompat.getDrawable(this, R.drawable.style_circle_black)
            AppConstant.colorBlue -> viewBinding.tvPaintColor.background =
                ContextCompat.getDrawable(this, R.drawable.style_circle_blue)
            AppConstant.colorRed -> viewBinding.tvPaintColor.background =
                ContextCompat.getDrawable(this, R.drawable.style_circle_red)
            AppConstant.colorGreen -> viewBinding.tvPaintColor.background =
                ContextCompat.getDrawable(this, R.drawable.style_circle_green)
            AppConstant.colorYellow -> viewBinding.tvPaintColor.background =
                ContextCompat.getDrawable(this, R.drawable.style_circle_yellow)
        }
    }

    private fun updateDrawingType(drawing: Int) {
        drawingPad!!.setDrawing(drawing)
        setSelectedDrawing(drawing)
        addIntegerPreference(this@MainActivity, AppConstant.PREFERENCE_SELECTED_DRAWING_TYPE, drawing)
    }

    private fun enableEraser() {
        drawingPad!!.setEraserMode(Color.WHITE, AppConstant.STROKE_WIDTH_5)
        isEraserModeActive = true
        viewBinding.ivEraser.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_eraser_selected))
    }

    private fun disableEraser() {
        drawingPad!!.setEraserMode(selectedColor, selectedStrokeWidth)
        isEraserModeActive = false
        viewBinding.ivEraser.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_eraser))
    }

    private fun saveToInternalStorage(bitmapImage: Bitmap): String {
        val cw = ContextWrapper(applicationContext)
        // path to /data/data/yourapp/app_data/imageDir
        val directory = cw.getDir("imageDir", MODE_PRIVATE)
        // Create imageDir
        val mypath = File(directory, "drawing.png")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.absolutePath
    }

    private fun loadImageFromStorage(path: String?): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val f = File(path, "drawing.png")
            bitmap = BitmapFactory.decodeStream(FileInputStream(f))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return bitmap
    }

    private fun checkPermissions(): Boolean {
        var result: Int
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(this, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }

        if (SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val result1: Int
            result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            if (result1 != PackageManager.PERMISSION_GRANTED) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                    startActivityForResult(intent, 2296)
                } catch (e: java.lang.Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivityForResult(intent, 2296)
                }
            }
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), MULTIPLE_PERMISSIONS)
            return false
        }

        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MULTIPLE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}