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
import java.io.File
import java.io.IOException

object Util {
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
//                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                intent.putExtra("return-data", true)
                fragment.startActivityForResult(intent, SignUpFragment.CAMERA_REQUEST_CODE)
//                fragment.startActivity(intent)
            }
        }
        return file
    }

    fun getUriForFile(file: File?, context: Context?): Uri? {
        val authority = "com.aaronsum.personify.fileprovider"
        if (file != null && context != null) {
            return FileProvider.getUriForFile(context, authority, file)
        }
        return null
    }

    private fun createImageFIle(context: Context?): File? {
        context?.let {
            try {
                val file = File(it.filesDir, "images")
                return createTempFile("tmp", ".jpg", file)
            } catch (exception: IOException) {
                Log.e("TAG", "unable to create file. ${exception.message}")
            }
        }
        return null
    }
}