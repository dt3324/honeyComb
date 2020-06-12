package com.hnf.honeycomb.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class WebCacheControlFilter extends OncePerRequestFilter {

    private static final long DEFAULT_CACHE_SECONDS = 1800;

    @Override
    protected void doFilterInternal(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse,
        FilterChain filterChain
    ) throws ServletException, IOException {
        Assert.notNull(httpServletRequest,"Servlet请求不能为null");
        Assert.notNull(httpServletResponse,"Servlet响应不能为null");
        Assert.notNull(filterChain,"filterChain 不能为 null");
        String originCacheControl = httpServletResponse.getHeader("Cache-Control");
        if (!StringUtils.hasText(originCacheControl)) {
            httpServletResponse.addHeader(
                "Cache-Control", "max-age=" + DEFAULT_CACHE_SECONDS
            );
        } else if (!originCacheControl.contains("max-age") &&
            !originCacheControl.contains("no-cache")) {
            httpServletResponse.addHeader(
                "Cache-Control", originCacheControl + ", max-age=" + DEFAULT_CACHE_SECONDS
            );
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}