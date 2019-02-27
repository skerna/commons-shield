/*
 * Copyright (c)  2019  SKERNA
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.skerna.shield.jwks;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Jwk provider that loads them from a {@link URL}
 */
@SuppressWarnings("WeakerAccess")
public class UrlJwkProvider implements JwkProvider {

    @VisibleForTesting
    static final String WELL_KNOWN_JWKS_PATH = "/.well-known/jwks.json";

    final URL url;
    private final Integer connectTimeout;
    private final Integer readTimeout;

    /**
     * Creates a provider that loads from the given URL
     * @param url to load the jwks
     */
    public UrlJwkProvider(URL url) {
        this(url, null, null);
    }
    
    /**
     * Creates a provider that loads from the given URL
     * @param url to load the jwks
     * @param connectTimeout connection timeout in milliseconds (null for default)
     * @param readTimeout read timeout in milliseconds (null for default)
     */
    public UrlJwkProvider(URL url, Integer connectTimeout, Integer readTimeout) {
        Preconditions.checkArgument(url != null, "A non-null url is required");
        Preconditions.checkArgument(connectTimeout == null || connectTimeout >= 0, "Invalid connect timeout value '" + connectTimeout + "'. Must be a non-negative integer.");
        Preconditions.checkArgument(readTimeout == null || readTimeout >= 0, "Invalid read timeout value '" + readTimeout + "'. Must be a non-negative integer.");
        
        this.url = url;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * Creates a provider that loads from the given domain's well-known directory.
     * <br><br> It can be a url link 'https://samples.auth0.com' or just a domain 'samples.auth0.com'.
     * If the protocol (http or https) is not provided then https is used by default.
     * The default jwks path "/.well-known/jwks.json" is appended to the given string domain.
     * <br><br> For example, when the domain is "samples.auth0.com"
     * the jwks url that will be used is "https://samples.auth0.com/.well-known/jwks.json"
     * <br><br> Use {@link #UrlJwkProvider(URL)} if you need to pass a full URL.
     * @param domain where jwks is published
     */
    public UrlJwkProvider(String domain) {
        this(urlForDomain(domain));
    }

    static URL urlForDomain(String domain) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(domain), "A domain is required");

        if (!domain.startsWith("http")) {
            domain = "https://" + domain;
        }

        try {
            final URL url = new URL(domain);
            return new URL(url, WELL_KNOWN_JWKS_PATH);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid jwks uri", e);
        }
    }

    private JSONObject getJwks() throws SigningKeyNotFoundException {
        try {
            final URLConnection c = this.url.openConnection();
            if(connectTimeout != null) {
                c.setConnectTimeout(connectTimeout);
            }
            if(readTimeout != null) {
                c.setReadTimeout(readTimeout);
            }
            final InputStream inputStream = c.getInputStream();

            JSONObject jsonObject = new JSONObject(new JSONTokener(inputStream));

            return jsonObject;
        } catch (IOException e) {
            throw new SigningKeyNotFoundException("Cannot obtain jwks from url " + url.toString(), e);
        }
    }

    private List<Jwk> getAll() throws SigningKeyNotFoundException {
        List<Jwk> jwks = Lists.newArrayList();
        @SuppressWarnings("unchecked")
        final JSONArray keys =  getJwks().getJSONArray("keys");

        if (keys == null || keys.length()==0) {
            throw new SigningKeyNotFoundException("No keys found in " + url.toString(), null);
        }

        try {
            for (int index = 0; index < keys.length(); index++) {
                JSONObject key = keys.getJSONObject(index);
                Map<String,Object> map = Utils.toMap(key);
                jwks.add(Jwk.fromValues(map));
            }
        } catch(IllegalArgumentException e) {
            throw new SigningKeyNotFoundException("Failed to parse jwk from json", e);
        }
        return jwks;
    }

    @Override
    public Jwk get(String keyId) throws JwkException {
        final List<Jwk> jwks = getAll();
        for (Jwk jwk: jwks) {
            if (keyId.equals(jwk.getId())) {
                return jwk;
            }
        }
        throw new SigningKeyNotFoundException("No key found in " + url.toString() + " with kid " + keyId, null);
    }
}
