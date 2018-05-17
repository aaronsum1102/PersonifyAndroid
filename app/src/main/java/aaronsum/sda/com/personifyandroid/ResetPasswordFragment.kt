package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_reset_password.*

class ResetPasswordFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reset_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userViewModel = ViewModelProviders.of(activity!!)[UserViewModel::class.java]

        confirmButton.setOnClickListener {
            val email = emailText.text.toString()
            userViewModel.resetPassword(email, object : OnFirebaseActionCompleteCallback {
                override fun onActionCompleted() {
                    fragmentManager?.popBackStack()
                }
            })
        }
    }
}