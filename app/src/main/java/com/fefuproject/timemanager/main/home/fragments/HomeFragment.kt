package com.fefuproject.timemanager.main.home.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fefuproject.timemanager.MainActivity
import com.fefuproject.timemanager.MainActivity.Companion.mainAuth
import com.fefuproject.timemanager.MainActivity.Companion.sharedPreferences
import com.fefuproject.timemanager.R
import com.fefuproject.timemanager.base.BaseFragment
import com.fefuproject.timemanager.components.Constants.APP_PREF_OFFLINE
import com.fefuproject.timemanager.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment() {

    companion object {
        private const val TAG = "HOME_TAG"
    }

    private var statusOffline: Boolean = false

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
}