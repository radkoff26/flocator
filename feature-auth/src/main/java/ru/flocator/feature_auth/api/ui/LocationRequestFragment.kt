package ru.flocator.feature_auth.api.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ru.flocator.core_controller.NavController
import ru.flocator.core_controller.findNavController
import ru.flocator.core_dependency.findDependencies
import ru.flocator.core_sections.AuthenticationSection
import ru.flocator.feature_auth.databinding.FragmentLocationRequestBinding
import ru.flocator.feature_auth.internal.di.DaggerAuthComponent
import javax.inject.Inject

class LocationRequestFragment : Fragment(), AuthenticationSection {
    private var _binding: FragmentLocationRequestBinding? = null
    private val binding: FragmentLocationRequestBinding
        get() = _binding!!
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var sharedPrefs: SharedPreferences

    private lateinit var controller: NavController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerAuthComponent.factory()
            .create(
                findDependencies(),
                findNavController()
            )
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationRequestBinding.inflate(inflater, container, false)

        binding.allowBtn.setOnClickListener {
            val dontShowAgain =
                sharedPrefs.getBoolean("location_permission_requested_dont_show_again", false)

            if (dontShowAgain) {
                if (hasLocationPermission(requireContext())) {
                    goToMainSection()
                } else {
                    showPermissionRationaleDialog()
                }
            } else {
                requestPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefs = requireActivity().getSharedPreferences(
            "LocationRequestFragmentPrefs", Context.MODE_PRIVATE
        )

        setupRequestPermissionsLauncher()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRequestPermissionsLauncher() {
        requestPermissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (hasLocationPermission(requireContext())) {
                    goToMainSection()
                } else if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || !shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    // Пользователь нажал "не показывать снова"
                    val editor = sharedPrefs.edit()
                    editor.putBoolean("location_permission_requested_dont_show_again", true)
                    editor.apply()
                }
            }
    }

    private fun showPermissionRationaleDialog() {
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

    private fun hasLocationPermission(context: Context): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationPermission && coarseLocationPermission
    }

    private fun goToMainSection() = controller.toMain().clearAll().commit()
}
