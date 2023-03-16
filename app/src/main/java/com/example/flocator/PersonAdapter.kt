package com.example.flocator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flocator.databinding.ItemPersonNewFriendBinding

class PersonAdapter(private val personActionListener: PersonActionListener) :
    RecyclerView.Adapter<PersonAdapter.PersonViewHolder>(), View.OnClickListener {
    var data: List<Person> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class PersonViewHolder(val binding: ItemPersonNewFriendBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPersonNewFriendBinding.inflate(inflater, parent, false)

        binding.root.setOnClickListener(this)
        binding.buttonAccept.setOnClickListener(this)
        binding.buttonCancel.setOnClickListener(this)
        return PersonViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = data[position] // Получение человека из списка данных по позиции
        val context = holder.itemView.context

        with(holder.binding) {
            newFriendNameAndSurname.text = person.nameAndSurname

            Glide.with(context).load(person.photo)
                .circleCrop() // Отрисовка фотографии пользователя с помощью библиотеки Glide
                .error(R.drawable.avatar)
                .placeholder(R.drawable.avatar).into(profileImage)
        }
    }


    override fun onClick(view: View?) {
        val person: Person = view?.tag as Person // Получаем из тэга человека

        when (view.id) {
            R.id.buttonCancel -> personActionListener.onPersonCancel(person)
            R.id.buttonAccept -> personActionListener.onPersonAccept(person)
            else -> personActionListener.onPersonGetId(person)
        }
    }

}