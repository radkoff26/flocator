package com.example.flocator.community.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.example.flocator.R
import com.example.flocator.community.data_classes.User
import com.example.flocator.databinding.PersonNewFriendItemBinding
import com.example.flocator.common.utils.LoadUtils
import com.example.flocator.community.api.UserApi
import com.example.flocator.community.data_classes.FriendRequests
import com.example.flocator.community.data_classes.UserExternal
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class PersonAdapter(private val userNewFriendActionListener: UserNewFriendActionListener) :
    RecyclerView.Adapter<PersonAdapter.PersonViewHolder>(), View.OnClickListener {
    var data: List<FriendRequests> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }
    var isOpen = false
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }


    class PersonViewHolder(val binding: PersonNewFriendItemBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PersonNewFriendItemBinding.inflate(inflater, parent, false)

        binding.root.setOnClickListener(this)
        binding.buttonAccept.setOnClickListener(this)
        binding.buttonCancel.setOnClickListener(this)
        return PersonViewHolder(binding)
    }

    override fun getItemCount(): Int {
        val limit = 2
        if (!isOpen) {
            return data.size.coerceAtMost(limit)
        }
        return data.size
    }

    fun getAllCount(): Int{

        return data.size
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = data[position]
        val context = holder.itemView.context


        with(holder.binding) {
            newFriendNameAndSurname.text = person.firstName + " " + person.lastName
            if(person.avatarUri != null){
                setAvatar(person.avatarUri!!, holder)
            }
        }

        holder.itemView.tag = person
        holder.binding.buttonAccept.tag = person
        holder.binding.buttonCancel.tag = person

    }
    override fun onClick(view: View?) {
        val user: FriendRequests = view?.tag as FriendRequests
        when (view.id) {
            R.id.buttonCancel -> userNewFriendActionListener.onPersonCancel(user)
            R.id.buttonAccept -> userNewFriendActionListener.onPersonAccept(user)
            else -> userNewFriendActionListener.onPersonOpenProfile(user)
        }
    }

    private fun setAvatar(uri: String, holder: PersonViewHolder){
        holder.binding.userPhotoSkeleton.showSkeleton()
        holder.binding.userNameSkeleton.showSkeleton()
        LoadUtils.loadPictureFromUrl(uri, 100)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    holder.binding.profileImage.setImageBitmap(it)
                    holder.binding.userNameSkeleton.showOriginal()
                    holder.binding.userPhotoSkeleton.showOriginal()
                },
                {
                    Log.d("TestLog", "no")
                }
            )
    }

}