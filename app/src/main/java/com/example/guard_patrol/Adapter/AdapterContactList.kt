package com.example.guard_patrol.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.guard_patrol.Class.AuthenticationPhone
import com.example.guard_patrol.databinding.CustomContactListBinding

@SuppressLint("NotifyDataSetChanged")
class AdapterContactList: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var dataList = ArrayList<AuthenticationPhone>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var callPhone: ((number: String?)-> Unit)? = null

    fun setFilteredList(wList: ArrayList<AuthenticationPhone>){
        this.dataList = wList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CustomContactListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomContactListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CustomContactListViewHolder).bindContactList(dataList[position])
    }

    inner class CustomContactListViewHolder(private val binding: CustomContactListBinding) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bindContactList(authenticationPhone: AuthenticationPhone){
            val fname = authenticationPhone.firstname
            val lname = authenticationPhone.lastname
            binding.txtTitle.text = "$fname $lname"
            binding.txtSubTitle.text = authenticationPhone.phoneNumber
            binding.contactLayout.setOnClickListener{
                callPhone?.invoke(authenticationPhone.phoneNumber)
            }
        }
    }
}