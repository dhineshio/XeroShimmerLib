package com.xero.xeroshimmer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.xero.xeroshimmerlib.ShimmerFrameLayout

class MainActivity : AppCompatActivity() {
  private lateinit var shimmerViewContainer: ShimmerFrameLayout
//  private lateinit var presetButtons: Array<Button>
  override fun onCreate(savedInstanceState: Bundle?) {
	super.onCreate(savedInstanceState)
	enableEdgeToEdge()
	setContentView(R.layout.activity_main)
	shimmerViewContainer = findViewById(R.id.shimmer_view_container)
//	presetButtons =
//	  arrayOf(
//		findViewById(R.id.preset_button0),
//		findViewById(R.id.preset_button2),
//		findViewById(R.id.preset_button3),
//		findViewById(R.id.preset_button4),
//		findViewById(R.id.preset_button5),
//		findViewById(R.id.preset_button6))
//	presetButtons.forEach { it.setOnClickListener(this@MainActivity) }
//	selectPreset(0)
  }

//  override fun onClick(v: View) {
//	selectPreset(presetButtons.indexOf(v as Button))
//  }

  override fun onResume() {
	super.onResume()
	shimmerViewContainer.startShimmer()
  }

  override fun onPause() {
	shimmerViewContainer.stopShimmer()
	super.onPause()
  }
//
//  private fun selectPreset(preset: Int) {
//	val shimmerBuilder = Shimmer.Builder.AlphaHighlightBuilder()
//	shimmerViewContainer.setShimmer(
//	  when (preset) {
//		1 -> {
//		  // Slow and reverse
//		  shimmerBuilder.setDuration(5000).setRepeatMode(ValueAnimator.REVERSE)
//		}
//		2 -> {
//		  // Thin, straight and transparent
//		  shimmerBuilder.setBaseAlpha(0.1f).setDropoff(0.1f).setTilt(0f)
//		}
//		3 -> {
//		  // Sweep angle 90
//		  shimmerBuilder.setDirection(Shimmer.Direction.TOP_TO_BOTTOM).setTilt(0f)
//		}
//		4 -> {
//		  // Spotlight
//		  shimmerBuilder
//			.setBaseAlpha(0f)
//			.setDuration(2000)
//			.setDropoff(0.1f)
//			.setIntensity(0.35f)
//			.setShape(Shimmer.Shape.RADIAL)
//		}
//		5 -> {
//		  // Spotlight angle 45
//		  shimmerBuilder
//			.setBaseAlpha(0f)
//			.setDuration(2000)
//			.setDropoff(0.1f)
//			.setIntensity(0.35f)
//			.setTilt(45f)
//			.setShape(Shimmer.Shape.RADIAL)
//		}
//		6 -> {
//		  // Off
//		  null
//		}
//		else -> {
//		  shimmerBuilder
//		}
//	  }?.build())
//  }
}