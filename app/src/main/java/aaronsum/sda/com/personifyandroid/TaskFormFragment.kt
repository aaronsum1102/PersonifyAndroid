package aaronsum.sda.com.personifyandroid

import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_task_form.*
import java.util.*

class TaskFormFragment : Fragment(), TextWatcher {
    private var existingTask: Task? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addTaskButton.isEnabled = false
        removeTaskButton.visibility = View.INVISIBLE

        taskNameText.addTextChangedListener(this)
        dueDate.addTextChangedListener(this)
        status.addTextChangedListener(this)
        priority.addTextChangedListener(this)

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

        dueDate.setOnClickListener {
            val calender = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(context,
                    DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                        dueDate.setText("$year-${month + 1}-$dayOfMonth")
                    },
                    calender[Calendar.YEAR],
                    calender[Calendar.MONTH],
                    calender[Calendar.DAY_OF_MONTH])
            datePickerDialog.show()
        }
    }


    override fun afterTextChanged(s: Editable?) {
        addTaskButton.isEnabled = taskNameText.text.isNotEmpty() &&
                dueDate.text.isNotEmpty() &&
                status.text.isNotEmpty() &&
                priority.text.isNotEmpty()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}

