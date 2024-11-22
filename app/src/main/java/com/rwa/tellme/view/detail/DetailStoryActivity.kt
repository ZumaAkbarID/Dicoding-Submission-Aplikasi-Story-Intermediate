package com.rwa.tellme.view.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.rwa.tellme.R
import com.rwa.tellme.data.model.StoryModel
import com.rwa.tellme.databinding.ActivityDetailStoryBinding

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding
    private val detailStoryViewModel: DetailStoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupData()
    }

    private fun setupData() {
        val story = intent.getParcelableExtra<StoryModel>("Story") as StoryModel

        Glide.with(applicationContext).load(story.photo).into(binding.ivDetailPhoto)
        binding.tvDetailName.text = story.name
        binding.tvDetailDescription.text = story.description

        val formattedDate = detailStoryViewModel.formatCreatedAt(story.createdAt)
        binding.tvStoryDate.text = formattedDate

        setupMapAction(story)
    }

    private fun setupMapAction(story: StoryModel) {
        if(story.lon == 0.0 || story.lat == 0.0) {
            binding.btnViewMap.visibility = View.GONE
            return
        }

        binding.btnViewMap.setOnClickListener {
            val uri = Uri.parse("google.navigation:q="+story.lat+","+story.lon + "&mode=d")

            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        }
    }
}