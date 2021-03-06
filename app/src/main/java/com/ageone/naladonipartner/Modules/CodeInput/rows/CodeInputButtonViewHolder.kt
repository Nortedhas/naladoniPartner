package com.ageone.naladonipartner.Modules.CodeInput.rows

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import androidx.constraintlayout.widget.ConstraintLayout
import com.ageone.naladonipartner.External.Base.Button.BaseButton
import com.ageone.naladonipartner.External.Base.RecyclerView.BaseViewHolder
import yummypets.com.stevia.*

class CodeInputButtonViewHolder(val constraintLayout: ConstraintLayout) :
    BaseViewHolder(constraintLayout) {

    val buttonCode by lazy {
        val button = BaseButton()
        button.backgroundColor = Color.parseColor("#F06F28")
        button.text = "Применить"
        button.cornerRadius = 8.dp
        button.textColor = Color.WHITE
        button.inputType = InputType.TYPE_TEXT_VARIATION_NORMAL
        button.gradient = Color.rgb(242, 132, 45)
        button.orientation = GradientDrawable.Orientation.TOP_BOTTOM
        button.initialize()
        button
    }

    init {
        renderUI()
    }
}

fun CodeInputButtonViewHolder.renderUI() {
    constraintLayout.subviews(
        buttonCode
    )

    buttonCode
        .constrainTopToTopOf(constraintLayout,64)
        .fillHorizontally()
        .height(50F)
}

fun CodeInputButtonViewHolder.initialize() {

}
