# SKERNA SHIELD

KEYCLOAK es una implementación de OpenIDConnect el cual a su vez esta basado en el standar OAUTh2.0
la rotación de claves es una característica importante en keycloack

```text
    This makes it possible to regularly rotate the keys without any downtime or interruption to users.
    
    
    Extract Keycloack 
    
    It’s recommended to regularly rotate keys. To do so you should start by creating new keys with a higher priority
    than the existing active keys. Or create new keys with the same priority and making the previous keys passive.
    Once new keys are available all new tokens and cookies will be signed with the new keys. When a user authenticates
    to an application the SSO cookie is updated with the new signature. When OpenID Connect tokens are refreshed new tokens are signed with the new keys. This means that over time all cookies and tokens will use the new keys and after a while the old keys can be removed.

```
### Utilidad:

- Estas librerías permiten obtener los token usando JWKS de keycloack

- Permite trabajar con ambientes donde se utiliza ampliamente JWT / Oauth / OIDC

### Motivación

Uno de los problemas encontrados al trabajar con JWT en microservicios es la necesidad  de manter un clave valida en el servidor
para la validación de tokens enviados por el cliente, es aquí que en lugar de copiar claves por todo lado, la librería
accede a .well-know config para generar las claves de validación localmente.

```text
The discovery endpoint can be used to retrieve metadata about your IdentityServer - it returns information like the issuer name, key material, supported scopes etc. See the spec for more details.

The discovery endpoint is available via /.well-known/openid-configuration relative to the base address, e.g.:

https://demo.identityserver.io/.well-known/openid-configuration

```

In OIDC, the server provides features such as:
- Rotation of keys.
- Regeneration

This library allows to keep the server free of configurations (secret keys, files) regarding the verification of tokens.
for this, the endpoint provided by OIDC that returns the set of JWK keys is used, and regenerate the key locally

Features .
- Keys generated from JWK
- Local key cache
- Keycloack
- RSA

## What problems does it solve?

Estas librerias se mantienen por R2B, para el desarrollo de servicios internos y su 
interacion con el servidor OIDC interno.


## Modulos

#### SE4J-JWKS

Este modulo permite resolver las claves criptograficas para la verfificación de tokens, desde un servidor que soporte la especificación 
[RFC7517](https://tools.ietf.org/html/rfc7517#section-4), proporcionando una lista de valores de tipo

```json
    {
      "kty":"EC",
      "crv":"P-256",
      "x":"f83OJ3D2xF1Bg8vub9tLe1gHMzV76e8Tus9uPHvRVEU",
      "y":"x_FEzRu9m36HLN_tue659LNpXW6pCyStikYjKIWI5a0",
      "kid":"Public key used in JWS spec Appendix A.3 example"
     }
``` 


#### SEC4J-JWTRESOLVER

Este modulo permite obtener los tokens de acceso desde un Refresh Token, y obteniendo nuevos tokens
cuando el token actualmente en cache este cercano a su periodo de expiración

#### SEC4J-JWTVERIFIER

Este modulo permite verificar un token existente usando JWKS como provedor de tokens

#### SEC4J-VERTX
Este modulo permite utilizar los modulos existentes en un entorno de desarrollo donde se use Vertx como Plataforma de desarrollo de microservicio

## Install

### Maven

```xml
<dependency>
    <groupId>io.r2b.olibs.jwks4j</groupId>
    <artifactId>jwks-rsa</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```gradle
compile(group: 'io.r2b.olibs.jwks4j', name: 'jwks-rsa', version: '1.0.0-SNAPSHOT')
```


