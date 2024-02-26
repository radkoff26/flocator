package ru.flocator.feature_main.internal.ui.fragments

import android.content.Context
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import ru.flocator.core.cache.runtime.data.PhotoState
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.section.MainSection
import ru.flocator.data.database.entities.MarkPhoto
import ru.flocator.data.database.entities.MarkWithPhotos
import ru.flocator.design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.feature_main.R
import ru.flocator.feature_main.databinding.FragmentMarkBinding
import ru.flocator.feature_main.internal.data.model.fragment.MarkFragmentState
import ru.flocator.feature_main.internal.data.model.user_name.UsernameDto
import ru.flocator.feature_main.internal.core.di.DaggerMainComponent
import ru.flocator.feature_main.internal.ui.adapters.mark.MarkPhotoRecyclerViewAdapter
import ru.flocator.feature_main.internal.ui.view_models.MarkFragmentViewModel
import ru.flocator.pager.api.PhotoPagerContractions
import ru.flocator.pager.api.ui.PhotoPagerFragment
import javax.inject.Inject

internal class MarkFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), MainSection {
    private var _binding: FragmentMarkBinding? = null
    private val binding
        get() = _binding!!

    private var carouselAdapter: MarkPhotoRecyclerViewAdapter? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var markFragmentViewModel: MarkFragmentViewModel

    private val markId: Long by lazy {
        requireArguments().getLong(Contraction.MARK_ID, -1).also {
            require(it != -1L) {
                "Argument has not been provided to fragment!"
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerMainComponent.builder()
            .mainDependencies(findDependencies())
            .navController(findNavController())
            .build()
            .inject(this)

        markFragmentViewModel =
            ViewModelProvider(this, viewModelFactory)[MarkFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mark, container, false)

        _binding = FragmentMarkBinding.bind(view)

        markFragmentViewModel.initialize(markId)

        return view
    }

    override fun getCoordinatorLayout(): CoordinatorLayout = binding.coordinator

    override fun getBottomSheetScrollView(): NestedScrollView = binding.bs

    override fun getInnerLayout(): ViewGroup = binding.linearContainer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.likeBtn.setOnClickListener {
            markFragmentViewModel.toggleLike()
        }

        binding.closeFragmentBtn.setOnClickListener {
            dismiss()
        }

        binding.retryFragmentButton.setOnRetryCallback {
            markFragmentViewModel.loadData()
        }

        markFragmentViewModel.userNameLiveData.observe(viewLifecycleOwner, this::onUpdateUserData)
        markFragmentViewModel.markLiveData.observe(viewLifecycleOwner, this::onUpdateMarkData)
        markFragmentViewModel.photosStateLiveData.observe(viewLifecycleOwner, this::onPhotosUpdated)
        markFragmentViewModel.markFragmentStateLiveData.observe(
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
        val photoPagerFragment =
            PhotoPagerFragment()
        val bundle = Bundle()
        bundle.putInt(
            PhotoPagerContractions.POSITION,
            position
        )
        bundle.putStringArrayList(
            PhotoPagerContractions.URI_LIST,
            ArrayList(markFragmentViewModel.markLiveData.value!!.photos.map(MarkPhoto::uri))
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
        markFragmentViewModel.loadPhotoByUri(uri)
    }

    private fun onUpdateMarkData(value: MarkWithPhotos?) {
        if (value == null) {
            return
        }
        // Switch title in case of user's mark
        binding.address.text = value.mark.place

        if (carouselAdapter == null) {
            carouselAdapter =
                MarkPhotoRecyclerViewAdapter(
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
            val photos = markFragmentViewModel.photosStateLiveData.value
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

    private fun onUpdateUserData(value: UsernameDto?) {
        if (value == null) {
            return
        }
        binding.userName.text =
            resources.getString(R.string.name_surname, value.firstName, value.lastName)
    }

    private object Contraction {
        const val MARK_ID = "MARK_ID"
    }

    companion object {
        const val TAG = "Mark Fragment"
        const val BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO = 0.9
        const val BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO = 0.8

        fun newInstance(markId: Long): MarkFragment {
            return MarkFragment().apply {
                arguments = Bundle().apply {
                    putLong(Contraction.MARK_ID, markId)
                }
            }
        }
    }
}