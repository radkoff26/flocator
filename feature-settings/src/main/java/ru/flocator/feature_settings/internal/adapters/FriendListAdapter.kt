package ru.flocator.feature_settings.internal.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import ru.flocator.core_utils.LoadUtils
import ru.flocator.feature_settings.R
import ru.flocator.feature_settings.internal.domain.friend.Friend

internal class FriendListAdapter(
) : RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    private var friends = mutableListOf<Friend>()
    val publisher = PublishSubject.create<Friend>()

    @SuppressLint("NotifyDataSetChanged")
    fun setFriendList(friends: List<Friend>) {
        this.friends = friends.toMutableList()
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.friend_avatar)
        private val name: TextView = itemView.findViewById(R.id.friend_name)
        private val tick: ImageView = itemView.findViewById(R.id.friend_tick)
        private val friendElement: ConstraintLayout = itemView.findViewById(R.id.friend_element)
        fun bind(friend: Friend, holder: ViewHolder) {

            name.text = friend.name


            val avaUri = friend.avaURI
            if (avaUri != null) {

                LoadUtils.loadPictureFromUrl(avaUri, QUALITY_FACTOR)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            icon.setImageBitmap(it)
                        },
                        {
                            icon.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    holder.friendElement.resources,
                                    ru.flocator.core_design.R.drawable.base_avatar_image,
                                    null
                                )
                            )
                        }
                    )
            }

            if (friend.isChecked) {
                tick.imageAlpha = 100
            } else {
                tick.imageAlpha = 0
            }
            friendElement.setOnClickListener {
                friend.isChecked = !friend.isChecked
                if (friend.isChecked) {
                    tick.imageAlpha = 100
                } else {
                    tick.imageAlpha = 0
                }
                publisher.onNext(friend)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_privacy_friend, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    fun getItems(): ArrayList<Friend> {
        return ArrayList(friends.toList())
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friends[position], holder)

    }

    fun selectAll() {
        for ((i, friend) in friends.withIndex()) {
            if (!friend.isChecked) {
                friend.isChecked = true
                publisher.onNext(friend)
                notifyItemChanged(i)
            }
        }
    }

    fun unselectAll() {
        for ((i, friend) in friends.withIndex()) {
            if (friend.isChecked) {
                friend.isChecked = false
                publisher.onNext(friend)
                notifyItemChanged(i)
            }
        }
    }

    fun changeStates(states: Map<Long, Boolean>) {
        for ((i, friend) in friends.withIndex()) {
            val newState = states[friend.userId]
            if (newState != null && friend.isChecked != newState) {
                friend.isChecked = newState
                notifyItemChanged(i)
            }
        }
    }

    fun all(filter: (friend: Friend) -> Boolean): Boolean {
        return friends.all(filter)
    }

    companion object {
        const val QUALITY_FACTOR = 30
    }
}