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

class SignUpFragment : Fragment(), TextWatcher, Target {
    companion object {
        const val CAMERA_REQUEST_CODE = 100
        private const val TAG = "SignUpFragment"
    }

    private var fileToUpload: File? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addTextWatcher()

        createAccountButton.setOnClickListener {
            createAccountButton.isEnabled = false
            createNewAccount(UserInfo(
                    nameText.text.toString(),
                    emailText.text.toString(),
                    passwordText.text.toString()))
        }

        addPhoto.setOnClickListener {
            fileToUpload = Util.cameraIntent(this)
        }
    }

    private fun createNewAccount(userInfo: UserInfo) {
        val userViewModel = ViewModelProviders.of(activity!!)[UserViewModel::class.java]
        userViewModel.createNewUser(userInfo)
                ?.addOnSuccessListener {
                    val user = it.user
                    userViewModel.updateUserProfile(userInfo, user)
                            .addOnSuccessListener {
                                addTaskEventListener(user.uid)
                                initUserStatisticCollection(user.uid)
                                uploadProfilePhoto(user.uid)
                                view?.let { Util.hideSoftKeyboard(activity, view as View) }
                                user.displayName?.let { initTaskListFragment(it) }
                            }
                            .addOnFailureListener {
                                Log.i(TAG, "unable to update profile. ${it.localizedMessage}")
                            }
                }
                ?.addOnFailureListener { exception ->
                    context?.let {
                        Toast.makeText(context,
                                "Failed to create an account. ${exception.localizedMessage}",
                                Toast.LENGTH_LONG)
                                .show()
                        createAccountButton.isEnabled = true
                    }
                }
    }

    private fun addTaskEventListener(userId: String) {
        val taskViewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
        taskViewModel.initUserTaskDocument(userId)
    }

    private fun initUserStatisticCollection(userId: String) {
        val userStatisticViewModel = ViewModelProviders.of(activity!!)[UserStatisticViewModel::class.java]
        userStatisticViewModel.initUserStatistic(userId)
    }

    private fun uploadProfilePhoto(userId: String) {
        val photoViewModel = ViewModelProviders.of(activity!!)[PhotoViewModel::class.java]
        photoViewModel.initProfilePhotoDocument(userId)
        val uri = Util.getUriForFile(fileToUpload, context)
        uri?.let { internalUri ->
            photoViewModel.uploadPhoto(internalUri)
                    ?.continueWith {
                        it.result.storage.downloadUrl.addOnSuccessListener {
                            photoViewModel.writeUserProfilePictureURL(it)
                        }
                    }
        }
    }

    private fun initTaskListFragment(userName: String) {
        context?.let {
            Toast.makeText(context,
                    getString(R.string.welcome_new_user, userName), Toast.LENGTH_SHORT)
                    .show()
        }
        fragmentManager?.popBackStack(WelcomeScreenFragment.STACK_NAME,
                FragmentManager.POP_BACK_STACK_INCLUSIVE)
        fragmentManager
                ?.beginTransaction()
                ?.replace(R.id.container, TaskListFragment())
                ?.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    fileToUpload?.let {file ->
                        val uri = Util.getUriForFile(file, context)
                        fileToUpload = Util.resizeImage(uri, file, this@SignUpFragment.context)
                        uri?.let {
                            Picasso.get().load(uri).fetch(object : Callback {
                                override fun onSuccess() {
                                    Picasso.get().load(uri).into(this@SignUpFragment)
                                }

                                override fun onError(e: Exception?) {
                                    Log.e(TAG, "something went wrong. ${e?.localizedMessage}")
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    private fun addTextWatcher() {
        val fragment = this@SignUpFragment
        nameText.addTextChangedListener(fragment)
        emailText.addTextChangedListener(fragment)
        passwordText.addTextChangedListener(fragment)
        verifyPasswordText.addTextChangedListener(fragment)
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

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        profilePhoto.background = BitmapDrawable(resources, bitmap)
    }
}