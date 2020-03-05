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
package io.jawg.osmcontributor.ui.utils.views;

import android.os.CountDownTimer;

import org.greenrobot.eventbus.EventBus;


/**
 * {@link android.os.CountDownTimer} who posts events (sticky or not) on the default {@link org.greenrobot.eventbus.EventBus}
 * on finish.
 */
public class EventCountDownTimer extends CountDownTimer {

    private Object event;
    private Object stickyEvent;
    private EventBus bus;

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public EventCountDownTimer(long millisInFuture, long countDownInterval, EventBus bus) {
        super(millisInFuture, countDownInterval);
        this.bus = bus;
    }

    /**
     * Do nothing.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public void onTick(long millisUntilFinished) {
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Post the event and post sticky the sticky event on the default EventBus.
     */
    @Override
    public void onFinish() {
        if (event != null) {
            bus.post(event);
        }
        if (stickyEvent != null) {
            bus.postSticky(stickyEvent);
        }
    }

    /**
     * Set the event to post on finish.
     *
     * @param event The event to post.
     */
    public void setEvent(Object event) {
        this.event = event;
    }

    /**
     * Set the sticky event to post on finish.
     *
     * @param stickyEvent The sticky event to post.
     */
    public void setStickyEvent(Object stickyEvent) {
        this.stickyEvent = stickyEvent;
    }
}
