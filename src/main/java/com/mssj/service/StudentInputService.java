package com.mssj.service;

import com.mssj.pojo.JobProfile;
import com.mssj.pojo.JobRelation;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface StudentInputService {

    // 保存文本内容到文件
    void saveTextAsFile(String studentText, File textFile) throws IOException;


}
