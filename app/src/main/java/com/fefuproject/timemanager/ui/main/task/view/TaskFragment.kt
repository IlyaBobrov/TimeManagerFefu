package com.fefuproject.timemanager.ui.main.task.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fefuproject.timemanager.R
import com.fefuproject.timemanager.databinding.FragmentTaskBinding
import com.fefuproject.timemanager.logic.db.AppDatabase
import com.fefuproject.timemanager.logic.models.CategoryModel
import com.fefuproject.timemanager.logic.models.NoteModel
import com.fefuproject.timemanager.ui.main.home.view.HomeFragment
import com.fefuproject.timemanager.ui.main.home.view.HomeFragment.Companion.CREATE_TASK
import com.fefuproject.timemanager.ui.main.home.view.HomeFragment.Companion.KEY_CREATE
import com.fefuproject.timemanager.ui.main.home.view.HomeFragment.Companion.KEY_DEFAULT
import com.fefuproject.timemanager.ui.main.home.view.HomeFragment.Companion.KEY_EDIT
import com.fefuproject.timemanager.ui.main.home.view.HomeFragment.Companion.NOTE_TASK
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class TaskFragment : Fragment() {

    companion object {
        private const val TAG = "HOME_TAG"
    }

    lateinit var db: AppDatabase
    var note: NoteModel? = null

    private var statusOffline: Boolean = false
    private var _binding: FragmentTaskBinding? = null
    private val binding: FragmentTaskBinding
        get() = _binding!!

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
        onListeners()
        initView()

    }

    lateinit var categoryList: List<CategoryModel>
    lateinit var selectedCategory: CategoryModel

    private fun initSpinner() {

        var categoryArray = mutableListOf<String>()
        categoryList.forEach{
            categoryArray.add(it.title.toString())
        }
        Log.d(TAG, "initSpinner: $categoryArray")
        val spinnerAdapter: SpinnerAdapter = ArrayAdapter(
            this.requireContext(), R.layout.custom_spinner_item, categoryArray
        )
        (spinnerAdapter as ArrayAdapter<*>).setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.spinnerTaskCategory.adapter = spinnerAdapter
        binding.spinnerTaskCategory.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCategory = categoryList[position]

                Toast.makeText(
                    requireContext(),
                    "Вы выбрали: " + categoryList[position].title,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(requireContext(), "Ничего не выбано", Toast.LENGTH_SHORT).show()
            }
        }
    }

    lateinit var returnKey: String

    private fun initView() {
        when (arguments?.get(CREATE_TASK)) {
            KEY_CREATE -> {
                returnKey = KEY_CREATE
                Toast.makeText(requireContext(), "Create task", Toast.LENGTH_SHORT).show()
            }
            KEY_EDIT -> {
                returnKey = KEY_EDIT
                try {
                    note = arguments?.getParcelable<NoteModel>(NOTE_TASK)!!
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(requireContext(), "Edit task", Toast.LENGTH_SHORT).show()
                initFields()
            }
            else -> {
                returnKey = KEY_DEFAULT
            }
        }
    }


    private fun initFields() {
        //todo
    }


    private fun onListeners() {
        _binding?.btnTaskSave?.setOnClickListener {
            if (!validFields()) return@setOnClickListener
            buildModel()
            Log.d(TAG, "returnKey: $returnKey")
            if (note == null) {
                note = NoteModel("5", "Заголовок", "Описание", "Работа", "2021-05-17", null, false)
            }
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                returnKey,
                note
            )
            finishFragment()
        }

        _binding?.btnTackStartDate?.setOnClickListener {

        }

        _binding?.btnTackEndDate?.setOnClickListener {

        }

        _binding?.btnTaskAddCategory?.setOnClickListener {

        }

    }


    private fun buildModel() {
        var title: String
        var description: String
        var dateStart: Date
        var dateEnd: Date
        var category: CategoryModel

        with(_binding!!) {
            title = if (etTaskTitle.text.equals("")) "" else etTaskTitle.text.toString()
            description = etTaskDescription.toString()
            category = categoryList.first{ it.title == selectedCategory.title.toString()}
        }
    }

    private fun validFields(): Boolean {
        with(_binding!!) {
            if (etTaskDescription.text == null || etTaskDescription.text.equals("")) return false
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

        GlobalScope.launch {
            categoryList = db.categoryModelDao().getAll()
            initSpinner()
        }
    }

}