package gr.cardlink.payments.presentation.ui.base

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import gr.cardlink.payments.di.DataModule
import gr.cardlink.payments.domain.repository.SessionRepository
import io.reactivex.rxjava3.disposables.CompositeDisposable

internal abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!
    protected var callbacks: FragmentCallbacks? = null
    protected var disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       _binding = inflateViewBinding(inflater, container)
        checkColor(binding.root)
        return binding.root
    }

    private fun checkColor(rootView: View) {
        DataModule.sessionRepository.get<Int>(SessionRepository.Key.COLOR_RES)?.let { colorRes ->
            try {
                val colorInt = ContextCompat.getColor(requireContext(), colorRes)
                rootView.setBackgroundColor(colorInt)
                updateColors(colorInt)
            } catch (ex: Exception) {
                Log.e(null, "Could not update color", ex)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as? FragmentCallbacks
    }

    override fun onDetach() {
        callbacks = null
        super.onDetach()
    }

    override fun onDestroyView() {
        disposables.clear()
        _binding = null
        super.onDestroyView()
    }

    abstract fun updateColors(@ColorInt colorInt: Int)

    abstract fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

}