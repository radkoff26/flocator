package ru.flocator.app.add_mark.ui

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView.*
import ru.flocator.app.R
import ru.flocator.core_design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.app.databinding.FragmentAddMarkBinding
import ru.flocator.app.add_mark.adapters.CarouselRecyclerViewAdapter
import ru.flocator.core_dto.mark.AddMarkDto
import ru.flocator.app.add_mark.domain.fragment.AddMarkFragmentState
import ru.flocator.app.add_mark.domain.carousel.CarouselItemState
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.flocator.app.add_mark.view_models.AddMarkFragmentViewModel

@AndroidEntryPoint
class AddMarkFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), ru.flocator.core_sections.MainSection {
    private var _binding: FragmentAddMarkBinding? = null
    private val binding: FragmentAddMarkBinding
        get() = _binding!!

    private val viewModel: AddMarkFragmentViewModel by viewModels()

    private lateinit var carouselAdapter: CarouselRecyclerViewAdapter
    private lateinit var photoAddLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_mark, container, false)

        _binding = FragmentAddMarkBinding.bind(view)

        photoAddLauncher =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { result ->
                viewModel.updatePhotosLiveData(result)
            }

        return view
    }

    override fun getCoordinatorLayout(): CoordinatorLayout = binding.coordinator

    override fun getBottomSheetScrollView(): NestedScrollView = binding.bs

    override fun getInnerLayout(): ViewGroup = binding.content

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addPhotoBtn.setOnClickListener {
            launchPhotoPicker()
        }

        binding.cameraPlaceholder.setOnClickListener {
            launchPhotoPicker()
        }

        binding.removePhotoBtn.setOnClickListener {
            viewModel.removeItems()
        }

        binding.saveMarkBtn.setOnClickListener {
            val photos = carouselAdapter.getSetOfPhotos()
            if (photos.isEmpty()) {
                Snackbar.make(
                    binding.root,
                    resources.getString(R.string.no_photo_chosen),
                    Snackbar.LENGTH_LONG
                ).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
                return@setOnClickListener
            }
            try {
                viewModel.saveMark(
                    prepareAndGetMark(),
                    photos
                )
            } catch (e: IllegalStateException) {
                Snackbar.make(
                    binding.root,
                    resources.getString(R.string.no_address_available),
                    Snackbar.LENGTH_LONG
                ).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
            }
        }

        binding.cancelMarkBtn.setOnClickListener {
            dismiss()
        }

        viewModel.addressLiveData.observe(viewLifecycleOwner, this::onAddressUpdated)

        adjustRecyclerView()

        extractCurrentLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onAddressUpdated(value: String?) {
        if (value != null) {
            binding.address.text = value
        }
    }

    private fun extractCurrentLocation() {
        val latitude =
            requireArguments().getDouble(ru.flocator.core_contractions.BundleArgumentsContraction.AddMarkFragmentArguments.LATITUDE)
        val longitude =
            requireArguments().getDouble(ru.flocator.core_contractions.BundleArgumentsContraction.AddMarkFragmentArguments.LONGITUDE)

        viewModel.updateUserPoint(
            LatLng(
                latitude,
                longitude
            )
        )
    }

    private fun launchPhotoPicker() {
        photoAddLauncher.launch("image/*")
    }

    private fun adjustRecyclerView() {
        // Assign adapter to RecyclerView
        carouselAdapter =
            CarouselRecyclerViewAdapter { uri, b -> viewModel.toggleItem(uri, b) }
        binding.photoCarousel.adapter = carouselAdapter

        // Add spaces between items of RecyclerView
        val itemDecoration = DividerItemDecoration(requireContext(), HORIZONTAL)
        itemDecoration.setDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.big_whitespace_rv_divider
            )!!
        )
        binding.photoCarousel.addItemDecoration(itemDecoration)

        // Set observers to LiveData
        viewModel.carouselLiveData.observe(
            this,
            this::onCarouselStateChangedCallback
        )
        viewModel.fragmentStateLiveData.observe(
            this,
            this::onFragmentStateChangedCallback
        )
    }

    private fun onCarouselStateChangedCallback(value: List<CarouselItemState>) {
        val isEmpty = value.isEmpty()
        toggleOnStateEmptinessChange(isEmpty)
        if (isEmpty) {
            if (binding.removePhotoBtn.visibility != GONE) {
                animateRemovePhotoButtonOut()
            }
        } else {
            if (binding.removePhotoBtn.visibility == GONE) {
                animateRemovePhotoButtonIn()
            }
            if (viewModel.isAnyTaken()) {
                enableRemovePhotoButton()
            } else {
                disableRemovePhotoButton()
            }
        }
        carouselAdapter.updateData(value)
    }

    private fun onFragmentStateChangedCallback(value: AddMarkFragmentState) {
        when (value) {
            is AddMarkFragmentState.Editing -> setEditing()
            is AddMarkFragmentState.Saving -> setSaving()
            is AddMarkFragmentState.Saved -> dismiss()
            is AddMarkFragmentState.Failed -> setFailed(value.cause)
        }
    }

    private fun setFailed(throwable: Throwable) {
        Toast.makeText(
            requireContext(),
            "Ошибка во время сохранения: ${throwable.message}",
            Toast.LENGTH_SHORT
        ).show()
        setEditing()
    }

    private fun setEditing() {
        binding.loader.stopAnimation()
        binding.loader.visibility = GONE
        binding.buttons.visibility = VISIBLE
    }

    private fun setSaving() {
        binding.buttons.visibility = GONE
        binding.loader.visibility = VISIBLE
        binding.loader.startAnimation()
    }

    private fun prepareAndGetMark(): AddMarkDto {
        if (viewModel.addressLiveData.value == null) {
            throw IllegalStateException()
        }
        return AddMarkDto(
            0,
            viewModel.userPoint,
            binding.markText.text.toString(),
            binding.isPublicCheckBox.isChecked,
            viewModel.addressLiveData.value!!
        )
    }

    private fun animateRemovePhotoButtonOut() {
        (binding.removePhotoBtn.layoutParams as LinearLayout.LayoutParams).weight = 1F
        val animatorSet = getAnimator()
        animatorSet.doOnEnd {
            binding.removePhotoBtn.visibility = GONE
        }
        animatorSet.start()
    }

    private fun animateRemovePhotoButtonIn() {
        binding.removePhotoBtn.visibility = VISIBLE
        (binding.removePhotoBtn.layoutParams as LinearLayout.LayoutParams).weight = 0F
        val animatorSet = getAnimatorReversed()
        animatorSet.start()
    }

    private fun getAnimator(): AnimatorSet {
        val weightValueAnimator = ValueAnimator.ofFloat(1F, 0F)
        weightValueAnimator.addUpdateListener {
            (binding.removePhotoBtn.layoutParams as LinearLayout.LayoutParams).weight =
                it.animatedValue as Float
            (binding.addPhotoBtn.layoutParams as LinearLayout.LayoutParams).weight =
                2F - (it.animatedValue as Float)
            binding.removePhotoBtn.requestLayout()
            binding.addPhotoBtn.requestLayout()
        }
        val marginStartAnimator = ValueAnimator.ofInt(
            requireContext().resources.getDimensionPixelSize(ru.flocator.core_design.R.dimen.margin_between_btns),
            0
        )
        marginStartAnimator.addUpdateListener {
            val layoutParams = binding.removePhotoBtn.layoutParams as LinearLayout.LayoutParams
            layoutParams.marginStart = it.animatedValue as Int
            binding.removePhotoBtn.layoutParams = layoutParams
            binding.removePhotoBtn.requestLayout()
        }
        val animationSet = AnimatorSet()
        animationSet.duration = 600
        animationSet.playTogether(weightValueAnimator, marginStartAnimator)
        return animationSet
    }

    private fun getAnimatorReversed(): AnimatorSet {
        val weightValueAnimator = ValueAnimator.ofFloat(0F, 1F)
        weightValueAnimator.addUpdateListener {
            (binding.removePhotoBtn.layoutParams as LinearLayout.LayoutParams).weight =
                it.animatedValue as Float
            (binding.addPhotoBtn.layoutParams as LinearLayout.LayoutParams).weight =
                2F - (it.animatedValue as Float)
            binding.removePhotoBtn.requestLayout()
            binding.addPhotoBtn.requestLayout()
        }
        val marginStartAnimator = ValueAnimator.ofInt(
            0,
            requireContext().resources.getDimensionPixelSize(ru.flocator.core_design.R.dimen.margin_between_btns)
        )
        marginStartAnimator.addUpdateListener {
            val layoutParams = binding.removePhotoBtn.layoutParams as LinearLayout.LayoutParams
            layoutParams.marginStart = it.animatedValue as Int
            binding.removePhotoBtn.layoutParams = layoutParams
            binding.removePhotoBtn.requestLayout()
        }
        val animationSet = AnimatorSet()
        animationSet.duration = 600
        animationSet.playTogether(weightValueAnimator, marginStartAnimator)
        return animationSet
    }

    private fun enableRemovePhotoButton() {
        binding.removePhotoBtn.isEnabled = true
        binding.removePhotoBtn.iconTint =
            ContextCompat.getColorStateList(requireContext(), ru.flocator.core_design.R.color.white)
        binding.removePhotoBtn.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), ru.flocator.core_design.R.color.danger)
        binding.removePhotoBtn.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                ru.flocator.core_design.R.color.white
            )
        )
    }

    private fun disableRemovePhotoButton() {
        binding.removePhotoBtn.isEnabled = false
        binding.removePhotoBtn.iconTint =
            ContextCompat.getColorStateList(requireContext(), ru.flocator.core_design.R.color.danger)
        binding.removePhotoBtn.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), ru.flocator.core_design.R.color.white)
        binding.removePhotoBtn.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                ru.flocator.core_design.R.color.danger
            )
        )
    }

    private fun toggleOnStateEmptinessChange(isEmpty: Boolean) {
        if (isEmpty) {
            binding.cameraPlaceholder.visibility = VISIBLE
            binding.photoCarousel.visibility = GONE
        } else {
            binding.cameraPlaceholder.visibility = GONE
            binding.photoCarousel.visibility = VISIBLE
        }
    }

    companion object {
        const val TAG = "Add Mark Fragment"
        const val BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO = 0.9
        const val BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO = 0.8
    }
}