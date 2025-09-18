package com.back.domain.article.article.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Article {
    private long id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String title;
    private String body;
    private boolean isBlind;
}
