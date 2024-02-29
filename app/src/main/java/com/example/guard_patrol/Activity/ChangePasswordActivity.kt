package com.example.guard_patrol.Activity

import BasedActivity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.guard_patrol.Class.ChangePasswordClass
import com.example.guard_patrol.Class.TokenClass
import com.example.guard_patrol.Data.Service.AllService
import com.example.guard_patrol.R
import com.example.guard_patrol.databinding.ActivityChangePasswordBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

class ChangePasswordActivity : BasedActivity() {
    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener{
            onBackPressed()
        }

        binding.btnSetNewPassword.setOnClickListener{
            loadingDialog.showLoadingDialog(this)
            setNewPasswordCheck()
        }

        binding.checkboxShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.editOldPassword.transformationMethod = null
                binding.editNewPassword.transformationMethod = null
                binding.editConfirmPassword.transformationMethod = null
            } else {
                binding.editOldPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.editNewPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.editConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
    }

    private fun setNewPasswordCheck() {
        val editOldPassword = binding.editOldPassword.text
        val editNewPassword = binding.editNewPassword.text
        val editConfirmPassword = binding.editConfirmPassword.text

        binding.inputOldPassword.apply {
            strokeLineColor = if (editOldPassword.isNullOrEmpty()) Color.RED else Color.TRANSPARENT
            strokeLineWidth = if (editOldPassword.isNullOrEmpty()) 10F else 0F
        }

        binding.inputNewPassword.apply {
            strokeLineColor = if (editNewPassword.isNullOrEmpty()) Color.RED else Color.TRANSPARENT
            strokeLineWidth = if (editNewPassword.isNullOrEmpty()) 10F else 0F
        }

        binding.inputConfirmPassword.apply {
            strokeLineColor = if (editConfirmPassword.isNullOrEmpty()) Color.RED else Color.TRANSPARENT
            strokeLineWidth = if (editConfirmPassword.isNullOrEmpty()) 10F else 0F
        }

        if (!editOldPassword.isNullOrEmpty() && !editNewPassword.isNullOrEmpty() && !editConfirmPassword.isNullOrEmpty()){
            if (editOldPassword.toString() == editNewPassword.toString()){
                binding.inputNewPassword.apply {
                    strokeLineColor = Color.RED
                    strokeLineWidth = 10F
                }
                loadingDialog.dismissLoadingDialog()
                Toast.makeText(this, "รหัสผ่านใหม่ต้องไม่เหมือนกับรหัสผ่านปัจจุบัน", Toast.LENGTH_LONG).show()
            }else{
                if (editNewPassword.toString() == editConfirmPassword.toString()){
                    checkResponse(editOldPassword.toString(),editNewPassword.toString()) { status ->
                        if (status != "false") {
                            if (status == "true"){
                                loadingDialog.dismissLoadingDialog()
                                showCustomPassDialogBox()
                            }
                        }else {
                            binding.inputOldPassword.apply {
                                strokeLineColor = Color.RED
                                strokeLineWidth = 10F
                            }
                            loadingDialog.dismissLoadingDialog()
                            Toast.makeText(this, "รหัสผ่านปัจจุบันไม่ถูกต้อง", Toast.LENGTH_LONG).show()
                        }
                    }
                }else{
                    binding.inputConfirmPassword.apply {
                        strokeLineColor = Color.RED
                        strokeLineWidth = 10F
                    }
                    loadingDialog.dismissLoadingDialog()
                    Toast.makeText(this, "การยืนยันรหัสผ่านไม่ตรงกัน", Toast.LENGTH_LONG).show()
                }
            }
        }else{
            loadingDialog.dismissLoadingDialog()
            Toast.makeText(this, "กรุณากรอกรหัสผ่านให้ครบ", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkResponse(oldPassword: String, newPassword:String, callback: (String?) -> Unit) {
        val paramObject = JsonObject()
        paramObject.addProperty("passwordOld", oldPassword)
        paramObject.addProperty("passwordNew", newPassword)
        val query: String = "mutation ChangePassword(\$passwordOld: String!, \$passwordNew: String!) {\n" +
                "  changePassword(password_old: \$passwordOld, password_new: \$passwordNew) {\n" +
                "    status\n" +
                "    massager\n" +
                "  }\n" +
                "}"
        val reqObject = JsonObject()
        reqObject.addProperty("query",query)
        reqObject.add("variables", paramObject)
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
                        val dataResponse = gson.fromJson(responseBody, ChangePasswordClass::class.java)
                        val status = dataResponse.data?.changePassword?.status
//                        Log.e("TestStatus", "status $status")
                        callback(status)

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

    private fun showCustomPassDialogBox(){
        val title = "เปลี่ยนรหัสผ่านสำเร็จ"
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_pass_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val icDialog : ImageView = dialog.findViewById(R.id.icDialog)
        val txtTitleDialog : TextView = dialog.findViewById(R.id.txtTitleDialog)

        icDialog.setImageResource(R.drawable.ic_pass)
        txtTitleDialog.text = title

        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = layoutParams

        dialog.show()

        Handler().postDelayed({
            onBackPressed()
            dialog.dismiss()
        }, 2000)
    }
}