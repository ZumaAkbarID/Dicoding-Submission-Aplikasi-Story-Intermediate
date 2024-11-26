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
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rwa.tellme.R
import com.rwa.tellme.databinding.ActivityMainBinding
import com.rwa.tellme.utils.wrapEspressoIdlingResource
import com.rwa.tellme.view.StoryViewModelFactory
import com.rwa.tellme.view.ViewModelFactory
import com.rwa.tellme.view.addnew.AddNewStoryActivity
import com.rwa.tellme.view.maps.MapsActivity
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

        progressBar = binding.progressBarMain
        setupAuthViewModel()

        viewModel.getSession().observe(this) { user ->
            wrapEspressoIdlingResource {
                if (!user.isLogin) {
                    viewModel.clearTokenFromInterceptor()
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    finish()
                } else {
                    viewModel.initTokenInterceptor(this)
                    setupStoryViewModel()
                    observeViewModel()
                    loadStoryData()
                }
            }
        }

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

        storyViewModel?.isLoading?.observe(this) { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadStoryData() {
        adapter = ListStoryAdapter()
        binding.rvStory.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )

        storyViewModel?.story?.observe(this) {
            adapter.submitData(lifecycle, it)
        }

        adapter.addLoadStateListener { loadState ->
            val isLoading = loadState.refresh is LoadState.Loading
            binding.progressBarMain.visibility = if (isLoading) View.VISIBLE else View.GONE

            val isListEmpty = (loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0) || (loadState.refresh is LoadState.Error && adapter.itemCount == 0)
            binding.textMessage.visibility = if (isListEmpty) View.VISIBLE else View.GONE

            val errorState = loadState.refresh as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error

            errorState?.let {
                storyViewModel?.setErrorMessage(it.error.localizedMessage)
            }
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
                R.id.action_maps -> {
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
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
    }

    private fun observeViewModel() {
        storyViewModel?.errorMessage?.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}