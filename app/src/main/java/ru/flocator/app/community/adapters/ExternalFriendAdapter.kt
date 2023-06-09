package ru.flocator.app.community.adapters

import ru.flocator.app.community.data_classes.UserExternalFriends

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.databinding.PersonYourFriendItemBinding
import ru.flocator.app.common.utils.LoadUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ExternalFriendAdapter(private val friendActionListener: ExternalFriendActionListener) :
    RecyclerView.Adapter<ExternalFriendAdapter.ExternalFriendViewHolder>(), View.OnClickListener {
    var data: MutableList<UserExternalFriends> = mutableListOf()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class ExternalFriendViewHolder(val binding: PersonYourFriendItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExternalFriendViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PersonYourFriendItemBinding.inflate(inflater, parent, false)
        binding.root.setOnClickListener(this)
        return ExternalFriendViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ExternalFriendViewHolder, position: Int) {
        val person = data[position]
        with(holder.binding) {
            yourFriendNameAndSurname.text = person.firstName + " " + person.lastName
            if(person.avatarUri != null){
                setAvatar(person.avatarUri!!, holder)
            }
        }
        holder.itemView.tag = person
    }

    override fun onClick(view: View?) {
        val user: UserExternalFriends = view?.tag as UserExternalFriends
        friendActionListener.onPersonOpenProfile(user)
    }

    private fun setAvatar(uri: String, holder: ExternalFriendViewHolder){
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