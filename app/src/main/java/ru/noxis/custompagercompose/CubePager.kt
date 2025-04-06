package ru.noxis.custompagercompose

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
import kotlin.math.min

@Composable
fun CubePager(modifier: Modifier = Modifier) {
    val state = rememberPagerState(pageCount = { 10 })

    val scale by remember {
        derivedStateOf {
            1f - (state.currentPageOffsetFraction.absoluteValue) * .3f
        }
    }

    Box(
        modifier = modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val offsetFromStart = state.offsetForPage(0).absoluteValue
        //Тень под кубом
        //уменьшаем его масштаб, чтобы он соответствовал перспективе.
        //поворачиваем его относительно смещения от первой страницы.
        // И, наконец, добавляем модификатор blur
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .offset { IntOffset(0, 150.dp.roundToPx()) }
                .scale(scaleX = .6f, scaleY = .24f)
                .scale(scale)
                .rotate(offsetFromStart * 90f)
                .blur(
                    radius = 110.dp,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded,
                )
                .background(Color.Black.copy(alpha = .5f))
        )

        HorizontalPager(
            state = state,
            modifier = Modifier
                .scale(1f, scaleY = scale)
                .aspectRatio(1f),
        ) { page ->

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    /**
                     * Внутри модификатора graphicsLayer для страницы нам
                     * нужно проверить, находится ли страница справа или слева.
                     * Это определит угол поворота страницы и точку отсчёта.
                     */
                    .graphicsLayer {
                        val pageOffset = state.offsetForPage(page)
                        val offScreenRight = pageOffset < 0f

                        /**
                         * Если страница уходит за пределы экрана вправо,
                         * мы повернём её на 105° вокруг оси Y и на -105°,
                         * если она уходит за пределы экрана влево.
                         * Я выбрал 105°, чтобы создать иллюзию трёхмерной перспективы.
                         */
                        val deg = 105f
                        val interpolated = FastOutLinearInEasing.transform(pageOffset.absoluteValue)
                        /**
                         * Но на самом деле при повороте компонуемого элемента на 105°
                         * появляется перевёрнутая версия изображения,
                         * особенно при прокрутке страницы вправо.
                         * Есть несколько способов обойти этот артефакт,
                         * но я решил просто ограничить поворот до 90°.
                         */
                        rotationY = min(interpolated * if (offScreenRight) deg else -deg, 90f)

                        transformOrigin = TransformOrigin(
                            pivotFractionX = if (offScreenRight) 0f else 1f,
                            pivotFractionY = .5f
                        )
                    }
                    /**
                     * затемнение грани куба по мере удаления от центра.
                     */
                    .drawWithContent {
                        val pageOffset = state.offsetForPage(page)

                        this.drawContent()
                        drawRect(
                            Color.Black.copy(
                                (pageOffset.absoluteValue * .7f)
                            )
                        )
                    }
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
//                AsyncImage(
//                    model = "https://source.unsplash.com/random?desert,dune,$page",
//                    contentDescription = null,
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier.fillMaxSize()
//                )
                Text(
                    text = "Hello $page",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = .6f),
                            blurRadius = 30f,
                        )
                    )
                )
            }
        }
    }
}

