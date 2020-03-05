/**
 * Copyright (C) 2019 Takima
 *
 * This file is part of OSM Contributor.
 *
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jawg.osmcontributor.ui.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.ScaleGestureDetector;
import android.view.animation.DecelerateInterpolator;

import java.util.Queue;

import io.jawg.osmcontributor.utils.LimitedQueue;

/**
 * @author Tommy Buonomo on 16/06/16.
 */
public abstract class ZoomAnimationGestureDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private static final float MAX_SPEED = 50.0f;
    private static final float MIN_SPEED = 2.0f;
    private Queue<Float> previousSpeedQueue = new LimitedQueue<>(5);

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        previousSpeedQueue.clear();
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        float currentSpeed = scaleGestureDetector.getPreviousSpan() - scaleGestureDetector.getCurrentSpan();
        previousSpeedQueue.add(currentSpeed);
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        float sum = 0;
        for (Float speed : previousSpeedQueue) {
            sum += speed;
        }
        float moy = sum / previousSpeedQueue.size();
        if (Math.abs(moy) > MAX_SPEED) {
            moy = moy > 0 ? MAX_SPEED : -MAX_SPEED;
        }

        ValueAnimator valueAnimator = ObjectAnimator.ofFloat(-moy / 1000, 0);
        int duration = (int) (Math.abs(moy) * 12);
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        onZoomAnimationEnd(valueAnimator);

        if (Math.abs(moy) > MIN_SPEED) {
            onZoomAnimationEnd(valueAnimator);
        }
    }

    public abstract void onZoomAnimationEnd(ValueAnimator animator);
}
