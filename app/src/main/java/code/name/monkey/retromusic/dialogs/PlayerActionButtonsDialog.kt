package code.name.monkey.retromusic.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.retromusic.R
import androidx.recyclerview.widget.ItemTouchHelper
import code.name.monkey.retromusic.adapter.PlayerActionButtonAdapter
import code.name.monkey.retromusic.model.PlayerActionButton
import code.name.monkey.retromusic.util.PreferenceUtil
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class PlayerActionButtonsDialog : DialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlayerActionButtonAdapter
    private lateinit var actionButtons: MutableList<PlayerActionButton>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_player_action_buttons, null)
        recyclerView = view.findViewById(R.id.action_buttons_recycler_view)

        loadActionButtons()
        adapter = PlayerActionButtonAdapter(actionButtons)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                adapter.onItemMove(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Not needed for reordering
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)


        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                saveActionButtons()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    private fun loadActionButtons() {
        actionButtons = mutableListOf()
        val savedButtonsJson = PreferenceUtil.playerActionButtonsOrder

        if (savedButtonsJson.isNotEmpty()) {
            val jsonArray = JSONArray(savedButtonsJson as String) // Explicitly cast to String
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val id = jsonObject.getInt("id")
                val name = jsonObject.getString("name")
                val isVisible = jsonObject.getBoolean("isVisible")
                actionButtons.add(PlayerActionButton(id, name, isVisible))
            }
        } else {
            // Default order and visibility
            actionButtons.add(PlayerActionButton(R.id.action_sleep_timer, getString(R.string.action_sleep_timer), PreferenceUtil.showSleepTimerButton))
            actionButtons.add(PlayerActionButton(R.id.action_toggle_lyrics, getString(R.string.action_toggle_lyrics), PreferenceUtil.showLyricsButton))
            actionButtons.add(PlayerActionButton(R.id.action_toggle_favorite, getString(R.string.action_toggle_favorite), PreferenceUtil.showFavoriteButton))
            // Add other default buttons here as needed
        }
    }

    private fun saveActionButtons() {
        val jsonArray = JSONArray()
        adapter.getActionButtons().forEach { button ->
            val jsonObject = JSONObject()
            jsonObject.put("id", button.id)
            jsonObject.put("name", button.name)
            jsonObject.put("isVisible", button.isVisible)
            jsonArray.put(jsonObject)
        }
        PreferenceUtil.playerActionButtonsOrder = jsonArray.toString()
    }

    companion object {
        const val TAG = "PlayerActionButtonsDialog"
    }
}
