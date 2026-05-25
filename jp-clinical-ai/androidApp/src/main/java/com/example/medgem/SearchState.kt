package com.example.medgem

import com.example.medgem.data.KnowledgeEntity

data class SearchState(
    val query: String = "",
    val results: List<Pair<KnowledgeEntity, Double>> = emptyList(),
    val status: String = "Enter a query to search the medical knowledge base."
)
