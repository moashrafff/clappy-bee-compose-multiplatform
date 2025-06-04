package com.moashraf.beegame

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import beegame.composeapp.generated.resources.Res
import beegame.composeapp.generated.resources.bee_sprite
import beegame.composeapp.generated.resources.clappy_bee_background
import beegame.composeapp.generated.resources.moving_background
import beegame.composeapp.generated.resources.pipe
import beegame.composeapp.generated.resources.pipe_cap
import com.moashraf.beegame.domain.Game
import com.moashraf.beegame.domain.GameStatus
import com.moashraf.beegame.ui.orange
import com.moashraf.beegame.util.ChewyFontFamily
import com.moashraf.beegame.util.Platform
import com.moashraf.beegame.util.getPlatform
import com.stevdza_san.sprite.component.drawSpriteView
import com.stevdza_san.sprite.domain.SpriteSheet
import com.stevdza_san.sprite.domain.SpriteSpec
import com.stevdza_san.sprite.domain.rememberSpriteState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

const val BEE_FRAME_SIZE = 80
const val PIPE_CAP_HEIGHT = 50F

@Composable
@Preview
fun App() {
    MaterialTheme {
        val platform = remember { getPlatform() }
        var screenWidth by remember { mutableStateOf(0) }
        var screenHeight by remember { mutableStateOf(0) }
        var game by remember { mutableStateOf(Game()) }

        val spriteState = rememberSpriteState(
            totalFrames = 9, framesPerRow = 3
        )

        val spriteSpec = remember {
            SpriteSpec(
                screenWidth = screenWidth.toFloat(), default = SpriteSheet(
                    frameWidth = BEE_FRAME_SIZE,
                    frameHeight = BEE_FRAME_SIZE,
                    image = Res.drawable.bee_sprite
                )
            )
        }

        val currentFrame by spriteState.currentFrame.collectAsState()
        val sheetImage = spriteSpec.imageBitmap
        val animatedAngle by animateFloatAsState(
            targetValue = when {
                game.beeVelocity > game.beeMaxVelocity / 1.1 -> 30f
                else -> 0f
            }
        )

        DisposableEffect(Unit) {
            onDispose {
                spriteState.stop()
                spriteState.cleanup()
                game.cleanUp()
            }
        }

        LaunchedEffect(game.status) {
            while (game.status == GameStatus.STARTED) {
                withFrameMillis {
                    game.updateGameProgress()
                }
            }
            if (game.status == GameStatus.OVER) {
                spriteState.stop()
            }
        }

        val scope = rememberCoroutineScope()
        val backgroundOffsetX = remember { Animatable(0f) }
        var imageWidth by remember { mutableStateOf(0) }
        val pipeImage = imageResource(Res.drawable.pipe)
        val pipeCapImage = imageResource(Res.drawable.pipe_cap)

        LaunchedEffect(game.status) {
            while (game.status == GameStatus.STARTED) {
                backgroundOffsetX.animateTo(
                    targetValue = -imageWidth.toFloat(),
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = when(platform) {
                                Platform.Android -> 4000
                                Platform.iOS -> 4000
                                Platform.Wasm -> 11000
                                Platform.Desktop -> 9000
                            },
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(Res.drawable.clappy_bee_background),
                contentScale = ContentScale.Crop,
                contentDescription = "Background"
            )
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged {
                        imageWidth = it.width
                    }
                    .offset { IntOffset(x = backgroundOffsetX.value.toInt(), y = 0) },
                painter = painterResource(Res.drawable.moving_background),
                contentScale = ContentScale.FillHeight,
                contentDescription = "Moving Background Image"
            )
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(x = backgroundOffsetX.value.toInt() + imageWidth, y = 0) },
                painter = painterResource(Res.drawable.moving_background),
                contentScale = ContentScale.FillHeight,
                contentDescription = "Moving Background Image"
            )
        }

        Canvas(modifier = Modifier.fillMaxSize().onGloballyPositioned {
            val size = it.size
            if (screenWidth != size.width || screenHeight != size.height) {
                screenWidth = size.width
                screenHeight = size.height
                game = game.copy(
                    screenWidth = size.width, screenHeight = size.height
                )
            }
        }.clickable {
            if (game.status == GameStatus.STARTED) {
                game.jump()
            }
        }) {
            rotate(
                degrees = animatedAngle, pivot = Offset(
                    x = game.bee.x - game.beeRadius, y = game.bee.y - game.beeRadius
                )
            ) {
                drawSpriteView(
                    spriteState = spriteState,
                    spriteSpec = spriteSpec,
                    currentFrame = currentFrame,
                    image = sheetImage,
                    offset = IntOffset(
                        x = (game.bee.x - game.beeRadius).toInt(),
                        y = (game.bee.y.toInt() - game.beeRadius).toInt()
                    )
                )
            }
            game.pipePairs.forEach { pipePair ->
                drawImage(
                    image = pipeImage,
                    dstOffset = IntOffset(
                        x = (pipePair.x - game.pipeWidth / 2).toInt(),
                        y = 0
                    ),
                    dstSize = IntSize(
                        width = game.pipeWidth.toInt(),
                        height = (pipePair.topHeight - PIPE_CAP_HEIGHT).toInt()
                    )
                )
                drawImage(
                    image = pipeCapImage,
                    dstOffset = IntOffset(
                        x = (pipePair.x - game.pipeWidth / 2).toInt(),
                        y = (pipePair.topHeight - PIPE_CAP_HEIGHT).toInt()
                    ),
                    dstSize = IntSize(
                        width = game.pipeWidth.toInt(),
                        height = PIPE_CAP_HEIGHT.toInt()
                    )
                )
                drawImage(
                    image = pipeCapImage,
                    dstOffset = IntOffset(
                        x = (pipePair.x - game.pipeWidth / 2).toInt(),
                        y = (pipePair.y + game.pipeGapSize / 2).toInt()
                    ),
                    dstSize = IntSize(
                        width = game.pipeWidth.toInt(),
                        height = PIPE_CAP_HEIGHT.toInt()
                    )
                )
                drawImage(
                    image = pipeImage,
                    dstOffset = IntOffset(
                        x = (pipePair.x - game.pipeWidth / 2).toInt(),
                        y = (pipePair.y + game.pipeGapSize / 2 + PIPE_CAP_HEIGHT).toInt()
                    ),
                    dstSize = IntSize(
                        width = game.pipeWidth.toInt(),
                        height = (pipePair.bottomHeight - PIPE_CAP_HEIGHT).toInt()
                    )
                )
            }

        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(all = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "BEST : ${game.bestScore}",
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = ChewyFontFamily()
            )
            Text(
                text = "${game.currentScore}",
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = ChewyFontFamily()
            )
        }

        if (game.status == GameStatus.IDLE) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    modifier = Modifier.height(54.dp),
                    shape = RoundedCornerShape(size = 20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = orange
                    ),
                    onClick = {
                        game.startGame()
                        spriteState.start()
                    },
                    content = {
                        Text(
                            text = "START",
                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
                            fontFamily = ChewyFontFamily()
                        )
                    })
            }
        }

        if (game.status == GameStatus.OVER) {
            Column(
                modifier = Modifier.fillMaxSize().background(
                    color = Color.Black.copy(alpha = 0.5f)
                ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Game Over!",
                    color = Color.White,
                    fontSize = MaterialTheme.typography.displayMedium.fontSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ChewyFontFamily()

                )
                Text(
                    text = "SCORE: ${game.currentScore}",
                    color = Color.White,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontFamily = ChewyFontFamily()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    modifier = Modifier.height(54.dp),
                    shape = RoundedCornerShape(size = 20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = orange
                    ),
                    onClick = {
                        game.restartGame()
                        spriteState.start()
                        scope.launch {
                            backgroundOffsetX.snapTo(0f)
                        }
                    }) {
                    Text(
                        text = "RESTART",
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontFamily = ChewyFontFamily()
                    )
                }
            }
        }
    }
}