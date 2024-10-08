package com.xero.xeroshimmerlib

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.annotation.Px
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sin

class Shimmer {
  companion object {
	private val COMPONENT_COUNT = 4
  }

  @Retention(AnnotationRetention.SOURCE)
  @IntDef(Shape.LINEAR, Shape.RADIAL)
  annotation class Shape {
	companion object {
	  /** Linear gives a ray reflection effect. */
	  const val LINEAR = 0

	  /** Radial gives a spotlight effect. */
	  const val RADIAL = 1
	}
  }

  /** Direction of the shimmer's sweep. */
  @Retention(AnnotationRetention.SOURCE)
  @IntDef(
	Direction.LEFT_TO_RIGHT,
	Direction.TOP_TO_BOTTOM,
	Direction.RIGHT_TO_LEFT,
	Direction.BOTTOM_TO_TOP
  )
  annotation class Direction {
	companion object {
	  const val LEFT_TO_RIGHT = 0
	  const val TOP_TO_BOTTOM = 1
	  const val RIGHT_TO_LEFT = 2
	  const val BOTTOM_TO_TOP = 3
	}
  }

  // Arrays
  val positions = FloatArray(COMPONENT_COUNT)
  val colors = IntArray(COMPONENT_COUNT)
  val bounds = RectF()

  // Enums/Annotations
  @Direction
  var direction: Int = Direction.LEFT_TO_RIGHT

  @ColorInt
  var highlightColor: Int = Color.WHITE

  @ColorInt
  var baseColor: Int = 0x4cffffff

  @Shape
  var shape: Int = Shape.LINEAR

  // Integers and Floats
  var fixedWidth: Int = 0
  var fixedHeight: Int = 0

  var widthRatio: Float = 1f
  var heightRatio: Float = 1f
  var intensity: Float = 0f
  var dropoff: Float = 0.5f
  var tilt: Float = 20f

  // Booleans
  var clipToChildren: Boolean = true
  var autoStart: Boolean = true
  var alphaShimmer: Boolean = true

  // Animation settings
  var repeatCount: Int = ValueAnimator.INFINITE
  var repeatMode: Int = ValueAnimator.RESTART
  var animationDuration: Int = 1000
  var repeatDelay: Int = 0
  var startDelay: Int = 0

  fun width(width: Int): Int {
	return if (fixedWidth > 0) fixedWidth else (widthRatio * width).roundToInt()
  }

  fun height(height: Int): Int {
	return if (fixedHeight > 0) fixedHeight else (heightRatio * height).roundToInt()
  }

  fun updateColors() {
	when (shape) {
	  Shape.LINEAR -> {
		colors[0] = baseColor
		colors[1] = highlightColor
		colors[2] = highlightColor
		colors[3] = baseColor
	  }

	  Shape.RADIAL -> {
		colors[0] = highlightColor
		colors[1] = highlightColor
		colors[2] = baseColor
		colors[3] = baseColor
	  }

	  else -> {
		// Handle other shapes if needed or repeat the default case logic
		colors[0] = baseColor
		colors[1] = highlightColor
		colors[2] = highlightColor
		colors[3] = baseColor
	  }
	}
  }

  fun updatePositions() {
	when (shape) {
	  Shape.LINEAR -> {
		positions[0] = max((1f - intensity - dropoff) / 2f, 0f)
		positions[1] = max((1f - intensity - 0.001f) / 2f, 0f)
		positions[2] = min((1f + intensity + 0.001f) / 2f, 1f)
		positions[3] = min((1f + intensity + dropoff) / 2f, 1f)
	  }

	  Shape.RADIAL -> {
		positions[0] = 0f
		positions[1] = min(intensity, 1f)
		positions[2] = min(intensity + dropoff, 1f)
		positions[3] = 1f
	  }

	  else -> {
		// Handle other shapes if needed or repeat the default case logic
		positions[0] = max((1f - intensity - dropoff) / 2f, 0f)
		positions[1] = max((1f - intensity - 0.001f) / 2f, 0f)
		positions[2] = min((1f + intensity + 0.001f) / 2f, 1f)
		positions[3] = min((1f + intensity + dropoff) / 2f, 1f)
	  }
	}
  }

