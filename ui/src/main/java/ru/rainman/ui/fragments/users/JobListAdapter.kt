package ru.rainman.ui.fragments.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.rainman.domain.model.Job
import ru.rainman.ui.databinding.CardJobItemBinding
import ru.rainman.ui.helperutils.represent

class JobListAdapter : ListAdapter<Job, JobListAdapter.ViewHolder>(Diff()) {

    inner class ViewHolder(private val binding: CardJobItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Job) {

            binding.apply {

                title.text = job.name
                position.text = job.position
                dates.text = "From ${job.start} to present"
                link.root.isVisible = job.link != null
                job.link?.let { link.represent(it) }
            }

        }
    }


    class Diff: DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardJobItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).let { holder.bind(it) }
    }
}