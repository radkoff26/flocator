package ru.flocator.feature_settings.internal.ui.adapters.privacy

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
import ru.flocator.feature_settings.databinding.ItemPrivacyBinding
import ru.flocator.feature_settings.internal.data.model.privacy.PrivacyUser

internal class PrivacyListAdapter(
    private val toggleUserPrivacyTypeCallback: (userId: Long) -> Unit,
    private val loadPhotoCallback: (uri: String) -> Single<Bitmap>
) : RecyclerView.Adapter<PrivacyListAdapter.ViewHolder>() {
    private var friends = mutableListOf<PrivacyUser>()
    private val compositeDisposable = CompositeDisposable()

    fun setFriendList(friends: List<PrivacyUser>) {
        val diffResult = DiffUtil.calculateDiff(PrivacyDiffUtilCallback(this.friends, friends))
        this.friends = friends.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding: ItemPrivacyBinding = ItemPrivacyBinding.bind(itemView)

        fun bind(user: PrivacyUser) {
            binding.friendName.text = binding.root.resources.getString(
                R.string.name_surname,
                user.firstName,
                user.lastName
            )

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

            if (user.isChecked) {
                binding.friendTick.visibility = View.VISIBLE
            } else {
                binding.friendTick.visibility = View.GONE
            }

            binding.friendElement.setOnClickListener {
                toggleUserPrivacyTypeCallback.invoke(user.userId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_privacy, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friends[position])
    }
}