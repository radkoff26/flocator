package ru.flocator.feature_main.internal.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.cache.runtime.PhotoState
import ru.flocator.core_design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.core_dto.location.LatLngDto
import ru.flocator.core_dto.mark.MarkDto
import ru.flocator.core_utils.DistanceUtils
import ru.flocator.feature_main.R
import ru.flocator.feature_main.databinding.FragmentMarksListBinding
import ru.flocator.feature_main.internal.domain.photo.Photo
import ru.flocator.feature_main.internal.contractions.MarkContractions
import ru.flocator.feature_main.internal.adapters.marks_list.MarksListRecyclerViewAdapter
import ru.flocator.feature_main.internal.contractions.MarksListContractions
import ru.flocator.feature_main.internal.domain.dto.ListMarkDto
import ru.flocator.feature_main.internal.view_models.MarksListFragmentViewModel

internal class MarksListFragment :
    ResponsiveBottomSheetDialogFragment(WIDTH_RATION_PORTRAIT, WIDTH_RATION_LANDSCAPE) {
    private var _binding: FragmentMarksListBinding? = null
    private val binding: FragmentMarksListBinding
        get() = _binding!!

    private val compositeDisposable = CompositeDisposable()

    private val viewModel: MarksListFragmentViewModel by viewModels()

    private lateinit var adapter: MarksListRecyclerViewAdapter

    override fun getCoordinatorLayout(): CoordinatorLayout = binding.coordinator

    override fun getBottomSheetScrollView(): NestedScrollView = binding.bottomSheet

    override fun getInnerLayout(): ViewGroup = binding.container

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
        viewModel.marksListLiveData.observe(viewLifecycleOwner, this::onMarksUpdate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onMarksUpdate(value: List<ListMarkDto>?) {
        if (value == null || !this::adapter.isInitialized) {
            return
        }
        adapter.updateMarks(value)
    }

    @Suppress("UNCHECKED_CAST")
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
        ) as LatLngDto
        val marks = requireArguments().getSerializable(
            MarksListContractions.MARKS
        ) as ArrayList<MarkDto>
        binding.marksCount.text =
            resources.getString(R.string.marks_count, marks.size)
        // If data has been already loaded into view model,
        // then it's very likely that its data will be more complete
        val listMarkDtoList = if (viewModel.marksListLiveData.value == null) {
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
                        markWithPhotos.mark.location
                    ),
                    markWithPhotos.photos.size
                )
            }.also {
                viewModel.submitMarks(it)
            }
        } else {
            viewModel.marksListLiveData.value!!
        }
        adapter = MarksListRecyclerViewAdapter(
            listMarkDtoList,
            viewModel::requestPhotoLoading,
            viewModel::requestUsernameLoading
        ) { markId ->
            compositeDisposable.add(
                viewModel.getUserId()
                    .subscribe(
                        { userId ->
                            val markFragment = MarkFragment().apply {
                                arguments = Bundle().apply {
                                    putLong(
                                        MarkContractions.MARK_ID,
                                        markId
                                    )
                                    putLong(
                                        MarkContractions.USER_ID,
                                        userId
                                    )
                                }
                            }
                            markFragment.show(
                                requireActivity().supportFragmentManager,
                                MarkFragment.TAG
                            )
                        },
                        {
                            Snackbar.make(
                                binding.root,
                                "Ошибка!",
                                Snackbar.LENGTH_SHORT
                            ).setAnimationMode(Snackbar.ANIMATION_MODE_FADE).show()
                            Log.e(TAG, "adjustRecyclerView: error while fetching user id", it)
                        }
                    )
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