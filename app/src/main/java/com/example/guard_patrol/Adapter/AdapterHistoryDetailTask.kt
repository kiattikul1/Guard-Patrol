package com.example.guard_patrol.Adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guard_patrol.Class.Tasks
import com.example.guard_patrol.R
import com.example.guard_patrol.databinding.CustomHistoryDetailTaskBinding
import com.squareup.picasso.Picasso

@SuppressLint("NotifyDataSetChanged")
class AdapterHistoryDetailTask(private var showImage: ((imageUrl: String)-> Unit)? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dataList = ArrayList<Tasks>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CustomHistoryDetailTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomHistoryDetailTaskViewHolder(binding)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? AdapterHistoryDetailTask.CustomHistoryDetailTaskViewHolder)?.bindTaskView(dataList[position])
    }

    inner class CustomHistoryDetailTaskViewHolder(private val binding : CustomHistoryDetailTaskBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindTaskView(task: Tasks) {
            binding.txtTask.text = task.titleTask
            binding.recyclerViewDetailCheckList.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)

            val adapter = AdapterHistoryDetailTaskSop()
            adapter.dataList = task.sops!!
            binding.recyclerViewDetailCheckList.adapter = adapter

            if (task.isNormal == true){
                binding.radioBtnNormal.apply {
                    binding.layoutShowImageAndNote.visibility = View.GONE
                    binding.layoutWarning.visibility = View.GONE
                    setTextColor(Color.BLACK)
                    isChecked = true
                }
            }else{
                binding.radioBtnUnNormal.apply{
                    binding.layoutShowImageAndNote.visibility = View.VISIBLE
                    binding.layoutWarning.visibility = View.GONE
                    setTextColor(Color.BLACK)
                    isChecked = true
                }

//                Log.e("TestDetailTaskHistory", "Check bitmap position $position ${task.evidenceImages}")
                if (!task.evidenceImages.isNullOrEmpty()) {
                    val eImage1 = task.evidenceImages.getOrNull(0)
                    if (eImage1 != null) {
                        Picasso.get()
                            .load(eImage1)
                            .error(R.mipmap.ic_launcher)
                            .into(binding.imageFirst)
                        binding.imagePicker1.setOnClickListener {
                            showImage?.invoke(eImage1)
                        }
                    }

                    val eImage2 = task.evidenceImages.getOrNull(1)
                    if (eImage2 != null) {
                        Picasso.get()
                            .load(eImage2)
                            .error(R.mipmap.ic_launcher)
                            .into(binding.imageSecond)
                        binding.imagePicker2.setOnClickListener {
                            showImage?.invoke(eImage2)
                        }
                    }else{
                        binding.imagePicker2.visibility = View.GONE
                    }

                    val eImage3 = task.evidenceImages.getOrNull(2)
                    if (eImage3 != null) {
                        Picasso.get()
                            .load(eImage3)
                            .error(R.mipmap.ic_launcher)
                            .into(binding.imageThird)
                        binding.imagePicker3.setOnClickListener {
                            showImage?.invoke(eImage3)
                        }
                    }else{
                        binding.imagePicker3.visibility = View.GONE
                    }

                }else{
                    binding.txtPhoto.visibility = View.GONE
                    binding.layoutImagePicker.visibility = View.GONE
                }

                binding.textAreaInformation.apply {
                    setText(task.remark)
                }
            }
        }
    }



}