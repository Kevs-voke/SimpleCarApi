package com.rtx.Car.Exceptions;


    public class CarServiceException extends RuntimeException {

        public CarServiceException(String message) {
            super(message);
        }

        public CarServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }


