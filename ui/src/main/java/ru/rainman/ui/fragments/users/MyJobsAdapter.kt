package ru.rainman.ui.fragments.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.rainman.domain.model.Job
import ru.rainman.ui.databinding.CardJobItemBinding
import ru.rainman.ui.databinding.CardMyJobItemBinding
import ru.rainman.ui.helperutils.represent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MyJobsAdapter (
    private val onMyJobClickListener: OnMyJobClickListener
        ): ListAdapter<Job, MyJobsAdapter.ViewHolder>(Diff()) {

    inner class ViewHolder(private val binding: CardMyJobItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Job) {

            binding.apply {

                title.text = job.name
                position.text = job.position
                dates.text = dates(job.start.toLocalDate(), job.finish?.toLocalDate())
                link.root.isVisible = job.link != null
                job.link?.let { link.represent(it) }
            }

            binding.edit.setOnClickListener {
                onMyJobClickListener.onEditClicked(job.id)
            }

            binding.delete.setOnClickListener {
                onMyJobClickListener.onDeleteClicked(job.id)
            }

        }

        private fun dates(start: LocalDate, finish: LocalDate?) : String {
            return "From ${start.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))} to ${
                finish?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "present day"}"
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
            CardMyJobItemBinding.inflate(
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