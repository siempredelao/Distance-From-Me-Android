/*
 * Copyright (c) 2019 David Aguiar Gonzalez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gc.david.dfm.ui.animation

import android.animation.Animator
import android.view.View
import android.view.animation.DecelerateInterpolator

/**
 * Created by david on 09.01.17.
 */
object AnimatorUtil {

    private const val ANIMATION_DURATION = 500L

    fun replaceViews(view1: View, view2: View) {
        val elevationChartLocation = IntArray(2)
        view1.getLocationOnScreen(elevationChartLocation)

        val showChartFabLocation = IntArray(2)
        view2.getLocationOnScreen(showChartFabLocation)

        val leftDelta = showChartFabLocation[0] - elevationChartLocation[0]
        val topDelta = showChartFabLocation[1] - elevationChartLocation[1]

        val widthScale = view2.width.toFloat() / view1.width
        val heightScale = view2.height.toFloat() / view1.height

        view1.pivotX = 0F
        view1.pivotY = 0F

        view1.animate()
                .scaleX(widthScale)
                .scaleY(heightScale)
                .translationX(leftDelta.toFloat())
                .translationY(topDelta.toFloat())
                .alpha(0.5F)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(DecelerateInterpolator())
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {
                        // nothing
                    }

                    override fun onAnimationEnd(animator: Animator) {
                        view1.apply {
                            translationX = 0F
                            translationY = 0F
                            scaleX = 1F
                            scaleY = 1F
                            alpha = 1F
                            visibility = View.INVISIBLE
                        }

                        view2.visibility = View.VISIBLE
                    }

                    override fun onAnimationCancel(animator: Animator) {
                        // nothing
                    }

                    override fun onAnimationRepeat(animator: Animator) {
                        // nothing
                    }
                })
    }
}
