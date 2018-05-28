package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_tasks_list.*
import java.lang.Exception

interface OnTaskClickListener {
    fun onTaskClick(task: Task, taskId: String)
}

class TaskListFragment : Fragment(), OnTaskClickListener {
    companion object {
        const val KEY_TASK_ID = "task id"
        const val TASK_LIST_BACK_STACK = "taskList"
    }

    private val TAG = "TaskListFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tasks_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        val viewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
        val photoViewModel = ViewModelProviders.of(activity!!)[PhotoViewModel::class.java]

        photoViewModel.profilePhotoUrl.observe(this, Observer { uri ->
            uri?.let {
                Picasso.get().load(uri).fetch(object : Callback {
                    override fun onSuccess() {
                        Picasso.get().load(uri).into(object : Target {
                            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

                            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {

                                toProfilePage.background = BitmapDrawable(resources, bitmap)
                            }
                        })
                    }

                    override fun onError(e: Exception?) {
                        Log.e(TAG, "Something went wrong. $e.")
                    }
                })
            }
        })

        val adaptor = TaskViewAdaptor(this)
        taskRecyclerView.adapter = adaptor

        viewModel.tasks.observe(this, Observer { tasks ->
            tasks?.let {
                adaptor.tasks = tasks
                adaptor.notifyDataSetChanged()
            }
        })

        addTaskFab.setOnClickListener {
            if (savedInstanceState == null) {
                fragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.container, TaskFormFragment())
                        ?.addToBackStack(null)
                        ?.commit()
            }
        }

        toProfilePage.setOnClickListener {
            fragmentManager
                    ?.beginTransaction()
                    ?.replace(R.id.container, ProfileFragment())
                    ?.addToBackStack(TASK_LIST_BACK_STACK)
                    ?.commit()
        }
    }

    override fun onTaskClick(task: Task, taskId: String) {
        val taskFormFragment = TaskFormFragment()
        val arguments = Bundle()
        arguments.putString(KEY_TASK_ID, taskId)
        taskFormFragment.arguments = arguments
        fragmentManager
                ?.beginTransaction()
                ?.replace(R.id.container, taskFormFragment)
                ?.addToBackStack(null)
                ?.commit()
    }

    class TaskViewAdaptor(private val taskClickListener: OnTaskClickListener) : RecyclerView.Adapter<TaskViewHolder>() {
        var tasks: List<Pair<String, Task>> = listOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
            return TaskViewHolder(view)
        }

        override fun getItemCount(): Int = tasks.size

        override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
            holder.bind(tasks[position], taskClickListener)
        }
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(pair: Pair<String, Task>, taskClickListener: OnTaskClickListener) {
            val task = pair.second
            itemView.findViewById<TextView>(R.id.taskName).text = task.name
            itemView.findViewById<TextView>(R.id.dueDate).text = task.dueDate
            itemView.findViewById<TextView>(R.id.status).text = task.status
            itemView.findViewById<TextView>(R.id.priority).text = task.priority
            itemView.setOnClickListener {
                taskClickListener.onTaskClick(task, pair.first)
            }
        }
    }
}