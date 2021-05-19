package com.fefuproject.timemanager.main.home.view

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
import com.fefuproject.timemanager.MainActivity
import com.fefuproject.timemanager.MainActivity.Companion.mainAuth
import com.fefuproject.timemanager.MainActivity.Companion.sharedPreferences
import com.fefuproject.timemanager.R
import com.fefuproject.timemanager.base.BaseFragment
import com.fefuproject.timemanager.components.Constants.APP_PREF_OFFLINE
import com.fefuproject.timemanager.databinding.FragmentHomeBinding
import com.fefuproject.timemanager.main.home.adapters.HomeAdapter
import com.fefuproject.timemanager.models.NoteListModel
import com.fefuproject.timemanager.models.NoteModel


class HomeFragment : BaseFragment(), HomeView, HomeAdapter.HomeOnItemClickListener {

    companion object {
        private const val TAG = "HOME_TAG"
    }

    private var statusOffline: Boolean = false
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
        onClick()
        statusOffline = sharedPreferences.getBoolean(APP_PREF_OFFLINE, false)
        if (statusOffline)
            Toast.makeText(context, "Оффлайн режим", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(context, mainAuth.currentUser?.email, Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onViewCreated: $statusOffline")
        initToolBar()
        initMainRecycler()
        initSpinner()
    }

    private fun initMainRecycler() {
        binding.rvHome.apply {
            adapter = adapterHome
            layoutManager = LinearLayoutManager(activity)
        }
        binding.swipeContainerHome.setOnRefreshListener {
            updateHomeInfo()
        }

        val note = NoteListModel(
            mutableListOf(
                NoteModel(0, "Учеба", "2021-05-19", "Первая заметка!", false),
                NoteModel(1, "Учеба", "2021-05-18", "Заметка!", false),
                NoteModel(2, "Работа", "2021-05-17", "Заметка!", false),
                NoteModel(3, "Работа", "2021-05-16", "Заметка!", true),
                NoteModel(4, "Быт", "2021-05-19", "Заметка!", false)
            )
        )

        setListData(note)

    }

    private fun updateHomeInfo() {
        Toast.makeText(requireContext(), "В разарботке", Toast.LENGTH_SHORT).show()
    }

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
        TODO("Not yet implemented")
    }

    override fun setListData(data: NoteListModel) {
        adapterHome.submitList(data.data)
    }
}