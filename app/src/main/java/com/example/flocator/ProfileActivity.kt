package com.example.flocator

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flocator.databinding.CommunityBinding

class ProfileActivity: AppCompatActivity() {
    private lateinit var binding: CommunityBinding
    private lateinit var adapter: PersonAdapter // Объект Adapter
    private lateinit var adapterForYourFriends: FriendAdapter
    private val personService: PersonService // Объект PersonService
        get() = (applicationContext as App).personService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val manager = LinearLayoutManager(this) // LayoutManager
        adapter = PersonAdapter(object: PersonActionListener{
            override fun onPersonGetId(person: Person) =
                Toast.makeText(this@ProfileActivity, "Persons ID: ${person.id}", Toast.LENGTH_SHORT).show()
            override fun onPersonCancel(person: Person) = personService.cancelPerson(person)
            override fun onPersonAccept(person: Person) = personService.acceptPerson(person)
        }) // Создание объекта
        adapter.data = personService.getPersons() // Заполнение данными

        binding.newFriendsRecyclerView.layoutManager = manager
        binding.newFriendsRecyclerView.adapter = adapter

        adapterForYourFriends = FriendAdapter()
        adapterForYourFriends.data = PersonService().getPersons()

        binding.yourFriendsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.yourFriendsRecyclerView.adapter = adapterForYourFriends

        binding.buttonViewAll.setOnClickListener{
            adapter.data = personService.addPersonsInList(5)
            binding.newFriendsRecyclerView.layoutManager = manager
            binding.newFriendsRecyclerView.adapter = adapter
            binding.buttonViewAll.visibility = View.INVISIBLE
            binding.buttonNotViewAll.visibility = View.VISIBLE
        }

        binding.buttonNotViewAll.setOnClickListener{
            adapter.data = personService.removeExtraPersonsInList()
            binding.newFriendsRecyclerView.layoutManager = manager
            binding.newFriendsRecyclerView.adapter = adapter
            binding.buttonViewAll.visibility = View.VISIBLE
            binding.buttonNotViewAll.visibility = View.INVISIBLE
        }

    }

}