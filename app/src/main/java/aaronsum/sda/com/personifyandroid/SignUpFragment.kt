package aaronsum.sda.com.personifyandroid

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_signup.*
import java.io.File
import java.lang.Exception

data class UserInfo(val name: String, val email: String, val password: String)

class SignUpFragment : Fragment(), TextWatcher {
    companion object {
        const val CAMERA_REQUEST_CODE = 100
    }

    private val TAG = "SignUpFragment"
    private lateinit var userViewModel: UserViewModel
    private var fileToUpload: File? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel = ViewModelProviders.of(activity!!)[UserViewModel::class.java]

        createAccountButton.isEnabled = false

        nameText.addTextChangedListener(this)
        emailText.addTextChangedListener(this)
        passwordText.addTextChangedListener(this)
        verifyPasswordText.addTextChangedListener(this)

        createAccountButton.setOnClickListener {
            val userInfo = UserInfo(nameText.text.toString(),
                    emailText.text.toString(),
                    passwordText.text.toString())
            createNewAccount(userInfo)
        }

        addPhoto.setOnClickListener {
            fileToUpload = Util.cameraIntent(this)
        }
    }

    private fun createNewAccount(userInfo: UserInfo) {
        userViewModel.createNewUser(userInfo)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.continueWith {
                            val userId = it.result.user.uid
                            addTaskEventListener(userId)
                            initalisedUserStatisticCollection(userId)
                            uploadProfilePhoto(userId)
                            context?.let {
                                Toast.makeText(context,
                                        "Welcome, ${userInfo.name}", Toast.LENGTH_SHORT)
                                        .show()
                            }
                            view?.let { Util.hideSoftKeyboard(activity, view as View) }
                            fragmentManager?.popBackStack("welcome",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE)
                            fragmentManager
                                    ?.beginTransaction()
                                    ?.replace(R.id.container, TaskListFragment())
                                    ?.commit()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    context?.let {
                        Toast.makeText(context,
                                "Failed to create an account. ${exception.localizedMessage}",
                                Toast.LENGTH_LONG)
                                .show()
                    }
                }
    }

    private fun addTaskEventListener(userId: String) {
        val taskViewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
        taskViewModel.addEventListenerToDB(userId)
    }

    private fun initalisedUserStatisticCollection(userId: String) {
        val userStatisticViewModel = ViewModelProviders.of(activity!!)[UserStatisticViewModel::class.java]
        userStatisticViewModel.loadUserStatistic(userId)
    }

    private fun uploadProfilePhoto(userId: String) {
        val photoViewModel = ViewModelProviders.of(activity!!)[PhotoViewModel::class.java]
        photoViewModel.initProfilePhotoDocument(userId)
        val uri = Util.getUriForFile(fileToUpload, context)
        uri?.let {
            photoViewModel.uploadPhoto(it)
                    ?.continueWith {
                        it.result.storage.downloadUrl.addOnSuccessListener {
                            photoViewModel.writeUserProfilePictureURL(it)
                        }
                    }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    fileToUpload?.let {
                        val uri = Util.getUriForFile(fileToUpload!!, context)
                        uri?.let {
                            Picasso.get().load(uri).fetch(object : Callback {
                                override fun onSuccess() {
                                    Picasso.get().load(uri).into(object : Target {
                                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                                        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

                                        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                            profilePhoto.background = BitmapDrawable(resources, bitmap)
                                        }
                                    })
                                }

                                override fun onError(e: Exception?) {
                                    Log.e(TAG, "something went wrong. ${e?.message}.")
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
        createAccountButton.isEnabled = nameText.text.isNotEmpty()
                && emailText.text.contains("@")
                && passwordText.text.toString() == verifyPasswordText.text.toString()
                && passwordText.text.length >= 6
        if (passwordText.text.length == verifyPasswordText.text.length &&
                passwordText.text.toString() != verifyPasswordText.text.toString() &&
                passwordText.text.isNotEmpty()) {
            context?.let {
                Toast.makeText(context, "Password doesn't match.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}