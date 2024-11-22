package com.rwa.tellme.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rwa.tellme.R
import com.rwa.tellme.databinding.ActivityMainBinding
import com.rwa.tellme.view.StoryViewModelFactory
import com.rwa.tellme.view.ViewModelFactory
import com.rwa.tellme.view.addnew.AddNewStoryActivity
import com.rwa.tellme.view.welcome.WelcomeActivity

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private var storyViewModel: MainStoryViewModel? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var rvStory: RecyclerView
    private lateinit var adapter: ListStoryAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAuthViewModel()

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                setupStoryViewModel()
                observeViewModel()
                loadStoryData()
            }
        }

        setupProgressBar()
        setupView()
        setupAction()
        setupRV()
    }

    private fun setupAuthViewModel() {
        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
    }

    private fun setupStoryViewModel() {
        val storyFactory = StoryViewModelFactory.getInstance(this)
        storyViewModel = ViewModelProvider(this, storyFactory)[MainStoryViewModel::class.java]
    }

    private fun loadStoryData() {
        storyViewModel?.showAllStory()
    }

    override fun onResume() {
        super.onResume()
        loadStoryData()
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
        binding.buttonAdd.setOnClickListener {
            val intent = Intent(this, AddNewStoryActivity::class.java)
            startActivity(intent)
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    viewModel.logout()
                    true
                }
                R.id.action_change_lang -> {
                    startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRV() {
        rvStory = binding.rvStory
        rvStory.setHasFixedSize(true)
        rvStory.layoutManager = LinearLayoutManager(this)

        adapter = ListStoryAdapter()
        rvStory.adapter = adapter
    }

    private fun setupProgressBar() {
        progressBar = binding.progressBar
        storyViewModel?.isLoading?.observe(this) { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun observeViewModel() {
        storyViewModel?.listStory?.observe(this) { stories ->
            adapter.submitList(stories)
        }

        storyViewModel?.errorMessage?.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}