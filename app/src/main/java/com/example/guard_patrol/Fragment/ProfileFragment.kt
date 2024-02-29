package com.example.guard_patrol.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.guard_patrol.Activity.ChangePasswordActivity
import com.example.guard_patrol.Activity.LoginActivity
import com.example.guard_patrol.Class.ProfileClass
import com.example.guard_patrol.Data.Dialog.LoadingDialog
import com.example.guard_patrol.Data.Service.AllService
import com.example.guard_patrol.Data.Preference.TokenPref
import com.example.guard_patrol.Data.Preference.WorkspacePref
import com.example.guard_patrol.databinding.FragmentProfileBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@SuppressLint("SetTextI18n")
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding  // Declare the binding variable
    private lateinit var workspacePreference: WorkspacePref
    private lateinit var tokenPreference: TokenPref
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        workspacePreference = WorkspacePref(requireContext())
        tokenPreference = TokenPref(requireContext())
        loadingDialog = LoadingDialog(requireContext())

        binding.btnLogout.setOnClickListener{
            workspacePreference.clearData()
            tokenPreference.clearData()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnChangePass.setOnClickListener{
            val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        getProfile()

        return binding.root
    }

    private fun getProfile(){
        val query: String = "query SelectProfile {\n" +
                "  selectProfile {\n" +
                "    id\n" +
                "    firstname\n" +
                "    lastname\n" +
                "    role\n" +
                "    birthday\n" +
                "    phone_number\n" +
                "    email\n" +
                "    username\n" +
                "    password\n" +
                "  }\n" +
                "}"

        val reqObject = JsonObject()
        reqObject.addProperty("query",query)
        val token = tokenPreference.getPreferences()
        val headersObject = JsonObject()
        headersObject.addProperty("Authorization", "Bearer $token")
        reqObject.add("headers", headersObject)

        val retrofitService = AllService.getInstance()
        retrofitService.callGraphQLService(reqObject).enqueue(object:
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
                        val gson = Gson()
                        val profileData = gson.fromJson(responseBody, ProfileClass::class.java)
                        val profile = profileData.data?.selectProfile
                        binding.fullNameTxt.text = "${profile?.firstname} ${profile?.lastname}"
                        var role = ""
                        if(profile?.role == "user"){
                            role = "พนักงานรักษาความปลอดภัย"
                        }else if (profile?.role == "Superuser"){
                            role = "หัวหน้ารปภ"
                        }
                        binding.roleTxt.text = role

                        val myBirthday = profile?.birthday?.let { convertDateSelect(it) }
                        binding.birthdayTxt.text = "$myBirthday"
                        binding.phoneNumberTxt.text = "${profile?.phoneNumber}"
                        binding.emailTxt.text = "${profile?.email}"
//                        Log.d("TestProfile", "Pass Test $responseBody")
                    } catch (e: Exception) {
                        Log.e("TestProfile", "Error parsing response body: $e")
                    }
                } else {
                    Log.e("TestProfile", "Fail Test ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("TestProfile","Error $t")
            }
        })
    }

    fun convertDateSelect(dateString: String): String {
        val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date: Date? = sdfInput.parse(dateString)

        val localDate = date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
        val thaiBuddhistDate = localDate?.let { LocalDate.from(it) }
        val fullDateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("th", "TH"))

        return thaiBuddhistDate?.format(fullDateFormat) ?: ""
    }

}