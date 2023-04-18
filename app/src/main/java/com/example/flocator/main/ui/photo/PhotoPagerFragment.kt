package com.example.flocator.main.ui.photo

import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.fragment.app.DialogFragment
import com.example.flocator.R
import com.example.flocator.databinding.FragmentPhotoPagerBinding
import com.example.flocator.main.MainSection
import com.example.flocator.main.config.BundleArgumentsContraction
import com.example.flocator.main.ui.photo.adapters.PhotoRecyclerViewAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoPagerFragment : DialogFragment(), MainSection {
    private var _binding: FragmentPhotoPagerBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var viewModel: PhotoPagerFragmentViewModel

    private lateinit var adapter: PhotoRecyclerViewAdapter

    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uriList =
            requireArguments().getStringArrayList(BundleArgumentsContraction.PhotoPagerFragment.URI_LIST)
        viewModel = PhotoPagerFragmentViewModel(uriList!!.toList())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_photo_pager, container, false)
        _binding = FragmentPhotoPagerBinding.bind(view)

        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (velocityY < 0) {
                    binding.photoMotionLayout.transitionToState(R.id.transitionUp)
                } else {
                    binding.photoMotionLayout.transitionToState(R.id.transitionDown)
                }
                return true
            }
        })

        adjustToolbar()
        adjustViewPager()

        binding.photoMotionLayout.setTransitionListener(object : TransitionAdapter() {
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                when (currentId) {
                    R.id.endDown,
                    R.id.endUp -> {
//                        dismiss()
                    }
                    else -> {
                        return
                    }
                }
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.photosLiveData.observe(viewLifecycleOwner, this::onUpdatePhotos)
    }

    private fun adjustToolbar() {
        binding.toolbar.title = ""

        binding.toolbar.setNavigationIcon(R.drawable.back)

        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    private fun adjustViewPager() {
        adapter = PhotoRecyclerViewAdapter(viewModel.photosLiveData.value!!) {
            viewModel.requestPhotoLoading(it)
        }

        binding.photoPager.adapter = adapter

        val position =
            requireArguments().getInt(BundleArgumentsContraction.PhotoPagerFragment.POSITION)

        binding.photoPager.setCurrentItem(position, false)
    }

    private fun onUpdatePhotos(value: List<Bitmap?>) {
        adapter.updatePhotos(value)
    }

    companion object {
        const val TAG = "Photo Pager Fragment"
    }
}
