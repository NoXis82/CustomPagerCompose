package ru.noxis.custompagercompose

import androidx.compose.foundation.pager.PagerState

/**
 * С помощью currentPageOffset и currentPage мы можем вычислить смещение
 * для любой страницы в нашем пейджере
 */
// ACTUAL OFFSET
fun PagerState.offsetForPage(page: Int) = (currentPage - page) + currentPageOffsetFraction


// OFFSET ONLY FROM THE LEFT
fun PagerState.startOffsetForPage(page: Int): Float {
    return offsetForPage(page).coerceAtLeast(0f)
}

// OFFSET ONLY FROM THE RIGHT
fun PagerState.endOffsetForPage(page: Int): Float {
    return offsetForPage(page).coerceAtMost(0f)
}