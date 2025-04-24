package com.example.organica20.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.organica20.R
import com.example.organica20.data.model.Lesson

class LessonAdapter(
    private val lessons: List<Lesson>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<LessonViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LessonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: LessonViewHolder,
        position: Int
    ) {
        val lesson = lessons[position]
        holder.bind(lesson)

        holder.itemView.setOnClickListener {
            onItemClick(lesson.id)
        }
    }

    override fun getItemCount(): Int = lessons.size

}