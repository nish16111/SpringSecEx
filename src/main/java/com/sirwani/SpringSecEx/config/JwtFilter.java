package com.sirwani.SpringSecEx.config;

import com.sirwani.SpringSecEx.service.JWTService;
import com.sirwani.SpringSecEx.service.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    JWTService jwtService;

    @Autowired
    ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        /* JWT has 3 parts
        1. Header - algo and type {HMAC256, JWT}
        2. Payload - {subject - username, roles - (admin, user), other props - (issuer, issuanceAt, expiration)}
        3. Signature - The signature is used to verify that the token has not been tampered with
        */
        /*
        Example JWT
        Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtaXNoYSIsImlhdCI6MTcyODEwNjkwOSwiZXhwIjoxNzI4MTA3MDE3fQ.wbyhoy6x1KyoKPBU-6M_BrKnvIr_9K5D0jkpSJS2n_I
         */
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if(authHeader != null && authHeader.startsWith("Bearer ")){
            token = authHeader.substring(7);
            System.out.println(token);
            username = jwtService.extractUserName(token);
        }

        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = context.getBean(MyUserDetailsService.class).loadUserByUsername(username);

            if(jwtService.validateToken(username, userDetails)){
                /*
                UsernamePasswordAuthenticationToken is created to tell the SecurityContext that the user
                has been authenticated successfully, basically it is used to create an authenticated object.
                The parameter credential is also passed as null cuz the user has already been validated
                via the token (credentials mean values like pwd).
                 */
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null,
                                                                userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
