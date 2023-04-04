package com.example.flocator.settings

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.flocator.R
import com.example.flocator.utils.FragmentNavigationUtils
import java.util.*

class SettingsFragment : Fragment(), SettingsSection {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_settings, container, false)
        val exitLinearLayout = fragmentView.findViewById<LinearLayout>(R.id.exit_account_line)
        val birthDateLinearLayout = fragmentView.findViewById<LinearLayout>(R.id.date_of_birth_line)
        val birthDateField = fragmentView.findViewById<TextView>(R.id.date_of_birth_field)
        val blacklistLine = fragmentView.findViewById<LinearLayout>(R.id.blacklist_line)
        val privacyLine = fragmentView.findViewById<LinearLayout>(R.id.privacy_line)
        val changePasswordLine = fragmentView.findViewById<LinearLayout>(R.id.change_password_line)
        val deleteAccountLine = fragmentView.findViewById<LinearLayout>(R.id.delete_account_line)

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
                    birthDateField.text = getString(
                        R.string.date_with_placeholders,
                        resDay,
                        resMonth + 1,
                        resYear)
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        privacyLine.setOnClickListener {
            FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                PrivacySettings()
            )
        }

        blacklistLine.setOnClickListener {
            FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                BlackListFragment()
            )
        }

        changePasswordLine.setOnClickListener {
            val changePasswordFragment = ChangePasswordFragment()
            changePasswordFragment.show(parentFragmentManager, ChangePasswordFragment.TAG)
        }

        exitLinearLayout.setOnClickListener {
            val exitAccountFragment = ExitAccountFragment()
            exitAccountFragment.show(parentFragmentManager, ExitAccountFragment.TAG)
        }

        deleteAccountLine.setOnClickListener {
            val deleteAccountFragment = DeleteAccountFragment()
            deleteAccountFragment.show(parentFragmentManager, DeleteAccountFragment.TAG)
        }
        return fragmentView
    }

}