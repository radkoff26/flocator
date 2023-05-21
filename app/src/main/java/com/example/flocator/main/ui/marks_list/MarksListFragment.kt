package com.example.flocator.main.ui.marks_list

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
import com.example.flocator.R
import com.example.flocator.common.cache.runtime.PhotoState
import com.example.flocator.common.fragments.ResponsiveBottomSheetDialogFragment
import com.example.flocator.common.utils.DistanceUtils
import com.example.flocator.databinding.FragmentMarksListBinding
import com.example.flocator.main.config.BundleArgumentsContraction
import com.example.flocator.main.data.Photo
import com.example.flocator.main.models.dto.MarkDto
import com.example.flocator.main.ui.main.data.PointDto
import com.example.flocator.main.ui.marks_list.adapters.MarksListRecyclerViewAdapter
import com.example.flocator.main.ui.marks_list.data.ListMarkDto
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MarksListFragment :
    ResponsiveBottomSheetDialogFragment(WIDTH_RATION_PORTRAIT, WIDTH_RATION_LANDSCAPE) {
    private var _binding: FragmentMarksListBinding? = null
    private val binding: FragmentMarksListBinding
        get() = _binding!!

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
                BundleArgumentsContraction.MarksListFragmentArguments.USER_POINT
            )
            ||
            !requireArguments().containsKey(
                BundleArgumentsContraction.MarksListFragmentArguments.MARKS
            )
        ) {
            closeDown()
            return
        }
        val userPoint = requireArguments().getSerializable(
            BundleArgumentsContraction.MarksListFragmentArguments.USER_POINT
        ) as PointDto
        val marks = requireArguments().getSerializable(
            BundleArgumentsContraction.MarksListFragmentArguments.MARKS
        ) as ArrayList<MarkDto>
        binding.marksCount.text = resources.getString(R.string.marks_count, marks.size)
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
                        Point(
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
        )
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