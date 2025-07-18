package ai.gbox.chatdroid

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ai.gbox.chatdroid.databinding.ActivityLoginBinding
import ai.gbox.chatdroid.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val repo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If a token already exists, skip login
        if (!ai.gbox.chatdroid.datastore.AuthPreferences.currentToken().isNullOrBlank()) {
            startActivity(android.content.Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin(); true
            } else false
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                repo.login(email, password)
            }
            binding.progressBar.visibility = android.view.View.GONE
            binding.btnLogin.isEnabled = true

            result.fold(onSuccess = {
                Toast.makeText(this@LoginActivity, "Login success", Toast.LENGTH_SHORT).show()
                // Navigate to main activity
                startActivity(android.content.Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }, onFailure = { err ->
                Toast.makeText(this@LoginActivity, err.message ?: "Login failed", Toast.LENGTH_LONG).show()
            })
        }
    }
} 