package com.fefuproject.timemanager.ui.main.task.view

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fefuproject.timemanager.R
import com.fefuproject.timemanager.databinding.FragmentTaskBinding
import com.fefuproject.timemanager.logic.db.AppDatabase
import com.fefuproject.timemanager.logic.models.CategoryModel
import com.fefuproject.timemanager.logic.models.NoteModel
import com.fefuproject.timemanager.ui.main.home.view.HomeFragment.Companion.CREATE_TASK
import com.fefuproject.timemanager.ui.main.home.view.HomeFragment.Companion.KEY_CREATE
import com.fefuproject.timemanager.ui.main.home.view.HomeFragment.Companion.KEY_DEFAULT
import com.fefuproject.timemanager.ui.main.home.view.HomeFragment.Companion.KEY_EDIT
import com.fefuproject.timemanager.ui.main.home.view.HomeFragment.Companion.NOTE_TASK
import com.fefuproject.timemanager.ui.main.task.dialog.CategoryDialog
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext


class TaskFragment : Fragment(), CategoryDialog.NoticeDialogListener, CoroutineScope {

    private val job = SupervisorJob()
    private lateinit var initCategoryJob: Job
    private lateinit var createCategoryJob: Job

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    companion object {
        private const val TAG = "HOME_TAG"
    }

    lateinit var db: AppDatabase
    var note: NoteModel? = null

    private var statusOffline: Boolean = false
    private var _binding: FragmentTaskBinding? = null
    private val binding: FragmentTaskBinding
        get() = _binding!!

    var title: String? = null
    var description: String? = null
    var dateStart: String? = null
    var dateEnd: String? = null
    var category: CategoryModel? = null
    var complete: Boolean? = null

    lateinit var categoryList: List<CategoryModel>
    lateinit var selectedCategory: CategoryModel
    lateinit var spinnerAdapter: SpinnerAdapter

    val calendar = Calendar.getInstance()

    var yearStart = calendar.get(Calendar.YEAR)
    var monthStart = calendar.get(Calendar.MONTH)
    var dayStart = calendar.get(Calendar.DAY_OF_MONTH)
    lateinit var dateStartDialog: DatePickerDialog

