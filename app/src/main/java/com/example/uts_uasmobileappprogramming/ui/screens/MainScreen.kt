package com.example.uts_uasmobileappprogramming.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.uts_uasmobileappprogramming.navigation.Screen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(mainNavController: NavHostController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            AnimatedBottomBar(bottomNavController)
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(mainNavController) }
            composable(Screen.SkinJourney.route) { SkinJourneyScreen(mainNavController) }
            composable(Screen.ProductRecommendation.route) { ProductRecommendationScreen(mainNavController) }
            composable(Screen.Profile.route) { ProfileScreen(mainNavController) }
        }
    }
}

@Composable
fun AnimatedBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Screen.Home to "Home",
        Screen.SkinJourney to "Journey",
        Screen.ProductRecommendation to "Products",
        Screen.Profile to "Profile"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .shadow(12.dp, RoundedCornerShape(30.dp))
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
                    )
                )
            )
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (screen, label) ->
                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                val transition = updateTransition(selected, label = "SelectedTransition")

                val scale by transition.animateFloat(
                    transitionSpec = { tween(250, easing = LinearOutSlowInEasing) },
                    label = "ScaleAnim"
                ) { if (it) 1.2f else 1f }

                val alpha by transition.animateFloat(
                    transitionSpec = { tween(300) },
                    label = "AlphaAnim"
                ) { if (it) 1f else 0.6f }

                val indicatorColor =
                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else Color.Transparent

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(indicatorColor)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickableWithoutRipple {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (screen) {
                                Screen.Home -> Icons.Default.Home
                                Screen.SkinJourney -> Icons.AutoMirrored.Filled.List
                                Screen.ProductRecommendation -> Icons.Default.FavoriteBorder
                                else -> Icons.Default.AccountCircle
                            },
                            contentDescription = label,
                            tint = if (selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(26.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .alpha(alpha)
                        )
                        AnimatedVisibility(
                            visible = selected,
                            enter = fadeIn(tween(200)) + expandVertically(),
                            exit = fadeOut(tween(150)) + shrinkVertically()
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(
        indication = null,
        interactionSource = interactionSource
    ) { onClick() }
}

