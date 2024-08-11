package com.bjit.royalclub.royalclubfootball.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Response {

    private Long timeStamp;
    private int statusCode;
    private String status;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object content;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer numberOfElement;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long rowCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ErrorResponse> errors;
}
