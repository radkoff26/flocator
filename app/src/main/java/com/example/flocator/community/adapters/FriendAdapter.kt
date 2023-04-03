package com.example.flocator.community.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flocator.community.data_classes.Person
import com.example.flocator.databinding.PersonYourFriendItemBinding
import com.example.flocator.R
import com.example.flocator.databinding.PersonNewFriendItemBinding

class FriendAdapter(private val friendActionListener: FriendActionListener) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>(), View.OnClickListener {
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
        binding.root.setOnClickListener(this)
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
        holder.itemView.tag = person
    }

    override fun onClick(view: View?) {
        val person: Person = view?.tag as Person
        friendActionListener.onPersonOpenProfile(person)
    }
}