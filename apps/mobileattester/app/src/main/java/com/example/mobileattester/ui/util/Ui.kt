package com.example.mobileattester.ui.util

import android.graphics.drawable.VectorDrawable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mobileattester.data.model.*
import com.example.mobileattester.data.model.ElementResult.Companion.CODE_RESULT_ERROR
import com.example.mobileattester.data.model.ElementResult.Companion.CODE_RESULT_OK
import com.example.mobileattester.data.model.ElementResult.Companion.CODE_RESULT_VERIFY_ERROR
import com.example.mobileattester.ui.theme.Attention
import com.example.mobileattester.ui.theme.Error
import com.example.mobileattester.ui.theme.Ok
import compose.icons.TablerIcons
import compose.icons.tablericons.*

fun Modifier.`if`(
    condition: Boolean,
    then: Modifier.() -> Modifier,
): Modifier =
    if (condition) {
        then()
    } else {
        this
    }


fun getCodeColor(code: Int): Color {
    return when (code) {
        CODE_RESULT_OK -> Ok
        CODE_RESULT_ERROR -> Error
        CODE_RESULT_VERIFY_ERROR -> Attention
        else -> Color.DarkGray
    }
}

fun getResultIcon(result: ElementResult): ImageVector {
    return when (result.result) {
        CODE_RESULT_OK -> TablerIcons.Check
        CODE_RESULT_VERIFY_ERROR -> TablerIcons.SquareX
        CODE_RESULT_ERROR -> TablerIcons.SquareX
        else -> TablerIcons.QuestionMark
    }
}