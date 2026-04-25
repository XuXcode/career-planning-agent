package com.mssj.service.impl;

import com.mssj.service.StudentInputService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;



@Service
public class StudentInputServiceImpl implements StudentInputService {
    @Override
    // 保存文本内容到文件
    public void saveTextAsFile(String text, File file) throws IOException {
        Files.write(file.toPath(), text.getBytes());
    }

}
