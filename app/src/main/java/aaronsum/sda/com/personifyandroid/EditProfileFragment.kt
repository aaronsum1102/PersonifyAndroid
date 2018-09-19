package aaronsum.sda.com.personifyandroid

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.fragment_edit_profile.*

class EditProfileFragment : androidx.fragment.app.Fragment(), TextWatcher {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            val analytics = FirebaseAnalytics.getInstance(it)
            analytics.setCurrentScreen(it, "EditProfile", null)
        }
        initialisedToolbar()
        addTextWatcher()

        val userViewModel = ViewModelProviders.of(activity!!)[UserViewModel::class.java]

        userViewModel.currentUser.observe(this, Observer {
            newNameText.setText(it?.username)
            newEmailText.setText(it?.email)
        })

        deleteProfileButton.setOnClickListener {
            context?.let { context ->
                createDialogForAccountDeletionConfirmation(context, userViewModel)
            }
        }

        saveButton.setOnClickListener {
            userViewModel.verifyPassword(passwordText.text.toString())
                    ?.addOnSuccessListener {
                        registerNewProfileInfo(userViewModel)
                        Util.hideSoftKeyboard(activity, view)
                        fragmentManager?.popBackStack(TaskListFragment.TASK_LIST_BACK_STACK,
                                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    }
                    ?.addOnFailureListener { exception ->
                        context?.let {
                            Toast.makeText(context,
                                    "Unable to update your profile. ${exception.message}",
                                    Toast.LENGTH_LONG).show()
                        }
                    }
        }
    }

    private fun createDialogForAccountDeletionConfirmation(context: Context, userViewModel: UserViewModel) {
        val email = newEmailText.text.toString()
        val password = passwordText.text.toString()
        AlertDialog.Builder(context)
                .setTitle(getString(R.string.alert_title_delete_profile))
                .setMessage(getString(R.string.alert_content_delete_profile))
                .setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                    if (password.isEmpty()) {
                        Toast.makeText(context,
                                "Please enter your current password in the form for confirmation.",
                                Toast.LENGTH_LONG).show()
                    } else {
                        userViewModel.authenticateUserBeforeDelete(email, password)
                                .addOnSuccessListener {
                                    deleteProfile(userViewModel)
                                            ?.addOnSuccessListener {
                                                initUserManagementFragment()
                                                dialog.dismiss()
                                            }
                                            ?.addOnFailureListener {
                                                Toast.makeText(context,
                                                        "Unable to delete your account. ${it.message}",
                                                        Toast.LENGTH_LONG).show()
                                            }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                                }
                    }
                }
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
    }

    private fun deleteProfile(userViewModel: UserViewModel): Task<Void>? {
        val taskViewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
        val photoViewModel = ViewModelProviders.of(activity!!)[PhotoViewModel::class.java]
        val userStatisticViewModel = ViewModelProviders.of(activity!!)[UserStatisticViewModel::class.java]

        taskViewModel.deleteUserDocument()
        photoViewModel.deleteUserProfile()
        userStatisticViewModel.deleteStatistic()
        return userViewModel.deleteProfile()
    }

    private fun initUserManagementFragment() {
        view?.let { Util.hideSoftKeyboard(activity, it) }
        fragmentManager?.apply {
            popBackStack(TaskListFragment.TASK_LIST_BACK_STACK,
                    androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            beginTransaction()
                    .replace(R.id.container, UserManagementFragment())
                    .commit()
        }
    }

    private fun initialisedToolbar() {
        setHasOptionsMenu(true)
        val appCompatActivity = activity as AppCompatActivity
        appCompatActivity.setSupportActionBar(editProfileToolbar)
        val supportActionBar = appCompatActivity.supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun addTextWatcher() {
        newEmailText.addTextChangedListener(this@EditProfileFragment)
        passwordText.addTextChangedListener(this@EditProfileFragment)
    }

    private fun registerNewProfileInfo(userViewModel: UserViewModel) {
        val userInfo = UserInfo(newNameText.text.toString(),
                newEmailText.text.toString(),
                passwordText.text.toString())
        userViewModel.editProfile(userInfo)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        activity?.onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun afterTextChanged(s: Editable?) {
        saveButton.isEnabled = newEmailText.text.toString().contains("@") &&
                newEmailText.text.toString().contains(".com") &&
                passwordText.text.toString().length >= 6
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}