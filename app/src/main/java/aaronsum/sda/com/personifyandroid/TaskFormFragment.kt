package aaronsum.sda.com.personifyandroid

import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_task_form.*
import java.util.*

class TaskFormFragment : Fragment() {
    private var existingTask: Task? = null
    private lateinit var status: String
    private lateinit var priority: String
    private val taskIsDone = "Done"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dueDate.text = Util.getCurrentDate()
        prioritySpinner.setSelection(2)

        val viewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
        val userStatisticViewModel = ViewModelProviders.of(activity!!)[UserStatisticViewModel::class.java]

        val taskId: String = arguments?.getString(TaskListFragment.KEY_TASK_ID) ?: ""
        if (taskId.isNotEmpty()) {
            viewModel.loadTask(taskId)
                    .observe(this, Observer {
                        it?.let {
                            existingTask = it
                            taskNameText.setText(it.name)
                            dueDate.text = it.dueDate
                            val statusArrayAdapter: ArrayAdapter<String> = statusSpinner.adapter as ArrayAdapter<String>
                            statusSpinner.setSelection(statusArrayAdapter.getPosition(it.status))
                            val priorityArrayAdapter = prioritySpinner.adapter as ArrayAdapter<String>
                            prioritySpinner.setSelection(priorityArrayAdapter.getPosition(it.priority))
                            remarksText.setText(it.remarks)

                            if (it.status == taskIsDone) {
                                statusSpinner.isEnabled = false
                                addTaskButton.isEnabled = false
                            }
                        }
                    })
            removeTaskButton.visibility = View.VISIBLE
            removeTaskButton.setOnClickListener {
                existingTask?.let {
                    viewModel.deleteTask(taskId)
                    Util.hideSoftKeyboard(activity, view)
                    fragmentManager?.popBackStack()
                }
            }
        }

        clearFormButton.setOnClickListener {
            taskNameText.text.clear()
            dueDate.text = Util.getCurrentDate()
            remarksText.text.clear()
        }

        addTaskButton.setOnClickListener {
            val name = taskNameText.text.toString()
            val dueDate = dueDate.text.toString()
            val remarks = remarksText.text.toString()
            val daysLeft = Util.getDaysDifference(dueDate)
            if (this::status.isInitialized && this::priority.isInitialized) {
                val task = Task(name, dueDate, status, priority, remarks, daysLeft)
                if (taskId.isNotEmpty()) {
                    viewModel.modifyTask(taskId to task)
                    if (status == taskIsDone) {
                        if (daysLeft >= 0) {
                            userStatisticViewModel.updateStatistic(UserStatisticRepository.COMPLETION_ON_TIME)
                        } else {
                            userStatisticViewModel.updateStatistic(UserStatisticRepository.OVERDUE)
                        }
                    }
                } else {
                    viewModel.addTask(task)
                    userStatisticViewModel.updateStatistic(UserStatisticRepository.NEW_TASK)
                }
                Util.hideSoftKeyboard(activity, view)
                fragmentManager?.popBackStack()
            }
        }

        taskNameText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                addTaskButton.isEnabled = taskNameText.text.isNotEmpty() && dueDate.text.isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        calenderButton.setOnClickListener {
            val currentDateCalender = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(context,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        val dateFormat = Util.dateFormat
                        val calendar = dateFormat.calendar
                        calendar.set(year, month, dayOfMonth)
                        val date = dateFormat.format(calendar.time)
                        dueDate.text = date
                    },
                    currentDateCalender[Calendar.YEAR],
                    currentDateCalender[Calendar.MONTH],
                    currentDateCalender[Calendar.DAY_OF_MONTH])
            datePickerDialog.show()
        }

        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                status = statusSpinner.selectedItem.toString()
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                status = statusSpinner.selectedItem.toString()
            }
        }

        prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                priority = prioritySpinner.selectedItem.toString()
            }
        }
    }
}


