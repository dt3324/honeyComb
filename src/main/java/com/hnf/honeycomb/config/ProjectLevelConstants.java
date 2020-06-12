package com.hnf.honeycomb.config;

import com.hnf.honeycomb.bean.enumerations.HttpVersionEnum;
import org.springframework.web.cors.CorsConfiguration;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface ProjectLevelConstants {

    Boolean METRICS_JMX_ENABLED = Boolean.TRUE;
    Boolean METRICS_LOGS_ENABLED = Boolean.TRUE;
    Long METRICS_LOGS_IN_SECONDS_REPORTFREQUENCY = 3600L;

    Long HTTP_CACHE_TIMETOLIVEINDAY = 1L;
    HttpVersionEnum HTTP_VERSION = HttpVersionEnum.V_1_1;

    CorsConfiguration CORS_CONFIG = ((Supplier<CorsConfiguration>) () -> {
        CorsConfiguration result = new CorsConfiguration();
        result.addAllowedOrigin("*");
        result.addAllowedMethod("*");
        result.addAllowedHeader("*");
        result.addExposedHeader("Authorization");
        result.addExposedHeader("X-Total-Count");
        result.addExposedHeader("Link");
        result.setAllowCredentials(true);
        result.setMaxAge(1800L);
        return result;
    }).get();

    // Spring profiles for development, test and production, see https://www.jhipster.tech/profiles/
    String SPRING_PROFILE_DEVELOPMENT = "dev";
    String SPRING_PROFILE_TEST = "test";
    String SPRING_PROFILE_PRODUCTION = "prod";
    // Spring profile used when deploying with Spring Cloud (used when deploying to CloudFoundry)
    String SPRING_PROFILE_CLOUD = "cloud";
    // Spring profile used when deploying to Heroku
    String SPRING_PROFILE_HEROKU = "heroku";
    // Spring profile used when deploying to Amazon ECS
    String SPRING_PROFILE_AWS_ECS = "aws-ecs";
    // Spring profile used to disable swagger
    String SPRING_PROFILE_SWAGGER = "swagger";
    // Spring profile used to disable running liquibase
    String SPRING_PROFILE_NO_LIQUIBASE = "no-liquibase";
    // Spring profile used when deploying to Kubernetes and OpenShift
    String SPRING_PROFILE_K8S = "k8s";
    // The load_factor of best performance calculated by [lim(log(2)/log(s/(s-1)))/s as s->infinity]
    float CUSTOM_LOAD_FACTOR = 0.694f;
    String FREQUENT_USE_DATETIME_FORMAT = "yyyy-MM-dd H:mm:ss";
    String MAP_KEY_MD5 = "md5";

    Predicate<String> PROVINCE_CODE_PREDICATE = s -> s.matches("((0[1-9])|([1-9][0-9]))");
    Predicate<String> CITY_CODE_PREDICATE = s -> s.matches("((0[1-9])|([1-9][0-9]))((0[1-9])|([1-9][0-9]))");
    Predicate<String> COUNTY_CODE_PREDICATE = s -> s.matches("((0[1-9])|([1-9][0-9]))((0[1-9])|([1-9][0-9]))((0[1-9a-zA-Z])|([1-9a-zA-Z][0-9a-zA-Z]))");
    Predicate<String> TEAM_CODE_PREDICATE = s -> s.matches("((0[1-9])|([1-9][0-9]))((0[1-9])|([1-9][0-9]))00((0[1-9a-zA-Z])|([1-9a-zA-Z][0-9a-zA-Z]))");
    Predicate<String> COUNTY_DEPARTMENT_CODE_PREDICATE = s -> {
        final String countyCode = s.substring(0, 6);
        final String remains = s.substring(6, s.length());
        return COUNTY_CODE_PREDICATE.test(countyCode) &&
                remains.chars().allMatch(c -> String.valueOf((char) c).matches("([0-9a-zA-Z])")) &&
                remains.chars().anyMatch(c -> String.valueOf((char) c).matches("([1-9a-zA-Z])"));
    };
    Predicate<String> TEAM_DEPARTMENT_CODE_PREDICATE = s -> {
        final String countyCode = s.substring(0, 8);
        final String remains = s.substring(8, s.length());
        return TEAM_CODE_PREDICATE.test(countyCode) &&
                remains.chars().allMatch(c -> String.valueOf((char) c).matches("([0-9a-zA-Z])")) &&
                remains.chars().anyMatch(c -> String.valueOf((char) c).matches("([1-9a-zA-Z])"));
    };
    Predicate<String> COUNTY_AND_TEAM_CODE_PREDICATE = s -> Stream.of(
            COUNTY_CODE_PREDICATE,
            TEAM_CODE_PREDICATE
    ).anyMatch(p -> p.test(s));
    Predicate<String> TOWN_DEPARTMENT_CODE_PREDICATE = s -> Stream.of(
            COUNTY_DEPARTMENT_CODE_PREDICATE,
            TEAM_DEPARTMENT_CODE_PREDICATE
    ).anyMatch(p -> p.test(s));
}
