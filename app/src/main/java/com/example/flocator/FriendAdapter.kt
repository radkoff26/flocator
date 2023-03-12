package com.example.flocator

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flocator.databinding.ItemPersonYourFriendBinding

class FriendAdapter: RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {
    var data: List<Person> = emptyList()
        set(newValue){
            field = newValue
            notifyDataSetChanged()
        }
    class FriendViewHolder(val binding: ItemPersonYourFriendBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPersonYourFriendBinding.inflate(inflater, parent, false)
        return FriendViewHolder(binding)
    }
    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val person = data[position] // Получение человека из списка данных по позиции
        val context = holder.itemView.context

        with(holder.binding) {
            yourFriendNameAndSurname.text = person.nameAndSurname

            Glide.with(context).load(person.photo).circleCrop() // Отрисовка фотографии пользователя с помощью библиотеки Glide
                .error(R.drawable.avatar)
                .placeholder(R.drawable.avatar).into(profileImage)
        }
    }
}