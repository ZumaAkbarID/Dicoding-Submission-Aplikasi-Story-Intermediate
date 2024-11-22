package com.rwa.tellme.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.rwa.tellme.R
import com.rwa.tellme.databinding.ActivitySignupBinding
import com.rwa.tellme.view.ViewModelFactory
import com.rwa.tellme.view.customview.EmailEditText
import com.rwa.tellme.view.customview.PasswordEditText
import com.rwa.tellme.data.Result
import com.rwa.tellme.utils.showAlertDialog
import com.rwa.tellme.utils.showToastMessage

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var passwordEditText: PasswordEditText
    private lateinit var emailEditText: EmailEditText
    private lateinit var signupViewModel: SignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
        setupAction()
        playAnimation()
        observeSignup()

        passwordEditText = binding.edRegisterPassword
        emailEditText = binding.edRegisterEmail
    }

    private fun setupViewModel() {
        val factory = ViewModelFactory.getInstance(this)
        signupViewModel = ViewModelProvider(this, factory)[SignupViewModel::class.java]
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val nameTextV = ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(100)
        val nameLayout = ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val emailTextV = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(100)
        val emailLayout = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val passwordTextV = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100)
        val passwordLayout = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val signUpBtn = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(title, nameTextV, nameLayout, emailTextV, emailLayout, passwordTextV, passwordLayout, signUpBtn)
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

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            val name: String = binding.edRegisterName.text.toString()
            val email: String = binding.edRegisterEmail.text.toString()
            val password: String = binding.edRegisterPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showAlertDialog(this, getString(R.string.alert), getString(R.string.fill_all_field))
                return@setOnClickListener
            }

            signupViewModel.registerUser(name = name, email = email, password = password)
        }
    }

    private fun observeSignup() {
        signupViewModel.signupResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> setLoading(true)
                is Result.Success -> {
                    setLoading(false)
                    showToastMessage(this, getString(R.string.account_created))
                    finish()
                }
                is Result.Error -> {
                    setLoading(false)
                    val errorMessage = result.error
                    showAlertDialog(this, getString(R.string.failed), errorMessage)
                }
            }
        }
    }

    private fun setLoading(isVisible: Boolean) {
        var visibility = View.GONE
        if(isVisible) {
            visibility = View.VISIBLE
        }

        binding.loadingProgressBar.visibility = visibility
    }
}