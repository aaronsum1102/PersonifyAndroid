package aaronsum.sda.com.personifyandroid

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.File
import java.lang.Exception
import kotlin.math.abs

class ProfileFragment : Fragment(), Target {
    companion object {
        const val PROFILE_BACK_STACK = "profile"
    }

    private lateinit var userViewModel: UserViewModel
    private lateinit var photoViewModel: PhotoViewModel
    private var uploadedFile: File? = null
    private lateinit var user: User

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProviders.of(activity!!)[UserViewModel::class.java]
        photoViewModel = ViewModelProviders.of(activity!!)[PhotoViewModel::class.java]

        userViewModel.currentUser.observe(this, Observer { user ->
            user?.let {
                this.user = it
                initialisedToolbar(this.user.username)
            }
        })

        photoViewModel.profilePhotoMetadata.observe(this, Observer { picMetadata ->
            picMetadata?.let { Util.fetchPhoto(this, it) }
        })

        changeProfilePicButton.setOnClickListener {
            uploadedFile = Util.cameraIntent(this)
        }

        val userStatisticViewModel = ViewModelProviders.of(activity!!)[UserStatisticViewModel::class.java]
        userStatisticViewModel.userStatistics.observe(this, Observer { userStatistics ->
            userStatistics?.let {
                val earliestCompletion = userStatistics.earliestCompletion
                if (earliestCompletion != 0) {
                    earliestCompletionText.text = resources.getQuantityString(R.plurals.number_of_days, earliestCompletion, earliestCompletion)
                }
                val longestOverdue = userStatistics.longestOverdue
                if (longestOverdue != 0) {
                    longestOverDueText.text = resources.getQuantityString(R.plurals.number_of_days, abs(longestOverdue), abs(longestOverdue))
                }
                val taskCompletionRate = userStatistics.taskCompletionRate
                if (taskCompletionRate != 0) {
                    completionRateText.text = getString(R.string.number_percent, taskCompletionRate)
                }
                val taskOverdueRate = userStatistics.taskOverdueRate
                if (taskOverdueRate != 0) {
                    overDueRateText.text = getString(R.string.number_percent, taskOverdueRate)
                }
            }
        })
    }

    private fun initialisedToolbar(username: String) {
        setHasOptionsMenu(true)
        val appCompatActivity = activity as AppCompatActivity
        appCompatActivity.setSupportActionBar(profileToolbar)
        val supportActionBar = appCompatActivity.supportActionBar
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarTitle.text = username
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.profile_toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item?.itemId
        itemId?.let {
            when (itemId) {
                R.id.action_settings -> {
                    val anchor = activity?.findViewById<View>(R.id.action_settings)
                    val menu = PopupMenu(context, anchor)
                    menu.run {
                        setOnMenuItemClickListener { settingMenuSelector(it) }
                        inflate(R.menu.profile_setting_menu)
                        show()
                    }
                }

                else -> {
                    Log.d("special", "on back pressed.")
                    activity?.onBackPressed()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun settingMenuSelector(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.actionEditProfile -> {
                fragmentManager?.apply {
                    beginTransaction()
                            .replace(R.id.container, EditProfileFragment())
                            .addToBackStack(PROFILE_BACK_STACK)
                            .commit()
                }
            }

            R.id.actionLogOut -> {
                val userId = userViewModel.currentUser.value?.userId
                userId?.let {
                    val taskViewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
                    val userStatisticViewModel = ViewModelProviders.of(activity!!)[UserStatisticViewModel::class.java]
                    userViewModel.signOut()
                    photoViewModel.clearProfilePicWhenNoUser()
                    taskViewModel.clearTaskWhenNoUserInSession()
                    userStatisticViewModel.clearStatisticWhenNoUser()
                    fragmentManager?.apply {
                        popBackStack()
                        beginTransaction()
                                .replace(R.id.container, UserManagementFragment())
                                .commit()
                    }
                }
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SignUpFragment.CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    uploadedFile?.let { file ->
                        val internalUri = Util.getUriForFile(file, this@ProfileFragment.context)
                        val orientation = Util.getPicOrientation(internalUri, context)
                        uploadedFile = Util.resizeImage(internalUri, file, this@ProfileFragment.context)
                        if (internalUri != null && orientation != null) {
                            updateProfileImage(internalUri, orientation)
                        }
                    }
                }
            }
        }
    }

    private fun updateProfileImage(internalUri: Uri, orientation: String) {
        if (this::user.isInitialized) {
            photoViewModel.uploadPhoto(internalUri)
                    ?.continueWith {
                        it.result.storage.downloadUrl.addOnSuccessListener { url ->
                            photoViewModel.writeUserProfilePictureURL(PicMetadata(url.toString(), orientation))
                        }
                    }
        }
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        bitmap?.let {
            profilePhoto?.background = BitmapDrawable(resources, bitmap)
        }
    }
}
