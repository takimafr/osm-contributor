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
package io.jawg.osmcontributor.rest.events;

/**
 * @author Tommy Buonomo on 25/07/16.
 */
public class GoogleAuthenticatedEvent {
    private String consumer;
    private String consumerSecret;
    private String token;
    private String tokenSecret;

    public GoogleAuthenticatedEvent(String token, String tokenSecret, String consumer, String consumerSecret) {
        this.consumer = consumer;
        this.consumerSecret = consumerSecret;
        this.token = token;
        this.tokenSecret = tokenSecret;
    }

    public String getToken() {
        return token;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public String getConsumer() {
        return consumer;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public boolean isSuccessful() {
        return consumer != null && consumerSecret != null && token != null && tokenSecret != null
                && !consumer.isEmpty() && !consumerSecret.isEmpty() && !tokenSecret.isEmpty() && !token.isEmpty();
    }
}
