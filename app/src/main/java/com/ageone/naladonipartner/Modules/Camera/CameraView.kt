package com.ageone.naladonipartner.Modules.Camera

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.hardware.Camera
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ageone.naladonipartner.Application.currentActivity
import com.ageone.naladonipartner.External.Base.Module.BaseModule
import com.ageone.naladonipartner.External.Base.TextView.BaseTextView
import com.ageone.naladonipartner.External.Base.View.BaseView
import com.ageone.naladonipartner.External.InitModuleUI
import com.ageone.naladonipartner.External.Libraries.Alert.alertManager
import com.ageone.naladonipartner.External.Libraries.Alert.single
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import yummypets.com.stevia.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class CameraView(initModuleUI: InitModuleUI = InitModuleUI()) : BaseModule(initModuleUI),SurfaceHolder.Callback, Camera.PictureCallback {


    val viewModel = CameraViewModel()

    private var surfaceHolder: SurfaceHolder? = null
    private var camera: Camera? = null
    private var surfaceView: SurfaceView? = null

    private val neededPermissions = arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE)

    val surfaceViewBase by lazy {
        val surface = SurfaceView(currentActivity)
        surface
    }

    val transparentView by lazy {
        val view = BaseView()
        view.alpha = 0.3F
        view.backgroundColor = Color.BLACK
        view.initialize()
        view
    }

    val textViewCamera by lazy {
        val textView = BaseTextView()
        textView.textSize = 23F
        textView.textColor = Color.WHITE
        textView.typeface = Typeface.DEFAULT_BOLD
        textView.backgroundColor = Color.TRANSPARENT
        textView.text = "Отсканируйте QR-код"
        textView
    }

    val textViewDescription by lazy {
        val textView = BaseTextView()
        textView.textSize = 15F
        textView.textColor = Color.WHITE
        textView.backgroundColor = Color.TRANSPARENT
        textView.gravity = Gravity.CENTER_HORIZONTAL
        textView.text = "Наведите камеру на экран\nклиента и считайте код"
        textView
    }

    init {
//        viewModel.loadRealmData()

        setBackgroundColor(Color.WHITE)

        toolbar.title = "Вкусная шаверма"
        toolbar.setBackgroundColor(Color.parseColor("#F06F28"))
        toolbar.textColor = Color.WHITE

        renderToolbar()

        renderUIO()
        surfaceView = surfaceViewBase

        val result = checkPermission()
        var cameraStream:ByteArray = ByteArray(1)
        if(result) {
            setupSurfaceHolder()
            captureImage()

        }

        bindUI()
    }

    fun bindUI() {
        /*compositeDisposable.add(
            RxBus.listen(RxEvent.Event::class.java).subscribe {//TODO: change type event
                bodyTable.adapter?.notifyDataSetChanged()
            }
        )*/
    }

        fun setupSurfaceHolder() {
            surfaceView!!.visibility = View.VISIBLE
            surfaceHolder = surfaceView!!.holder
            surfaceHolder!!.addCallback(this)
        }


        private fun startCamera() {
            camera = Camera.open()
            camera!!.setDisplayOrientation(90)
            try {
                camera!!.setPreviewDisplay(surfaceHolder)
                camera!!.startPreview()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
            startCamera()
        }

        override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
            resetCamera()
        }

        private fun resetCamera() {
            if (surfaceHolder!!.surface == null) {
                return
            }
            camera!!.stopPreview()
            try {
                camera!!.setPreviewDisplay(surfaceHolder)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            camera?.startPreview()
        }

        override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
            releaseCamera()
        }


        private fun releaseCamera() {
            camera?.stopPreview()
            camera?.release()
            camera = null
        }

    private fun checkPermission(): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            val permissionsNotGranted = ArrayList<String>()
            for (permission in neededPermissions) {
                if (ContextCompat.checkSelfPermission(
                        currentActivity!!,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsNotGranted.add(permission)
                }
            }
            if (permissionsNotGranted.size > 0) {
                var shouldShowAlert = false
                for (permission in permissionsNotGranted) {
                    shouldShowAlert = ActivityCompat.shouldShowRequestPermissionRationale(
                        currentActivity!!, permission
                    )
                }

                val arr = arrayOfNulls<String>(permissionsNotGranted.size)
                val permissions = permissionsNotGranted.toArray(arr)
                if (shouldShowAlert) {
                    showPermissionAlert(permissions)
                } else {
                    requestPermissions(permissions)
                }
                return false
            }
        }
        return true
    }

    private fun showPermissionAlert(permissions: Array<String?>) {
        alertManager.single(
            "Требуется разрешение",
            "Вы должны предоставить разрешение на доступ к камере для запуска этого приложения.",
            null,
            "Ok"
        ) { _, position ->
            if (position == 0) {
                requestPermissions(permissions)
            }
        }
    }


    private fun requestPermissions(permissions: Array<String?>) {
        ActivityCompat.requestPermissions(currentActivity!!, permissions, REQUEST_CODE)
    }

    override fun onPictureTaken(p0: ByteArray?, p1: Camera?) {
        Timber.i("Byte array : $p0")
        saveImage(p0)
        resetCamera()
        //saveByteArray(p0)
    }

    private fun saveImage(bytes: ByteArray?) {
        val outStream: FileOutputStream
        try {
            val fileName = "naladoni" + System.currentTimeMillis() + ".jpg"
            val file = File(Environment.getExternalStorageDirectory(), fileName)
            outStream = FileOutputStream(file)
            outStream.write(bytes)
            Timber.i("ByteArray : ${bytes.toString()}")
            outStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun saveByteArray(byteArray: ByteArray?){
        var photobByteArray = byteArray
        Timber.i("ByteArray : $photobByteArray")
    }

    fun captureImage(){
        if(camera != null){
            camera!!.takePicture(null,null,this)
        }
    }

    companion object {
        const val REQUEST_CODE = 100
    }
}

fun CameraView.renderUIO() {
    innerContent.subviews(
        surfaceViewBase,
        transparentView,
        textViewCamera,
        textViewDescription

    )

    surfaceViewBase
        .fillHorizontally()
        .fillVertically()

    transparentView
        .constrainTopToTopOf(innerContent)
        .fillHorizontally()
        .height(112)
        .setOnClickListener { captureImage() }

    textViewCamera
        .constrainTopToTopOf(transparentView,20)
        .constrainCenterXToCenterXOf(innerContent)

    textViewDescription
        .constrainTopToBottomOf(textViewCamera,8)
        .constrainCenterXToCenterXOf(textViewCamera)
}





