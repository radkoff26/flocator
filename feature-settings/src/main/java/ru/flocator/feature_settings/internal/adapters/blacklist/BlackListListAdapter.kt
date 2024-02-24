package ru.flocator.feature_settings.internal.adapters.blacklist

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.core.extensions.findDrawable
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.databinding.ItemBlackListBinding
import ru.flocator.feature_settings.internal.data.friend.BlackListUser

internal class BlackListListAdapter(
    private val unblockUserCallback: (userId: Long) -> Unit,
    private val loadPhotoCallback: (uri: String) -> Single<Bitmap>
) : RecyclerView.Adapter<BlackListListAdapter.ViewHolder>() {
    private var blackListUsers = listOf<BlackListUser>()
    private val compositeDisposable = CompositeDisposable()

    fun setFriendList(blackListUsers: List<BlackListUser>) {
        val diffResult = DiffUtil.calculateDiff(BlackListDiffUtilCallback(this.blackListUsers, blackListUsers))
        this.blackListUsers = blackListUsers
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding: ItemBlackListBinding = ItemBlackListBinding.bind(itemView)

        fun bind(user: BlackListUser) {
            binding.friendName.text = user.firstName

            val avatarUri = user.avatarUri

            if (avatarUri != null) {
                binding.friendAvatarSkeleton.showSkeleton()
                compositeDisposable.add(
                    loadPhotoCallback(avatarUri)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                binding.friendAvatar.setImageBitmap(it)
                                binding.friendAvatarSkeleton.showOriginal()
                            },
                            {
                                binding.friendAvatar.setImageDrawable(
                                    itemView.resources.findDrawable(
                                        ru.flocator.design.R.drawable.base_avatar_image
                                    )
                                )
                                binding.friendAvatarSkeleton.showOriginal()
                            }
                        )
                )
            }

            binding.unblock.setOnClickListener {
                unblockUserCallback.invoke(user.userId)
            }

            // TODO: open person's profile by clicking on avatar
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_black_list, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return blackListUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(blackListUsers[position])
    }
}