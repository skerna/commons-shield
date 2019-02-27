package io.skerna.shield.jwtverifier;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.util.Map;

public class Jwt {

    /**
     * Static method vetify JWT without expose jjwt libs as transitive
     * @param jwt jwt a verificar
     * @param rsaJwksAdapter rsa apdater
     * @return Mapa de jwt entries
     * @throws VerifyException
     */
    public static Map<String,Object> verify(String jwt, RSAJwksAdapter rsaJwksAdapter){
        try{
            Claims data = Jwts.parser()
                    .setSigningKeyResolver(rsaJwksAdapter)
                    .parseClaimsJws(jwt)
                    .getBody();
            return data;

        }catch (Exception ex){
            throw new VerifyException(ex);
        }
    }
}
