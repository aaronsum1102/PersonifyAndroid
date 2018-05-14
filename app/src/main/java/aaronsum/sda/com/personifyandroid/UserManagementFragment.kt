package aaronsum.sda.com.personifyandroid

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_user_management.*

class UserManagementFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {

            signUpButton.setOnClickListener {
                fragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.container, SignUpFragment())
                        ?.addToBackStack("welcome")
                        ?.commit()
            }

            logInButton.setOnClickListener {
                fragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.container, LogInFragment())
                        ?.addToBackStack("welcome")
                        ?.commit()
            }
        }
    }
}

