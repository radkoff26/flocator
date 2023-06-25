package ru.flocator.feature_settings.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import ru.flocator.app.R
import ru.flocator.core_design.fragments.ResponsiveBottomSheetDialogFragment
import ru.flocator.core_api.api.MainRepository
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.flocator.feature_auth.api.ui.AuthFragment
import javax.inject.Inject

@AndroidEntryPoint
class ExitAccountFragment : ResponsiveBottomSheetDialogFragment(
    BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO,
    BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO
), ru.flocator.core_sections.SettingsSection {
    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var repository: MainRepository
    private lateinit var fragmentView: View

    override fun getCoordinatorLayout(): CoordinatorLayout {
        return fragmentView.findViewById(R.id.coordinator)
    }

    override fun getBottomSheetScrollView(): NestedScrollView {
        return fragmentView.findViewById(R.id.bs)
    }

    override fun getInnerLayout(): ViewGroup {
        return fragmentView.findViewById(R.id.content)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_exit_account, container, false)
        val exitButton = fragmentView.findViewById<ImageView>(R.id.exit_account_close_button)
        val confirmButton = fragmentView.findViewById<MaterialButton>(R.id.exit_account_confirm_button)

        exitButton.setOnClickListener {
            dismiss()
        }

        confirmButton.setOnClickListener {
            compositeDisposable.add(
                repository.clearAllCache()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError {
                        Log.e(TAG, "onCreateView: failed to clear cache!", it)
                        openAuthFragment()
                    }
                    .subscribe {
                        openAuthFragment()
                    }
            )
        }

        return fragmentView
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        super.onDestroyView()
    }

    private fun openAuthFragment() {
        dismiss()
        ru.flocator.core_utils.FragmentNavigationUtils.clearAllAndOpenFragment(
            requireActivity().supportFragmentManager,
            AuthFragment()
        )
    }

    companion object {
        const val TAG = "Exit account fragment"
        const val BOTTOM_SHEET_PORTRAIT_WIDTH_RATIO = 0.95
        const val BOTTOM_SHEET_LANDSCAPE_WIDTH_RATIO = 0.9
    }
}