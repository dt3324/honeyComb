package com.hnf.honeycomb.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * @author hnf
 */
@Aspect
@Component
public class ImpactSimpleServiceImplMonitorAspect {

    private static final Logger logger = LoggerFactory.getLogger(ImpactSimpleServiceImplMonitorAspect.class);

    @Pointcut(value = "within(com.hnf.honeycomb.serviceimpl.ImpactSimpleServiceImpl)")
    public void point() {
    }

    @Pointcut(value = "execution(public * com.hnf.honeycomb.dao..*.*CountAndQuery(..))")
    public void dao() {
    }

    @Around(value = "point()")
    public Object monitorPoint(ProceedingJoinPoint pjp) throws Throwable {
        return monitorPJP(pjp);
    }

    @Around(value = "dao()")
    public Object monitorDao(ProceedingJoinPoint pjp) throws Throwable {
        return monitorPJP(pjp);
    }

    private Object monitorPJP(ProceedingJoinPoint pjp) throws Throwable {
        final Instant start = Instant.now();
        Object result = pjp.proceed();
        final Duration between = Duration.between(start, Instant.now());
        try {
            logger.debug(
                    String.format("\n executing %s\n\twith args ([%s])\n\texpired %d millis\n\tand returns (%s)",
                            pjp.getSignature(),
                            StringUtils.join(pjp.getArgs(), "],["),
                            between.abs().toMillis(),
                            new ObjectMapper().writeValueAsString(result))
            );
        } catch (Exception e) {
            logger.warn("exception occurred when logging", e);
        }
        logger.info("access search by{ " + pjp.getSignature().getName() + " 方法 " + "}running AS ms: " + between.toMillis());
        return result;
    }


}
