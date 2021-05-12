package com.fefuproject.timemanager.main.home.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fefuproject.timemanager.MainActivity
import com.fefuproject.timemanager.MainActivity.Companion.mainAuth
import com.fefuproject.timemanager.base.BaseFragment
import com.fefuproject.timemanager.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment() {

    companion object {
        private const val TAG = "HOME_TAG"
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onListener()

    }

    fun onListener() {
        binding.homeOut.setOnClickListener {
            (requireActivity() as MainActivity).getOut()
        }
    }
}