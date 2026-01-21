package com.hospital.gateway.filter;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import com.alibaba.fastjson.JSONObject;
import com.hospital.gateway.dto.CheckTokenResp;
import com.hospital.gateway.exception.BusinessException;
import com.hospital.gateway.feign.PassportFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author lvzeqiang
 * @date 2022/7/1 20:21
 * @description
 **/
@Slf4j
@Component
public class TokenFilter implements GlobalFilter, Ordered {
    @Autowired
    private PassportFeign passportFeign;

    private final String traceId = "X-HOS-TRACE-ID";

    private static Cache<String, Long> APPID_CACHE = CacheUtil.newTimedCache(1000 * 60 * 30);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String tid = insertMDC(request);
        try {
            List<String> list = request.getHeaders().get(HttpHeaders.AUTHORIZATION);

            if (CollectionUtils.isEmpty(list)) {
                throw new BusinessException("Authorization为空");
            }
            String token = list.get(0).replace("Bearer ", "");
            log.info("token : {}", token);

            Long appId;
            if (!APPID_CACHE.containsKey(token) || (appId = APPID_CACHE.get(token)) == null) {
                String result = passportFeign.checkToken(token);

                log.info("token :{}, response :{}", token, result);
                CheckTokenResp resp = JSONObject.parseObject(result, CheckTokenResp.class);

                if (resp.getCode() != 200) {
                    throw new BusinessException("token异常");
                }
                appId = resp.getAppId();
                if (null == appId) {
                    throw new BusinessException("应用 id 不存在");
                }
                APPID_CACHE.put(token, appId);
            }


            Long finalAppId = appId;
            request.mutate().headers(new Consumer<HttpHeaders>() {
                @Override
                public void accept(HttpHeaders t) {
                    t.add("appId", finalAppId.toString());
                    t.add(traceId, tid);
                }
            }).build();
            return chain.filter(exchange);
        } finally {
            if (StringUtils.isNotBlank(tid)) {
                MDC.remove(traceId);
            }
        }
    }

    private String insertMDC(ServerHttpRequest request) {
        try {
            List<String> tids = request.getHeaders().get(traceId);
            String tid;
            if (!CollectionUtils.isEmpty(tids)) {
                tid = tids.get(0);
                MDC.put("traceId", tid);

            } else {
                tid = UUID.randomUUID().toString()
                        .replace("-", "").toLowerCase(Locale.getDefault());
                MDC.put("traceId", tid);
            }
            return tid;
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public int getOrder() {
        return -99;
    }
}
