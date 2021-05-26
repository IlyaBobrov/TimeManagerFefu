package com.fefuproject.timemanager.ui.main.home.presenter

import android.util.Log
import com.fefuproject.timemanager.base.presenter.BasePresenter
import com.fefuproject.timemanager.logic.db.AppDatabase
import com.fefuproject.timemanager.logic.models.NoteModel
import com.fefuproject.timemanager.ui.main.home.interactor.IHomeInteractor
import com.fefuproject.timemanager.ui.main.home.view.IHomeView
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class HomePresenter<V : IHomeView, I : IHomeInteractor>
constructor(
    val db: AppDatabase,
    interactor: I
) : BasePresenter<V, I>(interactor),
    IHomePresenter<V, I>, CoroutineScope {

    private val job = SupervisorJob()
    private lateinit var dbJob: Job
    private lateinit var insertNoteJob: Job
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private val handler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    val TAG = "PRESENTER_TAG"

    override fun getNotes() {
        Log.d(TAG, "getNotes")
        dbJob = launch(handler) {
            val notes = db.noteModelDao().getAll()
            Log.d(TAG, "Notes: $notes")
            if (notes.equals(null)) {
                Log.d(TAG, "Notes: hull")
            } else {
                Log.d(TAG, "Notes not null: $notes")

                getView()?.getDb(notes = notes)
            }
        }
    }

    override fun insertNote() {
        Log.d(TAG, "insertNote")
        insertNoteJob = launch(handler) {
            val data = withContext(Dispatchers.IO) {
                suspend {
                    db.noteModelDao().insertAll(
                        NoteModel(1, "Work", "Today", "hi", false)
                    )
                    db.noteModelDao().getAll()
                }
            }
            Log.d(TAG, "insertNote: ${data}")

        }
    }


}