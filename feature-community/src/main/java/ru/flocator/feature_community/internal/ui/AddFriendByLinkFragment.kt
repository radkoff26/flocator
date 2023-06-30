package ru.flocator.feature_community.internal.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.flocator.core_design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.core_sections.CommunitySection
import ru.flocator.feature_community.databinding.FragmentAddFriendBinding
import ru.flocator.feature_community.internal.view_models.AddFriendByLinkFragmentViewModel
import javax.inject.Inject
import kotlin.properties.Delegates

internal class AddFriendByLinkFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), CommunitySection {
    private var _binding: FragmentAddFriendBinding? = null
    private val binding: FragmentAddFriendBinding
        get() = _binding!!
    private var currentUserId by Delegates.notNull<Long>()
    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var addFriendByLinkFragmentViewModel: AddFriendByLinkFragmentViewModel
    
    override fun getCoordinatorLayout(): CoordinatorLayout {
        return binding.coordinator
    }

    override fun getBottomSheetScrollView(): NestedScrollView {
        return binding.bs
    }

    override fun getInnerLayout(): ViewGroup {
        return binding.content
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        val args: Bundle? = arguments
        currentUserId = args?.getLong("currentUserId") ?: -1

        binding.addFriendConfirmButton.setOnClickListener {
            if(binding.userLoginText.text.toString().isNotEmpty()){
                compositeDisposable.add(
                    addFriendByLinkFragmentViewModel.checkLogin(binding.userLoginText.text.toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                if(!it){
                                    binding.message.visibility = View.VISIBLE
                                    binding.message.setTextColor(resources.getColor(ru.flocator.core_design.R.color.black))
                                    binding.message.text = "Запрос в друзья отправлен!"
                                } else {
                                    binding.message.visibility = View.VISIBLE
                                    binding.message.setTextColor(resources.getColor(ru.flocator.core_design.R.color.danger))
                                    binding.message.text = "Пользователя не существует!"
                                }
                            },
                            {
                                Log.e(AddFriendByLinkFragmentViewModel.TAG, "checkLogin ERROR", it)
                            }
                        )
                )
                addFriendByLinkFragmentViewModel.addFriendByLogin(currentUserId,binding.userLoginText.text.toString())
                //println("СУЩЕСТВУЕТ??????????  " +  addFriendByLinkFragmentViewModel.checkUserLogin(binding.userLoginText.text.toString()))
            } else {
                binding.message.visibility = View.VISIBLE
                binding.message.setTextColor(resources.getColor(ru.flocator.core_design.R.color.black))
                binding.message.text = "Поле должно быть заполнено!"
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