/*
 * Copyright (c) 2020 - Present Ranjith Suranga. All Rights Reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */

package lk.ijse.exp.jwt.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

/**
 * @author : Ranjith Suranga <suranga@ijse.lk>
 **/
@WebFilter(filterName = "SecurityFilter", urlPatterns = "/*")
public class SecurityFilter extends HttpFilter {

    /* No one knows about this, only we know */
    private static final String SECRET = "Cpt.Sura-Boy.jwt-secret@IJSE2020-12-22";

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {

        /* If there is no authorization header or if it doesn't have login credentials or a token
         * then there is no point of continuing */
        String authorization = req.getHeader("Authorization");
        if (authorization == null || !(authorization.matches("Basic .+") || authorization.matches("Bearer .+"))){
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        /* Extract the token or base64encoded login credentials */
        String token = authorization.replaceAll("(Basic)|(Bearer)", "").trim();

        /* Let's find out whether it is the token or login credentials */
        if (authorization.matches("Basic .+")){
            String decodedCredentials = new String(Base64.getDecoder().decode(token.getBytes()));
            String[] credentials = decodedCredentials.split(":");

            /* Credentials array length should be two after splitting, otherwise it is invalid */
            if (credentials.length != 2){
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            /* Let's check whether the username and password is correct
             * You may need to make a call to the DB server here in a real-world application */
            if (credentials[0].equals("admin@ijse.lk") && credentials[1].equals("admin")){
                String jws = Jwts.builder()
                        .setIssuer("sura-boy")
                        .claim("email", credentials[0])

                        /* Play with the expiration time :) It is fun */
                        .setExpiration(new Date(new Date().getTime() + (1000 * 60 * 2)))
                        .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                        .compact();

                res.addHeader("X-Auth-Token", jws);
                chain.doFilter(req,res);
            }else{
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

        }else{
            try {
                Jws<Claims> jwsClaims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                        .requireIssuer("sura-boy")
                        .build()
                        .parseClaimsJws(token);

                /* We can trust this token, let's proceed */
                chain.doFilter(req, res);

            }catch (ExpiredJwtException exp){

                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("text/html");
                res.getWriter().println("<h1>Token has expired, Please log in again</h1>");
                return;

            }catch (JwtException exp){

                exp.printStackTrace();

                /* We can't trust this token */
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
    }
}
