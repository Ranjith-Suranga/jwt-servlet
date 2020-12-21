/*
 * Copyright (c) 2020 - Present Ranjith Suranga. All Rights Reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */

package lk.ijse.exp.jwt.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author : Ranjith Suranga <suranga@ijse.lk>
 **/
@WebFilter(filterName = "SecurityFilter", urlPatterns = "/*")
public class SecurityFilter extends HttpFilter {

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
        String token = authorization.replace("(Basic)|(Bearer) ", "");

        chain.doFilter(req,res);
    }
}
