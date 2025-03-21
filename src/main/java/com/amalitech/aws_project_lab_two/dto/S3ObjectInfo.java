package com.amalitech.aws_project_lab_two.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class S3ObjectInfo {
    private String name;
    private String url;
    private String description;
}