package com.example.guard_patrol.Fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.guard_patrol.Activity.HistoryActivity
import com.example.guard_patrol.Activity.ScannerActivity
import com.example.guard_patrol.Activity.SelectWorkspaceActivity
import com.example.guard_patrol.Data.Preference.WorkspacePref
import com.example.guard_patrol.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding  // Declare the binding variable
    private lateinit var workspacePreference: WorkspacePref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        workspacePreference = WorkspacePref(requireContext())

        binding.selectWorkspace.setOnClickListener {
            val intent = Intent(requireContext(), SelectWorkspaceActivity::class.java)
            startActivity(intent)
        }
        binding.buttonScan.setOnClickListener {
            val intent = Intent(requireContext(), ScannerActivity::class.java)
            startActivity(intent)
        }
        binding.buttonHistory.setOnClickListener {
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            startActivity(intent)
        }

        // Get value SharedPreferences
        val valuesList = workspacePreference.getPreferences()
        binding.txtWorkspace.text = valuesList[1]
        binding.txtWorkspace.setTextColor(Color.WHITE)
        binding.txtWorkspace.alpha = 1F

        return binding.root
    }

}