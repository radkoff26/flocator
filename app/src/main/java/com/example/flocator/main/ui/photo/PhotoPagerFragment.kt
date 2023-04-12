package com.example.flocator.main.ui.photo

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private val viewModel: PhotoPagerFragmentViewModel

    private lateinit var adapter: PhotoRecyclerViewAdapter

    init {
        val uriList = requireArguments().getStringArrayList(BundleArgumentsContraction.PhotoPagerFragment.URI_LIST)
        viewModel = PhotoPagerFragmentViewModel(uriList!!.toList())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photo_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPhotoPagerBinding.bind(view)

        adjustViewPager()

        viewModel.photosLiveData.observe(viewLifecycleOwner, this::onUpdatePhotos)
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    private fun adjustViewPager() {
        adapter = PhotoRecyclerViewAdapter(viewModel.photosLiveData.value!!) {
            viewModel.requestPhotoLoading(it)
        }

        binding.photoPager.adapter = adapter

        val position = requireArguments().getInt(BundleArgumentsContraction.PhotoPagerFragment.POSITION)

        binding.photoPager.setCurrentItem(position, false)
    }

    private fun onUpdatePhotos(value: List<Bitmap?>) {
        adapter.updatePhotos(value)
    }
}
