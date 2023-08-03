package com.rocket.game.ui.game

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rocket.game.R
import com.rocket.game.core.library.shortToast
import com.rocket.game.databinding.FragmentGameBinding
import com.rocket.game.domain.other.AppPrefs
import com.rocket.game.ui.other.ViewBindingFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FragmentGame : ViewBindingFragment<FragmentGameBinding>(FragmentGameBinding::inflate) {
    private val viewModel: GameViewModel by viewModels()
    private val sp by lazy { AppPrefs(requireContext()) }
    private var moveScope = CoroutineScope(Dispatchers.Default)
    private val xy by lazy {
        val display = requireActivity().windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        Pair(size.x, size.y)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMovement()
        initPlayer()
        setStars()

        binding.buttonHome.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonBuy.setOnClickListener {
            if (sp.getStars() >= 15) {
                sp.buy()
                viewModel.buyEnergy()
                setStars()
            } else {
                shortToast(requireContext(), "Not enough stars")
            }
        }

        viewModel.starsCallback = {
            sp.addStar()
            setStars()
        }

        viewModel.playerXY.observe(viewLifecycleOwner) {
            binding.player.x = it.first
            binding.player.y = it.second
        }

        viewModel.planets.observe(viewLifecycleOwner) {
            binding.planetsLayout.removeAllViews()
            it.forEach { planet ->
                val image = when (planet.value) {
                    1 -> R.drawable.obstacle01
                    2 -> R.drawable.obstacle02
                    3 -> R.drawable.obstacle03
                    4 -> R.drawable.obstacle04
                    else -> R.drawable.obstacle05
                }
                val planetView = ImageView(requireContext())
                planetView.layoutParams = ViewGroup.LayoutParams(dpToPx(70), dpToPx(70))
                planetView.setImageResource(image)
                planetView.x = planet.position.first
                planetView.y = planet.position.second
                binding.planetsLayout.addView(planetView)
            }
        }

        viewModel.stars.observe(viewLifecycleOwner) {
            binding.starsLayout.removeAllViews()
            it.forEach { star ->
                val starView = ImageView(requireContext())
                starView.layoutParams = ViewGroup.LayoutParams(dpToPx(30), dpToPx(30))
                starView.setImageResource(R.drawable.img_star)
                starView.x = star.first
                starView.y = star.second
                binding.starsLayout.addView(starView)
            }
        }

        viewModel.hearts.observe(viewLifecycleOwner) {
            binding.heartsLayout.removeAllViews()
            it.forEach { heart ->
                val heartView = ImageView(requireContext())
                heartView.layoutParams = ViewGroup.LayoutParams(dpToPx(30), dpToPx(30))
                heartView.setImageResource(R.drawable.img_heart)
                heartView.x = heart.first
                heartView.y = heart.second
                binding.heartsLayout.addView(heartView)
            }
        }

        viewModel.fallingEnergy.observe(viewLifecycleOwner) {
            binding.energyLayout.removeAllViews()
            it.forEach { energy ->
                val energyView = ImageView(requireContext())
                energyView.layoutParams = ViewGroup.LayoutParams(dpToPx(30), dpToPx(30))
                energyView.setImageResource(R.drawable.img_energy)
                energyView.x = energy.first
                energyView.y = energy.second
                binding.energyLayout.addView(energyView)
            }
        }

        viewModel.energy.observe(viewLifecycleOwner) {
            binding.energy.text = it.toString()
            if (it == 0 && viewModel.gameState) {
                end()
            }
        }

        viewModel.lives.observe(viewLifecycleOwner) {
            binding.livesLayout.removeAllViews()
            repeat(it) {
                val liveView = ImageView(requireContext())
                liveView.layoutParams = LinearLayout.LayoutParams(dpToPx(25), dpToPx(25)).apply {
                    marginStart = dpToPx(3)
                    marginEnd = dpToPx(3)
                }
                liveView.setImageResource(R.drawable.img_heart)
                binding.livesLayout.addView(liveView)
            }
            if (it == 0 && viewModel.gameState) {
                end()
            }
        }

        lifecycleScope.launch {
            delay(20)
            if (viewModel.gameState) {
                viewModel.start(
                    xy.first,
                    dpToPx(70),
                    xy.second,
                    binding.player.width,
                    binding.player.height,
                    dpToPx(30),
                    dpToPx(30),
                    dpToPx(30),
                )
            }
        }
    }

    private fun end() {
        viewModel.stop()
        viewModel.gameState = false
        findNavController().navigate(FragmentGameDirections.actionFragmentGameToDialogEnd(viewModel.scores))
    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    private fun initPlayer() {
        lifecycleScope.launch {
            delay(20)
            if (viewModel.playerXY.value!! == 0f to 0f) {
                viewModel.initPlayer(
                    (xy.first / 2 - (binding.player.width / 2)).toFloat(),
                    (xy.second - dpToPx(180)).toFloat()
                )
            }
        }
    }

    private fun setStars() {
        binding.stars.text = sp.getStars().toString()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setMovement() {
        binding.buttonLeft.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveLeft(0f)
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveLeft(0f)
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }

        binding.buttonRight.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveRight(xy.first.toFloat() - binding.player.width)
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveRight(
                                xy.first.toFloat() - binding.player.width
                            )
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }
}