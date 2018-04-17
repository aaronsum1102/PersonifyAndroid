package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_task_form.*

class TaskFormFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        removeTaskButton.visibility = View.INVISIBLE
        val taskViewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
        val taskId = arguments?.getInt(TaskListFragment.KEY_TASK_INDEX) ?: -1
        if (taskId >= 0) {
            val task = taskViewModel.tasks.value?.get(taskId)
            taskNameText.setText(task?.name)
            dueDate.setText(task?.dueDate)
            status.setText(task?.status)
            priority.setText(task?.priority)
            remarksText.setText(task?.remarks)
            removeTaskButton.visibility = View.VISIBLE

            removeTaskButton.setOnClickListener {
                task?.let {
                    taskViewModel.removeTask(it)
                }
                fragmentManager?.popBackStack()
            }
        }

        clearFormButton.setOnClickListener {
            taskNameText.text.clear()
            dueDate.text.clear()
            status.text.clear()
            priority.text.clear()
            remarksText.text.clear()
        }

        addTaskButton.setOnClickListener {
            val name = taskNameText.text.toString()
            val dueDate = dueDate.text.toString()
            val status = status.text.toString()
            val priority = priority.text.toString()
            val remarks = remarksText.text.toString()
            val task = Task(name, dueDate, status, priority, remarks)
            taskViewModel.addTask(task)
            fragmentManager
                    ?.beginTransaction()
                    ?.replace(R.id.container, TaskListFragment())
                    ?.commitNow()
        }


    }
}