  fun updateBounds(viewWidth: Int, viewHeight: Int) {
	val magnitude = max(viewWidth, viewHeight)
	val rad = Math.PI / 2f - Math.toRadians(tilt % 90f.toDouble())
	val hyp = magnitude / sin(rad)
	val padding = (3 * round((hyp - magnitude) / 2f)).toInt()
	bounds.set(
	  -padding.toFloat(),
	  -padding.toFloat(),
	  width(viewWidth) + padding.toFloat(),
	  height(viewHeight) + padding.toFloat()
	)
  }

  abstract class Builder<T : Builder<T>> {
	val mShimmer: Shimmer = Shimmer()
	protected abstract fun self(): T

	/** Applies all specified options from the AttributeSet. */
	fun consumeAttributes(context: Context, attrs: AttributeSet): T {
	  val a = context.obtainStyledAttributes(attrs, R.styleable.ShimmerFrameLayout, 0, 0)
	  return consumeAttributes(a)
	}

	fun consumeAttributes(a: TypedArray): T {
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_clip_to_children)) {
		setClipToChildren(
		  a.getBoolean(
			R.styleable.ShimmerFrameLayout_shimmer_clip_to_children, mShimmer.clipToChildren
		  )
		)
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_auto_start)) {
		setAutoStart(
		  a.getBoolean(R.styleable.ShimmerFrameLayout_shimmer_auto_start, mShimmer.autoStart)
		)
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_base_alpha)) {
		setBaseAlpha(a.getFloat(R.styleable.ShimmerFrameLayout_shimmer_base_alpha, 0.3f))
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_highlight_alpha)) {
		setHighlightAlpha(a.getFloat(R.styleable.ShimmerFrameLayout_shimmer_highlight_alpha, 1f))
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_duration)) {
		setDuration(
		  a.getInt(
			R.styleable.ShimmerFrameLayout_shimmer_duration,
			mShimmer.animationDuration
		  )
		)
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_repeat_count)) {
		setRepeatCount(
		  a.getInt(R.styleable.ShimmerFrameLayout_shimmer_repeat_count, mShimmer.repeatCount)
		)
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_repeat_delay)) {
		setRepeatDelay(
		  a.getInt(
			R.styleable.ShimmerFrameLayout_shimmer_repeat_delay,
			mShimmer.repeatDelay
		  )
		)
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_repeat_mode)) {
		setRepeatMode(
		  a.getInt(R.styleable.ShimmerFrameLayout_shimmer_repeat_mode, mShimmer.repeatMode)
		)
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_start_delay)) {
		setStartDelay(
		  a.getInt(R.styleable.ShimmerFrameLayout_shimmer_start_delay, mShimmer.startDelay)
		)
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_direction)) {
		val direction =
		  a.getInt(R.styleable.ShimmerFrameLayout_shimmer_direction, mShimmer.direction)
		when (direction) {
		  Direction.LEFT_TO_RIGHT -> setDirection(Direction.LEFT_TO_RIGHT)
		  Direction.TOP_TO_BOTTOM -> setDirection(Direction.TOP_TO_BOTTOM)
		  Direction.RIGHT_TO_LEFT -> setDirection(Direction.RIGHT_TO_LEFT)
		  Direction.BOTTOM_TO_TOP -> setDirection(Direction.BOTTOM_TO_TOP)
		  else -> setDirection(Direction.LEFT_TO_RIGHT)
		}
	  }

	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_shape)) {
		val shape = a.getInt(R.styleable.ShimmerFrameLayout_shimmer_shape, mShimmer.shape)
		when (shape) {
		  Shape.LINEAR -> setShape(Shape.LINEAR)
		  Shape.RADIAL -> setShape(Shape.RADIAL)
		  else -> setShape(Shape.LINEAR)
		}
	  }

	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_dropoff)) {
		setDropoff(a.getFloat(R.styleable.ShimmerFrameLayout_shimmer_dropoff, mShimmer.dropoff))
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_fixed_width)) {
		setFixedWidth(
		  a.getDimensionPixelSize(
			R.styleable.ShimmerFrameLayout_shimmer_fixed_width, mShimmer.fixedWidth
		  )
		)
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_fixed_height)) {
		setFixedHeight(
		  a.getDimensionPixelSize(
			R.styleable.ShimmerFrameLayout_shimmer_fixed_height, mShimmer.fixedHeight
		  )
		)
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_intensity)) {
		setIntensity(
		  a.getFloat(R.styleable.ShimmerFrameLayout_shimmer_intensity, mShimmer.intensity)
		)
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_width_ratio)) {
		setWidthRatio(
		  a.getFloat(R.styleable.ShimmerFrameLayout_shimmer_width_ratio, mShimmer.widthRatio)
		)
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_height_ratio)) {
		setHeightRatio(
		  a.getFloat(R.styleable.ShimmerFrameLayout_shimmer_height_ratio, mShimmer.heightRatio)
		)
	  }
	  if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_tilt)) {
		setTilt(a.getFloat(R.styleable.ShimmerFrameLayout_shimmer_tilt, mShimmer.tilt))
	  }
	  return self()
	}

	/** Copies the configuration of an already built Shimmer to this builder */
	fun copyFrom(other: Shimmer): T {
	  setDirection(other.direction)
	  setShape(other.shape)
	  setFixedWidth(other.fixedWidth)
	  setFixedHeight(other.fixedHeight)
	  setWidthRatio(other.widthRatio)
	  setHeightRatio(other.heightRatio)
	  setIntensity(other.intensity)
	  setDropoff(other.dropoff)
	  setTilt(other.tilt)
	  setClipToChildren(other.clipToChildren)
	  setAutoStart(other.autoStart)
	  setRepeatCount(other.repeatCount)
	  setRepeatMode(other.repeatMode)
	  setRepeatDelay(other.repeatDelay)
	  setStartDelay(other.startDelay)
	  setDuration(other.animationDuration)
	  mShimmer.baseColor = other.baseColor
	  mShimmer.highlightColor = other.highlightColor
	  return self()
	}

	/** Sets the direction of the shimmer's sweep. See [Direction]. */
	fun setDirection(@Direction direction: Int): T {
	  mShimmer.direction = direction
	  return self()
	}

	/** Sets the shape of the shimmer. See [Shape]. */
	fun setShape(@Shape shape: Int): T {
	  mShimmer.shape = shape
	  return self()
	}

	/** Sets the fixed width of the shimmer, in pixels. */
	fun setFixedWidth(@Px fixedWidth: Int): T {
	  require(fixedWidth >= 0) { "Given invalid width: $fixedWidth" }
	  mShimmer.fixedWidth = fixedWidth
	  return self()
	}

	/** Sets the fixed height of the shimmer, in pixels. */
	fun setFixedHeight(@Px fixedHeight: Int): T {
	  require(fixedHeight >= 0) { "Given invalid height: $fixedHeight" }
	  mShimmer.fixedHeight = fixedHeight
	  return self()
	}

	/** Sets the width ratio of the shimmer, multiplied against the total width of the layout. */
	fun setWidthRatio(widthRatio: Float): T {
	  require(widthRatio >= 0f) { "Given invalid width ratio: $widthRatio" }
	  mShimmer.widthRatio = widthRatio
	  return self()
	}

	/** Sets the height ratio of the shimmer, multiplied against the total height of the layout. */
	fun setHeightRatio(heightRatio: Float): T {
	  require(heightRatio >= 0f) { "Given invalid height ratio: $heightRatio" }
	  mShimmer.heightRatio = heightRatio
	  return self()
	}

	/** Sets the intensity of the shimmer. A larger value causes the shimmer to be larger. */
	fun setIntensity(intensity: Float): T {
	  require(intensity >= 0f) { "Given invalid intensity value: $intensity" }
	  mShimmer.intensity = intensity
	  return self()
	}

	/**
	 * Sets how quickly the shimmer's gradient drops-off. A larger value causes a sharper drop-off.
	 */
	fun setDropoff(dropoff: Float): T {
	  require(dropoff >= 0f) { "Given invalid dropoff value: $dropoff" }
	  mShimmer.dropoff = dropoff
	  return self()
	}

	/** Sets the tilt angle of the shimmer in degrees. */
	fun setTilt(tilt: Float): T {
	  mShimmer.tilt = tilt
	  return self()
	}

	/** Sets the base alpha amount in the range [0, 1]. */
	fun setBaseAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): T {
	  val intAlpha = (clamp(0f, 1f, alpha) * 255f).toInt()
	  mShimmer.baseColor = (intAlpha shl 24) or (mShimmer.baseColor and 0x00FFFFFF)
	  return self()
	}

	/** Sets the shimmer alpha amount in the range [0, 1]. */
	fun setHighlightAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): T {
	  val intAlpha = (clamp(0f, 1f, alpha) * 255f).toInt()
	  mShimmer.highlightColor = (intAlpha shl 24) or (mShimmer.highlightColor and 0x00FFFFFF)
	  return self()
	}

	/**
	 * Sets whether the shimmer will clip to the children's contents, or if it will opaquely draw on
	 * top of the children.
	 */
	fun setClipToChildren(status: Boolean): T {
	  mShimmer.clipToChildren = status
	  return self()
	}

	/** Sets whether the shimmering animation will start automatically. */
	fun setAutoStart(status: Boolean): T {
	  mShimmer.autoStart = status
	  return self()
	}

	/**
	 * Sets how often the shimmering animation will repeat. See [android.animation.ValueAnimator.setRepeatCount].
	 */
	fun setRepeatCount(repeatCount: Int): T {
	  mShimmer.repeatCount = repeatCount
	  return self()
	}

	/**
	 * Sets how the shimmering animation will repeat. See [android.animation.ValueAnimator.setRepeatMode].
	 */
	fun setRepeatMode(mode: Int): T {
	  mShimmer.repeatMode = mode
	  return self()
	}

	/** Sets how long to wait in between repeats of the shimmering animation. */
	fun setRepeatDelay(millis: Int): T {
	  require(millis >= 0) { "Given a negative repeat delay: $millis" }
	  mShimmer.repeatDelay = millis
	  return self()
	}

	/** Sets how long to wait for starting the shimmering animation. */
	fun setStartDelay(millis: Int): T {
	  require(millis >= 0) { "Given a negative start delay: $millis" }
	  mShimmer.startDelay = millis
	  return self()
	}

	/** Sets how long the shimmering animation takes to do one full sweep. */
	fun setDuration(millis: Int): T {
	  require(millis >= 0) { "Given a negative duration: $millis" }
	  mShimmer.animationDuration = millis
	  return self()
	}

	fun build(): Shimmer {
	  mShimmer.updateColors()
	  mShimmer.updatePositions()
	  return mShimmer
	}

	private fun clamp(min: Float, max: Float, value: Float): Float {
	  return value.coerceIn(min, max)
	}

	open class AlphaHighlightBuilder : Builder<AlphaHighlightBuilder>() {
	  init {
		mShimmer.alphaShimmer = true
	  }

	  override fun self(): AlphaHighlightBuilder = this
	}

	open class ColorHighlightBuilder : Builder<ColorHighlightBuilder>() {
	  init {
		mShimmer.alphaShimmer = false
	  }

	  /** Sets the highlight color for the shimmer. */
	  fun setHighlightColor(@ColorInt color: Int): ColorHighlightBuilder {
		mShimmer.highlightColor = color
		return self()
	  }

	  /** Sets the base color for the shimmer. */
	  fun setBaseColor(@ColorInt color: Int): ColorHighlightBuilder {
		mShimmer.baseColor = (mShimmer.baseColor and 0xFF000000.toInt()) or (color and 0x00FFFFFF)
		return self()
	  }

	  override fun self(): ColorHighlightBuilder {
		return this
	  }
	}
  }
}