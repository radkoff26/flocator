package com.example.flocator.community.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flocator.community.data_classes.Person
import com.example.flocator.databinding.PersonYourFriendItemBinding
import com.example.flocator.R

class FriendAdapter() : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {
    var data: MutableList<Person> = mutableListOf()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class FriendViewHolder(val binding: PersonYourFriendItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PersonYourFriendItemBinding.inflate(inflater, parent, false)
        return FriendViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val person = data[position]
        val context = holder.itemView.context

        with(holder.binding) {
            yourFriendNameAndSurname.text = person.nameAndSurname

            Glide.with(context).load(person.photo)
                .circleCrop()
                .error(R.drawable.base_avatar_image)
                .placeholder(R.drawable.base_avatar_image).into(profileImage)
        }
    }

    fun addFriend(person: Person){
        data.add(person)
        notifyDataSetChanged()
    }
}