    var yearEnd = calendar.get(Calendar.YEAR)
    var monthEnd = calendar.get(Calendar.MONTH)
    var dayEnd = calendar.get(Calendar.DAY_OF_MONTH)
    lateinit var dateEndDialog: DatePickerDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDB()
        initToolBar()
        initView()
        initDialog()
        onListeners()
    }

    private fun initDialog() {
        dateStartDialog = DatePickerDialog(
            requireActivity(),
            { _, year, monthOfYear, dayOfMonth ->
                setViewDate(binding.btnTackStartDate, dayOfMonth, monthOfYear, year)
                yearStart = year
                monthStart = monthOfYear
                dayStart = dayOfMonth
            }, yearStart, monthStart, dayStart
        )
        dateEndDialog = DatePickerDialog(
            requireActivity(),
            { _, year, monthOfYear, dayOfMonth ->
                if (year >= yearStart &&
                    monthOfYear >= monthStart &&
                    dayOfMonth > dayStart
                ) {
                    setViewDate(binding.btnTackEndDate, dayOfMonth, monthOfYear, year)
                    yearEnd = year
                    monthEnd = monthOfYear
                    dayEnd = dayOfMonth
                } else {
                    setViewDate(binding.btnTackEndDate, dayStart, monthStart, yearStart)
                }
            }, yearEnd, monthEnd, dayEnd
        )
    }

    private fun initSpinner() {

        var categoryArray = mutableListOf<String>()
        categoryList.forEach {
            categoryArray.add(it.title.toString())
        }
        Log.d(TAG, "initSpinner: $categoryArray")
        spinnerAdapter = ArrayAdapter(
            this.requireContext(), R.layout.custom_spinner_item, categoryArray
        )
        (spinnerAdapter as ArrayAdapter<*>).setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.spinnerTaskCategory.adapter = spinnerAdapter
        binding.spinnerTaskCategory.setSelection(getIngex(binding.spinnerTaskCategory))
        binding.spinnerTaskCategory.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCategory = categoryList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(requireContext(), "Ничего не выбано", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getIngex(spinner: Spinner): Int {
        if (note?.category?.title == null) return 0
        for (i in 0..spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == note?.category?.title.toString()) {
                return i
            }
        }
        return 0
    }

    lateinit var returnKey: String

    private fun initView() {
        when (arguments?.get(CREATE_TASK)) {
            KEY_CREATE -> {
                returnKey = KEY_CREATE
                Toast.makeText(requireContext(), "Create task", Toast.LENGTH_SHORT).show()
                initFields(returnKey)
            }
            KEY_EDIT -> {
                returnKey = KEY_EDIT
                try {
                    note = arguments?.getParcelable<NoteModel>(NOTE_TASK)!!
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(requireContext(), "Edit task", Toast.LENGTH_SHORT).show()
                initFields(returnKey)
            }
            else -> {
                returnKey = KEY_DEFAULT
            }
        }
    }

    private fun initFields(key: String) {
        when (key) {
            KEY_CREATE -> {
                setViewDate(binding.btnTackStartDate, dayStart, monthStart, yearStart)
            }
            KEY_EDIT -> {
                binding.etTaskTitle.setText(note?.title ?: "")
                binding.etTaskDescription.setText(note?.description)
                setViewDate(binding.btnTackStartDate, dayStart, monthStart, yearStart)
                setViewDate(binding.btnTackEndDate, dayEnd, monthEnd, yearEnd)

            }
            else -> {
            }
        }
    }

    @SuppressLint("SetTextI18n")
    internal fun setViewDate(b: Button, day: Int, month: Int, year: Int) {
        b.text = "$day, $month, $year"
    }

    private fun onListeners() {
        _binding?.btnTaskSave?.setOnClickListener {
            if (validFields()) {
                buildModel()
                Log.d(TAG, "returnKey: $returnKey")
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    returnKey,
                    note
                )
                finishFragment()
            }
        }

        _binding?.btnTackStartDate?.setOnClickListener {
            dateStartDialog.show()
        }

        _binding?.btnTackEndDate?.setOnClickListener {
            dateEndDialog.show()
        }

        _binding?.btnTaskAddCategory?.setOnClickListener {
            val categoryDialog = CategoryDialog(this)
            categoryDialog.show(childFragmentManager, "TAG")

        }
    }


    private fun buildModel() {
        with(_binding!!) {
            Log.d(TAG, "buildModel")
            title = if (etTaskTitle.text.equals("")) "" else etTaskTitle.text.toString().trim()
            description = etTaskDescription.text.toString().trim()
            category = categoryList.first {
                it.title == selectedCategory.title
            }
            dateStart = "$dayStart $monthStart $yearStart"
            Log.d(TAG, "buildModel: $dayEnd")
            dateEnd =
                if (dayEnd.toString() == "" || (dayStart == dayEnd && monthStart == monthEnd && yearStart == yearEnd))
                    null
                else
                    "$dayEnd $monthEnd $yearEnd"
            complete = note?.complete?:false

            note = NoteModel(note?.id, title, description, category, dateStart, dateEnd, complete)
        }
    }

    private fun validFields(): Boolean {
        Log.d(TAG, "validFields: ${_binding?.etTaskDescription?.text}")
        with(_binding!!) {
            if (etTaskDescription.text == null || etTaskDescription.text.toString().trim() == "")
                return false
        }
        return true
    }

    private fun initToolBar() {
        binding.topTaskAppBar.setNavigationOnClickListener {
            finishFragment()
        }
    }

    private fun finishFragment() {
        findNavController().popBackStack()
    }

    private fun initDB() {
        db = AppDatabase.invoke(requireContext())
        setSpinner()
    }

    private fun setSpinner() {
        initCategoryJob = launch {
            categoryList = withContext(Dispatchers.IO) {
                db.categoryModelDao().getAll()
            }
            initSpinner()
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, category: String) {
        createCategoryJob = launch {
            withContext(Dispatchers.IO) {
                db.categoryModelDao().insertAll(CategoryModel(null, category))
            }
            setSpinner()
        }
    }

}