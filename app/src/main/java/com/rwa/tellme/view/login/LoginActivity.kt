package com.rwa.tellme.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.rwa.tellme.R
import com.rwa.tellme.data.Result
import com.rwa.tellme.databinding.ActivityLoginBinding
import com.rwa.tellme.utils.showAlertDialog
import com.rwa.tellme.utils.showToastMessage
import com.rwa.tellme.utils.wrapEspressoIdlingResource
import com.rwa.tellme.view.ViewModelFactory
import com.rwa.tellme.view.customview.EmailEditText
import com.rwa.tellme.view.customview.PasswordEditText
import com.rwa.tellme.view.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var passwordEditText: PasswordEditText
    private lateinit var emailEditText: EmailEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(owner = this, factory = factory)[LoginViewModel::class.java]

        setupView()
        setupAction()
        playAnimation()
        observeLogin()

        passwordEditText = binding.edLoginPassword
        emailEditText = binding.edLoginEmail
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val message =
            ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1f).setDuration(100)
        val emailTextV =
            ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(100)
        val emailLayout =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val passwordTextV =
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100)
        val passwordLayout =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val signInBtn = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                title,
                message,
                emailTextV,
                emailLayout,
                passwordTextV,
                passwordLayout,
                signInBtn
            )
            start()
            startDelay = 100
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun observeLogin() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> setLoading(true)
                is Result.Success -> {
                    setLoading(false)
                    val user = result.data

                    lifecycleScope.launch {
                        viewModel.saveSession(user)

                        viewModel.getSession().observe(this@LoginActivity) { session ->
                            if (session.isLogin) {
                                wrapEspressoIdlingResource {
                                    viewModel.setTokenToInterceptor(session.token)
                                    val intent = Intent(baseContext, MainActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    showToastMessage(baseContext, getString(R.string.welcome_back))
                                    finish()
                                }
                            }
                        }
                    }
                }

                is Result.Error -> {
                    setLoading(false)
                    val errorMessage = result.error
                    showAlertDialog(this, getString(R.string.failed), errorMessage)
                }
            }
        }
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            val email: String = binding.edLoginEmail.text.toString()
            val password: String = binding.edLoginPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                showAlertDialog(this, getString(R.string.alert), getString(R.string.fill_all_field))
                return@setOnClickListener
            }

            viewModel.loginUser(email, password)
        }
    }

    private fun setLoading(isVisible: Boolean) {
        var visibility = View.GONE
        if (isVisible) {
            visibility = View.VISIBLE
        }

        binding.loadingProgressBar.visibility = visibility
    }
}