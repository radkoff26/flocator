package ru.flocator.feature_main.internal.mark.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import ru.flocator.cache.runtime.PhotoState
import ru.flocator.core_api.api.MainRepository
import ru.flocator.core_database.entities.MarkPhoto
import ru.flocator.core_database.entities.MarkWithPhotos
import ru.flocator.core_design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.core_photo_pager.api.domain.contractions.PhotoPagerContractions
import ru.flocator.core_photo_pager.api.ui.PhotoPagerFragment
import ru.flocator.feature_main.R
import ru.flocator.feature_main.databinding.FragmentMarkBinding
import ru.flocator.feature_main.internal.mark.adapters.MarkPhotoCarouselAdapter
import ru.flocator.feature_main.internal.mark.contractions.MarkContractions
import ru.flocator.feature_main.internal.mark.domain.fragment.MarkFragmentState
import ru.flocator.feature_main.internal.mark.view_models.MarkFragmentViewModel
import javax.inject.Inject

internal class MarkFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), ru.flocator.core_sections.MainSection {
    private var _binding: FragmentMarkBinding? = null
    private val binding
        get() = _binding!!

    private var carouselAdapter: MarkPhotoCarouselAdapter? = null

    @Inject
    lateinit var repository: MainRepository

    private val viewModel: MarkFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mark, container, false)

        _binding = FragmentMarkBinding.bind(view)

        val markId =
            requireArguments().getLong(MarkContractions.MARK_ID)
        val userId =
            requireArguments().getLong(MarkContractions.USER_ID)

        viewModel.initialize(markId, userId)

        return view
    }

    override fun getCoordinatorLayout(): CoordinatorLayout = binding.coordinator

    override fun getBottomSheetScrollView(): NestedScrollView = binding.bs

    override fun getInnerLayout(): ViewGroup = binding.linearContainer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.likeBtn.setOnClickListener {
            viewModel.toggleLike()
        }

        binding.closeFragmentBtn.setOnClickListener {
            dismiss()
        }

        binding.retryFragmentButton.setOnRetryCallback {
            viewModel.loadData()
        }

        viewModel.userNameLiveData.observe(viewLifecycleOwner, this::onUpdateUserData)
        viewModel.markLiveData.observe(viewLifecycleOwner, this::onUpdateMarkData)
        viewModel.photosStateLiveData.observe(viewLifecycleOwner, this::onPhotosUpdated)
        viewModel.markFragmentStateLiveData.observe(
            viewLifecycleOwner,
            this::onUpdateFragmentState
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onPhotosUpdated(value: LruCache<String, PhotoState>) {
        carouselAdapter?.updatePhotos(value.snapshot())
    }

    private fun openPhotoPager(position: Int) {
        val photoPagerFragment = PhotoPagerFragment()
        val bundle = Bundle()
        bundle.putInt(
            PhotoPagerContractions.POSITION,
            position
        )
        bundle.putStringArrayList(
            PhotoPagerContractions.URI_LIST,
            ArrayList(viewModel.markLiveData.value!!.photos.map(MarkPhoto::uri))
        )
        photoPagerFragment.arguments = bundle
        photoPagerFragment.show(requireActivity().supportFragmentManager, TAG)
    }

    private fun onUpdateFragmentState(value: MarkFragmentState) {
        when (value) {
            is MarkFragmentState.Loading -> setLoadingState()
            is MarkFragmentState.Loaded -> setLoadedState()
            is MarkFragmentState.Failed -> setFailureState()
        }
    }

    private fun setLoadingState() {
        binding.content.visibility = GONE
        binding.retryFragmentButton.visibility = GONE
        binding.loader.visibility = VISIBLE
        binding.loader.startAnimation()
        layoutBottomSheet()
    }

    private fun setLoadedState() {
        binding.content.visibility = VISIBLE
        binding.retryFragmentButton.visibility = GONE
        binding.loader.visibility = GONE
        binding.loader.stopAnimation()
        layoutBottomSheet()
    }

    private fun setFailureState() {
        binding.content.visibility = GONE
        binding.retryFragmentButton.visibility = VISIBLE
        binding.loader.visibility = GONE
        binding.loader.stopAnimation()
        layoutBottomSheet()
    }

    private fun loadPhoto(uri: String) {
        viewModel.loadPhotoByUri(uri)
    }

    private fun onUpdateMarkData(value: MarkWithPhotos?) {
        if (value == null) {
            return
        }
        // Switch title in case of user's mark
        binding.address.text = value.mark.place

        if (carouselAdapter == null) {
            carouselAdapter =
                MarkPhotoCarouselAdapter(
                    value.photos.size,
                    value.photos.map(MarkPhoto::uri),
                    this::loadPhoto,
                    this::openPhotoPager
                )
            binding.photoCarousel.adapter = carouselAdapter
            val itemDecoration = DividerItemDecoration(requireContext(), RecyclerView.HORIZONTAL)
            itemDecoration.setDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.big_whitespace_rv_divider
                )!!
            )
            binding.photoCarousel.addItemDecoration(itemDecoration)
            val photos = viewModel.photosStateLiveData.value
            if (photos != null && photos.size() == value.photos.size) {
                carouselAdapter!!.updatePhotos(photos.snapshot())
            }
        }

        if (value.mark.hasUserLiked) {
            binding.likeBtn.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.liked_image,
                    null
                )
            )
        } else {
            binding.likeBtn.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.unliked_image,
                    null
                )
            )
        }
        binding.likeCounter.text = value.mark.likesCount.toString()
        binding.markText.text = value.mark.text
    }

    @SuppressLint("SetTextI18n")
    private fun onUpdateUserData(value: ru.flocator.core_dto.user_name.UsernameDto?) {
        if (value == null) {
            return
        }
        binding.userName.text = "${value.firstName} ${value.lastName}"
    }

    companion object {
        const val TAG = "Mark Fragment"
        const val BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO = 0.9
        const val BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO = 0.8
    }
}