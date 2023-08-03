package com.rocket.game.domain.other

import android.content.Context

class AppPrefs(context: Context) {
    private val sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE)

    fun getStars(): Int = sp.getInt("STARS", 0)
    fun addStar() = sp.edit().putInt("STARS",getStars() + 1).apply()

    fun buy() = sp.edit().putInt("STARS", getStars() - 15).apply()
}