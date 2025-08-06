package com.assembliestore.api.common.type;

import com.google.auto.value.AutoValue.Builder;

import lombok.Data;

@Data
@Builder
public class FileFormat {

    private String title;
    private String fileExtension;
    private String url;

}
