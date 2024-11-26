package com.rwa.tellme.view.main

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rwa.tellme.data.model.StoryModel
import com.rwa.tellme.databinding.ItemRowStoryBinding
import com.rwa.tellme.view.detail.DetailStoryActivity
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter

class ListStoryAdapter: PagingDataAdapter<StoryModel, ListStoryAdapter.ListViewHolder>(DIFF_CALLBACK) {

    class ListViewHolder(private val binding: ItemRowStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: StoryModel) {
            Glide.with(binding.ivItemPhoto.context).load(story.photo).into(binding.ivItemPhoto)
            binding.tvItemName.text = story.name

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailStoryActivity::class.java)
                intent.putExtra("Story", story)

                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.ivItemPhoto, "profile"),
                        Pair(binding.tvItemName, "name"),
                    )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemRowStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val story = getItem(position)
        if (story !== null) {
            holder.bind(story)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryModel>() {
            override fun areItemsTheSame(oldItem: StoryModel, newItem: StoryModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: StoryModel, newItem: StoryModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}