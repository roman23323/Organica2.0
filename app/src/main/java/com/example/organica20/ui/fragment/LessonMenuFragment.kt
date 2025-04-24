package com.example.organica20.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.organica20.R
import com.example.organica20.data.model.Lesson
import com.example.organica20.databinding.FragmentLessonsBinding
import com.example.organica20.ui.activity.PageActivity
import com.example.organica20.ui.adapter.LessonAdapter

class LessonMenuFragment: Fragment() {
    private lateinit var binding: FragmentLessonsBinding
    private lateinit var adapter: LessonAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLessonsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lessons = listOf(
            Lesson("lesson_1_1.json", "Алканы", "Простейшие соединения с одинарными связями", R.drawable.ic_alkane_lesson)
        )

        adapter = LessonAdapter(lessons) { id ->
            val intent = Intent(requireContext(), PageActivity::class.java)
            intent.putExtra("pageID", id)
            startActivity(intent)
        }

        binding.lessonsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LessonMenuFragment.adapter
        }
    }
}