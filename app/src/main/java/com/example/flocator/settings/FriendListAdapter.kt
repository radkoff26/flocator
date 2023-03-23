package com.example.flocator.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.R

class FriendListAdapter (
    private val friends: ArrayList<Friend>
): RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val icon: ImageView = itemView.findViewById(R.id.friend_avatar)
        private val name: TextView = itemView.findViewById(R.id.friend_name)
        private val tick: ImageView = itemView.findViewById(R.id.friend_tick)
        private val friendElement: ConstraintLayout = itemView.findViewById(R.id.friend_element)
        fun bind(friend: Friend) {
            icon.setImageResource(friend.icon)
            name.text = friend.name
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
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_privacy_friend, null, false)
        )
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friends[position])
    }

    fun selectAll() {
        for ((i, friend) in friends.withIndex()) {
            if (!friend.isChecked) {
                friend.isChecked = true
                notifyItemChanged(i)
            }
        }
    }

    fun unselectAll() {
        for ((i, friend) in friends.withIndex()) {
            if (friend.isChecked) {
                friend.isChecked = false
                notifyItemChanged(i)
            }
        }
    }
}