/*
 * Copyright (c) 2018 David Aguiar Gonzalez
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

package gc.david.dfm.ui.animation;

import android.animation.Animator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by david on 09.01.17.
 */
public class AnimatorUtil {

    private static final long ANIMATION_DURATION = 500L;

    public static void replaceViews(final View view1, final View view2) {
        final int[] elevationChartLocation = new int[2];
        view1.getLocationOnScreen(elevationChartLocation);

        final int[] showChartFabLocation = new int[2];
        view2.getLocationOnScreen(showChartFabLocation);

        final int leftDelta = showChartFabLocation[0] - elevationChartLocation[0];
        final int topDelta = showChartFabLocation[1] - elevationChartLocation[1];

        final float widthScale = (float) view2.getWidth() / view1.getWidth();
        final float heightScale = (float) view2.getHeight() / view1.getHeight();

        view1.setPivotX(0);
        view1.setPivotY(0);

        view1.animate()
             .scaleX(widthScale)
             .scaleY(heightScale)
             .translationX(leftDelta)
             .translationY(topDelta)
             .alpha(0.5F)
             .setDuration(ANIMATION_DURATION)
             .setInterpolator(new DecelerateInterpolator())
             .setListener(new Animator.AnimatorListener() {
                 @Override
                 public void onAnimationStart(Animator animator) {
                     // nothing
                 }

                 @Override
                 public void onAnimationEnd(Animator animator) {
                     view1.setTranslationX(0);
                     view1.setTranslationY(0);
                     view1.setScaleX(1F);
                     view1.setScaleY(1F);
                     view1.setAlpha(1F);
                     view1.setVisibility(View.INVISIBLE);

                     view2.setVisibility(View.VISIBLE);
                 }

                 @Override
                 public void onAnimationCancel(Animator animator) {
                     // nothing
                 }

                 @Override
                 public void onAnimationRepeat(Animator animator) {
                     // nothing
                 }
             });
    }
}
