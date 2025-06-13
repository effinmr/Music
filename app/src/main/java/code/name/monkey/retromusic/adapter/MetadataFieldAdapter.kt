package code.name.monkey.retromusic.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.appthemehelper.ThemeStore.Companion.accentColor
import code.name.monkey.retromusic.databinding.ListItemMetadataFieldSettingBinding
import code.name.monkey.retromusic.model.EditableMetadataField
import code.name.monkey.retromusic.model.MetadataField
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.SwipeAndDragHelper
import code.name.monkey.retromusic.util.SwipeAndDragHelper.ActionCompletionContract

class MetadataFieldAdapter : RecyclerView.Adapter<MetadataFieldAdapter.ViewHolder>(),
    ActionCompletionContract {

    var metadataFields: MutableList<EditableMetadataField> = mutableListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val touchHelper: ItemTouchHelper

    fun attachToRecyclerView(recyclerView: RecyclerView?) {
        touchHelper.attachToRecyclerView(recyclerView)
    }

    override fun getItemCount(): Int {
        return metadataFields.size
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val editableMetadataField = metadataFields[position]
        val metadataField = editableMetadataField.metadataField

        holder.binding.switchWidget.isChecked = editableMetadataField.isVisible
        holder.binding.title.text =
            holder.binding.title.resources.getString(metadataField.labelRes)

        holder.binding.switchWidget.setOnClickListener {
            editableMetadataField.isVisible = holder.binding.switchWidget.isChecked
        }

        holder.binding.dragHandle.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                touchHelper.startDrag(holder)
            }
            false
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ListItemMetadataFieldSettingBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onViewMoved(oldPosition: Int, newPosition: Int) {
        val metadataField = metadataFields[oldPosition]
        metadataFields.removeAt(oldPosition)
        metadataFields.add(newPosition, metadataField)
        notifyItemMoved(oldPosition, newPosition)
    }

    class ViewHolder(val binding: ListItemMetadataFieldSettingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.switchWidget.thumbTintList =
                ColorStateList.valueOf(accentColor(binding.switchWidget.context))
            binding.switchWidget.trackTintList =
                ColorStateList.valueOf(accentColor(binding.switchWidget.context))
        }
    }

    init {
        val swipeAndDragHelper = SwipeAndDragHelper(this)
        touchHelper = ItemTouchHelper(swipeAndDragHelper)
    }
}
