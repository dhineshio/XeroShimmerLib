package com.xero.xeroshimmerlib

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator

class ShimmerDrawable : Drawable() {
  private val mUpdateListener = ValueAnimator.AnimatorUpdateListener {
	invalidateSelf()
  }
  private val mShimmerPaint : Paint = Paint()
  private val mDrawRect : Rect = Rect()
  private val mShaderMatrix : Matrix = Matrix()

  private var mValueAnimator : ValueAnimator? = null
  private var mStaticAnimationProgress  = -1f

  private var mShimmer: com.xero.xeroshimmerlib.Shimmer? = null

  init {
	mShimmerPaint.isAntiAlias = true
  }

  fun setShimmer(shimmer: com.xero.xeroshimmerlib.Shimmer?) {
	mShimmer = shimmer
	if (mShimmer != null) {
	  mShimmerPaint.xfermode = PorterDuffXfermode(
		if (mShimmer!!.alphaShimmer) PorterDuff.Mode.DST_IN else PorterDuff.Mode.SRC_IN
	  )
	}
	updateShader()
	updateValueAnimator()
	invalidateSelf()
  }

  fun getShimmer(): com.xero.xeroshimmerlib.Shimmer? {
	return mShimmer
  }

  /** Starts the shimmer animation. */
  fun startShimmer() {
	if (mValueAnimator != null && !isShimmerStarted() && callback != null) {
	  mValueAnimator!!.start()
	}
  }

  /** Stops the shimmer animation. */
  fun stopShimmer() {
	if (mValueAnimator != null && isShimmerStarted()) {
	  mValueAnimator!!.cancel()
	}
  }

  /** Return whether the shimmer animation has been started. */
  fun isShimmerStarted(): Boolean {
	return mValueAnimator?.isStarted == true
  }

  /** Return whether the shimmer animation is running. */
  fun isShimmerRunning(): Boolean {
	return mValueAnimator?.isRunning == true
  }

  override fun onBoundsChange(bounds: Rect) {
	super.onBoundsChange(bounds)
	mDrawRect.set(bounds)
	updateShader()
	maybeStartShimmer()
  }

  fun setStaticAnimationProgress(value: Float) {
	if (value == mStaticAnimationProgress || (value < 0f && mStaticAnimationProgress < 0f)) {
	  return
	}
	mStaticAnimationProgress = value.coerceAtMost(1f)
	invalidateSelf()
  }

  fun clearStaticAnimationProgress() {
	setStaticAnimationProgress(-1f)
  }

  override fun draw(canvas: Canvas) {
	if (mShimmer == null || mShimmerPaint.shader == null) {
	  return
	}

	val tiltTan = Math.tan(Math.toRadians(mShimmer!!.tilt.toDouble())).toFloat()
	val translateHeight = mDrawRect.height() + tiltTan * mDrawRect.width()
	val translateWidth = mDrawRect.width() + tiltTan * mDrawRect.height()
	var dx: Float = 0f
	var dy: Float = 0f
	val animatedValue: Float

	animatedValue = if (mStaticAnimationProgress < 0f) {
	  mValueAnimator?.animatedValue as? Float ?: 0f
	} else {
	  mStaticAnimationProgress
	}

	when (mShimmer!!.direction) {
	  com.xero.xeroshimmerlib.Shimmer.Direction.LEFT_TO_RIGHT -> {
		dx = offset(-translateWidth, translateWidth, animatedValue)
		dy = 0f
	  }
	  com.xero.xeroshimmerlib.Shimmer.Direction.RIGHT_TO_LEFT -> {
		dx = offset(translateWidth, -translateWidth, animatedValue)
		dy = 0f
	  }
	  com.xero.xeroshimmerlib.Shimmer.Direction.TOP_TO_BOTTOM -> {
		dx = 0f
		dy = offset(-translateHeight, translateHeight, animatedValue)
	  }
	  com.xero.xeroshimmerlib.Shimmer.Direction.BOTTOM_TO_TOP -> {
		dx = 0f
		dy = offset(translateHeight, -translateHeight, animatedValue)
	  }
	}

	mShaderMatrix.reset()
	mShaderMatrix.setRotate(mShimmer!!.tilt, mDrawRect.width() / 2f, mDrawRect.height() / 2f)
	mShaderMatrix.preTranslate(dx, dy)
	mShimmerPaint.shader?.setLocalMatrix(mShaderMatrix)
	canvas.drawRect(mDrawRect, mShimmerPaint)
  }

  override fun setAlpha(alpha: Int) {
	TODO("Not yet implemented")
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
	TODO("Not yet implemented")
  }


  override fun getOpacity(): Int {
	return if (mShimmer != null && (mShimmer!!.clipToChildren || mShimmer!!.alphaShimmer)) {
	  PixelFormat.TRANSLUCENT
	} else {
	  PixelFormat.OPAQUE
	}
  }

  private fun offset(start: Float, end: Float, percent: Float): Float {
	return start + (end - start) * percent
  }

  private fun updateValueAnimator() {
	if (mShimmer == null) {
	  return
	}

	val started = mValueAnimator?.isStarted == true
	mValueAnimator?.apply {
	  cancel()
	  removeAllUpdateListeners()
	}

	mValueAnimator = ValueAnimator.ofFloat(0f, 1f + (mShimmer!!.repeatDelay / mShimmer!!.animationDuration).toFloat()).apply {
	  interpolator = LinearInterpolator()
	  repeatMode = mShimmer!!.repeatMode
	  startDelay = mShimmer!!.startDelay.toLong()
	  repeatCount = mShimmer!!.repeatCount
	  duration = (mShimmer!!.animationDuration + mShimmer!!.repeatDelay).toLong()
	  addUpdateListener(mUpdateListener)
	  if (started) start()
	}
  }

  fun maybeStartShimmer() {
	if (mValueAnimator != null
	  && !mValueAnimator!!.isStarted
	  && mShimmer != null
	  && mShimmer!!.autoStart
	  && callback != null) {
	  mValueAnimator!!.start()
	}
  }

  private fun updateShader() {
	val bounds = bounds
	val boundsWidth = bounds.width()
	val boundsHeight = bounds.height()
	if (boundsWidth == 0 || boundsHeight == 0 || mShimmer == null) {
	  return
	}
	val width = mShimmer!!.width(boundsWidth)
	val height = mShimmer!!.height(boundsHeight)

	val shader: Shader = when (mShimmer!!.shape) {
	  com.xero.xeroshimmerlib.Shimmer.Shape.LINEAR -> {
		val vertical = mShimmer!!.direction == com.xero.xeroshimmerlib.Shimmer.Direction.TOP_TO_BOTTOM ||
			mShimmer!!.direction == com.xero.xeroshimmerlib.Shimmer.Direction.BOTTOM_TO_TOP
		val endX = if (vertical) 0 else width
		val endY = if (vertical) height else 0
		LinearGradient(
		  0f, 0f, endX.toFloat(), endY.toFloat(),
		  mShimmer!!.colors, mShimmer!!.positions,
		  Shader.TileMode.CLAMP
		)
	  }
	  com.xero.xeroshimmerlib.Shimmer.Shape.RADIAL -> {
		RadialGradient(
		  (width / 2).toFloat(),
		  (height / 2).toFloat(),
		  (Math.max(width, height) / Math.sqrt(2.0)).toFloat(),
		  mShimmer!!.colors, mShimmer!!.positions,
		  Shader.TileMode.CLAMP
		)
	  }
	  else -> throw IllegalStateException("Unexpected shape: ${mShimmer!!.shape}")
	}
	mShimmerPaint.shader = shader
  }
}