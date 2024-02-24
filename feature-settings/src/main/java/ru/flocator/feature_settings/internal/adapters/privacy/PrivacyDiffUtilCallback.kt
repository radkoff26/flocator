package ru.flocator.feature_settings.internal.adapters.privacy

import androidx.recyclerview.widget.DiffUtil
import ru.flocator.feature_settings.internal.data.privacy.PrivacyUser

internal class PrivacyDiffUtilCallback(
    private val oldList: List<PrivacyUser>,
    private val newList: List<PrivacyUser>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].userId == newList[newItemPosition].userId

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]
}