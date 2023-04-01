package com.example.flocator.main.ui.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView.*
import com.example.flocator.R
import com.example.flocator.databinding.FragmentAddMarkBinding
import com.example.flocator.main.data.AddMarkFragmentState
import com.example.flocator.main.ui.adapters.CarouselRecyclerViewAdapter
import com.example.flocator.main.data.CarouselItemState
import com.example.flocator.main.models.dto.MarkDto
import com.example.flocator.main.ui.view_models.AddMarkFragmentViewModel
import com.example.flocator.utils.FragmentNavigationUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yandex.mapkit.geometry.Point
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddMarkFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentAddMarkBinding? = null
    private val binding: FragmentAddMarkBinding
        get() = _binding!!

    private lateinit var carouselAdapter: CarouselRecyclerViewAdapter
    private lateinit var photoAddLauncher: ActivityResultLauncher<String>
    private val addMarkFragmentViewModel = AddMarkFragmentViewModel()
    private var valueAnimator: ValueAnimator? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setContentView(R.layout.fragment_add_mark)

        dialog.setOnShowListener {
            val view = (it as BottomSheetDialog).findViewById<LinearLayout>(R.id.bs)
                ?: return@setOnShowListener
            expandBottomSheet(view)
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        photoAddLauncher =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { result ->
                addMarkFragmentViewModel.updateLiveData(result)
            }

        return inflater.inflate(R.layout.fragment_add_mark, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        valueAnimator?.end()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        binding.coordinator.post {
            binding.bs.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            binding.coordinator.requestLayout()
        }
        val behavior = BottomSheetBehavior.from(binding.bs)
        behavior.isHideable = true
        behavior.skipCollapsed = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddMarkBinding.bind(view)

        binding.addPhotoBtn.setOnClickListener {
            launchPhotoPicker()
        }

        binding.cameraPlaceholder.setOnClickListener {
            launchPhotoPicker()
        }

        binding.removePhotoBtn.setOnClickListener {
            addMarkFragmentViewModel.removeItems()
        }

        binding.addMarkBtn.setOnClickListener {
            addMarkFragmentViewModel.saveMark(
                prepareAndGetMark(),
                prepareAndGetParts()
            ) {
                FragmentNavigationUtils.closeFragment(requireActivity())
            }
        }

        binding.cancelMarkBtn.setOnClickListener {
            dismiss()
        }

        adjustRecyclerView()
    }

    private fun launchPhotoPicker() {
        photoAddLauncher.launch("image/*")
    }

    private fun expandBottomSheet(bottomSheetView: View) {
        val behavior = BottomSheetBehavior.from(bottomSheetView)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun adjustRecyclerView() {
        // Assign adapter to RecyclerView
        carouselAdapter =
            CarouselRecyclerViewAdapter { uri, b -> addMarkFragmentViewModel.toggleItem(uri, b) }
        binding.photoCarousel.adapter = carouselAdapter

        // Add spaces between items of RecyclerView
        val itemDecoration = DividerItemDecoration(requireContext(), HORIZONTAL)
        itemDecoration.setDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.rv_divider
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
            if (addMarkFragmentViewModel.isAnyTaken()) {
                enableRemovePhotoButton()
            } else {
                disableRemovePhotoButton()
            }
        }
        carouselAdapter.updateData(value)
    }

    private fun onFragmentStateChangedCallback(value: AddMarkFragmentState) { // TODO: make it work
        when (value) {
            AddMarkFragmentState.EDITING -> setEditing()
            AddMarkFragmentState.LOADING -> setLoading()
        }
    }

    private fun setEditing() {
        binding.loaderImage.clearAnimation()
        binding.loader.visibility = GONE
        binding.buttons.visibility = VISIBLE
    }

    private fun setLoading() {
        binding.buttons.visibility = GONE
        binding.loader.visibility = VISIBLE
        valueAnimator = ObjectAnimator.ofFloat(0f, 360f)
        valueAnimator!!.repeatCount = ValueAnimator.INFINITE
        valueAnimator!!.repeatMode = ValueAnimator.RESTART
        valueAnimator!!.duration = 500
        valueAnimator!!.addUpdateListener {
            binding.loaderImage.rotation = it.animatedValue as Float
        }
        valueAnimator!!.start()
    }

    private fun prepareAndGetMark(): MarkDto { // TODO: there must be full user data here
        return MarkDto(
            1,
            Point(59.921962, 30.355260),
            binding.markText.text.toString(),
            binding.isPublicCheckBox.isChecked
        )
    }

    private fun prepareAndGetParts(): List<MultipartBody.Part> {
        val set = carouselAdapter.getSetOfPhotos()
        return set.map {
            val requestBody = RequestBody.create(MediaType.parse("image/*"), it.value)
            MultipartBody.Part.createFormData("photos", it.key.toString(), requestBody)
        }
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
            requireContext().resources.getDimensionPixelSize(R.dimen.margin_between_btns),
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
            requireContext().resources.getDimensionPixelSize(R.dimen.margin_between_btns)
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
            ContextCompat.getColorStateList(requireContext(), R.color.white)
        binding.removePhotoBtn.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.danger)
        binding.removePhotoBtn.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.white
            )
        )
    }

    private fun disableRemovePhotoButton() {
        binding.removePhotoBtn.isEnabled = false
        binding.removePhotoBtn.iconTint =
            ContextCompat.getColorStateList(requireContext(), R.color.danger)
        binding.removePhotoBtn.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.white)
        binding.removePhotoBtn.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.danger
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
    }
}