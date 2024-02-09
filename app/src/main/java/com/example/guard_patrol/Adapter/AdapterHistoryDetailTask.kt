package com.example.guard_patrol.Adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guard_patrol.Class.Tasks
import com.example.guard_patrol.databinding.CustomHistoryDetailTaskBinding

@SuppressLint("NotifyDataSetChanged")
class AdapterHistoryDetailTask : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dataList = ArrayList<Tasks>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var addImage: ((image: ImageView?,imageBitmap: Bitmap)-> Unit)? = null
    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CustomHistoryDetailTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomHistoryDetailTaskViewHolder(binding)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? AdapterHistoryDetailTask.CustomHistoryDetailTaskViewHolder)?.bindTaskView(dataList[position],position)
    }

    inner class CustomHistoryDetailTaskViewHolder(private val binding : CustomHistoryDetailTaskBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindTaskView(task: Tasks,position: Int) {
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
                val eImage1 = task.evidenceImages.getOrNull(0)
                val eImage2 = task.evidenceImages.getOrNull(1)
                val eImage3 = task.evidenceImages.getOrNull(2)
                if (eImage1 == null){
                    binding.txtPhoto.visibility = View.GONE
                    binding.layoutImagePicker.visibility = View.GONE
                }else{
                    Log.e("TestDetailTaskHistory", "Check eImage1 $eImage1")
                    val bitmapImageFirst = BitmapFactory.decodeFile(eImage1)
                    Log.e("TestDetailTaskHistory", "Check bitmapImageFirst $bitmapImageFirst")
//                    addImage?.invoke(binding.imageFirst,bitmapImageFirst)
                    binding.imageFirst.setImageBitmap(bitmapImageFirst)
                }

                if (eImage2 == null){
                    binding.imagePicker2.visibility = View.GONE
                }else{
                    val bitmapImageSecond = BitmapFactory.decodeFile(eImage2)
                    binding.imageSecond.setImageBitmap(bitmapImageSecond)
                }

                if (eImage3 == null){
                    binding.imagePicker3.visibility = View.GONE
                }else{
                    val bitmapImageThird = BitmapFactory.decodeFile(eImage3)
                    binding.imageThird.setImageBitmap(bitmapImageThird)
                }

                binding.textAreaInformation.apply {
                    setText(task.remark)
                }
            }
        }
    }

}