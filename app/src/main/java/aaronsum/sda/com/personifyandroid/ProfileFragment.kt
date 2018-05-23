package aaronsum.sda.com.personifyandroid

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.PopupMenu
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialisedToolbar("aaron")
    }

    private fun initialisedToolbar(username: String) {
        setHasOptionsMenu(true)
        val appCompatActivity = activity as AppCompatActivity
        appCompatActivity.setSupportActionBar(profileToolbar)
        appCompatActivity.supportActionBar?.setDisplayShowTitleEnabled(false)
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
                    menu.inflate(R.menu.profile_setting_menu)
                    menu.show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}