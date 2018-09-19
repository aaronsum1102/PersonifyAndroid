package aaronsum.sda.com.personifyandroid

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.analytics.FirebaseAnalytics
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_done_tasks_with_ad.*
import java.lang.Exception
import kotlin.math.abs

class DoneTasksFragment : androidx.fragment.app.Fragment(), Target {
    companion object {
        const val BACK_STACK = "done task"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_done_tasks_with_ad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialisedToolbar()
        activity?.let {
            val analytics = FirebaseAnalytics.getInstance(it)
            analytics.setCurrentScreen(it, "DoneTasks", null)
        }
        ConsentUtil.displayAdd(this, R.layout.fragment_done_tasks, R.layout.fragment_done_tasks_with_ad)

        toProfilePage.setOnClickListener {
            fragmentManager
                    ?.beginTransaction()
                    ?.replace(R.id.container, ProfileFragment())
                    ?.addToBackStack(BACK_STACK)
                    ?.commit()
        }

        val photoViewModel = ViewModelProviders.of(activity!!)[PhotoViewModel::class.java]
        photoViewModel.profilePhotoMetadata.observe(this, Observer { picMetadata ->
            picMetadata?.let {
                Util.fetchPhoto(this, it)
            }
        })

        val adapter = DoneTasksViewAdapter()
        doneTasksRecyclerView.adapter = adapter

        val taskViewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
        if (taskViewModel.doneTasks.value == null) {
            noTasksNow.visibility = View.VISIBLE
        } else {
            noTasksNow.visibility = View.INVISIBLE
        }
        taskViewModel.doneTasks.observe(this, Observer { doneTasks ->
            doneTasks?.let {
                if (it.isEmpty()) {
                    noTasksNow.visibility = View.VISIBLE
                } else {
                    noTasksNow.visibility = View.INVISIBLE
                }
                adapter.doneTasks = it
                adapter.notifyDataSetChanged()
            }
        })
    }

    override fun onResume() {
        adView?.resume()
        super.onResume()
    }

    override fun onPause() {
        adView?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        adView?.destroy()
        super.onDestroy()
    }

    private fun initialisedToolbar() {
        setHasOptionsMenu(true)
        val appCompatActivity = activity as AppCompatActivity
        appCompatActivity.setSupportActionBar(toolbar)
        val supportActionBar = appCompatActivity.supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        activity?.onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        toProfilePage?.background = BitmapDrawable(resources, bitmap)
    }
}

class DoneTasksViewAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<DoneTasksViewHolder>() {
    var doneTasks: List<Pair<String, Task>> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoneTasksViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return DoneTasksViewHolder(view)
    }

    override fun getItemCount(): Int {
        return doneTasks.size
    }

    override fun onBindViewHolder(holder: DoneTasksViewHolder, position: Int) {
        holder.bind(doneTasks[position])
    }
}

class DoneTasksViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    fun bind(doneTask: Pair<String, Task>) {
        val task = doneTask.second
        itemView.findViewById<TextView>(R.id.taskName).text = task.name
        itemView.findViewById<TextView>(R.id.dueDate).text = task.dueDate
        itemView.findViewById<TextView>(R.id.status).text = task.status
        itemView.findViewById<TextView>(R.id.priority).text = task.priority
        val daysLeft = task.daysLeft
        val daysLeftText = itemView.findViewById<TextView>(R.id.daysLeftText)
        if (daysLeft >= 0) {
            daysLeftText.text = itemView.context.resources.getQuantityString(R.plurals.to_due, daysLeft, daysLeft)
            val textColor = ContextCompat.getColor(itemView.context, R.color.primaryTextColor)
            daysLeftText.setTextColor(textColor)
        } else {
            daysLeftText.text = itemView.context.resources.getQuantityString(R.plurals.overdue, abs(daysLeft), abs(daysLeft))
            daysLeftText.setTextColor(Color.RED)
        }
    }
}
