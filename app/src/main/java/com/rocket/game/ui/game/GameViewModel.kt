package com.rocket.game.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rocket.game.core.library.random
import com.rocket.game.domain.game.Planet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private var gameScope = CoroutineScope(Dispatchers.Default)
    var gameState = true
    var starsCallback: (() -> Unit)? = null
    var scores = 0

    private val _lives = MutableLiveData(3)
    val lives: LiveData<Int> = _lives

    private val _energy = MutableLiveData(100)
    val energy: LiveData<Int> = _energy

    private val _playerXY = MutableLiveData(0f to 0f)
    val playerXY: LiveData<Pair<Float, Float>> = _playerXY

    private val _planets = MutableLiveData<List<Planet>>(emptyList())
    val planets: LiveData<List<Planet>> = _planets

    private val _stars = MutableLiveData<List<Pair<Float, Float>>>(emptyList())
    val stars: LiveData<List<Pair<Float, Float>>> = _stars

    private val _hearts = MutableLiveData<List<Pair<Float, Float>>>(emptyList())
    val hearts: LiveData<List<Pair<Float, Float>>> = _hearts

    private val _fallingEnergy = MutableLiveData<List<Pair<Float, Float>>>(emptyList())
    val fallingEnergy: LiveData<List<Pair<Float, Float>>> = _fallingEnergy

    fun start(maxX: Int, planetSize: Int, y: Int, playerWidth: Int, playerHeight: Int, starSize: Int, heartSize: Int, energySize: Int) {
        gameScope = CoroutineScope(Dispatchers.Default)
        generatePlanets(maxX, planetSize)
        generateStars(maxX, starSize)
        generateHearts(maxX, heartSize)
        generateEnergy(maxX, energySize)
        letPlanetsMove(y, planetSize, playerWidth, playerHeight)
        letStarsMove(y, starSize, playerWidth, playerHeight)
        letHeartsMove(y, heartSize, playerWidth, playerHeight)
        letEnergyMove(y, energySize, playerWidth, playerHeight)
        removeEnergy()
    }

    fun stop() {
        gameScope.cancel()
    }

    fun buyEnergy() {
        _energy.postValue(100)
    }

    private fun removeEnergy() {
        gameScope.launch {
            while (true) {
                delay(1000)
                _energy.postValue(_energy.value!! - 1)
                scores += 1
            }
        }
    }

    private fun generatePlanets(maxX: Int, planetSize: Int) {
        gameScope.launch {
            while (true) {
                delay(3000)
                val newList = _planets.value!!.toMutableList()
                newList.add(
                    Planet(
                        1 random 5,
                        (0 random maxX - planetSize).toFloat() to (0 - planetSize).toFloat()
                    )
                )
                _planets.postValue(newList)
            }
        }
    }

    private fun generateStars(maxX: Int, starSize: Int) {
        gameScope.launch {
            while (true) {
                delay(8000)
                val newList = _stars.value!!.toMutableList()
                newList.add(
                    (0 random maxX - starSize).toFloat() to 0 - starSize.toFloat()
                )
                _stars.postValue(newList)
            }
        }
    }

    private fun generateHearts(maxX: Int, heartSize: Int) {
        gameScope.launch {
            while (true) {
                delay(30000)
                val newList = _hearts.value!!.toMutableList()
                newList.add(
                    (0 random maxX - heartSize).toFloat() to 0 - heartSize.toFloat()
                )
                _hearts.postValue(newList)
            }
        }
    }

    private fun generateEnergy(maxX: Int, energySize: Int) {
        gameScope.launch {
            while (true) {
                delay(7000)
                val newList = _fallingEnergy.value!!.toMutableList()
                newList.add(
                    (0 random maxX - energySize).toFloat() to 0 - energySize.toFloat()
                )
                _fallingEnergy.postValue(newList)
            }
        }
    }

    private fun letPlanetsMove(y: Int, planetSize: Int, playerWidth: Int, playerHeight: Int) {
        gameScope.launch {
            while (true) {
                delay(16)
                val currentList = _planets.value!!.toMutableList()
                val newList = mutableListOf<Planet>()
                currentList.forEach { planet ->
                    if (planet.position.second < y) {
                        val planetXLine =
                            (planet.position.first).toInt()..(planet.position.first + planetSize).toInt()
                        val planetYLine =
                            (planet.position.second).toInt()..(planet.position.second + planetSize).toInt()

                        val playerXLine =
                            (_playerXY.value!!.first).toInt()..(_playerXY.value!!.first + playerWidth).toInt()
                        val playerYLine =
                            (_playerXY.value!!.second).toInt()..(_playerXY.value!!.second + playerHeight).toInt()


                        if (playerXLine.any { it in planetXLine } && playerYLine.any { it in planetYLine }) {
                            if (_lives.value!! - 1 >= 0) {
                                _lives.postValue(_lives.value!! - 1)
                            }
                        } else {
                            planet.position = planet.position.first to planet.position.second + 6
                            newList.add(planet)
                        }
                    }
                }
                _planets.postValue(newList)
            }
        }
    }

    private fun letStarsMove(y: Int, starSize: Int, playerWidth: Int, playerHeight: Int) {
        gameScope.launch {
            while (true) {
                delay(10)
                val currentList = _stars.value!!.toMutableList()
                val newList = mutableListOf<Pair<Float, Float>>()
                currentList.forEach { star ->
                    if (star.second < y) {
                        val planetXLine =
                            (star.first).toInt()..(star.first + starSize).toInt()
                        val planetYLine =
                            (star.second).toInt()..(star.second + starSize).toInt()

                        val playerXLine =
                            (_playerXY.value!!.first).toInt()..(_playerXY.value!!.first + playerWidth).toInt()
                        val playerYLine =
                            (_playerXY.value!!.second).toInt()..(_playerXY.value!!.second + playerHeight).toInt()


                        if (playerXLine.any { it in planetXLine } && playerYLine.any { it in planetYLine }) {
                            starsCallback?.invoke()
                        } else {
                            newList.add(star.first to star.second + 6)
                        }
                    }
                }
                _stars.postValue(newList)
            }
        }
    }

    private fun letHeartsMove(y: Int, heartSize: Int, playerWidth: Int, playerHeight: Int) {
        gameScope.launch {
            while (true) {
                delay(10)
                val currentList = _hearts.value!!.toMutableList()
                val newList = mutableListOf<Pair<Float, Float>>()
                currentList.forEach { star ->
                    if (star.second < y) {
                        val heartXLine =
                            (star.first).toInt()..(star.first + heartSize).toInt()
                        val heartYLine =
                            (star.second).toInt()..(star.second + heartSize).toInt()

                        val playerXLine =
                            (_playerXY.value!!.first).toInt()..(_playerXY.value!!.first + playerWidth).toInt()
                        val playerYLine =
                            (_playerXY.value!!.second).toInt()..(_playerXY.value!!.second + playerHeight).toInt()


                        if (playerXLine.any { it in heartXLine } && playerYLine.any { it in heartYLine }) {
                            if (_lives.value!! + 1 <= 3) {
                                _lives.postValue(_lives.value!! + 1)
                            }
                        } else {
                            newList.add(star.first to star.second + 6)
                        }
                    }
                }
                _hearts.postValue(newList)
            }
        }
    }

    private fun letEnergyMove(y: Int, energySize: Int, playerWidth: Int, playerHeight: Int) {
        gameScope.launch {
            while (true) {
                delay(10)
                val currentList = _fallingEnergy.value!!.toMutableList()
                val newList = mutableListOf<Pair<Float, Float>>()
                currentList.forEach { energy ->
                    if (energy.second < y) {
                        val energyXLine =
                            (energy.first).toInt()..(energy.first + energySize).toInt()
                        val energyYLine =
                            (energy.second).toInt()..(energy.second + energySize).toInt()

                        val playerXLine =
                            (_playerXY.value!!.first).toInt()..(_playerXY.value!!.first + playerWidth).toInt()
                        val playerYLine =
                            (_playerXY.value!!.second).toInt()..(_playerXY.value!!.second + playerHeight).toInt()


                        if (playerXLine.any { it in energyXLine } && playerYLine.any { it in energyYLine }) {
                            if (_energy.value!! + 10 <= 100) {
                                _energy.postValue(_energy.value!! + 10)
                            } else {
                                _energy.postValue(100)
                            }
                        } else {
                            newList.add(energy.first to energy.second + 6)
                        }
                    }
                }
                _fallingEnergy.postValue(newList)
            }
        }
    }

    fun playerMoveLeft(limit: Float) {
        if (_playerXY.value!!.first - 4f > limit) {
            _playerXY.postValue(_playerXY.value!!.first - 4 to _playerXY.value!!.second)
        }
    }

    fun playerMoveRight(limit: Float) {
        if (_playerXY.value!!.first + 4f < limit) {
            _playerXY.postValue(_playerXY.value!!.first + 4 to _playerXY.value!!.second)
        }
    }

    fun initPlayer(x: Float, y: Float) {
        _playerXY.postValue(x to y)
    }

    override fun onCleared() {
        super.onCleared()
        gameScope.cancel()
    }
}