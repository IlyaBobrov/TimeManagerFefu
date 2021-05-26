package com.fefuproject.timemanager.ui.main.home.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fefuproject.timemanager.R
import com.fefuproject.timemanager.base.BaseFragment
import com.fefuproject.timemanager.components.Constants.APP_PREF_OFFLINE
import com.fefuproject.timemanager.databinding.FragmentHomeBinding
import com.fefuproject.timemanager.logic.db.AppDatabase
import com.fefuproject.timemanager.logic.models.NoteModel
import com.fefuproject.timemanager.ui.MainActivity
import com.fefuproject.timemanager.ui.MainActivity.Companion.mainAuth
import com.fefuproject.timemanager.ui.MainActivity.Companion.sharedPreferences
import com.fefuproject.timemanager.ui.main.home.adapters.*
import com.fefuproject.timemanager.ui.main.home.adapters.ListItem.Companion.TYPE_NOTE
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class HomeFragment : BaseFragment(),
    IHomeView,
    HomeAdapter.HomeOnItemClickListener {

    companion object {
        private const val TAG = "HOME_TAG"
    }

    lateinit var db: AppDatabase
    private var statusOffline: Boolean = false
    private val adapterHome = HomeAdapter(this)
    private var _binding: FragmentHomeBinding? = null
    var note = mutableListOf<ListItem>()
    lateinit var data: List<NoteModel>

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
        onClick()
        statusOffline = sharedPreferences.getBoolean(APP_PREF_OFFLINE, false)
        if (statusOffline) Toast.makeText(context, "Оффлайн режим", Toast.LENGTH_SHORT).show()
        else Toast.makeText(context, mainAuth.currentUser?.email, Toast.LENGTH_SHORT).show()
        initToolBar()
        initSwipeAndScroll()
        initDB()
        initSpinner()
    }

    private fun initSwipeAndScroll() {
        //swipe
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

        db = (requireActivity() as MainActivity).getDB()

        /*GlobalScope.launch {
            db.noteModelDao().insertAll(
                    NoteModel(1, "Работа", "2021-05-17", "Заметка!", false),
            )
        }*/

        GlobalScope.launch {
            data = db.noteModelDao().getAll()
            Log.d(TAG, "initDB: $data")
            initMainRecycler()
        }

    }

    override fun getDb(notes: List<NoteModel>) {
        Log.d(TAG, "getDb")
        Log.d(TAG, "getDb: $notes")
    }

    private fun convertToCategoryList(list: List<Note>): MutableList<ListItem> {
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
    }

    private fun initMainRecycler() {
        binding.rvHome.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterHome
        }

        setListData(data)
    }

    private fun updateHomeInfo() {
        Toast.makeText(requireContext(), "В разарботке", Toast.LENGTH_SHORT).show()
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


    private fun initSpinner() {
        //todo брать массив из db
        val arrayList = arrayListOf<String>()
        arrayList.add("Работа")
        arrayList.add("Учеба")
        arrayList.add("Быт")

        val spinnerAdapter: SpinnerAdapter = ArrayAdapter(
            this.requireContext(), R.layout.custom_spinner_item, arrayList
        )
        (spinnerAdapter as ArrayAdapter<*>).setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.topNavigationSpinner.adapter = spinnerAdapter
        binding.topNavigationSpinner.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                Toast.makeText(
                    requireContext(),
                    "Вы выбрали: " + arrayList.get(position),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
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
                else -> false
            }
        }
    }


    fun onClick() {
        binding.fabHomeAdd.setOnClickListener {
            Toast.makeText(context, getString(R.string.in_develop), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(context, "hi", Toast.LENGTH_SHORT).show()
    }

    override fun setListData(data: List<NoteModel>) {
        adapterHome.submitList(data)
    }

}