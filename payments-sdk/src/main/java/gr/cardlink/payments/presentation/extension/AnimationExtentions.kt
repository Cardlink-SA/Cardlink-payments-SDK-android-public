package gr.cardlink.payments.presentation.extension

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible

internal fun View.fadeInView(duration: Long = 220, callback: ((View) -> Unit)? = null) {
    val animator = ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f).apply {
        interpolator = DecelerateInterpolator()
        this.duration = duration

        doOnStart {
            visibility = View.VISIBLE
        }

        doOnEnd {
            callback?.invoke(this@fadeInView)
        }
    }
    animator.start()
}

internal fun View.fadeOutView(duration: Long = 220, callback: (() -> Unit)? = null) {
    val animator = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0f).apply {
        interpolator = AccelerateInterpolator()
        this.duration = duration

        doOnStart {
            isVisible = true
        }

        doOnEnd {
            isVisible = false
            callback?.invoke()
        }
    }
    animator.start()
}