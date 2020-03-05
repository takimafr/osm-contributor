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
package io.jawg.osmcontributor.database.preferences;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.jawg.osmcontributor.R;
import io.jawg.osmcontributor.rest.utils.AuthenticationRequestInterceptor;
import io.jawg.osmcontributor.rest.utils.MapParams;

/**
 * Class managing the credentials preferences of the application.
 */
@Singleton
public class LoginPreferences {

    Application application;
    SharedPreferences sharedPreferences;

    @Inject
    public LoginPreferences(Application application, SharedPreferences sharedPreferences) {
        this.application = application;
        this.sharedPreferences = sharedPreferences;
    }

    /**
     * Get the login from the preferences.
     *
     * @return The login.
     */
    public String retrieveLogin() {
        return sharedPreferences.getString(application.getString(R.string.shared_prefs_login), "");
    }

    /**
     * Get the password from the preferences.
     *
     * @return The password.
     */
    public String retrievePassword() {
        return sharedPreferences.getString(application.getString(R.string.shared_prefs_password), "");
    }

    /**
     * Update the login and the password in the preferences.
     *
     * @param login    The new login.
     * @param password The new password.
     */
    public void updateCredentials(String login, String password) {
        sharedPreferences.edit()
                .putString(application.getString(R.string.shared_prefs_login), login)
                .putString(application.getString(R.string.shared_prefs_password), password)
                .apply();
    }

    /**
     * Get the OAuth params from the preferences
     *
     * @return the OAuth params
     */
    public Map<String, String> retrieveOAuthParams() {
        String consumer = sharedPreferences.getString(application.getString(R.string.shared_prefs_consumer), null);
        String consumerSecret = sharedPreferences.getString(application.getString(R.string.shared_prefs_consumer_secret), null);
        String token = sharedPreferences.getString(application.getString(R.string.shared_prefs_token), null);
        String token_secret = sharedPreferences.getString(application.getString(R.string.shared_prefs_token_secret), null);

        if (consumer != null && consumerSecret != null && token != null && token_secret != null) {
            return new MapParams<String, String>().put(AuthenticationRequestInterceptor.CONSUMER_PARAM, consumer)
                    .put(AuthenticationRequestInterceptor.CONSUMER_SECRET_PARAM, consumerSecret)
                    .put(AuthenticationRequestInterceptor.TOKEN_PARAM, token)
                    .put(AuthenticationRequestInterceptor.TOKEN_SECRET_PARAM, token_secret)
                    .toMap();
        }

        return null;
    }

    public void updateGoogleCredentials(String consumerKey, String consumerKeySecret, String token, String tokenSecret) {
        sharedPreferences.edit()
                .putString(application.getString(R.string.shared_prefs_consumer_secret), consumerKeySecret)
                .putString(application.getString(R.string.shared_prefs_consumer), consumerKey)
                .putString(application.getString(R.string.shared_prefs_token), token)
                .putString(application.getString(R.string.shared_prefs_token_secret), tokenSecret)
                .apply();
    }

    public void setLogged(boolean logged) {
        sharedPreferences.edit().putBoolean(application.getString(R.string.shared_prefs_logged), logged).apply();
    }

    public boolean isLogged() {
        return sharedPreferences.getBoolean(application.getString(R.string.shared_prefs_logged), false);
    }
}
