package com.example.flocator.main.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.flocator.R
import com.example.flocator.common.connection.live_data.ConnectionLiveData
import com.example.flocator.common.polling.TimeoutPoller
import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.common.storage.db.entities.User
import com.example.flocator.common.storage.store.user.info.UserInfo
import com.example.flocator.common.utils.FragmentNavigationUtils
import com.example.flocator.common.utils.LocationUtils
import com.example.flocator.community.fragments.ProfileFragment
import com.example.flocator.databinding.FragmentMainBinding
import com.example.flocator.main.MainSection
import com.example.flocator.main.config.BundleArgumentsContraction
import com.example.flocator.main.ui.add_mark.AddMarkFragment
import com.example.flocator.main.ui.main.data.LatLngDto
import com.example.flocator.main.ui.map.ui.FLocatorMapFragment
import com.example.flocator.main.ui.mark.MarkFragment
import com.example.flocator.main.ui.marks_list.MarksListFragment
import com.example.flocator.main.utils.ViewUtils.Companion.dpToPx
import com.example.flocator.settings.SettingsFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment(), MainSection {
    // Binding
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding!!

    // ViewModel
    private val viewModel: MainFragmentViewModel by viewModels()

    // Connection
    @Inject
    lateinit var connectionLiveData: ConnectionLiveData

    // Rx
    private val compositeDisposable = CompositeDisposable()

    // Handlers
    private lateinit var userLocationTimeoutPoller: TimeoutPoller
    private lateinit var userInfoTimeoutPoller: TimeoutPoller
    private lateinit var marksFetchingTimeoutPoller: TimeoutPoller
    private lateinit var friendsFetchingTimeoutPoller: TimeoutPoller

    // Locations
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val isInitializedCamera = AtomicBoolean(false)

    private lateinit var mapFragment: FLocatorMapFragment

    // Fragment lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as FLocatorMapFragment

        mapFragment.initialize(
            viewModel::loadPhoto,
            null,
            { id ->
                if (viewModel.userInfoLiveData.value != null) {
                    val markFragment = MarkFragment().apply {
                        arguments = Bundle().apply {
                            putLong(
                                BundleArgumentsContraction.MarkFragmentArguments.MARK_ID,
                                id
                            )
                            putLong(
                                BundleArgumentsContraction.MarkFragmentArguments.USER_ID,
                                viewModel.userInfoLiveData.value!!.userId
                            )
                        }
                    }
                    markFragment.show(
                        requireActivity().supportFragmentManager,
                        TAG
                    )
                }
            },
            { marks ->
                if (viewModel.userLocationLiveData.value != null) {
                    val marksListFragment = MarksListFragment().apply {
                        arguments = Bundle().apply {
                            val markDtoList = ArrayList(
                                marks.map(MarkWithPhotos::toMarkDto)
                            )
                            putSerializable(
                                BundleArgumentsContraction.MarksListFragmentArguments.MARKS,
                                markDtoList
                            )
                            val userPoint = viewModel.userLocationLiveData.value!!
                            putSerializable(
                                BundleArgumentsContraction.MarksListFragmentArguments.USER_POINT,
                                LatLngDto(
                                    userPoint.latitude,
                                    userPoint.longitude
                                )
                            )
                        }
                    }
                    marksListFragment.show(
                        requireActivity().supportFragmentManager,
                        MarksListFragment.TAG
                    )
                }
            }
        )

        locateMapToUser()

        binding.openAddMarkFragment.setOnClickListener {
            if (viewModel.userLocationLiveData.value == null) {
                Snackbar.make(it, "Получение геолокации...", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val point = viewModel.userLocationLiveData.value!!

            val args = Bundle()

            args.putDouble(
                BundleArgumentsContraction.AddMarkFragmentArguments.LATITUDE,
                point.latitude
            )

            args.putDouble(
                BundleArgumentsContraction.AddMarkFragmentArguments.LONGITUDE,
                point.longitude
            )

            val addMarkFragment = AddMarkFragment()
            addMarkFragment.arguments = args
            addMarkFragment.show(requireActivity().supportFragmentManager, AddMarkFragment.TAG)
        }

        binding.communityBtn.setOnClickListener {
            FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                ProfileFragment()
            )
        }

        binding.settingsBtn.setOnClickListener {
            FragmentNavigationUtils.openFragment(
                requireActivity().supportFragmentManager,
                SettingsFragment()
            )
        }

        binding.targetBtn.setOnClickListener {
            if (viewModel.userInfoLiveData.value != null) {
                val userId = viewModel.userInfoLiveData.value!!.userId
                mapFragment.followUser(userId)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeWidths()

        viewModel.userLocationLiveData.observe(
            viewLifecycleOwner,
            this::onUserLocationChanged
        )
        viewModel.friendsLiveData.observe(
            viewLifecycleOwner,
            this::onFriendsStateChanged
        )
        viewModel.marksLiveData.observe(
            viewLifecycleOwner,
            this::onMarksStateChanged
        )
        viewModel.userInfoLiveData.observe(
            viewLifecycleOwner,
            this::onUserInfoChanged
        )
        connectionLiveData.observe(
            viewLifecycleOwner,
            this::onConnectionChanged
        )

        viewModel.requestInitialLoading()

        userLocationTimeoutPoller = TimeoutPoller(
            viewLifecycleOwner,
            TIMEOUT_TO_POLL_LOCATION_POST,
            { emitter ->
                LocationUtils.getCurrentLocation(requireContext(), fusedLocationProviderClient) {
                    if (it != null) {
                        viewModel.updateUserLocation(
                            LatLng(
                                it.latitude,
                                it.longitude
                            )
                        )
                        viewModel.postLocation()
                    }
                    emitter.emit()
                }
            }
        )

        userInfoTimeoutPoller = TimeoutPoller(
            viewLifecycleOwner,
            TIMEOUT_TO_FETCH_USER_INFO,
            viewModel::fetchUserInfo
        )

        friendsFetchingTimeoutPoller = TimeoutPoller(
            viewLifecycleOwner,
            TIMEOUT_TO_FETCH_FRIENDS,
            viewModel::fetchFriends
        )

        marksFetchingTimeoutPoller = TimeoutPoller(
            viewLifecycleOwner,
            TIMEOUT_TO_FETCH_MARKS,
            viewModel::fetchMarks
        )
    }

    private fun locateMapToUser() {
        val userLocation = viewModel.userLocationLiveData.value

        if (userLocation != null) {
            mapFragment.moveCameraTo(userLocation)
        }
    }

    // TODO: move to MainActivity
    private fun onConnectionChanged(value: Boolean) {
        if (value) {
            // Has connection
            Snackbar.make(
                binding.root,
                resources.getString(R.string.return_to_connection),
                Snackbar.LENGTH_LONG
            ).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
            viewModel.goOnlineAsUser()
        } else {
            // No connection
            Snackbar.make(
                binding.root,
                resources.getString(R.string.no_connection),
                Snackbar.LENGTH_LONG
            ).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
        }
    }

    private fun initializeWidths() {
        val mapView = mapFragment.requireView()
        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewModel.setWidths(
                    mapView.width.toFloat(),
                    dpToPx(56, requireContext()).toFloat()
                )
                mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        mapView.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    override fun onStart() {
        super.onStart()
        viewModel.goOnlineAsUser()
    }

    override fun onPause() {
        super.onPause()
        viewModel.goOfflineAsUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        compositeDisposable.dispose()
    }

    // Observer callbacks
    private fun onUserLocationChanged(latLng: LatLng?) {
        if (latLng == null || viewModel.userInfoLiveData.value == null) {
            return
        }
        if (!isInitializedCamera.get()) {
            mapFragment.moveCameraTo(latLng)
            isInitializedCamera.set(true)
        }
        mapFragment.updateUserLocation(latLng)
    }

    private fun onUserInfoChanged(value: UserInfo?) {
        if (viewModel.userLocationLiveData.value == null || value == null) {
            return
        }
        mapFragment.submitUser(value)
    }

    private fun onFriendsStateChanged(value: List<User>) {
        mapFragment.submitFriends(value)
    }

    private fun onMarksStateChanged(value: List<MarkWithPhotos>) {
        mapFragment.submitMarks(value)
    }

    companion object {
        const val TAG = "Main Fragment"
        const val TIMEOUT_TO_POLL_LOCATION_POST = 3000L
        const val TIMEOUT_TO_FETCH_FRIENDS = 5000L
        const val TIMEOUT_TO_FETCH_MARKS = 10000L
        const val TIMEOUT_TO_FETCH_USER_INFO = 10000L
    }
}
