package com.example.guard_patrol.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.guard_patrol.Class.SelectEmergency
import com.example.guard_patrol.databinding.CustomDateSelectBinding
import com.example.guard_patrol.databinding.CustomEmergencyCallBinding

@SuppressLint("NotifyDataSetChanged")
class AdapterEmergencyCall: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var dataList = ArrayList<SelectEmergency>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var callPhone: ((number: String?)-> Unit)? = null

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CustomEmergencyCallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomEmergencyCallViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CustomEmergencyCallViewHolder).bindEmergencyCall(dataList[position],position)
    }

    inner class CustomEmergencyCallViewHolder(private val binding: CustomEmergencyCallBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindEmergencyCall(selectEmergency: SelectEmergency,position: Int){
            binding.txtTitle.text = selectEmergency.title
            binding.callPhoneLayout.setOnClickListener{
                callPhone?.invoke(selectEmergency.phoneNumber)
            }
        }
    }
}