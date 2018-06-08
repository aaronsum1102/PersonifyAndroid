package aaronsum.sda.com.personifyandroid

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.support.media.ExifInterface
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
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
    const val PERMISSION_REQUEST_CODE = 100

    fun hideSoftKeyboard(activity: FragmentActivity?, view: View) {
        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun chooseImage(fragment: Fragment) {
        val context = fragment.context
        context?.let {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            val createChooser = Intent.createChooser(intent, "Choose Image")
            if (createChooser.resolveActivity(context.packageManager) != null) {
                fragment.startActivityForResult(createChooser, SignUpFragment.IMAGE_REQUEST_CODE)
            }
        }
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
            }
        }
        return null
    }

    fun resizeImage(uri: Uri?, context: Context?): Uri? {
        uri?.let {
            val bitmap = getBitmap(uri, context)
            bitmap?.let {
                val ratio = 0.5
                val newImage = Bitmap.createScaledBitmap(bitmap,
                        (bitmap.width * ratio).toInt(),
                        (bitmap.height * ratio).toInt(), false)
                val outputStream = ByteArrayOutputStream()
                newImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                val file = createImageFIle(context)
                file?.writeBytes(outputStream.toByteArray())
                outputStream.flush()
                outputStream.close()
                return getUriForFile(file, context)
            }
        }
        return null
    }

    private fun getBitmap(uri: Uri, context: Context?): Bitmap? {
        context?.let {
            val cursor = context.contentResolver.query(uri,
                    arrayOf(MediaStore.Images.Media.DATA),
                    null, null, null)
            cursor.moveToFirst()
            val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            cursor.close()
            return BitmapFactory.decodeFile(path)
        }
        return null
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

    fun getDaysDifference(date: String): Int {
        val dateInFormat = dateFormat.parse(date)
        val currentDate = dateFormat.parse(getCurrentDate())
        return ((dateInFormat.time - currentDate.time) / (1000 * 60 * 60 * 24)).toInt()
    }

    fun fetchPhoto(target: Target, picMetadataMetaData: PicMetadata) {
        val requestCreator = Picasso.get().load(picMetadataMetaData.url)
        requestCreator.fetch(object : Callback {
            override fun onSuccess() {
                try {
                    val orientation = picMetadataMetaData.orientation.toInt()
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> {
                            requestCreator.rotate(90f).centerCrop().resize(600, 600).into(target)
                        }
                        ExifInterface.ORIENTATION_ROTATE_180 -> {
                            requestCreator.rotate(180f).centerCrop().resize(600, 600).into(target)
                        }
                        ExifInterface.ORIENTATION_ROTATE_270 -> {
                            requestCreator.rotate(270f).centerCrop().resize(600, 600).into(target)
                        }
                        else -> {
                            requestCreator.centerCrop().resize(600, 600).into(target)
                        }
                    }
                } catch (exception: NumberFormatException) {
                }
            }

            override fun onError(e: Exception?) {}
        })
    }

    fun checkForPermission(fragment: Fragment) {
        val context = fragment.context
        val permissionRequired = Manifest.permission.READ_EXTERNAL_STORAGE
        context?.let {
            if (ContextCompat.checkSelfPermission(context, permissionRequired)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity,
                                permissionRequired)) {
                    AlertDialog.Builder(context)
                            .setTitle(context.getString(R.string.alert_title_request_for_permission))
                            .setMessage(context.getString(R.string.permission_dialog_content))
                            .setPositiveButton(context.getString(R.string.acknowledge)) { dialog, _ ->
                                dialog.dismiss()
                                fragment.requestPermissions(arrayOf(permissionRequired),
                                        PERMISSION_REQUEST_CODE)
                            }
                            .create()
                            .show()
                } else {
                    fragment.requestPermissions(arrayOf(permissionRequired),
                            PERMISSION_REQUEST_CODE)
                }
            } else {
                chooseImage(fragment)
            }
        }
    }
}