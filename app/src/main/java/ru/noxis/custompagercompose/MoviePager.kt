package ru.noxis.custompagercompose

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MoviePager(modifier: Modifier = Modifier) {

    val horizontalState = rememberPagerState(pageCount = { movies.size })

    Column(modifier = modifier) {
        HorizontalPager(
            state = horizontalState,
            modifier = Modifier
                .weight(.7f)
                .padding(
                    top = 32.dp
                ),
            pageSpacing = 1.dp,
            beyondViewportPageCount = 9
        ) { page ->
            Box(
                modifier = Modifier
                    .zIndex(page * 10f)
                    .padding(
                        start = 64.dp,
                        end = 32.dp,
                    )
                    .graphicsLayer {
                        /**
                         * Поскольку все анимации находятся на странице слева,
                         * мы используем startOffsetForPage() для всех анимаций.
                         */
                        val startOffset = horizontalState.startOffsetForPage(page)
                        /**
                         * Сначала мы замедляем страницу, чтобы она оставалась под текущей страницей,
                         * и достигаем эффекта параллакса.
                         * Для этого мы задаём translationX часть ширины страницы.
                         */
                        translationX = size.width * (startOffset * .99f)

                        //затуманиваем и размываем страницу
                        alpha = (2f - startOffset) / 2f
                        val blur = (startOffset * 20f).coerceAtLeast(0.1f)
                        renderEffect = RenderEffect
                            .createBlurEffect(
                                blur, blur, Shader.TileMode.DECAL
                            )
                            .asComposeRenderEffect()

                        /**
                         * для создания лёгкого 3D-эффекта, мы немного уменьшаем масштаб
                         * страницы по мере её перемещения влево
                         */
                        val scale = 1f - (startOffset * .1f)
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                Card(
                    modifier = Modifier.size(width = 400.dp, height = 400.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(movies[page].img),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = movies[page].title)
                        Text(text = movies[page].subtitle)
                        Text(text = movies[page].rating.toString())
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .weight(.3f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val verticalState = rememberPagerState(pageCount = { movies.size })

            VerticalPager(
                state = verticalState,
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                userScrollEnabled = false, //Это делает его недоступным для пользователя и позволяет управлять им только нам
                horizontalAlignment = Alignment.Start,
            ) { page ->
                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = movies[page].title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Thin,
                            fontSize = 28.sp,
                        )
                    )
                    Text(
                        text = movies[page].subtitle,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black.copy(alpha = .56f),
                        )
                    )
                }
            }

            /**
             * snapshotFlow для сбора значений из HorizontalPager со всеми постерами
             * Когда пользователь прокручивает Pager вниз,
             * мы устанавливаем те же значения в VerticalPager с заголовками
             */
            LaunchedEffect(Unit) {
                snapshotFlow {
                    Pair(
                        horizontalState.currentPage,
                        horizontalState.currentPageOffsetFraction
                    )
                }.collect { (page, offset) ->
                    verticalState.scrollToPage(page, offset)  // <---
                }
            }

            /**
             * нужно получить интерполированный рейтинг при прокрутке со страницы на страницу
             */
            val interpolatedRating by remember {
                derivedStateOf {
                    val position = horizontalState.offsetForPage(0)
                    val from = floor(position).roundToInt()
                    val to = ceil(position).roundToInt()

                    val fromRating = movies[from].rating.toFloat()
                    val toRating = movies[to].rating.toFloat()

                    val fraction = position - position.toInt()
                    fromRating + ((toRating - fromRating) * fraction)
                }
            }

            RatingStars(rating = interpolatedRating)

        }
    }
}

@Composable
fun RatingStars(
    modifier: Modifier = Modifier,
    rating: Float,
) {
    Row(
        modifier = modifier
    ) {

        for (i in 1..5) {
            val animatedScale by animateFloatAsState(
                targetValue = if (floor(rating) >= i) {
                    1f
                } else if (ceil(rating) < i) {
                    0f
                } else {
                    rating - rating.toInt()
                },
                animationSpec = spring(
                    stiffness = Spring.StiffnessMedium
                ),
                label = ""
            )

            Box(
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Rounded.Star),
                    contentDescription = null,
                    modifier = Modifier.alpha(.1f),
                )
                Icon(
                    painter = rememberVectorPainter(image = Icons.Rounded.Star),
                    contentDescription = null,
                    modifier = Modifier.scale(animatedScale),
                    tint = Color(0xFFD59411)
                )
            }
        }
    }
}

data class Movie(
    val title: String = "Avengers",
    val subtitle: String = "",
    val rating: Int = 4,
    val img: Color = Color.White,
)

val movies = listOf(
    Movie(
        title = "Moonlight",
        subtitle = "Barry Jenkins • 2016",
        rating = 4,
        img = Color.White,
    ),
    Movie(
        title = "Little Miss Sunshine",
        subtitle = "Dayton & Faris • 2006",
        rating = 5,
        img = Color.Green,
    ),
    Movie(
        title = "The Lobster",
        subtitle = "Yorgos Lanthimos • 2015",
        rating = 2,
        img = Color.LightGray,
    ),
    Movie(
        title = "Her",
        subtitle = "Spike Jonze • 2013",
        rating = 4,
        img = Color.Blue,
    ),
    Movie(
        title = "Memento",
        subtitle = "Christopher Nolan • 2000",
        rating = 3,
        img = Color.Cyan,
    ),
    Movie(
        title = "The Room",
        subtitle = "Tommy Wiseau • 2003",
        rating = 1,
        img = Color.Red,
    ),
)