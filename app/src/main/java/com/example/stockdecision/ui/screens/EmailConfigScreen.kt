package com.example.stockdecision.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.stockdecision.R
import com.example.stockdecision.data.model.EmailConfigPlain

/**
 * Email configuration screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailConfigScreen(
    config: EmailConfigPlain?,
    onSave: (EmailConfigPlain) -> Unit,
    onTest: (EmailConfigPlain) -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean = false,
    testResult: TestResult? = null
) {
    var smtpServer by remember { mutableStateOf(config?.smtpServer ?: "") }
    var port by remember { mutableStateOf(config?.port ?: "587") }
    var username by remember { mutableStateOf(config?.username ?: "") }
    var password by remember { mutableStateOf(config?.password ?: "") }
    var recipientEmail by remember { mutableStateOf(config?.recipientEmail ?: "") }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.email_config_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // SMTP Server
            OutlinedTextField(
                value = smtpServer,
                onValueChange = { smtpServer = it },
                label = { Text(stringResource(R.string.smtp_server)) },
                placeholder = { Text(stringResource(R.string.smtp_server_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Port
            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = { Text(stringResource(R.string.smtp_port)) },
                placeholder = { Text(stringResource(R.string.smtp_port_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.email_username)) },
                placeholder = { Text(stringResource(R.string.email_username_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.email_password)) },
                placeholder = { Text(stringResource(R.string.email_password_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Recipient Email
            OutlinedTextField(
                value = recipientEmail,
                onValueChange = { recipientEmail = it },
                label = { Text(stringResource(R.string.recipient_email)) },
                placeholder = { Text(stringResource(R.string.recipient_email_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Test Result
            testResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.success) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = result.message,
                        modifier = Modifier.padding(16.dp),
                        color = if (result.success) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Test Email Button
            OutlinedButton(
                onClick = {
                    onTest(
                        EmailConfigPlain(
                            smtpServer = smtpServer,
                            port = port,
                            username = username,
                            password = password,
                            recipientEmail = recipientEmail
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && smtpServer.isNotBlank() && 
                         username.isNotBlank() && password.isNotBlank() && 
                         recipientEmail.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.test_email))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Save Button
            Button(
                onClick = {
                    onSave(
                        EmailConfigPlain(
                            smtpServer = smtpServer,
                            port = port,
                            username = username,
                            password = password,
                            recipientEmail = recipientEmail
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = smtpServer.isNotBlank() && 
                         username.isNotBlank() && password.isNotBlank() && 
                         recipientEmail.isNotBlank()
            ) {
                Text(stringResource(R.string.save_config))
            }
        }
    }
}

data class TestResult(
    val success: Boolean,
    val message: String
)
