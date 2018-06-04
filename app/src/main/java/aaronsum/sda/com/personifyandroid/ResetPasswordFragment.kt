package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_reset_password.*

class ResetPasswordFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reset_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userViewModel = ViewModelProviders.of(activity!!)[UserViewModel::class.java]

        emailText.addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        confirmButton.isEnabled = emailText.text.toString().contains("@") &&
                                emailText.text.toString().contains(".com")
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                }
        )

        confirmButton.setOnClickListener {
            val email = emailText.text.toString()
            userViewModel.resetPassword(email)
                    .addOnSuccessListener {
                        context?.let {
                            Toast.makeText(context,
                                    getString(R.string.reset_password),
                                    Toast.LENGTH_LONG)
                                    .show()
                        }
                        fragmentManager?.popBackStack()
                    }
                    .addOnFailureListener {
                        context?.let {
                            Toast.makeText(context,
                                    getString(R.string.failed_request_password),
                                    Toast.LENGTH_LONG)
                                    .show()
                        }
                    }
        }
    }
}