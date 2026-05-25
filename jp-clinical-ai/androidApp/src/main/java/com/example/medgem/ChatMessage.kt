package com.example.medgem

import com.example.medgem.ui.components.DisplayableMessage

data class ChatMessage(
    override val id: Long = System.nanoTime(),
    override val content: String,
    override val isUser: Boolean,
    override val imagePaths: List<String> = emptyList(),
    override val initiallyExpanded: Boolean = false,
    override val thought: String? = null
) : DisplayableMessage
