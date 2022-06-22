package com.bottomline.automate.release.model;

import lombok.Data;

import java.util.List;

@Data
public class IssueInfo {
    private String version;
    private List<String> tickets;
}
