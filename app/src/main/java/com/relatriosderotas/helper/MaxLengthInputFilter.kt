package com.relatriosderotas.helper

import android.text.InputFilter
import android.text.Spanned

class MaxLengthInputFilter(private val maxLength: Int) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val keep = maxLength - (dest?.length ?: 0) - (dend - dstart)
        return if (keep <= 0) {
            ""
        } else if (keep >= end - start) {
            null // Manter original
        } else {
            source?.subSequence(start, start + keep)
        }
    }
}


