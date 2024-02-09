package com.example.guard_patrol.Adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.guard_patrol.Class.HistoryClass
import com.example.guard_patrol.databinding.CustomDateSelectBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
@SuppressLint("NotifyDataSetChanged")
class AdapterDateSelectHistory: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var formattedNameDay: String
    lateinit var formattedDay: String
    lateinit var formattedFullDate: String
    var dataList = ArrayList<HistoryClass>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var positionSelect = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var changeDate: ((date: String?)-> Unit)? = null

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CustomDateSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomDateSelectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CustomDateSelectViewHolder).bindDateSelect(dataList[position],position)
    }

    inner class CustomDateSelectViewHolder(private val binding: CustomDateSelectBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindDateSelect(dateHistoryClass: HistoryClass ,position: Int){
            dateHistoryClass.dateSelect?.let { convertDateSelect(it) }
            binding.txtNameOfDay.text = formattedNameDay
            binding.txtDay.text = formattedDay

            if (positionSelect == position){
                binding.layoutBtnSelect.apply {
                    backgroundColor = Color.TRANSPARENT
                    strokeLineColor = Color.WHITE
                    strokeLineWidth = 15F
                }
                binding.txtNameOfDay.setTextColor(Color.WHITE)
                binding.txtDay.setTextColor(Color.WHITE)
                changeDate?.invoke(formattedFullDate)

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
        val date: Date? = sdfInput.parse(dateString)

        val localDate = date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
        val thaiBuddhistDate = localDate?.let { LocalDate.from(it) }

        val nameDayFormat = DateTimeFormatter.ofPattern("E", Locale("th", "TH"))
        val dayFormat = DateTimeFormatter.ofPattern("dd", Locale("th", "TH"))
        val fullDateFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale("th", "TH"))

        formattedNameDay = thaiBuddhistDate?.format(nameDayFormat) ?: ""
        formattedDay = thaiBuddhistDate?.format(dayFormat) ?: ""
        formattedFullDate = thaiBuddhistDate?.format(fullDateFormat) ?: ""
    }
}