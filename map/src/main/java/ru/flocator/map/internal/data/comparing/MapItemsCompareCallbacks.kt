package ru.flocator.map.internal.data.comparing

import ru.flocator.data.database.entities.User
import ru.flocator.map.api.entity.MapMark
import ru.flocator.map.internal.data.entity.MarkGroup
import ru.flocator.map.internal.data.holder.MarkGroupHolder
import ru.flocator.map.internal.data.holder.MarkHolder
import ru.flocator.map.internal.data.holder.UserHolder

internal object MapItemsCompareCallbacks {
    object MarkCompareCallback :
        CompareCallback<MarkHolder, MapMark> {
        override fun areComparedItemsTheSame(item1: MarkHolder, item2: MapMark): Boolean {
            return item1.mark.markId == item2.markId
        }

        override fun areComparedItemsContentsTheSame(
            item1: MarkHolder,
            item2: MapMark
        ): Boolean {
            return item1.mark.location == item2.location
                    &&
                    item1.mark.thumbnailUri == item2.thumbnailUri
                    &&
                    item1.mark.authorAvatarUri == item2.authorAvatarUri
        }
    }

    object MarkGroupCompareCallback :
        CompareCallback<MarkGroupHolder, MarkGroup> {
        override fun areComparedItemsTheSame(
            item1: MarkGroupHolder,
            item2: MarkGroup
        ): Boolean {
            return item1.markGroup.center == item2.center
                    && item1.markGroup.marks.size == item2.marks.size
        }

        override fun areComparedItemsContentsTheSame(
            item1: MarkGroupHolder,
            item2: MarkGroup
        ): Boolean {
            item1.markGroup.marks.forEachIndexed { index, oldMark ->
                val mark = item2.marks[index]
                if (mark.markId != oldMark.markId) {
                    return false
                }
            }
            return true
        }
    }

    object UserCompareCallback :
        CompareCallback<UserHolder, User> {
        override fun areComparedItemsTheSame(item1: UserHolder, item2: User): Boolean {
            return item1.user.userId == item2.userId
        }

        override fun areComparedItemsContentsTheSame(
            item1: UserHolder,
            item2: User
        ): Boolean {
            return item1.user.avatarUri == item2.avatarUri
                    &&
                    item1.user.location == item2.location
        }
    }
}