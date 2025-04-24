package com.example.organica20.ui.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.organica20.R
import com.example.organica20.data.model.Lesson

class LessonViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val titleTextView: TextView = itemView.findViewById(R.id.lessonTitle)
    private val iconImageView: ImageView = itemView.findViewById(R.id.lessonIcon)
    private val descriptionTextView: TextView = itemView.findViewById(R.id.lessonDescription)

    fun bind(lesson: Lesson) {
        titleTextView.text = lesson.title
        descriptionTextView.text = lesson.description
        iconImageView.setImageResource(lesson.iconResId)
    }
}