package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_tasks_list.*

class TaskListFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tasks_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
        val adaptor = TaskViewAdaptor()
        taskRecyclerView.adapter = adaptor

        viewModel.tasks.observe(this, Observer { tasks ->
            tasks?.let {
                adaptor.tasks = tasks
                adaptor.notifyDataSetChanged()
            }
        })
    }

    class TaskViewAdaptor : RecyclerView.Adapter<TaskViewHolder>() {
        var tasks: List<Task> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
            return TaskViewHolder(view)
        }

        override fun getItemCount(): Int = tasks.size

        override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
            holder.bind(tasks[position])
        }

    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(task: Task) {
            itemView.findViewById<TextView>(R.id.taskName).text = task.name
            itemView.findViewById<TextView>(R.id.dueDate).text = task.dueDate
            itemView.findViewById<TextView>(R.id.status).text = task.status
            itemView.findViewById<TextView>(R.id.priority).text = task.priority
        }
    }
}