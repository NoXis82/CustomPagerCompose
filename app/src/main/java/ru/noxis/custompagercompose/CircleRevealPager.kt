@file:OptIn(ExperimentalComposeUiApi::class)

package ru.noxis.custompagercompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import kotlin.math.sqrt

@Composable
fun CircleRevealPager(modifier: Modifier = Modifier) {
    val state = rememberPagerState(pageCount = { destinations.size })
    var offsetY by remember { mutableFloatStateOf(0f) }

    HorizontalPager(
        modifier = modifier
            /**
             * Начальная точка всегда должна находиться на правом краю.
             * Но положение по оси Y зависит от того, где пользователь начал касание.
             * offsetY устанавливается с помощью модификатора на HorizontalPager
             */
            .pointerInteropFilter {
                offsetY = it.y
                false
            }
            .padding(horizontal = 32.dp, vertical = 64.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(Color.Gray),
        state = state,
    ) { page ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // MAKE THE PAGE NOT MOVE
                    /**
                     * нужно сделать так, чтобы страница оставалась в одном положении
                     * и не двигалась по горизонтали. Это можно сделать, сместив страницу
                     * на ширину, умноженную на pageOffset
                     */
                    val pageOffset = state.offsetForPage(page)
                    translationX = size.width * pageOffset

                    // ADD THE CIRCULAR CLIPPING
                    /**
                     * добавляем круговую обрезку. Поскольку она влияет только
                     * на страницы, открывающиеся справа, мы используем функцию endOffsetForPage()
                     */
                    val endOffset = state.endOffsetForPage(page)

                    shadowElevation = 20f
                    /**
                     * По сути, это Shape , которая расширяется, чтобы заполнить весь экран,
                     * с плавающим значением в качестве прогресса и начальной точкой
                     */
                    shape = CirclePath(
                        progress = 1f - endOffset.absoluteValue,
                        origin = Offset(
                            size.width,
                            offsetY,
                        )
                    )
                    /**
                     * нужно установить clip значение true, чтобы активировать отсечение
                     */
                    clip = true

                    /**
                     * Чтобы добавить масштабирование с эффектом параллакса,
                     * мы используем абсолютное смещение страницы.
                     * Чем дальше от центра, тем больше страница.
                     * Благодаря этому исходящие страницы расширяются, а входящие сжимаются.
                     */
                    val absoluteOffset = state.offsetForPage(page).absoluteValue
                    val scale = 1f + (absoluteOffset.absoluteValue * .4f)

                    scaleX = scale
                    scaleY = scale

                    /**
                     * чтобы при прокрутке страницы влево она исчезала.
                     * Поскольку мы хотим повлиять только на эту страницу,
                     * мы используем startOffsetForPage() для установки alpha
                     */
                    val startOffset = state.startOffsetForPage(page)
                    alpha = (2f - startOffset) / 2f
                }
                .background(Color.Gray),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = destinations[page].location,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                    )
                )
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White)
                )
                Text(
                    text = destinations[page].description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                    )
                )
            }
        }
    }
}


class CirclePath(private val progress: Float, private val origin: Offset = Offset(0f, 0f)) : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {

        val center = Offset(
            x = size.center.x - ((size.center.x - origin.x) * (1f - progress)),
            y = size.center.y - ((size.center.y - origin.y) * (1f - progress)),
        )
        val radius = (sqrt(
            size.height * size.height + size.width * size.width
        ) * .5f) * progress

        return Outline.Generic(Path().apply {
            addOval(
                Rect(
                    center = center,
                    radius = radius,
                )
            )
        })
    }
}


data class Destination(
    val location: String,
    val description: String,
)

val destinations = listOf(
    Destination(
        "Bali",
        "Known for its beautiful beaches, stunning temples, and lush greenery, Bali is a tropical paradise that offers a unique blend of natural beauty and cultural experiences. It's an ideal destination for those seeking a relaxing vacation filled with yoga, spa treatments, and sunsets."
    ),
    Destination(
        "Santorini",
        "A picturesque island in the Aegean Sea, Santorini is famous for its white-washed buildings, blue-domed churches, and stunning sunsets. It's a perfect destination for honeymooners or those looking for a romantic getaway."
    ),
    Destination(
        "Tokyo",
        "A bustling city that seamlessly blends tradition and modernity, Tokyo offers an exciting vacation experience that's both vibrant and unique. From traditional Japanese gardens and temples to modern shopping districts and futuristic technology, there's something for everyone in Tokyo."
    ),
    Destination(
        "Machu Picchu",
        "A UNESCO World Heritage Site, Machu Picchu is an ancient Incan citadel situated high in the Andes Mountains. It's a destination for adventure seekers, as hiking the Inca Trail is the only way to reach the site."
    ),
    Destination(
        "Banf",
        "A stunning national park in the Canadian Rockies, Banff is a year-round destination that offers scenic vistas, crystal-clear lakes, and abundant wildlife. It's an ideal spot for nature lovers, hikers, and skiers."
    ),
    Destination(
        "Marrakech",
        "A vibrant city known for its bustling markets, colorful architecture, and rich cultural heritage, Marrakech is an exotic destination that offers a unique blend of ancient and modern experiences."
    ),
    Destination(
        "The Maldives",
        "An island paradise located in the Indian Ocean, the Maldives is known for its turquoise waters, white sandy beaches, and overwater bungalows. It's an ideal destination for those seeking a luxurious and romantic getaway."
    ),
    Destination(
        "Sydney",
        "A cosmopolitan city situated on a beautiful harbor, Sydney is a destination that offers both natural beauty and urban sophistication. It's an ideal spot for beachgoers, foodies, and culture lovers."
    ),
    Destination(
        "Maui",
        "A tropical island paradise in the Pacific Ocean, Maui is known for its beautiful beaches, scenic drives, and outdoor activities such as surfing, snorkeling, and hiking. It's an ideal destination for those seeking a laid-back vacation filled with sunshine and relaxation."
    ),
    Destination(
        "Venice",
        "A romantic city built on a series of canals, Venice is known for its stunning architecture, art, and history. It's an ideal destination for culture lovers, foodies, and those seeking a romantic getaway."
    ),
)