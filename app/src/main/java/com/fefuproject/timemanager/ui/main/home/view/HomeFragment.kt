package com.fefuproject.timemanager.ui.main.home.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fefuproject.timemanager.R
import com.fefuproject.timemanager.R.id.itemCheckBox
import com.fefuproject.timemanager.base.BaseFragment
import com.fefuproject.timemanager.components.Constants.APP_CATEGORY_PREF
import com.fefuproject.timemanager.components.Constants.APP_PREF_OFFLINE
import com.fefuproject.timemanager.databinding.FragmentHomeBinding
import com.fefuproject.timemanager.logic.firebase.models.Categories
import com.fefuproject.timemanager.logic.firebase.models.Items
import com.fefuproject.timemanager.logic.locale.models.NoteModel
import com.fefuproject.timemanager.ui.MainActivity
import com.fefuproject.timemanager.ui.MainActivity.Companion.mainAuth
import com.fefuproject.timemanager.ui.MainActivity.Companion.sharedPreferences
import com.fefuproject.timemanager.ui.main.home.adapters.HomeAdapter
import com.fefuproject.timemanager.ui.main.home.adapters.ItemTouchCallback
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.*
import kotlin.coroutines.CoroutineContext


class HomeFragment : BaseFragment(),
    IHomeView,
    HomeAdapter.HomeOnItemClickListener,
    CoroutineScope {

    companion object {
        private const val TAG = "HOME_TAG"
        const val CREATE_TASK = "CREATE_TASK"
        const val NOTE_TASK = "NOTE_TASK"

        const val KEY_CREATE = "KEY_CREATE"
        const val KEY_EDIT = "KEY_EDIT"
        const val KEY_DEFAULT = "KEY_DEFAULT"

        var fireCategoriesList = mutableListOf<Categories>()

    }

    //fire
    val firebase = Firebase.database
    private lateinit var firebaseReference: DatabaseReference
    private var user = mainAuth.currentUser
    private var user_id = mainAuth.currentUser?.uid


    private var statusOffline: Boolean = false
    private var currentCategory: String? = "-"
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
        onListeners()
        initBody()
    }

    private fun initBody() {
        if (statusOffline) {
            user_id = UUID.randomUUID().toString()
        }
        Log.d(TAG, "user_id: $user_id")
        Log.d(TAG, "user: $user")

//        moveFirebaseRecord(firebaseReference.child(user_id.toString()), )
        initFirebase()

        //initDB()
    }

    //todo
    fun moveFirebaseRecord(fromPath: DatabaseReference, toPath: DatabaseReference) {
        fromPath.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                toPath.setValue(dataSnapshot.value) { databaseError, databaseReference ->
                    if (databaseError != null) {
                        Log.d(TAG, "moveFirebaseRecord() failed: $databaseError")
                    } else {
                        fromPath.removeValue() // deleteAll from oldDB
                        Log.d(TAG, "moveFirebaseRecord() Great success!")
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }


    val fireItemsList = mutableListOf<Items>()

    private fun initFirebase() {
        Log.d(TAG, "initFirebase: statusOffline: $statusOffline")
        binding.swipeContainerHome.isRefreshing = true
        if (!statusOffline) {
            firebaseReference = Firebase.database(
                url = "https://taskmanagerfefu-default-rtdb.asia-southeast1.firebasedatabase.app"
            ).reference
            getItemsFromFire()
            getCategoriesFromFire()
        } else {
            binding.swipeContainerHome.isRefreshing = false
        }
    }

    private fun getCategoriesFromFire() {
        user_id = Firebase.auth.uid
        user = Firebase.auth.currentUser
        Log.d(TAG, "getCategoriesFromFire: ${user_id.toString()}")

        firebaseReference.child(user_id.toString()).child("categories")
            .addListenerForSingleValueEvent(
                object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        fireCategoriesList.clear()
                        snapshot.children.forEach {
                            fireCategoriesList.add(
                                Categories(
                                    it.key.toString(),
                                    (it.value as Map<*, *>)["title"].toString()
                                )
                            )
                        }
                        Log.d(TAG, "fireCategoriesList: $fireCategoriesList")
                        initSpinner(fireCategoriesList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "onCancelled: Error", error.toException())
                    }

                }
            )
    }

    private fun getItemsFromFire() {
        firebaseReference.child(user_id.toString()).child("items").addListenerForSingleValueEvent(
            object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    fireItemsList.clear()
                    snapshot.children.forEach {
                        val v = it.value as Map<*, *>
                        fireItemsList.add(
                            Items(
                                it.key.toString(),
                                v["title"].toString(),
                                v["text"].toString(),
                                v["dateToDo"].toString(),
                                v["deadline"].toString(),
                                v["isComplited"].toString().toBoolean(),
                                v["category"].toString()
                            )
                        )
                    }
                    Log.d(TAG, "fireItemsList: $fireItemsList")
                    adapterHome.submitList(fireItemsList)
                    binding.swipeContainerHome.isRefreshing = false
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: Error", error.toException())
                }

            }
        )
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


    private fun initMainRecycler() {
        binding.rvHome.apply {
            layoutManager = LinearLayoutManager(context)
//            adapterHome.submitList(null)
            adapter = adapterHome
        }
        setUpSwipeRecyclerView()
    }


    private fun setUpSwipeRecyclerView() {

        val swipeHandler = object : ItemTouchCallback(requireContext()) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                Log.d(TAG, "absoluteAdapterPosition: ${viewHolder.absoluteAdapterPosition}")

//                if (data.isNullOrEmpty()) return
//                val itemDelete = adapterHome.currentList[viewHolder.absoluteAdapterPosition]
//                localdbDeleteItem(itemDelete)

                adapterHome.notifyItemRemoved(viewHolder.absoluteAdapterPosition)

//                adapterHome.notifyItemChanged(viewHolder.absoluteAdapterPosition, (data.size - 1))
//                adapterHome.notifyDataSetChanged()
            }

            /*override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.absoluteAdapterPosition
                val toPosition = target.absoluteAdapterPosition
                Log.d(TAG, "fromPosition: $fromPosition")
                Log.d(TAG, "toPosition: $toPosition")

                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(data, i, i + 1)
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(data, i, i - 1)
                    }
                }
                adapterHome.notifyItemMoved(fromPosition, toPosition)
                return true
            }*/
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvHome)
    }


    private fun updateHomeInfo() {
        adapterHome.submitList(null)
//        localdbSortByCategory()
        sortByCategory()
    }

    private fun sortByCategory() {
        binding.swipeContainerHome.isRefreshing = true
        //todo
//        adapterHome.submitList(data.reversed())

        binding.swipeContainerHome.isRefreshing = false
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


    private fun initSpinner(list: List<Categories>) {

        val arrayList = arrayListOf<String>()
        list.forEach {
            arrayList.add(it.title)
        }
        Log.d(TAG, "initSpinner: $list")

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
//                localdbSortByCategory()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(requireContext(), "Ничего не выбано", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*private fun initSpinner(list: List<CategoryModel>) {

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
//                localdbSortByCategory()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(requireContext(), "Ничего не выбано", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

    private fun getIngex(spinner: Spinner): Int {
        if (spinner.count == 1) return 0
        for (i in 0..spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == currentCategory) {
                return i
            }
        }
        return 0
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

            this?.savedStateHandle?.getLiveData<Items>(KEY_CREATE)
                ?.observe(viewLifecycleOwner)
                {
                    fireNewNote(it)
                    savedStateHandle.remove<Items>(KEY_CREATE)
                }

            this?.savedStateHandle?.getLiveData<Items>(KEY_EDIT)?.observe(viewLifecycleOwner) {
                fireEditNote(it)
                savedStateHandle.remove<Items>(KEY_EDIT)
            }

        }
        /*with(findNavController().currentBackStackEntry) {

            this?.savedStateHandle?.getLiveData<NoteModel>(KEY_CREATE)
                ?.observe(viewLifecycleOwner)
                {

//                    localdbNewNote(it)
                    savedStateHandle.remove<NoteModel>(KEY_CREATE)
                }

            this?.savedStateHandle?.getLiveData<NoteModel>(KEY_EDIT)?.observe(viewLifecycleOwner) {

//                localdbUpdateNote(it)
                savedStateHandle.remove<NoteModel>(KEY_EDIT)
            }

        }*/
    }

    private fun fireEditNote(it: Items?) {
        Log.e(TAG, "fireEditNote: ")
        val key = firebaseReference.child(user_id.toString()).child("items").push().key
        if (key == null) {
            Log.w(TAG, "Couldn't get push key for posts")
            return
        }

        val item = it
        val postValues = item!!.toMap()

        val childUpdates = hashMapOf<String, Any>(
            "/posts/$key" to postValues,
            "/user-posts/$user_id/$key" to postValues
        )

        firebaseReference.updateChildren(childUpdates)
    }

    private fun fireNewNote(it: Items?) {

    }

    override fun onItemClick(v: View, item: Items, position: Int) {
        when (v.id) {
            itemCheckBox -> {
                Log.d(TAG, "onItemClick: ")
//                localdbSetCompite(item, position)
            }
            else -> {
                val bundle = Bundle()
                bundle.putString(CREATE_TASK, KEY_EDIT)
                bundle.putParcelable(NOTE_TASK, item)
                findNavController().navigate(
                    R.id.action_homeFragment_to_taskFragment,
                    args = bundle
                )
            }
        }
    }


    /*override fun onItemClick(v: View, item: NoteModel, position: Int) {
        when (v.id) {
            itemCheckBox -> {
                Log.d(TAG, "onItemClick: ")
//                localdbSetCompite(item, position)
            }
            else -> {
                val bundle = Bundle()
                bundle.putString(CREATE_TASK, KEY_EDIT)
                bundle.putParcelable(NOTE_TASK, item)
                findNavController().navigate(
                    R.id.action_homeFragment_to_taskFragment,
                    args = bundle
                )
            }
        }
    }*/


    //localDB
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

