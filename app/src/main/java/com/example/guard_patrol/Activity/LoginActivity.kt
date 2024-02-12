package com.example.guard_patrol.Activity

import BasedActivity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.guard_patrol.Class.WorkspaceClass
import com.example.guard_patrol.Class.WorkspaceItem
import com.example.guard_patrol.Data.AllService
import com.example.guard_patrol.Class.TokenClass
import com.example.guard_patrol.R
import com.example.guard_patrol.databinding.ActivityLoginBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

class LoginActivity : BasedActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var workspaceTodayList = ArrayList<WorkspaceItem>()

    private var iconEye = true
        set(value) {
            field = value
            if (field) {
                binding.icEye.setImageResource(R.drawable.ic_eye_gray)
                binding.editPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            } else {
                binding.icEye.setImageResource(R.drawable.ic_eye_slash_gray)
                binding.editPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.binding.btnLogin.setOnClickListener{
            loginCheck()
        }
        binding.icEye.setOnClickListener { iconEye = !iconEye }
        //fun check Input Username
        binding.editUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    binding.icCancel.visibility = View.GONE
                } else {
                    binding.icCancel.visibility = View.VISIBLE
                    //Clear Username Icon
                    binding.icCancel.setOnClickListener {
                        binding.editUsername.text.clear()
                        binding.icCancel.visibility = View.GONE
                    }
                }
            }
        }
        )
    }

    //fun check login error
    private fun loginCheck() {
        val usernameText = binding.editUsername.text
        val passwordText = binding.editPassword.text

        binding.inputUsername.apply {
            strokeLineColor = if (usernameText.isNullOrEmpty()) Color.RED else Color.TRANSPARENT
            strokeLineWidth = if (usernameText.isNullOrEmpty()) 10F else 0F
        }

        binding.inputPassword.apply {
            strokeLineColor = if (passwordText.isNullOrEmpty()) Color.RED else Color.TRANSPARENT
            strokeLineWidth = if (passwordText.isNullOrEmpty()) 10F else 0F
        }

        //fun get token
        checkResponse(usernameText.toString(),passwordText.toString()) { token ->
            if (token != null) {
                // create and show Progress Dialog
                loadingDialog = ProgressDialog.show(this, "Please wait.", "Loading...", true, false)

                tokenPreference.setPreferences(token)
//                Log.d("Test TOKEN##", tokenPreference.getPreferences())

                //Add Data workspace
                addDataWorkspaceToList()
            } else if (usernameText.isNullOrEmpty() || passwordText.isNullOrEmpty()) {
                Toast.makeText(this, "Please fill in both username and password", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Username or password is wrong. Please try again.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addDataWorkspaceToList(){
        val query: String = "query PerformTask {\n" +
                "  performTask {\n" +
                "    workspace_all {\n" +
                "      id\n" +
                "      workspace\n" +
                "    }\n" +
                "    workspace_today {\n" +
                "      id\n" +
                "      workspace\n" +
                "    }\n" +
                "  }\n" +
                "}"

        val reqObject = JsonObject()
        reqObject.addProperty("query",query)

        AllService.initialize(applicationContext)
        val retrofitService = AllService.getInstance()
        retrofitService.callGraphQLService(reqObject).enqueue(object :
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
//                        Log.d("TestWorkspaceResponse", "Test1 $responseBody")
                        val gson = Gson()
                        val workspaceResponse = gson.fromJson(responseBody, WorkspaceClass::class.java)
                        val workspaceToday = workspaceResponse.data?.performTask?.workspaceToday
                        val listWorkspaceTodayId = workspaceToday?.map { it.id }
                        val listWorkspaceTodayName = workspaceToday?.map { it.workspace }
                        val valuesList = workspacePreference.getPreferences()
                        if (listWorkspaceTodayName != null && listWorkspaceTodayId != null) {
                            for ((index, name) in listWorkspaceTodayName.withIndex()) {
                                val id = listWorkspaceTodayId.getOrNull(index) ?: ""
                                var stats = false
                                if (valuesList.isNotEmpty()){
                                    val selectedWorkspace = valuesList[1]
                                    stats = (name == selectedWorkspace)
                                }
                                workspaceTodayList.add(WorkspaceItem(id, name, stats))
//                                Log.d("TestWorkspaceList", "check $workspaceTodayList")
                            }
                            handleWorkspacePreferences()
                        }

                    } catch (e: Exception) {
                        Log.e("TestGetWorkspaceLogin", "Error parsing response body: $e")
                    }
                } else {
                    Log.e("TestGetWorkspaceLogin", "Fail Test ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("TestGetWorkspaceLogin","Error $t")
            }

        })
    }
    
    private fun checkResponse(username: String, password:String, callback: (String?) -> Unit) {
        val paramObject = JsonObject()
        paramObject.addProperty("username", username)
        paramObject.addProperty("password", password)
        val query: String = "query userLogin(\$username: String!, \$password: String!) {\n" +
                "  userLogin(username: \$username, password: \$password) {\n" +
                "    token\n" +
                "  }\n" +
                "}\n"

        val reqObject = JsonObject()
        reqObject.addProperty("query",query)
        reqObject.add("variables", paramObject)

        val retrofitService = AllService.getInstance()
        retrofitService.callGraphQLService(reqObject).enqueue(object:
            retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
                        val gson = Gson()
                        val userLoginResponse = gson.fromJson(responseBody, TokenClass::class.java)
                        val token = userLoginResponse.data?.userLogin?.token
//                        Log.e("TestQL", "Pass Test $token")
                        callback(token)

                    } catch (e: Exception) {
                        Log.e("TestQL", "Error parsing response body: $e")
                    }
                } else {
                    Log.e("TestQL", "Fail Test ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("TestQL","Error $t")
            }
        })
    }

    private fun handleWorkspacePreferences() {
        val value = workspacePreference.getPreferences()
        if (value.isEmpty()) {
//            Log.d("TestSize", "check ${workspaceTodayList.size}")
            if (workspaceTodayList.size == 1) {
                // Auto select Workspace when only one Workspace
                val defaultWorkspace: List<Pair<String?, String?>> = workspaceTodayList.map { it.id to it.workspace }
                defaultWorkspace.getOrNull(0)?.let { (id, workspace) ->
                    if (id != null && workspace != null) {
                        workspacePreference.setPreferences(id, workspace)
                    }
                }

                loadingDialog.dismiss()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                loadingDialog.dismiss()
                val intent = Intent(this, SelectWorkspaceActivity::class.java)
                startActivity(intent)
            }
        } else {
            loadingDialog.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}