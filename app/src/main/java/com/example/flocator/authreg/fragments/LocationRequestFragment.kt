package com.example.flocator.authreg.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.flocator.authreg.FragmentUtil
import com.example.flocator.databinding.FragmentLocationRequestBinding
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

class LocationRequestFragment : Fragment() {
    private lateinit var binding: FragmentLocationRequestBinding
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationRequestBinding.inflate(inflater, container, false)

        binding.allowBtn.setOnClickListener {
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

                if (fineLocationGranted && coarseLocationGranted) {
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    FragmentUtil.replaceFragment(transaction, AuthFragment())
                } else {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Необходимо разрешение")
                    builder.setMessage("Пожалуйста, предоставьте разрешение на доступ к местоположению в настройках приложения.")
                    builder.setPositiveButton("Перейти в настройки") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", requireActivity().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    builder.setNegativeButton("Отмена", null)
                    builder.show()
                }
            }
    }
}
