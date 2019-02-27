# jvm-jwksrsa
Este repo toma como base a https://github.com/auth0/jwks-rsa-java



## Install

### Maven

```xml
<dependency>
    <groupId>io.vintx.libart.jvm</groupId>
    <artifactId>jvm-jwksrsa</artifactId>
    <version>X</version>
</dependency>
```

### Gradle

```gradle
compile 'io.vintx.libart.jvm:jvm-jwksrsa:x.x.x'
```

## Usage

The JSON Web Tokens you get from the Authorization Server include a [key id](https://tools.ietf.org/html/rfc7515#section-4.1.4) header parameter ("kid"), used to uniquely identify the Key used to sign the token.

i.e.: Given the following JWT:

```
eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlJrSTVNakk1T1VZNU9EYzFOMFE0UXpNME9VWXpOa1ZHTVRKRE9VRXpRa0ZDT1RVM05qRTJSZyJ9.eyJpc3MiOiJodHRwczovL3NhbmRyaW5vLmF1dGgwLmNvbS8iLCJzdWIiOiJhdXRoMHw1NjMyNTAxZjQ2OGYwZjE3NTZmNGNhYjAiLCJhdWQiOiJQN2JhQnRTc3JmQlhPY3A5bHlsMUZEZVh0ZmFKUzRyViIsImV4cCI6MTQ2ODk2NDkyNiwiaWF0IjoxNDY4OTI4OTI2fQ.NaNeRSDCNu522u4hcVhV65plQOiGPStgSzVW4vR0liZYQBlZ_3OKqCmHXsu28NwVHW7_KfVgOz4m3BK6eMDZk50dAKf9LQzHhiG8acZLzm5bNMU3iobSAJdRhweRht544ZJkzJ-scS1fyI4gaPS5aD3SaLRYWR0Xsb6N1HU86trnbn-XSYSspNqzIUeJjduEpPwC53V8E2r1WZXbqEHwM9_BGEeNTQ8X9NqCUvbQtnylgYR3mfJRL14JsCWNFmmamgNNHAI0uAJo84mu_03I25eVuCK0VYStLPd0XFEyMVFpk48Bg9KNWLMZ7OUGTB_uv_1u19wKYtqeTbt9m1YcPMQ
```

Decode it using any JWT library or tool like [jwt.io](https://jwt.io/?value=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IlJrSTVNakk1T1VZNU9EYzFOMFE0UXpNME9VWXpOa1ZHTVRKRE9VRXpRa0ZDT1RVM05qRTJSZyJ9.eyJpc3MiOiJodHRwczovL3NhbmRyaW5vLmF1dGgwLmNvbS8iLCJzdWIiOiJhdXRoMHw1NjMyNTAxZjQ2OGYwZjE3NTZmNGNhYjAiLCJhdWQiOiJQN2JhQnRTc3JmQlhPY3A5bHlsMUZEZVh0ZmFKUzRyViIsImV4cCI6MTQ2ODk2NDkyNiwiaWF0IjoxNDY4OTI4OTI2fQ.NaNeRSDCNu522u4hcVhV65plQOiGPStgSzVW4vR0liZYQBlZ_3OKqCmHXsu28NwVHW7_KfVgOz4m3BK6eMDZk50dAKf9LQzHhiG8acZLzm5bNMU3iobSAJdRhweRht544ZJkzJ-scS1fyI4gaPS5aD3SaLRYWR0Xsb6N1HU86trnbn-XSYSspNqzIUeJjduEpPwC53V8E2r1WZXbqEHwM9_BGEeNTQ8X9NqCUvbQtnylgYR3mfJRL14JsCWNFmmamgNNHAI0uAJo84mu_03I25eVuCK0VYStLPd0XFEyMVFpk48Bg9KNWLMZ7OUGTB_uv_1u19wKYtqeTbt9m1YcPMQ) and extract the `kid` parameter from the Header claims.

```json
{
  "typ": "JWT",
  "alg": "RS256",
  "kid": "RkI5MjI5OUY5ODc1N0Q4QzM0OUYzNkVGMTJDOUEzQkFCOTU3NjE2Rg"
}
```

Use this `kid` on any of the `JwkProviders` enumerated below to obtain the signing key provided by the JWKS endpoint you've configured.


### UrlJwkProvider

`UrlJwkProvider` fetches the jwk from `/.well-known/jwks.json` of the supplied domain issuer and returns a `Jwk` if the `kid` matches one of the registered keys.

```java
JwkProvider provider = new UrlJwkProvider("https://samples.auth0.com/");
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```


Also it can load `jwks.json` file from any given Url (even to a local file in your filesystem).

```java
JwkProvider provider = new UrlJwkProvider(new URL("https://samples.auth0.com/"));
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

### GuavaCachedJwkProvider

`GuavaCachedJwkProvider` cache the jwk in a LRU in memory cache, if the jwk is not found in the cache it will ask another provider for it and store it's result in the cache.

> By default it stores 5 keys for 10 hours but these values can be changed

```java
JwkProvider http = new UrlJwkProvider("https://samples.auth0.com/");
JwkProvider provider = new GuavaCachedJwkProvider(http);
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

### RateLimitJwkProvider

`RateLimitJwkProvider` will limit the amounts of different signing keys to get in a given time frame.

> By default the rate is limited to 10 different keys per minute but these values can be changed

```java
JwkProvider url = new UrlJwkProvider("https://samples.auth0.com/");
Bucket bucket = new Bucket(10, 1, TimeUnit.MINUTES);
JwkProvider provider = new RateLimitJwkProvider(url, bucket);
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

### JwkProviderBuilder

To create a provider for domain `https://samples.auth0.com` with cache and rate limit:

```java
JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
    .build();
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```

and specifying cache and rate limit attributes

```java
JwkProvider provider = new JwkProviderBuilder("https://samples.auth0.com/")
    .cached(10, 24, TimeUnit.HOURS)
    .rateLimited(10, 1, TimeUnit.MINUTES)
    .build();
Jwk jwk = provider.get("{kid of the signing key}"); //throws Exception when not found or can't get one
```
