package aaronsum.sda.com.personifyandroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object Util {
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
            context?.let {
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
                Log.e("TAG", "unable to create file. ${exception.message}")
            }
        }
        return null
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
}