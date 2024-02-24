package ru.flocator.feature_community.internal.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core.dependencies.findDependencies
import ru.flocator.core.navigation.findNavController
import ru.flocator.core.section.CommunitySection
import ru.flocator.design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.feature_community.R
import ru.flocator.feature_community.databinding.FragmentAddFriendBinding
import ru.flocator.feature_community.internal.di.DaggerCommunityComponent
import ru.flocator.feature_community.internal.view_models.AddFriendByLinkViewModel
import javax.inject.Inject

internal class AddFriendByLinkFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), CommunitySection {
    private var _binding: FragmentAddFriendBinding? = null
    private val binding: FragmentAddFriendBinding
        get() = _binding!!
    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: AddFriendByLinkViewModel

    override fun getCoordinatorLayout(): CoordinatorLayout {
        return binding.coordinator
    }

    override fun getBottomSheetScrollView(): NestedScrollView {
        return binding.bs
    }

    override fun getInnerLayout(): ViewGroup {
        return binding.content
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerCommunityComponent.builder()
            .communityDependencies(findDependencies())
            .navController(findNavController())
            .build()
            .inject(this)

        viewModel =
            ViewModelProvider(this, viewModelFactory)[AddFriendByLinkViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        val args: Bundle? = arguments

        binding.addFriendConfirmButton.setOnClickListener {
            if (binding.userLoginText.text.toString().isNotEmpty()) {
                compositeDisposable.add(
                    viewModel.checkLogin(binding.userLoginText.text.toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                if (!it) {
                                    binding.message.visibility = View.VISIBLE
                                    binding.message.setTextColor(resources.getColor(ru.flocator.design.R.color.black))
                                    binding.message.text =
                                        resources.getString(R.string.friend_request_sent)
                                } else {
                                    binding.message.visibility = View.VISIBLE
                                    binding.message.setTextColor(resources.getColor(ru.flocator.design.R.color.danger))
                                    binding.message.text =
                                        resources.getString(R.string.user_doesnt_exist)
                                }
                            },
                            {
                                Log.e(AddFriendByLinkViewModel.TAG, "checkLogin ERROR", it)
                            }
                        )
                )
                viewModel.addFriendByLogin(binding.userLoginText.text.toString())
            } else {
                binding.message.visibility = View.VISIBLE
                binding.message.setTextColor(resources.getColor(ru.flocator.design.R.color.black))
                binding.message.text = resources.getString(R.string.field_must_be_filled)
            }
        }

        binding.addFriendCloseButton.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        _binding = null
    }

    companion object {
        const val TAG = "Add friend by link fragment"
        const val BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO = 0.9
        const val BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO = 0.8
    }
}