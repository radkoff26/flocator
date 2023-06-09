package ru.flocator.app.community.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.example.flocator.R
import ru.flocator.app.common.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.app.common.repository.MainRepository
import ru.flocator.app.common.sections.CommunitySection
import ru.flocator.app.community.view_models.AddFriendByLinkFragmentViewModel
import com.example.flocator.databinding.FragmentAddFriendBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class AddFriendByLinkFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), CommunitySection {
    private var _binding: FragmentAddFriendBinding? = null
    private val binding: FragmentAddFriendBinding
        get() = _binding!!
    private var currentUserId by Delegates.notNull<Long>()
    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var repository: MainRepository


    private lateinit var addFriendByLinkFragmentViewModel: AddFriendByLinkFragmentViewModel
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
    ): View? {
        _binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        addFriendByLinkFragmentViewModel = AddFriendByLinkFragmentViewModel(repository)
        val args: Bundle? = arguments
        if(args != null){
            currentUserId = args.getLong("currentUserId")
        } else{
            currentUserId = -1
        }

        binding.addFriendConfirmButton.setOnClickListener {
            if(binding.userLoginText.text.toString().isNotEmpty()){
                compositeDisposable.add(
                    repository.restApi.checkLogin(binding.userLoginText.text.toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                if(!it){
                                    binding.message.visibility = View.VISIBLE
                                    binding.message.setTextColor(resources.getColor(R.color.black))
                                    binding.message.text = "Запрос в друзья отправлен!"
                                } else {
                                    binding.message.visibility = View.VISIBLE
                                    binding.message.setTextColor(resources.getColor(R.color.danger))
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
                binding.message.setTextColor(resources.getColor(R.color.black))
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
        _binding = null
    }

    companion object {
        const val TAG = "Add friend by link fragment"
        const val BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO = 0.9
        const val BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO = 0.8
    }
    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

}