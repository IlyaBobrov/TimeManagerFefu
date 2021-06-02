package com.fefuproject.timemanager.ui.main.home.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fefuproject.timemanager.R
import com.fefuproject.timemanager.R.id.itemCheckBox
import com.fefuproject.timemanager.base.BaseFragment
import com.fefuproject.timemanager.components.Constants.APP_CATEGORY_PREF
import com.fefuproject.timemanager.components.Constants.APP_PREF_OFFLINE
import com.fefuproject.timemanager.databinding.FragmentHomeBinding
import com.fefuproject.timemanager.logic.db.AppDatabase
import com.fefuproject.timemanager.logic.models.CategoryModel
import com.fefuproject.timemanager.logic.models.NoteModel
import com.fefuproject.timemanager.ui.MainActivity
import com.fefuproject.timemanager.ui.MainActivity.Companion.sharedPreferences
import com.fefuproject.timemanager.ui.main.home.adapters.HomeAdapter
import kotlinx.coroutines.*
import java.text.FieldPosition
import kotlin.coroutines.CoroutineContext


class HomeFragment : BaseFragment(),
    IHomeView,
    HomeAdapter.HomeOnItemClickListener,
    CoroutineScope {

    private val job = SupervisorJob()
    private lateinit var initDbJob: Job
    private lateinit var insertInDbJob: Job
    private lateinit var updateListJob: Job
    private lateinit var initCategoryJob: Job
    private lateinit var sortByCategoryJob: Job
    private lateinit var setCompiteJob: Job

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    companion object {
        private const val TAG = "HOME_TAG"
        const val CREATE_TASK = "CREATE_TASK"
        const val NOTE_TASK = "NOTE_TASK"

        const val KEY_CREATE = "KEY_CREATE"
        const val KEY_EDIT = "KEY_EDIT"
        const val KEY_DEFAULT = "KEY_DEFAULT"
    }

    private lateinit var db: AppDatabase
    private lateinit var data: List<NoteModel>
    private lateinit var folders: List<CategoryModel>

    private var statusOffline: Boolean = false
    private var currentCategory: String? = "Все"
    private val adapterHome = HomeAdapter(this)
    private var _binding: FragmentHomeBinding? = null

    private val binding: FragmentHomeBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statusOffline = sharedPreferences.getBoolean(APP_PREF_OFFLINE, false)
        currentCategory = sharedPreferences.getString(APP_CATEGORY_PREF, "Все")
        initToolBar()
        initSwipeRefreshAndScroll()
        initMainRecycler()
        initDB()
        onListeners()
        Log.d(TAG, "onViewCreated: ")
    }

    private fun initSwipeRefreshAndScroll() {
        //swipeRefresh
        binding.swipeContainerHome.setOnRefreshListener {

            updateHomeInfo()
        }
        //scroll
        val rvs = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisibleItemPosition =
                    (binding.rvHome.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                Log.d(TAG, "onScrolled: $lastVisibleItemPosition")
            }
        }
        binding.rvHome.addOnScrollListener(rvs)
    }

    private fun initDB() {
        db = AppDatabase.invoke(requireContext())
        binding.swipeContainerHome.isRefreshing = true
        initDbJob = launch {
            val localData = withContext(Dispatchers.IO) {
                db.noteModelDao().getAll()
            }
            if (localData.isNullOrEmpty()) {
                withContext(Dispatchers.IO) {
                    db.noteModelDao().insertAll(
                        NoteModel(
                            null,
                            "Заголовок",
                            "Описание",
                            CategoryModel(null, "Работа"),
                            "17 5 2021",
                            "18 5 2021",
                            false
                        )
                    )
                    db.categoryModelDao().insertAll(CategoryModel(null, "Все"))
                    db.categoryModelDao().insertAll(CategoryModel(null, "Работа"))
                    db.categoryModelDao().insertAll(CategoryModel(null, "Учеба"))
                }
            }
            sortByCategory()
            getCategories()
        }
    }

    /*private fun convertToCategoryList(list: List<Note>): MutableList<ListItem> {
        //разбиваем на категории
        val dateList = mutableListOf<String>()
        val categoryList = mutableListOf<ListItem>()

        list.forEach { item ->
            if (dateList.find { it.take(10) == item.date!!.take(10) } == null) {
                dateList.add(item.date!!)
            }
        }

        dateList.sortDescending()
        dateList.forEach { itemDate ->
            categoryList.add(Header(itemDate))
            list.forEach { item ->
                if (item.date!!.take(10) == itemDate.take(10)) {
                    categoryList.add(item)
                }
            }
        }
        Log.e(TAG, "initRV: $categoryList")
        return categoryList
    }*/

    private fun initMainRecycler() {
        binding.rvHome.apply {
            layoutManager = LinearLayoutManager(context)
//            adapterHome.submitList(null)
            adapter = adapterHome
        }

    }

    private fun updateHomeInfo() {
        adapterHome.submitList(null)
        sortByCategory()
    }

    //для отображения выбранных элементов в toolbar
    /*private fun callback() {
        val callback = object : ActionMode.Callback {

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                recyclerMenuInflater.inflate(R.menu.contextual_action_bar, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.cabComplete -> {
                        Toast.makeText(requireContext(), "Complete", Toast.LENGTH_LONG).show()
                        true
                    }
                    R.id.cabDelete -> {
                        // Handle delete icon press
                        true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
            }
        }

        val actionMode = (requireActivity() as MainActivity).startSupportActionMode(callback)
        actionMode?.title = "1 выбрано"
    }*/

    fun getCategories() {
        initCategoryJob = launch {
            folders = withContext(Dispatchers.IO) {
                db.categoryModelDao().getAll()
            }
            binding.topNavigationSpinner.adapter
            initSpinner(folders)
        }
    }

    private fun initSpinner(list: List<CategoryModel>) {

        val arrayList = arrayListOf<String>()
        list.forEach {
            arrayList.add(it.title.toString())
        }

        val spinnerAdapter: SpinnerAdapter = ArrayAdapter(
            this.requireContext(), R.layout.custom_spinner_item, arrayList
        )
        (spinnerAdapter as ArrayAdapter<*>).setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.topNavigationSpinner.adapter = spinnerAdapter
        binding.topNavigationSpinner.setSelection(getIngex(binding.topNavigationSpinner))
        binding.topNavigationSpinner.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentCategory = arrayList[position]
                sharedPreferences.edit().putString(APP_CATEGORY_PREF, currentCategory).apply()
                sortByCategory()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(requireContext(), "Ничего не выбано", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getIngex(spinner: Spinner): Int {
        for (i in 0..spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == currentCategory) {
                return i
            }
        }
        return 0
    }

    private fun sortByCategory() {
        binding.swipeContainerHome.isRefreshing = true
        sortByCategoryJob = launch {
            data = if (currentCategory == "Все") {
                withContext(Dispatchers.IO) {
                    db.noteModelDao().getAll()
                }
            } else {
                withContext(Dispatchers.IO) {
                    db.noteModelDao().findByCategory(currentCategory.toString())
                }
            }
            adapterHome.submitList(data.asReversed())
            binding.swipeContainerHome.isRefreshing = false
        }
    }

    private fun initToolBar() {
        binding.topAppBar.setNavigationOnClickListener {
            // Handle navigation icon press
            Toast.makeText(context, getString(R.string.in_develop), Toast.LENGTH_SHORT).show()
        }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.tbExit -> {
                    (requireActivity() as MainActivity).getOut()
                    true
                }
                R.id.tbComplete -> {
                    Toast.makeText(context, getString(R.string.in_develop), Toast.LENGTH_SHORT)
                        .show()
                    true
                }
                R.id.tbSort -> {
                    Toast.makeText(context, getString(R.string.in_develop), Toast.LENGTH_SHORT)
                        .show()
                    true
                }
                R.id.tbClear -> {

                    true
                }
                else -> false
            }
        }
    }

    private fun onListeners() {
        binding.fabHomeAdd.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(CREATE_TASK, KEY_CREATE)
            findNavController().navigate(R.id.action_homeFragment_to_taskFragment, args = bundle)
        }

        //обмен данными с TaskFragment
        with(findNavController().currentBackStackEntry) {

            this?.savedStateHandle?.getLiveData<NoteModel>(KEY_CREATE)
                ?.observe(viewLifecycleOwner)
                {
                    //todo добавить заметку в бд и обновить список
                    insertInDbJob = launch {
                        withContext(Dispatchers.IO) {
                            db.noteModelDao().insertAll(it)
                        }
                        sortByCategory()
                    }

                    savedStateHandle.remove<NoteModel>(KEY_CREATE)
                }

            this?.savedStateHandle?.getLiveData<NoteModel>(KEY_EDIT)?.observe(viewLifecycleOwner) {
                //todo обновить заметку
                updateListJob = launch {
                    withContext(Dispatchers.IO) {
                        db.noteModelDao().updateNote(it)
                    }
                    sortByCategory()
                }
                savedStateHandle.remove<NoteModel>(KEY_EDIT)
            }

        }
    }

    override fun onItemClick(v:View, item: NoteModel, position: Int) {
        when(v.id){
            itemCheckBox -> {
                Log.d(TAG, "onItemClick: ")
                val it:NoteModel = item
                setCompiteJob = launch {
                    withContext(Dispatchers.IO){
                        it.complete = item.complete?.not()
                        db.noteModelDao().updateNote(it)
                    }
                    adapterHome.notifyItemChanged(position)
                }
            }
            else ->{
                val bundle = Bundle()
                bundle.putString(CREATE_TASK, KEY_EDIT)
                bundle.putParcelable(NOTE_TASK, item)
                findNavController().navigate(R.id.action_homeFragment_to_taskFragment, args = bundle)
            }
        }


    }

}