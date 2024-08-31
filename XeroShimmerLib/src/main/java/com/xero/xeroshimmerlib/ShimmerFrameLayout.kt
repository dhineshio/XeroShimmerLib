package com.xero.xeroshimmerlib

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

class ShimmerFrameLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  private val mContentPaint = Paint()
  private val mShimmerDrawable = com.xero.xeroshimmerlib.ShimmerDrawable()

  private var mShowShimmer = true
  private var mStoppedShimmerBecauseVisibility = false

  init {
	init(context, attrs, defStyleAttr, defStyleRes)
  }

  private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
	setWillNotDraw(false)
	mShimmerDrawable.setCallback(this)

	if (attrs == null) {
	  // Create an instance of AlphaHighlightBuilder
	  val alphaHighlightBuilder = com.xero.xeroshimmerlib.Shimmer.Builder.AlphaHighlightBuilder()
	  setShimmer(alphaHighlightBuilder.build())
	  return
	}

	val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ShimmerFrameLayout, defStyleAttr, defStyleRes)
	try {
	  val shimmerBuilder = if (a.hasValue(R.styleable.ShimmerFrameLayout_shimmer_colored) &&
		a.getBoolean(R.styleable.ShimmerFrameLayout_shimmer_colored, false)) {
		com.xero.xeroshimmerlib.Shimmer.Builder.ColorHighlightBuilder()
	  } else {
		com.xero.xeroshimmerlib.Shimmer.Builder.AlphaHighlightBuilder()
	  }
	  setShimmer(shimmerBuilder.consumeAttributes(a).build())
	} finally {
	  a.recycle()
	}
  }

  fun setShimmer(shimmer: com.xero.xeroshimmerlib.Shimmer?) : ShimmerFrameLayout {
	mShimmerDrawable.setShimmer(shimmer)
	if (shimmer != null && shimmer.clipToChildren) {
	  setLayerType(LAYER_TYPE_HARDWARE, mContentPaint)
	} else {
	  setLayerType(LAYER_TYPE_NONE, null)
	}
	return this
  }

  fun getShimmer(): com.xero.xeroshimmerlib.Shimmer? {
	return mShimmerDrawable.getShimmer()
  }

  /** Starts the shimmer animation. */
  fun startShimmer() {
	if (isAttachedToWindow) {
	  mShimmerDrawable.startShimmer()
	}
  }

  /** Stops the shimmer animation. */
  fun stopShimmer() {
	mStoppedShimmerBecauseVisibility = false
	mShimmerDrawable.stopShimmer()
  }

  /** Return whether the shimmer animation has been started. */
  fun isShimmerStarted(): Boolean {
	return mShimmerDrawable.isShimmerStarted()
  }

  /**
   * Sets the ShimmerDrawable to be visible.
   *
   * @param startShimmer Whether to start the shimmer again.
   */
  fun showShimmer(startShimmer: Boolean) {
	mShowShimmer = true
	if (startShimmer) {
	  startShimmer()
	}
	invalidate()
  }

  /** Sets the ShimmerDrawable to be invisible, stopping it in the process. */
  fun hideShimmer() {
	stopShimmer()
	mShowShimmer = false
	invalidate()
  }

  /** Return whether the shimmer drawable is visible. */
  fun isShimmerVisible(): Boolean {
	return mShowShimmer
  }

  fun isShimmerRunning(): Boolean {
	return mShimmerDrawable.isShimmerRunning()
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
	super.onLayout(changed, left, top, right, bottom)
	val width = width
	val height = height
	mShimmerDrawable.setBounds(0, 0, width, height)
  }

  override fun onVisibilityChanged(changedView: View, visibility: Int) {
	super.onVisibilityChanged(changedView, visibility)
	if (visibility != View.VISIBLE) {
	  if (isShimmerStarted()) {
		stopShimmer()
		mStoppedShimmerBecauseVisibility = true
	  }
	} else if (mStoppedShimmerBecauseVisibility) {
	  mShimmerDrawable.maybeStartShimmer()
	  mStoppedShimmerBecauseVisibility = false
	}
  }

  override fun onAttachedToWindow() {
	super.onAttachedToWindow()
	mShimmerDrawable.maybeStartShimmer()
  }

  override fun onDetachedFromWindow() {
	super.onDetachedFromWindow()
	stopShimmer()
  }

  override fun dispatchDraw(canvas: Canvas) {
	super.dispatchDraw(canvas)
	if (mShowShimmer) {
	  mShimmerDrawable.draw(canvas)
	}
  }

  override fun verifyDrawable(who: Drawable): Boolean {
	return super.verifyDrawable(who) || who == mShimmerDrawable
  }

  fun setStaticAnimationProgress(value: Float) {
	mShimmerDrawable.setStaticAnimationProgress(value)
  }

  fun clearStaticAnimationProgress() {
	mShimmerDrawable.clearStaticAnimationProgress()
  }
}