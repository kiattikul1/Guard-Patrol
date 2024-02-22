package com.example.guard_patrol.Adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guard_patrol.Class.CellType
import com.example.guard_patrol.Class.TaskClass
import com.example.guard_patrol.databinding.BtnSentReportBinding
import com.example.guard_patrol.databinding.CustomTaskBinding

@SuppressLint("NotifyDataSetChanged")
class AdapterListTask(private var actionCamera: ((id: String,position: Int,image: ImageView)-> Unit)? = null,
                      private var actionSendNormalStatus: ((position:Int, isNormal:Boolean) -> Unit)? = null,
                      private var actionSendRemarks: ((position:Int,remark:String) -> Unit)? = null,
                      private var actionSubmitForm: (() -> Unit)? = null): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dataList = ArrayList<TaskClass>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position].cellType == CellType.TASK) {
            9000
        } else {
            9001
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            9000 -> {
                val binding = CustomTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CustomTaskViewHolder(binding)
            }
            9001 -> {
                val binding = BtnSentReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BtnSentReportViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? CustomTaskViewHolder)?.bindTaskView(dataList[position], position)
        (holder as? BtnSentReportViewHolder)?.bindBtnSentReportView()
    }

    inner class CustomTaskViewHolder(private val binding : CustomTaskBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindTaskView(taskClass: TaskClass, position: Int) {
            binding.txtTask.text = taskClass.data?.assignTask?.tasks?.firstOrNull()?.titleTask.orEmpty()
            binding.recyclerViewDetailCheckList.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)

            val adapter = AdapterTaskSop()
            adapter.dataList = taskClass.data?.assignTask?.tasks?.firstOrNull()?.sops!!
            binding.recyclerViewDetailCheckList.adapter = adapter

            //Set radio btn
            binding.radioBtnNormal.setOnClickListener {
                actionSendNormalStatus?.invoke(position, true)
                binding.layoutShowImageAndNote.visibility = View.GONE
                binding.layoutWarning.visibility = View.GONE
            }

            binding.radioBtnUnNormal.setOnClickListener{
                actionSendNormalStatus?.invoke(position, false)
                binding.layoutShowImageAndNote.visibility = View.VISIBLE
                binding.layoutWarning.visibility = View.GONE
            }

            //Capture Image
            binding.imagePicker1.setOnClickListener {
                actionCamera?.invoke(binding.imageFirst.id.toString(),position,binding.imageFirst,)
                binding.imageFirst.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                    binding.imagePicker2.post {
                        binding.imagePicker2.visibility = View.VISIBLE
                    }
                }
            }

            binding.imagePicker2.setOnClickListener {
                actionCamera?.invoke(binding.imageSecond.id.toString(),position,binding.imageSecond)
                binding.imageSecond.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                    binding.imagePicker3.post {
                        binding.imagePicker3.visibility = View.VISIBLE
                    }
                }
            }

            binding.imagePicker3.setOnClickListener {
                actionCamera?.invoke(binding.imageThird.id.toString(),position,binding.imageThird)
            }

            //TODO : fix remark
            val textWatcher = RemarkTextWatcher(position)
            binding.textAreaInformation.addTextChangedListener(textWatcher)
            binding.txtNote.setTextColor(Color.RED)

            binding.textAreaInformation.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    if (binding.textAreaInformation.text == null || binding.textAreaInformation.text.toString() == ""){
                        binding.txtNote.setTextColor(Color.RED)
                        binding.roundedEditText.apply {
                            strokeLineColor = Color.RED
                            strokeLineWidth = 10F
                        }
                    }else{
                        binding.txtNote.setTextColor(Color.BLACK)
                        binding.roundedEditText.apply {
                            strokeLineColor = Color.BLACK
                            strokeLineWidth = 5F
                        }
                    }
                }
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            })
        }
    }

    inner class BtnSentReportViewHolder(private val binding : BtnSentReportBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindBtnSentReportView() {
            binding.txtBtn.text = "ส่งรายงาน"
            binding.btnSentReport.setOnClickListener {
                actionSubmitForm?.invoke()
            }
        }
    }

    inner class RemarkTextWatcher(private val position: Int) : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(editable: Editable?) {
            val remark = editable.toString()
            actionSendRemarks?.invoke(position,remark)
        }
    }

}