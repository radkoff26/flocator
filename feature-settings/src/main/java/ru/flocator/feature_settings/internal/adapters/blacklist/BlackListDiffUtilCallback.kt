package ru.flocator.feature_settings.internal.adapters.blacklist

import androidx.recyclerview.widget.DiffUtil
import ru.flocator.feature_settings.internal.data.friend.BlackListUser

internal class BlackListDiffUtilCallback(
    private val oldList: List<BlackListUser>,
    private val newList: List<BlackListUser>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].userId == newList[newItemPosition].userId

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]
}