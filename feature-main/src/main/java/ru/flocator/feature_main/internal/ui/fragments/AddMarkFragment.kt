package ru.flocator.feature_main.internal.ui.fragments

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView.*
import com.google.android.material.snackbar.Snackbar
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.section.MainSection
import ru.flocator.data.models.location.Coordinates
import ru.flocator.design.SnackbarComposer
import ru.flocator.design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.feature_main.R
import ru.flocator.feature_main.databinding.FragmentAddMarkBinding
import ru.flocator.feature_main.internal.core.contractions.AddMarkContractions
import ru.flocator.feature_main.internal.data.model.carousel.CarouselEditableItemState
import ru.flocator.feature_main.internal.data.model.fragment.AddMarkFragmentState
import ru.flocator.feature_main.internal.data.model.mark.AddMarkDto
import ru.flocator.feature_main.internal.core.di.DaggerMainComponent
import ru.flocator.feature_main.internal.ui.adapters.add_mark.EditablePhotoRecyclerViewAdapter
import ru.flocator.feature_main.internal.ui.view_models.AddMarkFragmentViewModel
import javax.inject.Inject

internal class AddMarkFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), MainSection {
    private var _binding: FragmentAddMarkBinding? = null
    private val binding: FragmentAddMarkBinding
        get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var addMarkFragmentViewModel: AddMarkFragmentViewModel

    private lateinit var carouselAdapter: EditablePhotoRecyclerViewAdapter
    private lateinit var photoAddLauncher: ActivityResultLauncher<String>

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerMainComponent.builder()
            .mainDependencies(findDependencies())
            .navController(findNavController())
            .build()
            .inject(this)

        addMarkFragmentViewModel =
            ViewModelProvider(this, viewModelFactory)[AddMarkFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_mark, container, false)

        _binding = FragmentAddMarkBinding.bind(view)

        photoAddLauncher =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { result ->
                addMarkFragmentViewModel.updatePhotosLiveData(result)
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
            addMarkFragmentViewModel.removeItems()
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
                addMarkFragmentViewModel.saveMark(
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

        addMarkFragmentViewModel.addressLiveData.observe(viewLifecycleOwner, this::onAddressUpdated)

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
            requireArguments().getDouble(AddMarkContractions.LATITUDE)
        val longitude =
            requireArguments().getDouble(AddMarkContractions.LONGITUDE)

        addMarkFragmentViewModel.updateUserPoint(
            Coordinates(
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
            EditablePhotoRecyclerViewAdapter { uri, b ->
                addMarkFragmentViewModel.toggleItem(
                    uri,
                    b
                )
            }
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
        addMarkFragmentViewModel.carouselLiveData.observe(
            this,
            this::onCarouselStateChangedCallback
        )
        addMarkFragmentViewModel.fragmentStateLiveData.observe(
            this,
            this::onFragmentStateChangedCallback
        )
    }

    private fun onCarouselStateChangedCallback(value: List<CarouselEditableItemState>) {
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
            if (addMarkFragmentViewModel.isAnyTaken()) {
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
            is AddMarkFragmentState.Failed -> setFailed()
        }
    }

    private fun setFailed() {
        SnackbarComposer.composeDesignedSnackbar(
            binding.root,
            getString(R.string.saving_error)
        )
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
        if (addMarkFragmentViewModel.addressLiveData.value == null) {
            throw IllegalStateException()
        }
        return AddMarkDto(
            addMarkFragmentViewModel.userPoint,
            binding.markText.text.toString(),
            binding.isPublicCheckBox.isChecked,
            addMarkFragmentViewModel.addressLiveData.value!!
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
            requireContext().resources.getDimensionPixelSize(ru.flocator.design.R.dimen.margin_between_btns),
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
            requireContext().resources.getDimensionPixelSize(ru.flocator.design.R.dimen.margin_between_btns)
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
            ContextCompat.getColorStateList(requireContext(), ru.flocator.design.R.color.white)
        binding.removePhotoBtn.backgroundTintList =
            ContextCompat.getColorStateList(
                requireContext(),
                ru.flocator.design.R.color.danger
            )
        binding.removePhotoBtn.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                ru.flocator.design.R.color.white
            )
        )
    }

    private fun disableRemovePhotoButton() {
        binding.removePhotoBtn.isEnabled = false
        binding.removePhotoBtn.iconTint =
            ContextCompat.getColorStateList(
                requireContext(),
                ru.flocator.design.R.color.danger
            )
        binding.removePhotoBtn.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), ru.flocator.design.R.color.white)
        binding.removePhotoBtn.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                ru.flocator.design.R.color.danger
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