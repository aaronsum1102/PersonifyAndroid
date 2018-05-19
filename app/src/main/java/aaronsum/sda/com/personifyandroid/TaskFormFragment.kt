package aaronsum.sda.com.personifyandroid

import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_task_form.*
import java.util.*

class TaskFormFragment : Fragment() {
    companion object {
        private const val TAG = "TaskFormFragment"
    }

    private var existingTask: Task? = null
    private lateinit var status: String
    private lateinit var priority: String


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calender = Calendar.getInstance()
        val currentDate = "${calender[Calendar.YEAR]} - ${calender[Calendar.MONTH] + 1} - ${calender[Calendar.DAY_OF_MONTH]}"
        dueDate.text = currentDate
        prioritySpinner.setSelection(2)

        val viewModel = ViewModelProviders.of(activity!!)[TaskViewModel::class.java]
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
                        }
                    })
            removeTaskButton.visibility = View.VISIBLE
            removeTaskButton.setOnClickListener {
                existingTask?.let {
                    val taskId = arguments?.getString(TaskListFragment.KEY_TASK_ID)
                    taskId?.let {
                        viewModel.deleteTask(taskId)
                                .addOnSuccessListener {
                                    Toast.makeText(this@TaskFormFragment.context,
                                            "Deleting your task now...",
                                            Toast.LENGTH_SHORT)
                                            .show()
                                    Log.i(TAG, "task deleted.")
                                    fragmentManager?.popBackStack()
                                }
                                .addOnFailureListener {
                                    Log.w(TAG, "Error while deleting task, ${it.message}.")
                                }
                    }
                }
            }
        }

        clearFormButton.setOnClickListener {
            taskNameText.text.clear()
            dueDate.text = currentDate
            remarksText.text.clear()
        }

        addTaskButton.setOnClickListener {
            val name = taskNameText.text.toString()
            val dueDate = dueDate.text.toString()
            val remarks = remarksText.text.toString()
            if (this::status.isInitialized && this::priority.isInitialized) {
                val task = Task(name, dueDate, status, priority, remarks)
                val taskId = arguments?.getString(TaskListFragment.KEY_TASK_ID)
                if (taskId != null) {
                    viewModel.modifyTask(taskId to task)
                            .addOnSuccessListener {
                                Log.i(TAG, "new information saved to DB.")
                                fragmentManager?.popBackStack()
                            }
                            .addOnFailureListener {
                                Log.w(TAG, "Error saving new information: ${it.message}.")
                            }

                } else {
                    viewModel.addTask(task)
                            .addOnSuccessListener {
                                Log.i(TAG, "new task saved to DB.")
                                fragmentManager?.popBackStack()
                            }
                            .addOnFailureListener {
                                Log.w(TAG, "Error adding document: ${it.message}.")
                            }
                }
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
            val datePickerDialog = DatePickerDialog(context,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        dueDate.text = "$year - ${month + 1} - $dayOfMonth"
                    },
                    calender[Calendar.YEAR],
                    calender[Calendar.MONTH],
                    calender[Calendar.DAY_OF_MONTH])
            datePickerDialog.show()
        }

        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

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


