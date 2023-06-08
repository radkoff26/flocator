package com.example.flocator.main.ui.map.domain.comparing


import com.example.flocator.main.ui.map.domain.dto.MarkGroupViewDto
import com.example.flocator.main.ui.map.domain.dto.MarkViewDto
import com.example.flocator.main.ui.map.domain.dto.UserViewDto

object MapItemsCompareCallbacks {
    object MarkCompareCallback :
        CompareCallback<MarkViewDto> {
        override fun areComparedItemsTheSame(item1: MarkViewDto, item2: MarkViewDto): Boolean {
            return item1.mark.mark.markId == item2.mark.mark.markId
        }

        override fun areComparedItemsContentsTheSame(
            item1: MarkViewDto,
            item2: MarkViewDto
        ): Boolean {
            return item1.mark.mark.location == item2.mark.mark.location
                    &&
                    item1.mark.photos == item2.mark.photos
                    &&
                    item1.userAvatarUri == item2.userAvatarUri
        }
    }

    object MarkGroupCompareCallback :
        CompareCallback<MarkGroupViewDto> {
        override fun areComparedItemsTheSame(
            item1: MarkGroupViewDto,
            item2: MarkGroupViewDto
        ): Boolean {
            return item1.markGroup.center == item2.markGroup.center
        }

        override fun areComparedItemsContentsTheSame(
            item1: MarkGroupViewDto,
            item2: MarkGroupViewDto
        ): Boolean {
            if (item1.markGroup.marks.size != item2.markGroup.marks.size) {
                return false
            }
            item1.markGroup.marks.forEachIndexed { index, markWithPhotos ->
                val mark = item2.markGroup.marks[index]
                if (mark.mark.markId != markWithPhotos.mark.markId) {
                    return false
                }
            }
            return true
        }
    }

    object UserCompareCallback :
        CompareCallback<UserViewDto> {
        override fun areComparedItemsTheSame(item1: UserViewDto, item2: UserViewDto): Boolean {
            return item1.user.id == item2.user.id
        }

        override fun areComparedItemsContentsTheSame(
            item1: UserViewDto,
            item2: UserViewDto
        ): Boolean {
            return item1.user.avatarUri == item2.user.avatarUri
                    &&
                    item1.user.location == item2.user.location
        }
    }
}