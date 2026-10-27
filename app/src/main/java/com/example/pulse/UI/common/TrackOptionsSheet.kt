package com.example.pulse.ui.common

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.pulse.R
import com.example.pulse.data.Track
import com.example.pulse.databinding.BottomSheetTrackOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
data class SheetAction(val title: String, val onClick: () -> Unit)

object TrackOptionsSheet {

    fun show(context: Context, track: Track, actions: List<SheetAction>) {
        val inflater = LayoutInflater.from(context)
        val binding = BottomSheetTrackOptionsBinding.inflate(inflater)
        val dialog = BottomSheetDialog(context)

        binding.tvSheetTitle.text = track.title
        binding.tvSheetArtist.text = track.artist

        actions.forEach { action ->
            val row = inflater.inflate(
                R.layout.item_sheet_action,
                binding.actionsContainer,
                false
            ) as TextView
            row.text = action.title
            row.setOnClickListener {
                dialog.dismiss()
                action.onClick()
            }
            binding.actionsContainer.addView(row)
        }

        dialog.setContentView(binding.root)
        (binding.root.parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
        dialog.show()
    }
}