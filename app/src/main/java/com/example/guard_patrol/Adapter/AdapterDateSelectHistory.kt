package com.example.guard_patrol.Adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.guard_patrol.Class.HistoryDateClass
import com.example.guard_patrol.databinding.CustomDateSelectBinding
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.chrono.ThaiBuddhistDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class AdapterDateSelectHistory: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var formattedNameDay: String
    lateinit var formattedDay: String
    lateinit var formattedFullDate: String
    var positionSelect: Int = 0
    var dataList = ArrayList<HistoryDateClass>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var changeDate: ((date: String?,position: Int?)-> Unit)? = null

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CustomDateSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomDateSelectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CustomDateSelectViewHolder).bindDateSelect(dataList[position])
    }

    inner class CustomDateSelectViewHolder(private val binding: CustomDateSelectBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindDateSelect( dateHistoryClass: HistoryDateClass){
            convertDateSelect(dateHistoryClass.dateSelect)
            binding.txtNameOfDay.text = formattedNameDay
            binding.txtDay.text = formattedDay
            val position = adapterPosition

            if (positionSelect == position){
                binding.layoutBtnSelect.apply {
                    backgroundColor = Color.TRANSPARENT
                    strokeLineColor = Color.WHITE
                    strokeLineWidth = 15F
                }
                binding.txtNameOfDay.setTextColor(Color.WHITE)
                binding.txtDay.setTextColor(Color.WHITE)
                changeDate?.invoke(formattedFullDate,positionSelect)
            }else{
                binding.layoutBtnSelect.apply {
                    backgroundColor = Color.WHITE
                    strokeLineColor = Color.parseColor("#FF7233")
                    strokeLineWidth = 10F
                }
                binding.txtNameOfDay.setTextColor(Color.parseColor("#FF7233"))
                binding.txtDay.setTextColor(Color.parseColor("#FF7233"))
            }


            binding.layoutBtnSelect.setOnClickListener {
                positionSelect = position
                Log.d("ViewHolder", "Item at position $position clicked")
                notifyDataSetChanged()
            }


        }
    }

    fun convertDateSelect( dateString: String){
        val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date: Date = sdfInput.parse(dateString)
        val thaiBuddhistDate: ThaiBuddhistDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().let {
            ThaiBuddhistDate.of(it.year, it.monthValue, it.dayOfMonth)
        }
        val nameDayFormat = DateTimeFormatter.ofPattern("E", Locale("th", "TH"))
        val dayFormat = DateTimeFormatter.ofPattern("dd", Locale("th", "TH"))
        val fullDateFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale("th", "TH"))
        formattedNameDay = nameDayFormat.format(thaiBuddhistDate)
        formattedDay = dayFormat.format(thaiBuddhistDate)
        formattedFullDate = fullDateFormat.format(thaiBuddhistDate)
    }
}