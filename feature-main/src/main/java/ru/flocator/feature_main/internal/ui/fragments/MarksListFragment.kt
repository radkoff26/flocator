package ru.flocator.feature_main.internal.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView.*
import com.google.android.gms.maps.model.LatLng
import ru.flocator.core.cache.runtime.data.PhotoState
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.section.MainSection
import ru.flocator.core.utils.DistanceUtils
import ru.flocator.data.models.location.Coordinates
import ru.flocator.design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.feature_main.R
import ru.flocator.feature_main.databinding.FragmentMarksListBinding
import ru.flocator.feature_main.internal.core.contractions.MarksListContractions
import ru.flocator.feature_main.internal.data.model.dto.ListMarkDto
import ru.flocator.feature_main.internal.data.model.mark.MarkDto
import ru.flocator.feature_main.internal.data.model.photo.Photo
import ru.flocator.feature_main.internal.core.di.DaggerMainComponent
import ru.flocator.feature_main.internal.ui.adapters.marks_list.MarksListRecyclerViewAdapter
import ru.flocator.feature_main.internal.ui.view_models.MarksListFragmentViewModel
import javax.inject.Inject

internal class MarksListFragment : ResponsiveBottomSheetDialogFragment(
    WIDTH_RATION_PORTRAIT,
    WIDTH_RATION_LANDSCAPE
), MainSection {
    private var _binding: FragmentMarksListBinding? = null
    private val binding: FragmentMarksListBinding
        get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var marksListFragmentViewModel: MarksListFragmentViewModel

    private lateinit var adapter: MarksListRecyclerViewAdapter

    override fun getCoordinatorLayout(): CoordinatorLayout = binding.coordinator

    override fun getBottomSheetScrollView(): NestedScrollView = binding.bottomSheet

    override fun getInnerLayout(): ViewGroup = binding.container

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerMainComponent.builder()
            .mainDependencies(findDependencies())
            .navController(findNavController())
            .build()
            .inject(this)

        marksListFragmentViewModel =
            ViewModelProvider(this, viewModelFactory)[MarksListFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_marks_list, container, false)

        _binding = FragmentMarksListBinding.bind(view)

        adjustRecyclerView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        marksListFragmentViewModel.marksListLiveData.observe(
            viewLifecycleOwner,
            this::onMarksUpdate
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun onMarksUpdate(value: List<ListMarkDto>?) {
        if (value == null || !this::adapter.isInitialized) {
            return
        }
        adapter.updateMarks(value)
    }

    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    private fun adjustRecyclerView() {
        if (
            arguments == null
            ||
            !requireArguments().containsKey(
                MarksListContractions.USER_POINT
            )
            ||
            !requireArguments().containsKey(
                MarksListContractions.MARKS
            )
        ) {
            closeDown()
            return
        }
        val userPoint = requireArguments().getSerializable(
            MarksListContractions.USER_POINT
        ) as Coordinates
        val marks = requireArguments().getSerializable(
            MarksListContractions.MARKS
        ) as ArrayList<MarkDto>
        binding.marksCount.text =
            resources.getString(R.string.marks_count, marks.size)
        // If data has been already loaded into view model,
        // then it's very likely that its data will be more complete
        val listMarkDtoList = if (marksListFragmentViewModel.marksListLiveData.value == null) {
            marks.map {
                val markWithPhotos = it.toMarkWithPhotos()
                val thumbnail = markWithPhotos.photos[0]
                ListMarkDto(
                    markWithPhotos.mark,
                    null,
                    Photo(
                        thumbnail.uri,
                        PhotoState.Loading
                    ),
                    DistanceUtils.distanceBetweenToString(
                        LatLng(
                            userPoint.latitude,
                            userPoint.longitude
                        ),
                        LatLng(
                            markWithPhotos.mark.location.latitude,
                            markWithPhotos.mark.location.longitude
                        )
                    ),
                    markWithPhotos.photos.size
                )
            }.also {
                marksListFragmentViewModel.submitMarks(it)
            }
        } else {
            marksListFragmentViewModel.marksListLiveData.value!!
        }
        adapter = MarksListRecyclerViewAdapter(
            listMarkDtoList,
            marksListFragmentViewModel::requestPhotoLoading,
            marksListFragmentViewModel::requestUsernameLoading
        ) { markId ->
            val markFragment = MarkFragment.newInstance(markId)
            markFragment.show(
                requireActivity().supportFragmentManager,
                MarkFragment.TAG
            )
        }
        val horizontalLineDecoration = DividerItemDecoration(
            context,
            VERTICAL
        )
        horizontalLineDecoration.setDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.line_rv_divider,
                null
            )!!
        )
        binding.marksRecyclerView.addItemDecoration(horizontalLineDecoration)
        binding.marksRecyclerView.adapter = adapter
    }

    private fun closeDown() {
        Log.e(TAG, "closeDown: arguments don't satisfy requirements!")
        dismiss()
    }

    companion object {
        const val TAG = "Marks List Fragment"
        const val WIDTH_RATION_PORTRAIT = 1.0
        const val WIDTH_RATION_LANDSCAPE = 0.9
    }
}