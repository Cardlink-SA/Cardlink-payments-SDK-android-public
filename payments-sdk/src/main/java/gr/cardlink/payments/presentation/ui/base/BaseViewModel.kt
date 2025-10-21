package gr.cardlink.payments.presentation.ui.base

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable

internal abstract class BaseViewModel : ViewModel() {

    protected val disposables = CompositeDisposable()

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

}