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
package io.jawg.osmcontributor.flickr.util;

import android.util.Log;

import com.flickr4java.flickr.util.Base64;
import com.github.scribejava.core.model.Verb;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.jawg.osmcontributor.BuildConfig;


public class FlickrSecurityUtils {

    /*=========================================*/
    /*-------------CONSTANTS-------------------*/
    /*=========================================*/
    private static final String SEPARATOR = "&";

    private static final String EQUAL = "=";

    private static final String HMAC_SHA1 = "HmacSHA1";

    private static final String UTF_8 = "UTF-8";

    private static final String TAG = "FlickrSecurityUtils";

    /*=========================================*/
    /*------------UTILS METHOD-----------------*/
    /*=========================================*/
    /**
     * All flickr request must be signed. The signature is obtained from a concatenation of
     * elements in the url. See convertUrl method.
     * @param convertedRequestUrl converted url
     * @return HMAC-SHA1 signature
     */
    public static String getSignatureFromRequest(String convertedRequestUrl, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(new SecretKeySpec(key.getBytes(), HMAC_SHA1));
            byte[] digest = mac.doFinal(convertedRequestUrl.getBytes());
            if (BuildConfig.DEBUG) {
                Log.d(TAG, convertedRequestUrl);
            }
            return new String(Base64.encode(digest));
        } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
            return null;
        }
    }

    /**
     * This method must be called before signing the request. The converted request url is obtained
     * by concatenation of elemments of the url. The converted url contains the HTTP verb, the base
     * url and all the parameters sorted by alphabetical order
     * @param baseUrl base url for the request
     * @param httpVerb request type (GET or POST)
     * @param params request params (key : param's name, value : param's value
     * @return A converted url with following format: GET&url&param with escaped characters
     */
    public static String convertUrl(String baseUrl, Verb httpVerb, Map<String, String> params) {
        try {
            StringBuilder urlBuilder = new StringBuilder(httpVerb.name())
                    .append(SEPARATOR)
                    .append(URLEncoder.encode(baseUrl, UTF_8))
                    .append(SEPARATOR);

            StringBuilder paramsBuilder = new StringBuilder();
            for (Map.Entry<String, String> param : params.entrySet()) {
                paramsBuilder.append(param.getKey()).append(EQUAL).append(param.getValue()).append(SEPARATOR);
            }

            int lastIndexSep = paramsBuilder.lastIndexOf(SEPARATOR);
            String urlToEncode = (lastIndexSep >= 0) ? paramsBuilder.deleteCharAt(lastIndexSep).toString() : paramsBuilder.toString();
            String paramsEncoded = URLEncoder.encode(urlToEncode, UTF_8);
            return urlBuilder.append(paramsEncoded).toString();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * Create an Authorization header for Oauth params.
     * @param params oauth params
     * @return authorization string
     */
    public static String getAuthorizationHeader(Map<String, String> params) {
        StringBuilder authorizationBuilder = new StringBuilder("OAuth ");
        for (Map.Entry<String, String> param : params.entrySet()) {
            authorizationBuilder.append(param.getKey()).append("=\"").append(param.getValue()).append("\",");
        }
        return authorizationBuilder.deleteCharAt(authorizationBuilder.lastIndexOf(",")).toString();
    }
}