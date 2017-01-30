package com.example.eventhub;

public enum HTTPMethodEnum {

    HTTP_GET("GET"), HTTP_POST("POST"), HTTP_HEADER("HEADER");

    private String method;

    HTTPMethodEnum(String method) {
       this.method = method;
    }

    public String toString() {
        return method;
    }

}
