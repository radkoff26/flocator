package ru.flocator.feature_community.internal.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.flocator.core.photo.PhotoLoadingTool
import ru.flocator.feature_community.R
import ru.flocator.feature_community.databinding.PersonYourFriendItemBinding
import ru.flocator.feature_community.internal.data.model.UserItem

internal class ExternalFriendAdapter(private val friendActionListener: ExternalFriendActionListener) :
    RecyclerView.Adapter<ExternalFriendAdapter.ExternalFriendViewHolder>(), View.OnClickListener {
    var data: MutableList<UserItem> = mutableListOf()
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
            yourFriendNameAndSurname.text =
                root.resources.getString(R.string.name_surname, person.firstName, person.lastName)
            if (person.avatarUri != null) {
                setAvatar(person.avatarUri!!, holder)
            }
        }
        holder.itemView.tag = person
    }

    override fun onClick(view: View?) {
        val user: UserItem = view?.tag as UserItem
        friendActionListener.onPersonOpenProfile(user)
    }

    private fun setAvatar(uri: String, holder: ExternalFriendViewHolder) {
        holder.binding.userPhotoSkeleton.showSkeleton()
        holder.binding.userNameSkeleton.showSkeleton()
        PhotoLoadingTool.loadPictureFromUrl(uri, 100)
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