package aaronsum.sda.com.personifyandroid

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.support.media.ExifInterface
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object Util {
    private const val TAG = "util"
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val persistenceDBSetting = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

    fun hideSoftKeyboard(activity: FragmentActivity?, view: View) {
        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun cameraIntent(fragment: Fragment): File? {
        val context = fragment.context
        val file = createImageFIle(context)
        file?.let { file ->
            context?.let { context ->
                val uriForFile = getUriForFile(file, context)
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile)
                fragment.startActivityForResult(intent, SignUpFragment.CAMERA_REQUEST_CODE)
            }
        }
        return file
    }

    fun getUriForFile(file: File?, context: Context?): Uri? {
        val authority = "${context?.packageName}.fileprovider"
        if (file != null && context != null) {
            return FileProvider.getUriForFile(context, authority, file)
        }
        return null
    }

    private fun createImageFIle(context: Context?): File? {
        context?.let {
            try {
                val file = File(it.filesDir, "images")
                if (!file.exists()) {
                    file.mkdir()
                }
                return File.createTempFile("tmp", ".jpg", file)
            } catch (exception: IOException) {
                Log.e(TAG, "unable to create file. ${exception.message}")
            }
        }
        return null
    }

    fun resizeImage(uri: Uri?, file: File, context: Context?): File {
        uri?.let {
            val openInputStream = context?.contentResolver?.openInputStream(uri)
            openInputStream?.let {
                val bitmap = BitmapFactory.decodeStream(openInputStream)
                val ratio = 0.4
                val newImage = Bitmap.createScaledBitmap(bitmap,
                        (bitmap.width * ratio).toInt(),
                        (bitmap.height * ratio).toInt(), false)
                val outputStream = ByteArrayOutputStream()
                newImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                file.writeBytes(outputStream.toByteArray())
                outputStream.flush()
                outputStream.close()
            }
        }
        return file
    }

    fun getPicOrientation(uri: Uri?, context: Context?): String? {
        var rotation: String? = null
        if (uri != null && context != null) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exifInterface = ExifInterface(inputStream)
            rotation = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION)
        }
        return rotation
    }

    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        calendar.set(calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH],
                0, 0, 0)
        return dateFormat.format(calendar.time)
    }

    fun getDaysDifference(dueDate: String): Int {
        val date = dateFormat.parse(dueDate)
        val currentDate = dateFormat.parse(getCurrentDate())
        return ((date.time - currentDate.time) / (1000 * 60 * 60 * 24)).toInt()
    }

    fun fetchPhoto(target: Target, picMetadataMetaData: PicMetadata) {
        val requestCreator = Picasso.get().load(picMetadataMetaData.url)
        requestCreator.fetch(object : Callback {
            override fun onSuccess() {
                try {
                    val orientation = picMetadataMetaData.orientation.toInt()
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> {
                            requestCreator.rotate(90f).into(target)
                        }
                        ExifInterface.ORIENTATION_ROTATE_180 -> {
                            requestCreator.rotate(180f).into(target)
                        }
                        ExifInterface.ORIENTATION_ROTATE_270 -> {
                            requestCreator.rotate(270f).into(target)
                        }
                        else -> {
                            requestCreator.into(target)
                        }
                    }
                } catch (exception: NumberFormatException) {
                    Log.e(TAG, "something wrong with pic orientation. ${picMetadataMetaData.orientation}")
                }
            }

            override fun onError(e: Exception?) {
                Log.e(TAG, "something went wrong when fetching photo. ${e?.message}.")
            }
        })
    }
}