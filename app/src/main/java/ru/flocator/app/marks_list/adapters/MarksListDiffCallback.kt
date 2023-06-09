package ru.flocator.app.marks_list.adapters

import androidx.recyclerview.widget.DiffUtil
import ru.flocator.app.marks_list.domain.dto.ListMarkDto

class MarksListDiffCallback(
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