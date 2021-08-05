package com.tradeledger.cards.config;

import com.tradeledger.cards.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

/**
 * Created by daqwang on 5/8/21.
 */
public class WebClientFilter {
    private static final Logger LOG = LoggerFactory.getLogger(WebClientFilter.class);

    public static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            logStatus(response);
            return logBody(response);
        });
    }

    private static Mono<ClientResponse> logBody(ClientResponse response) {
        if (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
            return response.bodyToMono(String.class)
                    .flatMap(body -> {
                        LOG.error("Body is {}", body);
                        return Mono.error(new ServiceException(body, response.rawStatusCode()));
                    });
        } else {
            return Mono.just(response);
        }
    }

    private static void logStatus(ClientResponse response) {
        HttpStatus status = response.statusCode();
        LOG.debug("thirdparty service status code {} ({})", status.value(), status.getReasonPhrase());
    }
}
