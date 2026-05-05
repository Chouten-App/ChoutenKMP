package com.inumaki.features.repo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.inumaki.core.ui.components.AppButton
import com.inumaki.core.ui.components.alert.AlertScope
import com.inumaki.core.ui.components.alert.alert
import com.inumaki.core.ui.model.toolbar
import dev.chouten.core.repository.RepositoryManager
import kotlinx.coroutines.launch

@Composable
fun RepoView(
    repositoryManager: RepositoryManager
) {
    val scope = rememberCoroutineScope()
    var showAlert by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .toolbar {
                AppButton(
                    "drawable/plus-solid-full.svg",
                    0f,
                    modifier = Modifier
                        .alert("Add Repository", isPresented = showAlert) {
                            message { "Enter the json URL of the repository" }

                            textField(
                                value = input,
                                onChange = { input = it },
                                placeholder = "https://sample.com/repo.json"
                            )

                            button("Cancel", role = AlertScope.Role.Cancel) {
                                showAlert = false
                            }
                            button("Add") {
                                println("Ok")
                                scope.launch {
                                    repositoryManager.addRepository(input)
                                }
                                showAlert = false
                            }
                        }
                        .clickable {
                            showAlert = true
                        },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
    }
}