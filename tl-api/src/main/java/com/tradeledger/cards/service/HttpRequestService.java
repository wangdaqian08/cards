package com.tradeledger.cards.service;

import com.tradeledger.cards.config.WebClientConfig;
import com.tradeledger.cards.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserter;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Created by daqwang on 6/8/21.
 */
@Service
public class HttpRequestService {

    private final WebClientConfig webClientConfig;

    @Value("${request.timeout.second:15}")
    private int requestTimeoutInSecond;

    @Value("${request.retry:3}")
    private int retryTimes;

    @Value("${request.retry.backoff.second:5}")
    private int backOffInSecond;

    @Autowired
    public HttpRequestService(WebClientConfig webClientConfig) {
        this.webClientConfig = webClientConfig;
    }

    /**
     * Common post request builder. It's config with 3 times retry, at 5s, 10s ,15s after each failed request.
     *
     * @param url
     * @param body
     * @param clazz
     * @param <S>
     * @param <T>
     * @return object in Mono.
     */
    public <S, T> Mono<S> buildPostRequest(String url, BodyInserter<T, ReactiveHttpOutputMessage> body, Class<S> clazz) {
        return webClientConfig.thirdPartyWebClient()
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, response -> Mono.error(new ServiceException("Third party api server error", response.rawStatusCode())))
                .bodyToMono(clazz)
                .timeout(Duration.ofSeconds(requestTimeoutInSecond))
                .retryWhen(Retry.backoff(retryTimes, Duration.ofSeconds(backOffInSecond))
                        .filter(throwable -> throwable instanceof ServiceException)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            throw new ServiceException("third party api failed to process after max retries", HttpStatus.SERVICE_UNAVAILABLE.value());
                        }));
    }
}
