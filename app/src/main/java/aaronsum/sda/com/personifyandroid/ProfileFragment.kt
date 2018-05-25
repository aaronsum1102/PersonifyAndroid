package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.PopupMenu
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_profile.*
import java.lang.Exception

class ProfileFragment : Fragment() {
    companion object {
        const val PROFILE_BACK_STACK = "profile"
    }

    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProviders.of(activity!!)[UserViewModel::class.java]
        userViewModel.currentUser.observe(this, Observer { user ->
            user?.let { initialisedToolbar(it.username) }
        })

        val photoViewModel = ViewModelProviders.of(activity!!)[PhotoViewModel::class.java]
        photoViewModel.profilePhotoUrl.observe(this, Observer { uri ->
            uri?.let {
                Picasso.get().load(it).into(object : Target {
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        profilePhoto.background = BitmapDrawable(resources, bitmap)
                    }
                })
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
                    menu.setOnMenuItemClickListener { settingMenuSelector(it) }
                    menu.inflate(R.menu.profile_setting_menu)
                    menu.show()
                }

                else -> activity?.onBackPressed()
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
                userViewModel.signOut()
                fragmentManager?.apply {
                    popBackStack()
                    beginTransaction()
                            .replace(R.id.container, UserManagementFragment())
                            .commit()
                }
            }
        }
        return true
    }
}