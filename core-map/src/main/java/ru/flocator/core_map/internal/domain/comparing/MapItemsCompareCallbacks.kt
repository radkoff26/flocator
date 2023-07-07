package ru.flocator.core_map.internal.domain.comparing

import ru.flocator.core_map.api.entity.Mark
import ru.flocator.core_map.api.entity.User
import ru.flocator.core_map.internal.domain.entity.MarkGroup
import ru.flocator.core_map.internal.domain.holder.MarkGroupHolder
import ru.flocator.core_map.internal.domain.holder.MarkHolder
import ru.flocator.core_map.internal.domain.holder.UserHolder

internal object MapItemsCompareCallbacks {
    object MarkCompareCallback :
        CompareCallback<MarkHolder, Mark> {
        override fun areComparedItemsTheSame(item1: MarkHolder, item2: Mark): Boolean {
            return item1.mark.markId == item2.markId
        }

        override fun areComparedItemsContentsTheSame(
            item1: MarkHolder,
            item2: Mark
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