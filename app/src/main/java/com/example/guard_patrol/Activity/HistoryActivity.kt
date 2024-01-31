package com.example.guard_patrol.Activity

import BasedActivity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guard_patrol.Adapter.AdapterDateSelectHistory
import com.example.guard_patrol.Adapter.AdapterTaskHistory
import com.example.guard_patrol.Class.HistoryDateClass
import com.example.guard_patrol.Class.HistoryTaskDataClass
import com.example.guard_patrol.databinding.ActivityHistoryBinding

class HistoryActivity : BasedActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private var historySelect = ArrayList<HistoryDateClass>()
    private lateinit var historyAdapter: AdapterDateSelectHistory
    private lateinit var historyTaskAdapter: AdapterTaskHistory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        historyAdapter = AdapterDateSelectHistory()
        historyTaskAdapter = AdapterTaskHistory()
        addHistoryToData()

    }

    private fun addHistoryToData() {
        val taskHistoryDetail1 = ArrayList<HistoryTaskDataClass>()
        taskHistoryDetail1.add(HistoryTaskDataClass("หน้าหมู่บ้าน","13:05:02","14:00:01"))
        taskHistoryDetail1.add(HistoryTaskDataClass("กลางหมู่บ้าน","14:05:02","15:00:01"))
        taskHistoryDetail1.add(HistoryTaskDataClass("หลังหมู่บ้าน","",""))
        historySelect.add(HistoryDateClass("2567-01-04",taskHistoryDetail1))

        val taskHistoryDetail2 = ArrayList<HistoryTaskDataClass>()
        taskHistoryDetail2.add(HistoryTaskDataClass("อาคารชั้น 1","",""))
        taskHistoryDetail2.add(HistoryTaskDataClass("อาคารชั้น 2","",""))
        taskHistoryDetail2.add(HistoryTaskDataClass("อาคารชั้น 3","",""))
        taskHistoryDetail2.add(HistoryTaskDataClass("อาคารชั้น 4","12:00:12","15:00:21"))
        taskHistoryDetail2.add(HistoryTaskDataClass("อาคารชั้น 7","",""))
        taskHistoryDetail2.add(HistoryTaskDataClass("อาคารชั้น 9","",""))
        taskHistoryDetail2.add(HistoryTaskDataClass("อาคารชั้น 12","10:00:12","11:00:12 "))
        historySelect.add(HistoryDateClass("2567-01-01",taskHistoryDetail2))
        historyAdapter.dataList = historySelect

        //Add Date history
        this.binding.historyDateRecyclerView.apply {
            layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
            adapter = historyAdapter
        }
        historyAdapter.changeDate = { date , poistion ->
            binding.txtDateSelect.text = date
            historyTaskAdapter.dataList = historySelect[poistion!!].listData
        }

        //Add Task history
        this.binding.historyTaskRecyclerView.apply {
            layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
            adapter = historyTaskAdapter
        }

    }
}