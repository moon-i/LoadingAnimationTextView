package com.moon.loadinganimationtextview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import java.lang.Exception
import kotlin.collections.ArrayList

/**
 *  LoadingTextView.kt
 *  Created by moon.i on 2021/11/16
 */

class LoadingTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    private var IN_DURATION = 50L
    private var OUT_DURATION = 130L
    private var textList = ArrayList<TextView>()
    private var fadeInAnimationList = ArrayList<Animation>()
    private var wantFadeInStop = false
    private var wantFadeOutStop = false
    private var stopFadeInAnimationListener: (() -> Unit)? = null
    private var stopFadeOutAnimationListener: (() -> Unit)? = null

    var isLoading = false

    init {
        gravity = VERTICAL
    }

    // 텍스트가 위에서 내려오는 애니메이션 animation - 첫 화면 들어올때
    fun startFirst(textInfo: TextAttribute) {
        removeAllViews()
        textList.clear()
        isLoading = true

        val fadeInAnimationList = ArrayList<Animation>()
        for (index in textInfo.text.indices) {
            val fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.top_to_down_fade_in)
            fadeInAnimationList.add(fadeInAnimation.apply {
                duration = IN_DURATION
                startOffset = (duration * ((index * 0.9).toLong() + 1))
            })
            val textView = makeTextView(textInfo, index)
            addView(textView)
            textList.add(textView)

            textView.startAnimation(fadeInAnimation)
        }
    }

    fun setText(
        textInfo: TextAttribute,
    ) {
        removeAllViews()
        textList.clear()

        for (index in textInfo.text.indices) {
            val textView = makeTextView(textInfo, index)
            addView(textView)
            textList.add(textView)
        }
    }

    fun stopFadeInLoading(stopFadeInFunction: (()->Unit)? = null) {
        wantFadeInStop = true
        stopFadeInFunction?.let {
            stopFadeInAnimationListener = it
        }
    }

    fun stopFadeOutLoading(stopFadeOutFunction: (()->Unit)? = null) {
        wantFadeOutStop = true
        stopFadeOutFunction?.let {
            stopFadeOutAnimationListener = it
        }
    }

    fun startLoading() {
        isLoading = true
        val fadeOutAnimation = AnimationUtils.loadAnimation(context, R.anim.top_to_down_fade_out)
        textList.mapIndexed { i, textView ->
            fadeOutAnimation.apply {
                duration = OUT_DURATION
                startOffset = IN_DURATION
                if (i == textList.size - 1) {
                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {
                            if (wantFadeOutStop) { // stop을 부른 경우 글자 등장까지 실행하고 이후 로딩을 종료
                                isLoading = false
                                wantFadeOutStop = false
                                stopFadeOutAnimationListener?.invoke()
                            } else {
                                startTopToCenterAnimation()
                            }
                        }

                        override fun onAnimationRepeat(animation: Animation?) {}
                        override fun onAnimationStart(animation: Animation?) {}
                    })
                }
            }
            textView.startAnimation(fadeOutAnimation)
        }
    }

    fun startOneCycleAnimation(textInfo: TextAttribute) {
        removeAllViews()
        textList.clear()

        for (index in textInfo.text.indices) {
            val textView = makeTextView(textInfo, index)
            addView(textView)
            textList.add(textView)
        }

        isLoading = true
        val fadeOutAnimation = AnimationUtils.loadAnimation(context, R.anim.top_to_down_fade_out)

        textList.mapIndexed { i, textView ->
            fadeOutAnimation.apply {
                duration = OUT_DURATION
                startOffset = IN_DURATION
                if (i == textList.size - 1) {
                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {
                            for (j in 0 until textList.size) {
                                val fadeInAnimation =
                                    AnimationUtils.loadAnimation(context, R.anim.top_to_down_fade_in)
                                fadeInAnimation.apply {
                                    duration = IN_DURATION
                                    startOffset = duration * (j + 1)
                                    if (j == textList.size - 1) {
                                        setAnimationListener(object : Animation.AnimationListener {
                                            override fun onAnimationEnd(animation: Animation?) {
                                                isLoading = false
                                            }
                                            override fun onAnimationRepeat(animation: Animation?) {}
                                            override fun onAnimationStart(animation: Animation?) {}
                                        })
                                    }
                                }
                                textList[j].startAnimation(fadeInAnimation)
                            }
                        }
                        override fun onAnimationRepeat(animation: Animation?) {}
                        override fun onAnimationStart(animation: Animation?) {}
                    })
                }
                textView.startAnimation(fadeOutAnimation)
            }
        }
    }

    private fun makeTextView(textInfo: TextAttribute, index: Int): TextView {
        val params = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT)

        return TextView(context).apply {
            layoutParams = params
            text = textInfo.text[index].toString()
            textInfo.textTypeface?.let { typeface = it }
            textInfo.textSizeDp?.let {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, it)
            } ?: run {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SIZE)
            }
            textInfo.textColor?.let {
                setTextColor(Color.parseColor(it))
            } ?: run {
                setTextColor(Color.parseColor(DEFAULT_COLOR))
            }
        }
    }

    private fun startTopToCenterAnimation() {
        isLoading = true
        fadeInAnimationList.clear()
        for (i in 0 until textList.size) {
            val fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.top_to_down_fade_in)
            fadeInAnimationList.add(fadeInAnimation.apply {
                duration = IN_DURATION
                startOffset = duration * (i + 1)
                if (i == textList.size - 1) {
                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {
                            if (wantFadeInStop) { // stop을 부른 경우 글자 등장까지 실행하고 이후 로딩을 종료
                                stopFadeInAnimationListener?.invoke()
                                isLoading = false
                                wantFadeInStop = false
                            } else {
                                startLoading()
                            }
                        }

                        override fun onAnimationRepeat(animation: Animation?) {}
                        override fun onAnimationStart(animation: Animation?) {}
                    })
                }
            })
            textList[i].startAnimation(fadeInAnimationList[i])
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        try {
            super.dispatchDraw(canvas)
            return
        } catch (e: Exception) {
            return
        }
    }

    companion object {
        const val DEFAULT_COLOR = "#000000"
        const val DEFAULT_SIZE = 28f

        data class TextAttribute(
            val text: String,
            val textSizeDp: Float? = null,
            val textColor: String? = null,
            val textTypeface: Typeface? = null,
        )
    }
}