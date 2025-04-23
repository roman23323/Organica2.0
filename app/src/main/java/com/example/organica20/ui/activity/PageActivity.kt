package com.example.organica20.ui.activity

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.organica20.R
import com.example.organica20.customviews.PlaceLayout
import com.example.organica20.data.model.Element
import com.example.organica20.data.model.FormulaData
import com.example.organica20.data.model.PageElement
import com.example.organica20.data.repository.PageRepository
import com.example.organica20.ui.viewmodel.PageViewModel
import com.example.organica20.ui.viewmodel.PageViewModelFactory
import com.example.organica20.utils.setupEdgeToEdge

class PageActivity : AppCompatActivity() {
    private lateinit var viewModel: PageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page)
        setupEdgeToEdge(findViewById(android.R.id.content))

        val repository = PageRepository(this)
        viewModel = ViewModelProvider(this, PageViewModelFactory(repository))[PageViewModel::class.java]

        val pageID = intent.getStringExtra("pageID")

        viewModel.pageElements.observe(this) { elements ->
            displayPage(elements)
        }
        viewModel.loadPage(pageID!!)
    }

    private fun displayPage(elements: List<PageElement>) {
        val container = findViewById<ViewGroup>(R.id.content_container)
        container.removeAllViews()

        for (element in elements) {
            when (element.type) {
                Element.TEXT -> addTextView(container, element.content!!)
                Element.HEADER -> findViewById<TextView>(R.id.header).text = element.content
                Element.FORMULA -> addFormula(container, element.formulaData!!)
            }
        }
    }

    private fun addTextView(container: ViewGroup, text: String) {
        val textView = TextView(this, null, 0, R.style.PageText)
        textView.text = text
        container.addView(textView)
    }

    private fun addFormula(container: ViewGroup, formulaData: FormulaData) {
        val nodes = formulaData.nodes.split(' ')
        val mainChildPosition = formulaData.mainChildPosition
        val placement = formulaData.placement
        val additionalLines = formulaData.additionalLines

        val nodeViews = nodes.map {node ->
            TextView(this, null, 0, R.style.PageText_FormulaNode).apply {
                text = node
            }
        }

        val formulaView = PlaceLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            nodeViews.forEach { nodeView -> addView(nodeView) }
            this.placementString = placement
            this.mainChildPosition.apply {
                position = mainChildPosition.position
                xOffset = mainChildPosition.xOffset
                yOffset = mainChildPosition.yOffset
            }
            this.additionalLinesString = additionalLines ?: ""
            requestLayout()
        }
        container.addView(formulaView)
    }
}