package com.fefuproject.timemanager.ui.main.task.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.fefuproject.timemanager.R

class CategoryDialog(
    val clickListener: NoticeDialogListener
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            builder.setView(inflater.inflate(R.layout.dialog_create_category, null))
                .setPositiveButton(R.string.Save) { dialog, id ->
                    val et = getDialog()?.findViewById<EditText>(R.id.dialogEtNameFolder)
                    if (et?.text.toString().trim() != "") {
                        clickListener.onDialogPositiveClick(this, et?.text.toString().trim())
                        Toast.makeText(
                            requireContext(),
                            et?.text.toString().trim(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
                .setNegativeButton(R.string.Cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

   /* override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(("$context must implement NoticeDialogListener"))
        }
    }

    private lateinit var listener: NoticeDialogListener*/

    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, category: String)
    }

}