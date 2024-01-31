package com.example.guard_patrol.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.guard_patrol.Class.SOP
import com.example.guard_patrol.databinding.CustomTaskDetailBinding

class AdapterTaskDetail : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dataList = ArrayList<SOP>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CustomTaskDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CustomDetailViewHolder).bindDetailView(dataList[position],position)
    }

    inner class CustomDetailViewHolder(private val binding : CustomTaskDetailBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindDetailView(dataClass: SOP,position: Int){
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