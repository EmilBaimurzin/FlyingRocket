package com.rocket.game.domain.game

data class Planet(
    val value: Int,
    var position: Pair<Float, Float> = 0f to 0f
)