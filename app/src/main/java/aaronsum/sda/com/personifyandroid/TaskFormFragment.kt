package aaronsum.sda.com.personifyandroid

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_task_form.*

class TaskFormFragment : Fragment() {
    private var existingTask: Task? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        removeTaskButton.visibility = View.INVISIBLE
        val viewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
        val taskId = arguments?.getInt(TaskListFragment.KEY_TASK_ID) ?: 0
        if (taskId != 0) {
            viewModel.loadTask(taskId)
                    .observe(this, Observer {
                        existingTask = it
                        taskNameText.setText(it?.name ?: "")
                        dueDate.setText(it?.dueDate ?: "")
                        status.setText(it?.status ?: "")
                        priority.setText(it?.priority ?: "")
                        remarksText.setText(it?.remarks ?: "")
                    })

            removeTaskButton.visibility = View.VISIBLE
            removeTaskButton.setOnClickListener {
                existingTask?.let {
                    viewModel.deleteTask(it)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    {
                                        fragmentManager?.popBackStack()
                                    }, { error ->
                                Snackbar.make(view,
                                        "Something went wrong: ${error.message}.", Snackbar.LENGTH_SHORT).show()
                            })
                }
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
            val task = Task(existingTask?.id ?: 0, name, dueDate, status, priority, remarks)

            viewModel.addTask(task)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                fragmentManager?.popBackStack()
                            },
                            { error ->
                                Snackbar.make(view, "Something went wrong: ${error.message}.", Snackbar.LENGTH_SHORT).show()
                            })
        }


    }
}
