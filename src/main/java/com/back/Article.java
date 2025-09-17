package com.back;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Article {
    private long id;
    private String title;
    private String body;
    private boolean isBlind;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
