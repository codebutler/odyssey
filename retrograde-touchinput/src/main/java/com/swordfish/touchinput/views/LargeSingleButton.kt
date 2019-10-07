package com.swordfish.touchinput.views

import android.content.Context
import android.util.AttributeSet
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.views.base.BaseSingleButton

class LargeSingleButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSingleButton(context, attrs, defStyleAttr) {

    init {
        setBackgroundResource(R.drawable.large_button_selector)
    }
}
