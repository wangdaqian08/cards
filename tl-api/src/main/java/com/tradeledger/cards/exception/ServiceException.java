package com.tradeledger.cards.exception;

/**
 * Created by daqwang on 6/8/21.
 */
public class ServiceException extends RuntimeException {

    private int status;

    public ServiceException(String body, int status) {
        super(body);
        this.status = status;
    }
}
