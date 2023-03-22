package com.example.flocator.community.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flocator.R
import com.example.flocator.community.data_classes.Person
import com.example.flocator.databinding.PersonNewFriendItemBinding

class PersonAdapter(private val personActionListener: PersonActionListener) :
    RecyclerView.Adapter<PersonAdapter.PersonViewHolder>(), View.OnClickListener {
    var data: List<Person> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class PersonViewHolder(val binding: PersonNewFriendItemBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PersonNewFriendItemBinding.inflate(inflater, parent, false)

        binding.root.setOnClickListener(this)
        binding.buttonAccept.setOnClickListener(this)
        binding.buttonCancel.setOnClickListener(this)
        return PersonViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = data[position]
        val context = holder.itemView.context

        with(holder.binding) {
            newFriendNameAndSurname.text = person.nameAndSurname

            Glide.with(context).load(person.photo)
                .circleCrop()
                .error(R.drawable.base_avatar_image)
                .placeholder(R.drawable.base_avatar_image).into(profileImage)
        }
    }


    override fun onClick(view: View?) {
        val person: Person = view?.tag as Person

        when (view.id) {
            R.id.buttonCancel -> personActionListener.onPersonCancel(person)
            R.id.buttonAccept -> personActionListener.onPersonAccept(person)
            else -> personActionListener.onPersonGetId(person)
        }
    }

}