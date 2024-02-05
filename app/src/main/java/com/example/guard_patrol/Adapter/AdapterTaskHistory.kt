package com.example.guard_patrol.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.guard_patrol.Class.Patrol
import com.example.guard_patrol.R
import com.example.guard_patrol.databinding.CustomHistoryTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdapterTaskHistory: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dataList = ArrayList<Patrol>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CustomHistoryTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomHistoryTaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CustomHistoryTaskViewHolder).bindCustomHistoryTaskView(dataList[position])
    }

    inner class CustomHistoryTaskViewHolder(private val binding: CustomHistoryTaskBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindCustomHistoryTaskView(dateSelectHistory: Patrol){
            val txtStartTime = dateSelectHistory.processDate
            val txtEndTime = dateSelectHistory.completedAt
            val layoutParams = binding.iconCheck.layoutParams as ConstraintLayout.LayoutParams
            binding.txtPointName.text = dateSelectHistory.pointName
            val timeStartFormat = txtStartTime?.let { formatTime(it) }
            val timeEndFormat = txtEndTime?.let { formatTime(it) }

            if(txtStartTime.isNullOrEmpty()){
                binding.iconCheck.layoutParams = layoutParams
                binding.iconCheck.setImageResource(R.drawable.ic_task_not_check)
                binding.txtStartPatrol.visibility = View.GONE
                binding.txtEndPatrol.visibility = View.GONE
                binding.arrowRight.visibility = View.GONE
                val heightInDp = 50
                val density = itemView.context.resources.displayMetrics.density
                val heightInPixels = (heightInDp * density).toInt()
                binding.line.layoutParams.height = heightInPixels
                binding.line.backgroundColor = Color.parseColor("#CECDCD")
                binding.txtPointName.setTextColor(Color.parseColor("#CECDCD"))
                binding.layoutCell.backgroundColor = Color.TRANSPARENT
            }else{
                layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                binding.iconCheck.layoutParams = layoutParams
                binding.iconCheck.setImageResource(R.drawable.ic_task_check)
                binding.txtStartPatrol.visibility = View.VISIBLE
                binding.txtEndPatrol.visibility = View.VISIBLE
                binding.arrowRight.visibility = View.VISIBLE
                val heightInDp = 100
                val density = itemView.context.resources.displayMetrics.density
                val heightInPixels = (heightInDp * density).toInt()
                binding.line.layoutParams.height = heightInPixels
                binding.line.backgroundColor = Color.parseColor("#1FDE00")
                binding.txtPointName.setTextColor(Color.WHITE)
                binding.layoutCell.backgroundColor = Color.parseColor("#1FDE00")
                binding.txtStartPatrol.text = "เวลาเริ่มตรวจ: $timeStartFormat"
                binding.txtEndPatrol.text = "เวลาส่งรายงาน: $timeEndFormat"
            }
        }
    }

    fun formatTime(inputTime: String): String {
        try {
            val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH นาฬิกา mm นาที ss วินาที", Locale.getDefault())
            val date: Date = inputFormat.parse(inputTime) ?: Date()
            return outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return inputTime // Return the original input if formatting fails
    }
}