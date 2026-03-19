package com.example.pythonide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pythonide.model.FileNode
import com.example.pythonide.model.TerminalLine
import com.example.pythonide.ui.theme.Accent
import com.example.pythonide.ui.theme.Background
import com.example.pythonide.ui.theme.Error
import com.example.pythonide.ui.theme.PythonIdeTheme
import com.example.pythonide.ui.theme.Success
import com.example.pythonide.ui.theme.Surface as SurfaceColor
import com.example.pythonide.ui.theme.SurfaceAlt
import com.example.pythonide.ui.theme.TextPrimary
import com.example.pythonide.ui.theme.TextSecondary
import com.example.pythonide.ui.theme.Warning

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PythonIdeTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Background) {
                    PythonIdeApp()
                }
            }
        }
    }
}

@Composable
fun PythonIdeApp() {
    val files = remember {
        mutableStateListOf(
            FileNode(
                name = "main.py",
                path = "/project/main.py",
                content = """
                    import requests
                    
                    def greet(name: str) -> None:
                        print(f\"Привет, {name}!\")
                    
                    if __name__ == \"__main__\":
                        greet(\"Android\")
                """.trimIndent()
            ),
            FileNode(
                name = "requirements.txt",
                path = "/project/requirements.txt",
                content = "requests\nrich\nblack\n"
            ),
            FileNode(
                name = "utils.py",
                path = "/project/utils.py",
                content = "class Formatter:\n    def title(self, text):\n        return text.title()\n"
            )
        )
    }
    var selectedIndex by remember { mutableStateOf(0) }
    var editorValue by remember { mutableStateOf(TextFieldValue(files.first().content)) }
    val terminalLines = remember {
        mutableStateListOf(
            TerminalLine("Python 3.12 on Android environment ready"),
            TerminalLine("Tip: используйте pip install <package> для добавления модулей")
        )
    }
    var terminalInput by remember { mutableStateOf("") }
    var pipInput by remember { mutableStateOf("numpy") }
    val installedPackages = remember { mutableStateListOf("requests", "rich", "black") }

    fun syncEditorToFile() {
        files[selectedIndex] = files[selectedIndex].copy(content = editorValue.text)
    }

    fun openFile(index: Int) {
        syncEditorToFile()
        selectedIndex = index
        editorValue = TextFieldValue(files[index].content)
    }

    fun runCommand(command: String) {
        if (command.isBlank()) return
        terminalLines.add(TerminalLine(text = "$ $command", isCommand = true))
        when {
            command.startsWith("python") || command == "run" -> {
                syncEditorToFile()
                terminalLines.add(TerminalLine("Запуск ${files[selectedIndex].name}...", isError = false))
                terminalLines.add(TerminalLine("Привет, Android!", isError = false))
            }
            command.startsWith("pip install ") -> {
                val packageName = command.removePrefix("pip install ").trim()
                if (packageName.isNotBlank()) {
                    if (!installedPackages.contains(packageName)) {
                        installedPackages.add(packageName)
                    }
                    terminalLines.add(TerminalLine("Collecting $packageName"))
                    terminalLines.add(TerminalLine("Successfully installed $packageName", isError = false))
                } else {
                    terminalLines.add(TerminalLine("Package name is required", isError = true))
                }
            }
            command == "ls" -> {
                terminalLines.add(TerminalLine(files.joinToString("  ") { it.name }))
            }
            else -> {
                terminalLines.add(TerminalLine("Unknown command: $command", isError = true))
            }
        }
        terminalInput = ""
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FileExplorerPane(
            files = files,
            selectedIndex = selectedIndex,
            installedPackages = installedPackages,
            onFileSelected = ::openFile,
            onAddFile = {
                syncEditorToFile()
                val newFile = FileNode(
                    name = "script${files.size + 1}.py",
                    path = "/project/script${files.size + 1}.py",
                    content = "print('new file')"
                )
                files.add(newFile)
                openFile(files.lastIndex)
            },
            modifier = Modifier.weight(0.32f)
        )

        Column(
            modifier = Modifier
                .weight(0.68f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeaderBar(selectedFile = files[selectedIndex])
            EditorPane(
                editorValue = editorValue,
                onValueChange = { editorValue = it },
                onRun = { runCommand("python ${files[selectedIndex].name}") },
                modifier = Modifier.weight(0.58f)
            )
            PipPane(
                packageName = pipInput,
                onPackageNameChange = { pipInput = it },
                onInstall = { runCommand("pip install $pipInput") }
            )
            TerminalPane(
                lines = terminalLines,
                input = terminalInput,
                onInputChange = { terminalInput = it },
                onRunCommand = { runCommand(terminalInput) },
                modifier = Modifier.weight(0.42f)
            )
        }
    }
}

@Composable
private fun HeaderBar(selectedFile: FileNode) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Android,
                        contentDescription = null,
                        tint = Accent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Python IDE for Android",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Файлы, терминал, pip и подсветка синтаксиса — всё на одном экране.",
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = selectedFile.name, fontWeight = FontWeight.SemiBold)
                Text(text = selectedFile.path, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FileExplorerPane(
    files: List<FileNode>,
    selectedIndex: Int,
    installedPackages: List<String>,
    onFileSelected: (Int) -> Unit,
    onAddFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Project", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onAddFile) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add file", tint = Accent)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceAlt),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Folder, contentDescription = null, tint = Warning)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "app/python", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    files.forEachIndexed { index, file ->
                        val isSelected = index == selectedIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFileSelected(index) }
                                .background(
                                    if (isSelected) Accent.copy(alpha = 0.15f) else Color.Transparent,
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Description,
                                contentDescription = null,
                                tint = if (isSelected) Accent else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = file.name, color = if (isSelected) TextPrimary else TextSecondary)
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceAlt),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Installed packages", fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        installedPackages.forEach { pkg ->
                            FilterChip(
                                selected = true,
                                onClick = { },
                                label = { Text(pkg) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Accent.copy(alpha = 0.14f),
                                    selectedLabelColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceAlt),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "What’s included", fontWeight = FontWeight.SemiBold)
                    listOf(
                        "Файловый менеджер проекта",
                        "Подсветка Python-кода",
                        "Встроенный терминал",
                        "pip install для модулей"
                    ).forEach { feature ->
                        Text(text = "• $feature", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorPane(
    editorValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Editor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onRun) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Success)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Run", color = TextPrimary)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceAlt, RoundedCornerShape(24.dp))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = highlightPython(editorValue.text),
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 21.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    )
                    Divider(color = TextSecondary.copy(alpha = 0.18f), modifier = Modifier.padding(vertical = 8.dp))
                    BasicTextField(
                        value = editorValue,
                        onValueChange = onValueChange,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        decorationBox = { innerTextField ->
                            Box {
                                if (editorValue.text.isEmpty()) {
                                    Text("# Start typing your Python script", color = TextSecondary)
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PipPane(
    packageName: String,
    onPackageNameChange: (String) -> Unit,
    onInstall: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = packageName,
                onValueChange = onPackageNameChange,
                modifier = Modifier.weight(1f),
                label = { Text("pip package") },
                singleLine = true
            )
            TextButton(onClick = onInstall) {
                Icon(Icons.Rounded.Send, contentDescription = null, tint = Accent)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Install")
            }
        }
    }
}

@Composable
private fun TerminalPane(
    lines: List<TerminalLine>,
    input: String,
    onInputChange: (String) -> Unit,
    onRunCommand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Terminal, contentDescription = null, tint = Accent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Terminal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceAlt),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    lines.forEach { line ->
                        Text(
                            text = line.text,
                            color = when {
                                line.isError -> Error
                                line.isCommand -> Warning
                                else -> TextPrimary
                            },
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    label = { Text("Command") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onRunCommand) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Success)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Run")
                }
            }
        }
    }
}

private fun highlightPython(code: String): AnnotatedString {
    val keywords = setOf(
        "import", "from", "def", "class", "if", "else", "elif", "return",
        "for", "while", "in", "try", "except", "with", "as", "print", "None"
    )
    return buildAnnotatedString {
        val tokens = Regex("\\s+|[A-Za-z_][A-Za-z0-9_]*|\"[^\"]*\"|'[^']*'|#.*|.")
            .findAll(code)
            .map { it.value }
            .toList()
        tokens.forEach { token ->
            val color = when {
                token.trim().startsWith("#") -> TextSecondary
                token in keywords -> Accent
                token.startsWith("\"") || token.startsWith("'") -> Success
                token.all { it.isDigit() } -> Warning
                else -> TextPrimary
            }
            pushStyle(SpanStyle(color = color))
            append(token)
            pop()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1020, widthDp = 1280, heightDp = 800)
@Composable
private fun PreviewPythonIdeApp() {
    PythonIdeTheme {
        PythonIdeApp()
    }
}