//    private lateinit var initDbJob: Job
//    private lateinit var insertInDbJob: Job
//    private lateinit var updateListJob: Job
//    private lateinit var deleteItemJob: Job
//    private lateinit var initCategoryJob: Job
//    private lateinit var sortByCategoryJob: Job
//    private lateinit var setCompiteJob: Job

//    private lateinit var db: AppDatabase
//    private lateinit var data: List<NoteModel>
//    private lateinit var folders: List<CategoryModel>
//    val listNotes = mutableListOf<NoteModel>()

    /*

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
            localdbSortByCategory()
            getCategories()
        }
    }

    private fun localdbSortByCategory() {
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
            listNotes.clear()
            listNotes.addAll(data.reversed())
//            adapterHome.submitList(data.asReversed())
            adapterHome.submitList(data.reversed())
            binding.swipeContainerHome.isRefreshing = false
        }
    }

    fun localdbGetCategories() {
        initCategoryJob = launch {
            folders = withContext(Dispatchers.IO) {
                db.categoryModelDao().getAll()
            }
            binding.topNavigationSpinner.adapter
            initSpinner(folders)
        }
    }

    private fun localdbDeleteItem(item: NoteModel) {
        updateListJob = launch {
            withContext(Dispatchers.IO) {
                db.noteModelDao().delete(item)
            }
            localdbSortByCategory()
        }
    }
    private fun localdbNewNote(item: NoteModel) {
        //todo добавить заметку в бд и обновить список
        insertInDbJob = launch {
            withContext(Dispatchers.IO) {
                db.noteModelDao().insertAll(item)
            }
            localdbSortByCategory()
        }
    }
    private fun localdbUpdateNote(item: NoteModel) {
        //todo обновить заметку
        updateListJob = launch {
            withContext(Dispatchers.IO) {
                db.noteModelDao().updateNote(item)
            }
            localdbSortByCategory()
        }
    }

    private fun localdbSetCompite(item: NoteModel, position: Int) {
        val it: NoteModel = item
        setCompiteJob = launch {
            withContext(Dispatchers.IO) {
                it.complete = item.complete?.not()
                db.noteModelDao().updateNote(it)
            }
            adapterHome.notifyItemChanged(position)
        }
    }*/

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

}