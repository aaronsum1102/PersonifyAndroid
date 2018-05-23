package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_edit_profile.*

class EditProfileFragment : Fragment(), TextWatcher {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userViewModel = ViewModelProviders.of(activity!!)[UserViewModel::class.java]
        val taskViewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]

        newEmailText.addTextChangedListener(this)
        passwordText.addTextChangedListener(this)

        userViewModel.currentUser.observe(this, Observer {
            newNameText.setText(it?.username)
            newEmailText.setText(it?.email)
        })

        initialisedToolbar()

        deleteProfileButton.setOnClickListener {
            deleteProfile(userViewModel, taskViewModel)
        }

        saveButton.setOnClickListener {
            userViewModel.verifyPassword(passwordText.text.toString())
                    ?.addOnSuccessListener {
                        val userInfo = UserInfo(newNameText.text.toString(),
                                newEmailText.text.toString(),
                                passwordText.text.toString())
                        userViewModel.editProfile(userInfo)
                        fragmentManager?.popBackStack(TaskListFragment.TASK_LIST_BACK_STACK,
                                FragmentManager.POP_BACK_STACK_INCLUSIVE)
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

    private fun deleteProfile(userViewModel: UserViewModel, taskViewModel: TaskViewModel) {
        context?.let { context ->
            AlertDialog.Builder(context)
                    .setTitle("Warning!!!")
                    .setMessage("Are you sure you want to delete your account? All data will be removed and unrecoverable after that.")
                    .setPositiveButton("Confirm", { dialog, _ ->
                        run {
                            taskViewModel.deleteUserDocument()
                            userViewModel.deleteProfile()
                                    ?.addOnFailureListener {
                                        Toast.makeText(context,
                                                "Unable to delete your account. ${it.message}",
                                                Toast.LENGTH_LONG).show()
                                    }
                            fragmentManager?.apply {
                                popBackStack(TaskListFragment.TASK_LIST_BACK_STACK,
                                        FragmentManager.POP_BACK_STACK_INCLUSIVE)
                                beginTransaction()
                                        .replace(R.id.container, UserManagementFragment())
                                        .commit()
                            }
                            dialog.dismiss()
                        }
                    })
                    .setNegativeButton("Cancel", { dialog, _ -> dialog.dismiss() })
                    .create()
                    .show()
        }
    }

    private fun initialisedToolbar() {
        setHasOptionsMenu(true)
        val appCompatActivity = activity as AppCompatActivity
        appCompatActivity.setSupportActionBar(editProfileToolbar)
        val supportActionBar = appCompatActivity.supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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