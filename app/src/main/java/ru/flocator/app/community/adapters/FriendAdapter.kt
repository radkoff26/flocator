package ru.flocator.app.community.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.databinding.PersonYourFriendItemBinding
import ru.flocator.app.common.utils.LoadUtils
import ru.flocator.app.community.data_classes.Friends
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FriendAdapter(private val friendActionListener: FriendActionListener) :
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>(), View.OnClickListener {
    var data: MutableList<Friends> = mutableListOf()
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
            println("АВАТАРРРР  ->   " + person.avatarUri)
            if(person.avatarUri != null){
                setAvatar(person.avatarUri!!, holder)
            }
        }
        holder.itemView.tag = person
    }

    override fun onClick(view: View?) {
        val user: Friends = view?.tag as Friends
        friendActionListener.onPersonOpenProfile(user)
    }

    private fun setAvatar(uri: String, holder: FriendViewHolder){
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