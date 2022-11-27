package com.example.demo.Model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Website {

    private final String URL;

    private int score;
    private String content, title;
}
