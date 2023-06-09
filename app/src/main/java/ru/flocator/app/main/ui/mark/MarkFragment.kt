package ru.flocator.app.main.ui.mark

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
import com.example.flocator.R
import ru.flocator.app.common.cache.runtime.PhotoState
import ru.flocator.app.common.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.app.common.repository.MainRepository
import ru.flocator.app.common.storage.db.entities.MarkPhoto
import ru.flocator.app.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.databinding.FragmentMarkBinding
import ru.flocator.app.main.MainSection
import ru.flocator.app.main.config.BundleArgumentsContraction
import ru.flocator.app.main.models.dto.UsernameDto
import ru.flocator.app.main.ui.mark.adapters.MarkPhotoCarouselAdapter
import ru.flocator.app.main.ui.mark.data.MarkFragmentState
import ru.flocator.app.main.ui.photo.PhotoPagerFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MarkFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), MainSection {
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
            requireArguments().getLong(BundleArgumentsContraction.MarkFragmentArguments.MARK_ID)
        val userId =
            requireArguments().getLong(BundleArgumentsContraction.MarkFragmentArguments.USER_ID)

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
        bundle.putInt(BundleArgumentsContraction.PhotoPagerFragmentArguments.POSITION, position)
        bundle.putStringArrayList(
            BundleArgumentsContraction.PhotoPagerFragmentArguments.URI_LIST,
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
    private fun onUpdateUserData(value: UsernameDto?) {
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