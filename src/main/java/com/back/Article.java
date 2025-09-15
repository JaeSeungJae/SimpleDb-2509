package com.back;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Article {
    private int id;
    private String createdDate;
    private String modifiedDate;
    private String title;
    private String body;
    private boolean isBlind;
}
