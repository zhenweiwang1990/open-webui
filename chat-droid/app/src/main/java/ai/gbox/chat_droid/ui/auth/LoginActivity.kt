package ai.gbox.chat_droid.ui.auth

import ai.gbox.chat_droid.R
import ai.gbox.chat_droid.repository.AuthRepository
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        findViewById<com.google.android.material.button.MaterialButton>(R.id.signInButton).setOnClickListener {
            val email = emailEditText.text?.toString()?.trim() ?: ""
            val password = passwordEditText.text?.toString() ?: ""
            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signIn(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    authRepository.signIn(email, password)
                }
                saveToken(response.token)
                navigateToMain()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveToken(token: String) {
        val prefs = getSharedPreferences("open_webui_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("token", token).apply()
    }

    private fun navigateToMain() {
        val intent = Intent(this, ai.gbox.chat_droid.MainActivity::class.java)
        startActivity(intent)
        finish()
    }
} 