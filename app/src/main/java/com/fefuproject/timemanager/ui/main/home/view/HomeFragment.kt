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
import com.fefuproject.timemanager.components.Constants.APP_PREF_OFFLINE_CHANGE
import com.fefuproject.timemanager.databinding.FragmentHomeBinding
import com.fefuproject.timemanager.logic.db.AppDatabase
import com.fefuproject.timemanager.logic.firebase.models.CategoryFirebase
import com.fefuproject.timemanager.logic.firebase.models.NoteBodyFirebase
import com.fefuproject.timemanager.logic.firebase.models.NoteFirebase
import com.fefuproject.timemanager.logic.models.CategoryModel
import com.fefuproject.timemanager.logic.models.NoteModel
import com.fefuproject.timemanager.ui.MainActivity
import com.fefuproject.timemanager.ui.MainActivity.Companion.mainAuth
import com.fefuproject.timemanager.ui.MainActivity.Companion.sharedPreferences
import com.fefuproject.timemanager.ui.main.home.adapters.HomeAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.log


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
    private lateinit var setCompleteJob: Job
    private lateinit var insertAndDeleteListJop: Job
    private lateinit var deleteAllNoteJob: Job
    private lateinit var deleteAllCategoryJob: Job

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    companion object {
        private const val TAG = "HOME_TAG"
        const val CREATE_TASK = "CREATE_TASK"
        const val NOTE_TASK = "NOTE_TASK"

        const val KEY_CREATE = "KEY_CREATE"
        const val KEY_EDIT = "KEY_EDIT"
        const val KEY_DEFAULT = "KEY_DEFAULT"
        const val FIREBASE_ITEM = "items"
        const val FIREBASE_CATEGORIES = "categories"
    }

    private lateinit var db: AppDatabase
    private lateinit var data: List<NoteModel>
    private lateinit var dataFolders: List<CategoryModel>

    private var statusOffline: Boolean = false
    private var statusUpdateOfflineChange: Boolean = false
    private var statusUpdateNoteOfflineChange: Boolean = false
    private var statusUpdateCategoryOfflineChange: Boolean = false

    private var currentCategory: String? = "-"
    private val adapterHome = HomeAdapter(this)
    private var _binding: FragmentHomeBinding? = null

    private lateinit var databaseFirebase: DatabaseReference

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
        statusUpdateOfflineChange = sharedPreferences.getBoolean(APP_PREF_OFFLINE_CHANGE, false)
        statusUpdateNoteOfflineChange = statusUpdateOfflineChange
        statusUpdateCategoryOfflineChange = statusUpdateOfflineChange
        currentCategory = sharedPreferences.getString(APP_CATEGORY_PREF, "-")
        initToolBar()
        setupLocalDb()
        initSwipeRefreshAndScroll()
        initMainRecycler()
        initDatabase()
        onListeners()
    }

    private fun setupLocalDb() {
        Log.d(TAG, "1")
        db = AppDatabase.invoke(requireContext())
    }

    private fun initDatabase() {
        Log.d(TAG, "2")
        initLocalDb()
//        if (!statusOffline)
        initFirebaseDb()
    }

    private fun initSwipeRefreshAndScroll() {
        Log.d(TAG, "3")
        //swipeRefresh
        binding.swipeContainerHome.setOnRefreshListener {

            if (statusOffline)
                reloadHomeInfoFromLocalDb()
            else
                updateHomeInfoFromFirebase()
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
        Log.d(TAG, "4")

        binding.rvHome.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterHome
        }

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

    private fun initToolBar() {
        Log.d(TAG, "5")

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
                    deleteAllNoteLocalDb()
                    deleteAllNoteFirebase()
                    true
                }
                else -> false
            }
        }
    }

    private fun onListeners() {
        Log.d(TAG, "6")

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
                    if (statusOffline)
                        insertNoteLocalDb(it)

                    pushNoteFirebase(convertNoteToFirebase(it))

                    savedStateHandle.remove<NoteModel>(KEY_CREATE)
                }

            this?.savedStateHandle?.getLiveData<NoteModel>(KEY_EDIT)?.observe(viewLifecycleOwner) {
                //todo обновить заметку
                if (statusOffline)
                    updateNoteLocalDb(it)

//                pushUpdateNoteFirebase(convertNoteToFirebase(it))
                pushNoteFirebase(convertNoteToFirebase(it))

                savedStateHandle.remove<NoteModel>(KEY_EDIT)
            }

        }
    }


    override fun onItemClick(v: View, item: NoteModel, position: Int) {
        Log.d(TAG, "7")
        when (v.id) {
            itemCheckBox -> {
                Log.d(TAG, "onItemClick: ")
                var it: NoteModel = item
                it.complete = it.complete?.not()
                setCompleteNoteLocalDb(it, position)
//                pushCompleteNoteFirebase(convertNoteToFirebase(it))
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

    //TODO FIREBASE --------------------------------------------------------------------------------

    private fun initFirebaseDb() {
        Log.d(TAG, "8")

        if (!statusOffline) {

            try {
                databaseFirebase =
                    FirebaseDatabase.getInstance(
                        "https://taskmanagerfefu-default-rtdb.asia-southeast1.firebasedatabase.app"
                    ).reference

                getDataFromFirebase()
            } catch (e: Exception) {
                Log.e(TAG, "initFirebaseDb: ${e.message.toString()}", e)
                Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_LONG).show()
            }
        } else {
            user_id = UUID.randomUUID().toString()
        }

    }

    private fun getDataFromFirebase() {
        Log.d(TAG, "9")
        if (checkConnectionFirebase()) {
            getNotesFromFirebase()
            getCategoriesFromFirebase()
        }
    }

    private var user = mainAuth.currentUser
    private var user_id = mainAuth.currentUser?.uid

    var noteListFirebase = mutableListOf<NoteFirebase>()
    var categoryListFirebase = mutableListOf<CategoryFirebase>()


    private fun getCategoriesFromFirebase() {
        Log.d(TAG, "10")
        user_id = Firebase.auth.uid
        user = Firebase.auth.currentUser
        Log.d(TAG, "getCategoriesFromFire: ${user_id.toString()}")

        databaseFirebase.child(user_id.toString()).child("categories")
            .addValueEventListener(
                object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        categoryListFirebase.clear()
                        snapshot.children.forEach {
                            categoryListFirebase.add(
                                CategoryFirebase(
                                    it.key.toString(),
                                    (it.value as Map<*, *>)["title"].toString()
                                )
                            )
                        }
                        Log.d(TAG, "fireCategoriesList: $categoryListFirebase")
//                        initSpinner(fireCategoriesList)
                        if (statusUpdateOfflineChange) {
                            statusUpdateCategoryOfflineChange = false
                            firstInitSetCategoryFirebaseToLocal()
                            checkChangeUpdate()
                        } else
                            setCategoryFirebaseToLocal()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "onCancelled: Error", error.toException())
                    }

                }
            )
    }

    private fun getNotesFromFirebase() {
        Log.d(TAG, "11")

        databaseFirebase.child(user_id.toString()).child("items").addValueEventListener(
            object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange")
                    noteListFirebase.clear()
                    snapshot.children.forEach {
                        val v = it.value as Map<*, *>
                        val complete =
                            (it.value as HashMap<String, Boolean>).get("isComplited").toString()
                        Log.e(TAG, "isComplited: $complete")
                        noteListFirebase.add(
                            NoteFirebase(
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
                    Log.d(TAG, "fireItemsList: $noteListFirebase")

                    if (statusUpdateOfflineChange) {
                        statusUpdateNoteOfflineChange = false
                        firstInitSetNoteFirebaseToLocal()
                        checkChangeUpdate()
                    } else
                        setNoteFirebaseToLocal()

                    binding.swipeContainerHome.isRefreshing = false
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: Error", error.toException())
                }

            }
        )
    }

    //метод для поверки локальных данных и синхронизации с firebase
    private fun checkChangeUpdate() {
        Log.d(TAG, "12")

        if (!statusUpdateNoteOfflineChange && !statusUpdateCategoryOfflineChange) {
            statusUpdateOfflineChange = false
            sharedPreferences.edit().putBoolean(APP_PREF_OFFLINE_CHANGE, false).apply()
        }
    }

    private fun firstInitSetCategoryFirebaseToLocal() {
        Log.d(TAG, "13")
        val convertCategoryListLocale =
            mutableListOf<CategoryModel>()  //преобразованный Firebase лист
        var resultCategoryListLocale = mutableListOf<CategoryModel>()   //список на отображение
        val needInsertInLocalCategoryList = mutableListOf<CategoryModel>() //список на вставку
        val needUpdateInLocalCategoryList = mutableListOf<CategoryModel>() //список на обновление

        categoryListFirebase.forEach {
            convertCategoryListLocale.add(convertCategoryToLocal(it))
        }

        resultCategoryListLocale = convertCategoryListLocale

        var coincidence: Boolean

        //todo может работать неправильно
        dataFolders.forEach { localItem ->
            coincidence = false
            convertCategoryListLocale.forEach { fireItem ->
                if (localItem.uid == fireItem.uid) {
                    coincidence = true
                    needUpdateInLocalCategoryList.add(localItem)
                    return@forEach
                }
            }
            if (!coincidence) {
                resultCategoryListLocale.add(localItem)
            }
        }

        convertCategoryListLocale.forEach { fireItem ->
            coincidence = false
            dataFolders.forEach { localItem ->
                if (localItem.uid == fireItem.uid)
                    coincidence = true
            }
            if (!coincidence) {
                needInsertInLocalCategoryList.add(fireItem)
            }
        }
        Log.e(TAG, "setCategoryFirebaseToLocal: ----------")
        Log.i(TAG, "Common list: $resultCategoryListLocale")
        Log.i(TAG, "Need insert list: $needInsertInLocalCategoryList")
        Log.i(TAG, "Need update list: $needUpdateInLocalCategoryList")

        insertListCategoryLocalDb(needInsertInLocalCategoryList as List<CategoryModel>)
        updateListCategoryLocalDb(needUpdateInLocalCategoryList as List<CategoryModel>)
    }

    private fun firstInitSetNoteFirebaseToLocal() {
        Log.d(TAG, "14")
        val convertNoteListLocale = mutableListOf<NoteModel>()  //преобразованный Firebase лист
        var resultNoteListLocale = mutableListOf<NoteModel>()   //список на отображение
        val needInsertInLocalNoteList = mutableListOf<NoteModel>() //список на вставку
        val needUpdateInLocalNoteList = mutableListOf<NoteModel>() //список на обновление
        val needPushToFirebaseNoteList = mutableListOf<NoteFirebase>() //список на обновление

        noteListFirebase.forEach {
            convertNoteListLocale.add(convertNoteToLocal(it))
        }

        resultNoteListLocale = convertNoteListLocale

        var coincidence: Boolean

        //todo может работать неправильно
        if (!data.isNullOrEmpty()) {
            data.forEach { localItem ->
                coincidence = false
                convertNoteListLocale.forEach { fireItem ->
                    if (localItem.uid == fireItem.uid) {
                        coincidence = true
                        needUpdateInLocalNoteList.add(localItem)
                        return@forEach
                    }
                }
                if (!coincidence) {
                    resultNoteListLocale.add(localItem)
                    needPushToFirebaseNoteList.add(convertNoteToFirebase(localItem))
                }
            }

            convertNoteListLocale.forEach { fireItem ->
                coincidence = false
                data.forEach { localItem ->
                    if (localItem.uid == fireItem.uid)
                        coincidence = true
                }
                if (!coincidence) {
                    needInsertInLocalNoteList.add(fireItem)
                }
            }
            Log.e(TAG, "setNoteFirebaseToLocal: ----------")
            Log.i(TAG, "Common list: $resultNoteListLocale")
            Log.i(TAG, "Need insert list: $needInsertInLocalNoteList")
            Log.i(TAG, "Need update list: $needUpdateInLocalNoteList")
            Log.i(TAG, "Need push list: $needPushToFirebaseNoteList")

            insertListNoteLocalDb(needInsertInLocalNoteList as List<NoteModel>)
            updateListNoteLocalDb(needUpdateInLocalNoteList as List<NoteModel>)

            if (!needPushToFirebaseNoteList.isEmpty())
                pushNoteListToFirebase(needPushToFirebaseNoteList as List<NoteFirebase>)
        } else {
            insertListNoteLocalDb(convertNoteListLocale as List<NoteModel>)
        }

    }


    private fun setNoteFirebaseToLocal() {
        Log.d(TAG, "15")
        val convertNoteListLocale = mutableListOf<NoteModel>()  //преобразованный Firebase лист

        noteListFirebase.forEach {
            convertNoteListLocale.add(convertNoteToLocal(it))
        }

        deleteAndInsertNewNoteListLocalDb(convertNoteListLocale)
    }

    private fun setCategoryFirebaseToLocal() {
        Log.d(TAG, "16")
        val convertCategoryListLocale =
            mutableListOf<CategoryModel>()  //преобразованный Firebase лист

        var flagDefValue = false
        categoryListFirebase.forEach {
            if (it.title == "-") flagDefValue = true
        }

        if (!flagDefValue)
            convertCategoryListLocale.add(
                CategoryModel(null, UUID.randomUUID().toString(), "-")
            )

        categoryListFirebase.forEach {
            convertCategoryListLocale.add(convertCategoryToLocal(it))
        }

        deleteAndInsertNewCategoryListLocalDb(convertCategoryListLocale as List<CategoryModel>)
    }

    private fun convertNoteToFirebase(it: NoteModel): NoteFirebase {
        return NoteFirebase(
            id = it.uid ?: UUID.randomUUID().toString(),
            title = it.title.toString(),
            text = it.description.toString(),
            dateToDo = it.dateStart.toString(),
            deadline = it.dateEnd.toString(),
            isComplited = it.complete.toString().toBoolean(),
            category = it.category.toString()
//            it.category?.title.toString()
        )
    }

    private fun convertCategoryToLocal(it: CategoryFirebase): CategoryModel =
        CategoryModel(null, it.id, it.title)


    private fun convertNoteToLocal(it: NoteFirebase): NoteModel =
        NoteModel(
            null,
            it.id,
            it.title,
            it.text,
            it.category,
            it.dateToDo,
            it.deadline,
            it.isComplited
        )


    private fun pushNoteFirebase(it: NoteFirebase) {
        Log.d(TAG, "17")
        if (checkConnectionFirebase()) {
            Log.d(TAG, "pushNewNoteFirebase: $it")
            databaseFirebase
                .child(user_id.toString())
                .child(FIREBASE_ITEM)
                .child(it.id.toString())
                .setValue(convertBodyNote(it))
                .addOnSuccessListener {
//                    updateHomeInfoFromFirebase()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_LONG)
                        .show()
                    Log.e(TAG, "pushNewNoteFirebase: ${it.message}", it)
                }
        }
    }

    private fun pushNoteListToFirebase(noteList: List<NoteFirebase>) {
        Log.d(TAG, "18")
        if (checkConnectionFirebase()) {
            Log.e(TAG, "push list")
            noteList.forEach {
                databaseFirebase
                    .child(user_id.toString())
                    .child(FIREBASE_ITEM)
                    .child(it.id.toString())
                    .setValue(convertBodyNote(it))
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_LONG)
                            .show()
                        Log.e(TAG, "pushNoteListToFirebase: ${it.message}", it)
                    }
            }

        }
    }

    private fun convertBodyNote(it: NoteFirebase): NoteBodyFirebase =
        NoteBodyFirebase(it.title, it.text, it.dateToDo, it.deadline, it.isComplited, it.category)

    private fun updateHomeInfoFromFirebase() {
        Log.d(TAG, "19")
        getDataFromFirebase()
    }

    private fun checkConnectionFirebase(): Boolean {
        Log.d(TAG, "20")
        if (/*Firebase.auth.currentUser != user && !*/statusOffline) {
            Toast.makeText(
                requireContext(),
                "Локальное сохранение",
                Toast.LENGTH_LONG
            ).show()
            statusUpdateOfflineChange = true
            statusOffline = true
            sharedPreferences.edit().putBoolean(APP_PREF_OFFLINE_CHANGE, true).apply()
            return false
        }
        return true
    }

    private fun deleteAllNoteFirebase() {
        Log.d(TAG, "21")
        if (checkConnectionFirebase()) {
            Log.e(TAG, "push list")
            databaseFirebase
                .child(user_id.toString())
                .child(FIREBASE_ITEM)
                .removeValue()
                .addOnFailureListener {
                    Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_LONG)
                        .show()
                    Log.e(TAG, "pushNoteListToFirebase: ${it.message}", it)
                }
        }
    }

    private fun pushCompleteNoteFirebase(it: NoteFirebase) {
        Log.d(TAG, "pushCompleteNoteFirebase: 1")
        if (checkConnectionFirebase()) {
            Log.d(TAG, "pushCompleteNoteFirebase: 2")
            databaseFirebase
                .child(user_id.toString())
                .child(FIREBASE_ITEM)
                .child(it.id.toString())
                .setValue(convertBodyNote(it))
                .addOnFailureListener {
                    Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_LONG)
                        .show()
                    Log.e(TAG, "pushCompleteNoteFirebase: ${it.message}", it)
                }
        }
    }

    //TODO LOCALE DATABASE--------------------------------------------------------------------------

    private fun initSpinnerFromLocalDb(list: List<CategoryModel>) {
        Log.d(TAG, "22")

        val arrayList = arrayListOf<String>()
        list.forEach {
            arrayList.add(it.title.toString())
        }

        val spinnerAdapter: SpinnerAdapter = ArrayAdapter(
            this.requireContext(), R.layout.custom_home_spinner_item, arrayList
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
                sortByCategoryLocalDb()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(requireContext(), "Ничего не выбано", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getIngex(spinner: Spinner): Int {
        Log.d(TAG, "spinner.count: ${spinner.count}")
        val sizeSpinner = spinner.count - 1
        for (i in 0..sizeSpinner) {
            if (spinner.getItemAtPosition(i).toString() == currentCategory) {
                return i
            }
        }
        return 0
    }

    private fun initLocalDb() {
        Log.d(TAG, "23")
        binding.swipeContainerHome.isRefreshing = true
        initDbJob = launch {
            val localData = withContext(Dispatchers.IO) {
                db.noteModelDao().getAll()
            }
            if (localData.isNullOrEmpty()) {
                withContext(Dispatchers.IO) {
                    /*db.noteModelDao().insertAll(
                        NoteModel(
                            null,
                            UUID.randomUUID().toString(),
                            "Заголовок",
                            "Описание",
//                            CategoryModel(null, "Работа"),
                            "Работа",
                            "17 5 2021",
                            "18 5 2021",
                            false
                        )
                    )*/
                    /*db.categoryModelDao()
                        .insertAll(CategoryModel(null, UUID.randomUUID().toString(), "-"))*/
                    /*db.categoryModelDao()
                        .insertAll(CategoryModel(null, UUID.randomUUID().toString(), "Работа"))
                    db.categoryModelDao()
                        .insertAll(
                            CategoryModel(
                                null,
                                UUID.randomUUID().toString(),
                                "Учеба"
                            )
                        )*/
                }
            }
            data =
                withContext(Dispatchers.IO) {
                    db.noteModelDao().getAll()
                }

            getCategoriesLocalDb()

            if (statusOffline) {
                sortByCategoryLocalDb()
            }
        }
    }

    private fun sortByCategoryLocalDb() {
        Log.d(TAG, "24")
        binding.swipeContainerHome.isRefreshing = true
        sortByCategoryJob = launch {
            data = if (currentCategory == "-") {
                withContext(Dispatchers.IO) {
                    db.noteModelDao().getAll()
                }
            } else {
                withContext(Dispatchers.IO) {
                    db.noteModelDao().findByCategory(currentCategory.toString())
                }
            }
            Log.w(TAG, "final data: $data")
            adapterHome.submitList(data.asReversed())
            binding.swipeContainerHome.isRefreshing = false
        }
    }

    fun getCategoriesLocalDb() {
        Log.d(TAG, "25")
        initCategoryJob = launch {
            dataFolders = withContext(Dispatchers.IO) {
                db.categoryModelDao().getAll()
            }
            if (dataFolders.isEmpty()) {
                withContext(Dispatchers.IO) {
                    db.categoryModelDao()
                        .insertAll(CategoryModel(null, UUID.randomUUID().toString(), "-"))
                }
                dataFolders = withContext(Dispatchers.IO) {
                    db.categoryModelDao().getAll()
                }
            }

            binding.topNavigationSpinner.adapter
            initSpinnerFromLocalDb(dataFolders)
        }
    }

    private fun reloadHomeInfoFromLocalDb() {
        Log.d(TAG, "26")
        adapterHome.submitList(null)
        sortByCategoryLocalDb()
    }

    private fun updateNoteLocalDb(it: NoteModel) {
        Log.d(TAG, "27")
        updateListJob = launch {
            withContext(Dispatchers.IO) {
                db.noteModelDao().updateNote(it)
            }
            sortByCategoryLocalDb()
        }
    }

    private fun updateListNoteLocalDb(noteList: List<NoteModel>) {
        Log.d(TAG, "28")
        updateListJob = launch {
            withContext(Dispatchers.IO) {
                db.noteModelDao().updateNoteList(noteList)
            }
            sortByCategoryLocalDb()
        }
    }

    private fun insertListNoteLocalDb(noteList: List<NoteModel>) {
        Log.d(TAG, "29")
        updateListJob = launch {
            withContext(Dispatchers.IO) {
                db.noteModelDao().insertNoteList(noteList)
            }
            sortByCategoryLocalDb()
        }
    }

    private fun insertListCategoryLocalDb(categoryList: List<CategoryModel>) {
        Log.d(TAG, "30")
        updateListJob = launch {
            withContext(Dispatchers.IO) {
                db.categoryModelDao().insertCategoryList(categoryList)
            }
            sortByCategoryLocalDb()
        }
    }

    private fun updateListCategoryLocalDb(categoryList: List<CategoryModel>) {
        Log.d(TAG, "31")
        updateListJob = launch {
            withContext(Dispatchers.IO) {
                db.categoryModelDao().updateCategoryList(categoryList)
            }
            sortByCategoryLocalDb()
            getCategoriesLocalDb()
        }
    }

    private fun insertNoteLocalDb(it: NoteModel) {
        Log.d(TAG, "32")
        insertInDbJob = launch {
            withContext(Dispatchers.IO) {
                db.noteModelDao().insertAll(it)
            }
            if (!statusOffline) {
                sortByCategoryLocalDb()
                getCategoriesLocalDb()
            }
        }
    }

    private fun deleteAndInsertNewNoteListLocalDb(noteList: List<NoteModel>) {
        Log.d(TAG, "33")
        insertAndDeleteListJop = launch {
            withContext(Dispatchers.IO) {
//                if (!data.isNullOrEmpty())
//                    db.noteModelDao().deleteNoteList(data)
                    db.noteModelDao().deleteAll()
            }
            withContext(Dispatchers.IO) {
                db.noteModelDao().insertNoteList(noteList)
            }

            reloadHomeInfoFromLocalDb()
        }
    }

    private fun deleteAllNoteLocalDb() {
        Log.d(TAG, "34")
        deleteAllNoteJob = launch {
            withContext(Dispatchers.IO) {
                db.noteModelDao().deleteNoteList(data)
            }
            sortByCategoryLocalDb()
        }
    }

    private fun deleteAndInsertNewCategoryListLocalDb(categoryList: List<CategoryModel>) {
        Log.d(TAG, "35")
        deleteAllCategoryJob = launch {
            withContext(Dispatchers.IO) {
                if (!dataFolders.isNullOrEmpty()){
                    Log.e(TAG, "deleteAndInsertNewCategoryListLocalDb: delete", )
                    db.categoryModelDao().deleteCategoryList(dataFolders)
                }
            }
            withContext(Dispatchers.IO) {
                db.categoryModelDao().updateCategoryList(categoryList)
            }
            getCategoriesLocalDb()
            sortByCategoryLocalDb()
        }
    }

    private fun setCompleteNoteLocalDb(it: NoteModel, position: Int) {
        Log.d(TAG, "36")
        setCompleteJob = launch {
            withContext(Dispatchers.IO) {
                db.noteModelDao().updateNote(it)
            }
            adapterHome.notifyItemChanged(position)
        }
    }

}