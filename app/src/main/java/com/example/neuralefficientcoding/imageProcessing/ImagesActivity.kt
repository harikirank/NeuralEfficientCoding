package com.example.neuralefficientcoding.imageProcessing

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.example.neuralefficientcoding.BitmapUtils
import com.example.neuralefficientcoding.R
import com.example.neuralefficientcoding.databinding.ActivityImageBinding
import com.example.neuralefficientcoding.fastICA.FastICA
import com.example.neuralefficientcoding.imagePatches.imagePatches
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

const val REQUEST_IMAGE_CAPTURE = 96
const val RESULT_LOAD_IMAGE = 169
const val FILE_PROVIDER_AUTHORITY = "com.example.neuralefficientcoding.fileprovider"


class ImagesActivity : AppCompatActivity() {
    lateinit var binding: ActivityImageBinding
    private var image: File? = null
    private var currentImagePath: String? = null
    private var appExecutor: AppExecutor? = null
    var patchData: Array<DoubleArray>? = null
    var mixing: Array<DoubleArray>? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_image)
        val value = intent.getStringExtra("key")
        appExecutor = AppExecutor()

        hideActionBar()
        hideProcessingButtonsAndViews()

        binding.buttonBack.setOnClickListener {
            finishCurrentActivity()
        }

        binding.buttonHome.setOnClickListener {
            finishCurrentActivity()
        }

        binding.buttonTakePicture.setOnClickListener {
            if (notAllThreePermissionsGranted()
            ) {
                ActivityCompat.requestPermissions(this,
                    permissions(),
                    1)
            } else {
                launchCamera()
            }
        }

        binding.buttonSelectImage.setOnClickListener {
            launchImagePickingIntent()
        }

        binding.textViewCloser.setOnClickListener {
            if (mixing != null && patchData != null) {
                val imageProcessedIntent = Intent(this, ImageProcessed::class.java)
                val test: Bitmap = imagePatches.showPatches(imagePatches.transpose(mixing), 25)
                val imageURI: Uri = getImageUri(baseContext, test)
                val str = imageURI.toString()
                imageProcessedIntent.putExtra("str", str)
                startActivity(imageProcessedIntent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                Toast.makeText(this, "Please process the image first", Toast.LENGTH_SHORT).show()
            }

        }

        binding.buttonProcessImage.setOnClickListener {
            MyAsyncTask().execute()
        }
    }

    private fun hideActionBar() {
        supportActionBar?.hide()
    }

    private fun hideProcessingButtonsAndViews() {
        binding.buttonProcessImage.visibility = View.GONE
        binding.processImage.visibility = View.GONE
        binding.textViewCloser.visibility = View.GONE
    }

    private fun finishCurrentActivity() {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun notAllThreePermissionsGranted(): Boolean {
        return !(ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED)
    }

    private fun launchCamera() {
        val intentCaptureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            image = BitmapUtils.createTempImageFile(this)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (image != null) {
            currentImagePath = image!!.absolutePath
            val uri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, image!!)
            intentCaptureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(intentCaptureImage, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun launchImagePickingIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, RESULT_LOAD_IMAGE)
    }

    private var storagePermissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    var storagePermissions33 = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VIDEO
    )

    private fun permissions(): Array<String> {
        val p: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storagePermissions33
        } else {
            storagePermissions
        }
        return p
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val imageURI = data?.data
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            processAndSetImage()

            showProcessingButtonsAndViews()
        } else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            binding.imageSelectedImagePreview.setImageURI(imageURI)

            showProcessingButtonsAndViews()
        } else {
            if (currentImagePath != null) {
                BitmapUtils.deleteImageFile(this, currentImagePath)
            }
        }
    }

    private fun showProcessingButtonsAndViews() {
        binding.processImage.visibility = View.VISIBLE
        binding.textViewCloser.visibility = View.VISIBLE
        binding.imageSelectedImagePreview.visibility = View.VISIBLE
        binding.buttonProcessImage.visibility = View.VISIBLE
    }
    private fun processAndSetImage() {
        val resultBitmap = BitmapUtils.resamplePic(this, currentImagePath)
        binding.imageSelectedImagePreview.setImageBitmap(resultBitmap)

        //Save Image
        appExecutor!!.diskIO().execute {
            BitmapUtils.deleteImageFile(this, currentImagePath)

            BitmapUtils.saveImage(this, resultBitmap)
        }
    }

    inner class MyAsyncTask : AsyncTask<String?, Int?, Boolean>() {
        private var progressDialog: ProgressDialog? = null
        override fun onPreExecute() {
            progressDialog = ProgressDialog(this@ImagesActivity)
            progressDialog?.show()
            progressDialog?.setContentView(R.layout.progress_dialog)
            progressDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            progressDialog?.setCancelable(false)
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        override fun doInBackground(vararg params: String?): Boolean {
            heavyProcessImage()
            return true
        }


        override fun onPostExecute(aBoolean: Boolean) {
            //super.onPostExecute(aBoolean);
            progressDialog!!.dismiss()
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        private fun heavyProcessImage() {
            val drawable = binding.imageSelectedImagePreview.drawable as BitmapDrawable
            val bitmap = drawable.bitmap
            patchData = imagePatches.get_patches(100, 8, bitmap)
            val fastICA = FastICA()
            try {
                fastICA.fit(imagePatches.transpose(patchData), 25)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mixing = fastICA.em
            val test = imagePatches.showPatches(imagePatches.transpose(mixing), 25)
            if (test == null) Log.d("NULL", "Bitmap is Null")
            val testBit = (binding.imageSelectedImagePreview.drawable as BitmapDrawable).bitmap
            val imageURI: Uri = getImageUri(this@ImagesActivity, test)

            runOnUiThread {
                binding.imageSelectedImagePreview.setImageURI(imageURI)
            }
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

}