package com.example.guard_patrol.Adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.guard_patrol.Class.WorkspaceItem
import com.example.guard_patrol.databinding.CustomWorkspaceSelectBinding

@SuppressLint("NotifyDataSetChanged")
class AdapterWorkspaceAll: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    var dataList = ArrayList<WorkspaceItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var action: ((id: String? ,name: String?)-> Unit)? = null

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CustomWorkspaceSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomWorkspaceSelectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CustomWorkspaceSelectViewHolder).bindWorkspaceView(dataList[position])
    }

    inner class CustomWorkspaceSelectViewHolder(private val binding : CustomWorkspaceSelectBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindWorkspaceView(dataClass: WorkspaceItem){
            val idWorkspace = dataClass.id
            val workspaceName = dataClass.workspace
            val status = dataClass.status
            binding.txtWorkspace.text = workspaceName
            if (status == false){
                binding.icCheck.visibility = View.GONE
                binding.workspaceLayout.setBackgroundColor(Color.TRANSPARENT)
                binding.txtWorkspace.apply {
                    setTextColor(Color.BLACK)
                    layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                        leftMargin = 0 // Set the left margin to 0 pixels
                    }
                }
            }

            binding.workspaceLayout.setOnClickListener {
                action?.invoke(idWorkspace,workspaceName)
            }

        }
    }
}