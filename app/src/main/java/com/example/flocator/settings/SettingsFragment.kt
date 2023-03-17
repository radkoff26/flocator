package com.example.flocator.settings

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.example.flocator.R
import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_settings, container, false)
        val exitLinearLayout = fragmentView.findViewById<LinearLayout>(R.id.exit_account)
        val birthDateLinearLayout = fragmentView.findViewById<LinearLayout>(R.id.date_of_birth_line)
        val birthDateField = fragmentView.findViewById<TextView>(R.id.date_of_birth_field)
        val blacklistLine = fragmentView.findViewById<LinearLayout>(R.id.blacklist_line)
        val privacyLine = fragmentView.findViewById<LinearLayout>(R.id.privacy_line)
        val avatar = fragmentView.findViewById<ImageButton>(R.id.avatar)
        val nicknameInput = fragmentView.findViewById<EditText>(R.id.name_field)

        birthDateLinearLayout.setOnClickListener {

            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { view, resYear, resMonth, resDay ->
                    // on below line we are setting
                    // date to our text view.
                    birthDateField.text = "${resDay.toString().padStart(2, '0')}.${
                        (resMonth + 1).toString().padStart(2, '0')
                    }.$resYear"
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        blacklistLine.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, PrivacySettings())
                .addToBackStack(null)
                .commit()
        }

        privacyLine.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, PrivacySettings())
                .addToBackStack(null)
                .commit()
        }



        return fragmentView
    }

}