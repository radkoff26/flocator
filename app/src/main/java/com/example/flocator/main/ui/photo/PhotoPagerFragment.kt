package com.example.flocator.main.ui.photo

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.flocator.R
import com.example.flocator.common.cache.runtime.PhotoState
import com.example.flocator.databinding.FragmentPhotoPagerBinding
import com.example.flocator.main.MainSection
import com.example.flocator.main.config.BundleArgumentsContraction
import com.example.flocator.main.data.Photo
import com.example.flocator.main.ui.photo.adapters.PhotoRecyclerViewAdapter

class PhotoPagerFragment : DialogFragment(), MainSection {
    private var _binding: FragmentPhotoPagerBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: PhotoPagerFragmentViewModel by viewModels()

    private lateinit var adapter: PhotoRecyclerViewAdapter
    private lateinit var uriList: List<String>

    private var animator: ValueAnimator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Must be passed to Fragment
        uriList =
            requireArguments().getStringArrayList(BundleArgumentsContraction.PhotoPagerFragmentArguments.URI_LIST)!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setWindowAnimations(R.style.PhotoDialogTheme)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.PhotoDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_photo_pager, container, false)
        _binding = FragmentPhotoPagerBinding.bind(view)

        adjustToolbar()
        adjustViewPager()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.photoCacheLiveData.observe(viewLifecycleOwner, this::onUpdatePhotos)
        viewModel.toolbarDisplayedStateLiveData.observe(
            viewLifecycleOwner
        ) {
            if (it) {
                showToolbar()
            } else {
                hideToolbar()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (animator != null && animator!!.isRunning) {
            animator!!.cancel()
        }
        _binding = null
    }

    private fun hideToolbar() {
        if (animator != null && animator!!.isRunning) {
            animator!!.cancel()
        }
        animator = ValueAnimator.ofFloat(binding.toolbar.alpha, 0f).apply {
            addUpdateListener {
                binding.toolbar.alpha = it.animatedValue as Float
            }
            duration = (300 * binding.toolbar.alpha).toLong()
            doOnEnd {
                binding.toolbar.visibility = View.GONE
            }
            start()
        }
    }

    private fun showToolbar() {
        if (animator != null && animator!!.isRunning) {
            animator!!.cancel()
        }
        binding.toolbar.visibility = View.VISIBLE
        ValueAnimator.ofFloat(binding.toolbar.alpha, 1f).apply {
            addUpdateListener {
                binding.toolbar.alpha = it.animatedValue as Float
            }
            duration = (300 * (1 - binding.toolbar.alpha)).toLong()
            start()
        }
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
        adapter = PhotoRecyclerViewAdapter(
            constructPhotos(viewModel.photoCacheLiveData.value!!),
            {
                viewModel.requestPhotoLoading(it)
            },
            {
                viewModel.switchToolbarState()
            }
        )

        binding.photoPager.adapter = adapter

        val position =
            requireArguments().getInt(BundleArgumentsContraction.PhotoPagerFragmentArguments.POSITION)

        binding.photoPager.setCurrentItem(position, false)
    }

    private fun onUpdatePhotos(value: LruCache<String, PhotoState>) {
        adapter.updatePhotos(constructPhotos(value))
    }

    private fun constructPhotos(value: LruCache<String, PhotoState>): List<Photo> {
        if (!this::uriList.isInitialized) {
            return emptyList()
        }
        return uriList.map {
            Photo(
                it,
                value[it] ?: PhotoState.Loading
            )
        }
    }

    companion object {
        const val TAG = "Photo Pager Fragment"
    }
}
