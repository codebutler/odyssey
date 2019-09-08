package com.swordfish.touchinput.pads

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.swordfish.touchinput.controller.R
import com.swordfish.touchinput.data.EventsTransformers
import com.swordfish.touchinput.data.PadEvent
import com.swordfish.touchinput.views.ActionButtons
import com.swordfish.touchinput.views.DirectionPad
import com.swordfish.touchinput.views.base.BaseSingleButton
import io.reactivex.Observable

class GameBoyPad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseGamePad(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.layout_gb, this)
    }

    override fun getEvents(): Observable<PadEvent> {
        return Observable.merge(
            getStartEvent(),
            getSelectEvent(),
            getDirectionEvents(),
            getActionEvents()
        )
    }

    private fun getStartEvent(): Observable<PadEvent> {
        return findViewById<BaseSingleButton>(R.id.gb_start)
            .getEvents()
            .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_START))
    }

    private fun getSelectEvent(): Observable<PadEvent> {
        return findViewById<BaseSingleButton>(R.id.gb_select)
            .getEvents()
            .compose(EventsTransformers.singleButtonMap(KeyEvent.KEYCODE_BUTTON_SELECT))
    }

    private fun getActionEvents(): Observable<PadEvent> {
        return findViewById<ActionButtons>(R.id.gb_actions)
            .getEvents()
            .compose(EventsTransformers.actionButtonsMap(KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_A))
    }

    private fun getDirectionEvents(): Observable<PadEvent> {
        return findViewById<DirectionPad>(R.id.gb_direction).getEvents()
            .compose(EventsTransformers.directionPadMap())
    }
}
