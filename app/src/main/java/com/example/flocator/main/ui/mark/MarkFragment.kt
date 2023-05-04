package com.example.flocator.main.ui.mark

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.flocator.R
import com.example.flocator.common.repository.MainRepository
import com.example.flocator.common.storage.db.entities.MarkPhoto
import com.example.flocator.common.storage.db.entities.MarkWithPhotos
import com.example.flocator.databinding.FragmentMarkBinding
import com.example.flocator.main.MainSection
import com.example.flocator.main.config.BundleArgumentsContraction
import com.example.flocator.main.ui.mark.adapters.MarkPhotoCarouselAdapter
import com.example.flocator.main.ui.mark.data.CarouselPhotoState
import com.example.flocator.main.ui.mark.data.MarkFragmentState
import com.example.flocator.main.ui.mark.data.UserNameDto
import com.example.flocator.main.ui.photo.PhotoPagerFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MarkFragment : BottomSheetDialogFragment(), MainSection {
    private var _binding: FragmentMarkBinding? = null
    private val binding
        get() = _binding!!

    private var carouselAdapter: MarkPhotoCarouselAdapter? = null

    @Inject
    lateinit var repository: MainRepository

    private lateinit var markFragmentViewModel: MarkFragmentViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setContentView(R.layout.fragment_mark)

        dialog.setOnShowListener {
            val view = (it as BottomSheetDialog).findViewById<LinearLayout>(R.id.bs)
                ?: return@setOnShowListener

            expandBottomSheet(view)
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val markId =
            requireArguments().getLong(BundleArgumentsContraction.MarkFragmentArguments.MARK_ID)
        val userId =
            requireArguments().getLong(BundleArgumentsContraction.MarkFragmentArguments.USER_ID)
        markFragmentViewModel = MarkFragmentViewModel(
            repository,
            markId,
            userId
        )
        return inflater.inflate(R.layout.fragment_mark, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMarkBinding.bind(view)

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
        markFragmentViewModel.photosStateLiveData.observe(viewLifecycleOwner, this::onUpdatePhotos)
        markFragmentViewModel.markLiveData.observe(viewLifecycleOwner, this::onUpdateMarkData)
        markFragmentViewModel.markFragmentStateLiveData.observe(
            viewLifecycleOwner,
            this::onUpdateFragmentState
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openPhotoPager(position: Int) {
        val photoPagerFragment = PhotoPagerFragment()
        val bundle = Bundle()
        bundle.putInt(BundleArgumentsContraction.PhotoPagerFragment.POSITION, position)
        bundle.putStringArrayList(
            BundleArgumentsContraction.PhotoPagerFragment.URI_LIST,
            ArrayList(markFragmentViewModel.markLiveData.value!!.photos.map(MarkPhoto::uri))
        )
        photoPagerFragment.arguments = bundle
        photoPagerFragment.show(requireActivity().supportFragmentManager, TAG)
    }

    private fun expandBottomSheet(bottomSheetView: View) {
        val behavior = BottomSheetBehavior.from(bottomSheetView)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
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
    }

    private fun setLoadedState() {
        binding.content.visibility = VISIBLE
        binding.retryFragmentButton.visibility = GONE
        binding.loader.visibility = GONE
        binding.loader.stopAnimation()
    }

    private fun setFailureState() {
        binding.content.visibility = GONE
        binding.retryFragmentButton.visibility = VISIBLE
        binding.loader.visibility = GONE
        binding.loader.stopAnimation()
    }

    private fun loadPhoto(position: Int) {
        markFragmentViewModel.loadPhotoByPosition(position)
    }

    private fun onUpdatePhotos(value: List<CarouselPhotoState>?) {
        if (value == null) {
            return
        }
        carouselAdapter?.updatePhotos(value)
    }

    private fun onUpdateMarkData(value: MarkWithPhotos?) {
        if (value == null) {
            return
        }
        // Switch title in case of user's mark
        binding.address.text = value.mark.place

        if (carouselAdapter == null) {
            carouselAdapter =
                MarkPhotoCarouselAdapter(value.photos.size, this::loadPhoto, this::openPhotoPager)
            binding.photoCarousel.adapter = carouselAdapter
            val itemDecoration = DividerItemDecoration(requireContext(), RecyclerView.HORIZONTAL)
            itemDecoration.setDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.rv_divider
                )!!
            )
            binding.photoCarousel.addItemDecoration(itemDecoration)
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
    private fun onUpdateUserData(value: UserNameDto?) {
        if (value == null) {
            return
        }
        binding.userName.text = "${value.firstName} ${value.lastName}"
    }

    companion object {
        const val TAG = "Mark Fragment"
    }
}