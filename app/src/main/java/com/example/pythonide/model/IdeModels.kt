package com.example.pythonide.model

data class FileNode(
    val name: String,
    val path: String,
    val content: String,
    val isDirectory: Boolean = false
)

data class TerminalLine(
    val text: String,
    val isCommand: Boolean = false,
    val isError: Boolean = false
)
