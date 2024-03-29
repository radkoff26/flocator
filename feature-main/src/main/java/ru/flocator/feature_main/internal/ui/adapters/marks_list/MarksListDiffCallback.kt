package ru.flocator.feature_main.internal.ui.adapters.marks_list

import androidx.recyclerview.widget.DiffUtil
import ru.flocator.feature_main.internal.data.model.dto.ListMarkDto

internal class MarksListDiffCallback(
    private val oldMarks: List<ListMarkDto>,
    private val newMarks: List<ListMarkDto>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldMarks.size

    override fun getNewListSize(): Int = newMarks.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldMarks[oldItemPosition].mark.markId == newMarks[newItemPosition].mark.markId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldMarks[oldItemPosition] == newMarks[newItemPosition]
    }
}