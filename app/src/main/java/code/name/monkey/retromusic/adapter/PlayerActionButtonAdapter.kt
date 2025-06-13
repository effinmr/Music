package code.name.monkey.retromusic.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.model.PlayerActionButton
import java.util.*

class PlayerActionButtonAdapter(
    private val actionButtons: MutableList<PlayerActionButton>
) : ListAdapter<PlayerActionButton, PlayerActionButtonAdapter.ActionButtonViewHolder>(ActionButtonDiffCallback()) {

    init {
        submitList(actionButtons)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionButtonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_action_button_setting, parent, false)
        return ActionButtonViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActionButtonViewHolder, position: Int) {
        val button = getItem(position)
        holder.bind(button)
    }

    fun getActionButtons(): List<PlayerActionButton> {
        return currentList
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(actionButtons, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(actionButtons, i, i - 1)
            }
        }
        submitList(actionButtons.toMutableList()) // Submit a new list instance to trigger update
    }

    inner class ActionButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dragHandle: ImageView = itemView.findViewById(R.id.drag_handle)
        private val checkboxVisible: CheckBox = itemView.findViewById(R.id.checkbox_visible)
        private val actionButtonName: TextView = itemView.findViewById(R.id.action_button_name)

        fun bind(button: PlayerActionButton) {
            actionButtonName.text = button.name
            checkboxVisible.isChecked = button.isVisible
            checkboxVisible.setOnCheckedChangeListener { _, isChecked ->
                button.isVisible = isChecked
            }
            // Drag handle setup will be done in the ItemTouchHelper
        }
    }

    private class ActionButtonDiffCallback : DiffUtil.ItemCallback<PlayerActionButton>() {
        override fun areItemsTheSame(oldItem: PlayerActionButton, newItem: PlayerActionButton): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PlayerActionButton, newItem: PlayerActionButton): Boolean {
            return oldItem == newItem
        }
    }
}
