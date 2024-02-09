package com.example.guard_patrol.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.guard_patrol.Class.SOPS
import com.example.guard_patrol.databinding.CustomHistoryDetailTaskSopBinding

@SuppressLint("NotifyDataSetChanged")
class AdapterHistoryDetailTaskSop : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dataList = ArrayList<SOPS>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    override fun getItemCount(): Int {
        return dataList.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CustomHistoryDetailTaskSopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AdapterHistoryDetailTaskSop.CustomDetailViewHolder).bindDetailView(dataList[position],position)
    }

    inner class CustomDetailViewHolder(private val binding : CustomHistoryDetailTaskSopBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindDetailView(dataClass: SOPS, position: Int){
            val detail = dataClass.titleSop
            val description = dataClass.description
            binding.txtDetail.text = "${position+1}. $detail"
            if (description.isNullOrEmpty()){
                binding.txtDescription.visibility = View.GONE
            }else{
                binding.txtDescription.text = "- $description"
                binding.txtDescription.visibility = View.VISIBLE
            }
        }
    }
}