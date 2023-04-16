package com.example.flocator.community.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flocator.community.data_classes.Person
import com.example.flocator.databinding.PersonYourFriendItemBinding
import com.example.flocator.R
import com.example.flocator.community.data_classes.User
import com.example.flocator.databinding.PersonNewFriendItemBinding
import com.example.flocator.main.utils.LoadUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FriendAdapter(private val friendActionListener: FriendActionListener) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>(), View.OnClickListener {
    var data: MutableList<User> = mutableListOf()
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
            yourFriendNameAndSurname.text = person.firstName + " " + person.lastName
            setAvatar(person.avatarUrl!!, holder)
        }
        holder.itemView.tag = person
    }

    override fun onClick(view: View?) {
        val user: User = view?.tag as User
        friendActionListener.onPersonOpenProfile(user)
    }

    private fun setAvatar(uri: String, holder: FriendViewHolder){
        LoadUtils.loadPictureFromUrl(uri, 100)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    holder.binding.profileImage.setImageBitmap(it)
                },
                {
                    Log.d("TestLog", "no")
                }
            )
    }
}