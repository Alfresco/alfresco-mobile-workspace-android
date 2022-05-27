package com.alfresco.pdf

import android.graphics.Color

/**
 * Marked as PDFConstants class
 */
object PDFConstants {
    const val DEFAULT_FONT_SIZE_TEXT = "DefaultFontSize"
    const val DEFAULT_FONT_SIZE = 11
    const val DEFAULT_FONT_FAMILY_TEXT = "DefaultFontFamily"
    const val DEFAULT_FONT_FAMILY = "TIMES_ROMAN"
    const val DEFAULT_FONT_COLOR_TEXT = "DefaultFontColor"
    const val DEFAULT_FONT_COLOR = -16777216

    // key for text to pdf (TTP) page color
    const val DEFAULT_PAGE_COLOR_TTP = "DefaultPageColorTTP"

    // key for images to pdf (ITP) page color
    const val DEFAULT_PAGE_COLOR = Color.WHITE
    const val DEFAULT_PAGE_SIZE_TEXT = "DefaultPageSize"
    const val DEFAULT_PAGE_SIZE = "A4"
    const val IMAGE_SCALE_TYPE_ASPECT_RATIO = "maintain_aspect_ratio"
    const val PG_NUM_STYLE_PAGE_X_OF_N = "pg_num_style_page_x_of_n"
    const val PG_NUM_STYLE_X_OF_N = "pg_num_style_x_of_n"
    const val PG_NUM_STYLE_X = "pg_num_style_x"
}
