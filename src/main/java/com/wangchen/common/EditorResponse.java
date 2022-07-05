package com.wangchen.common;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EditorResponse {
    String errno;
    List<String> data;
    public EditorResponse(String errno, List<String> data){
        this.errno = errno;
        this.data = data;
    }
    public EditorResponse(String errno, String data){
        this.errno = errno;
        this.data = new ArrayList<>();
        this.data.add(data);
    }
}
