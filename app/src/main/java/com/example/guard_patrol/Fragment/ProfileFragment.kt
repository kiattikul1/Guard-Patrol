package com.example.guard_patrol.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.guard_patrol.Activity.LoginActivity
import com.example.guard_patrol.Activity.MainActivity
import com.example.guard_patrol.Data.Preference.TokenPref
import com.example.guard_patrol.Data.Preference.WorkspacePref
import com.example.guard_patrol.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding  // Declare the binding variable
    private lateinit var workspacePreference: WorkspacePref
    private lateinit var tokenPreference: TokenPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        workspacePreference = WorkspacePref(requireContext())
        tokenPreference = TokenPref(requireContext())

        binding.btnLogout.setOnClickListener{
            workspacePreference.clearData()
            tokenPreference.clearData()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

}