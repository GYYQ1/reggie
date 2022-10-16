package com.yinqing;

import org.junit.jupiter.api.Test;

public class UploadFileTest {
    @Test
    public void test01(){
        String fileName="QuoYinqQing.jsp";
        String substring = fileName.substring(fileName.indexOf("."));
        System.out.println(substring);
    }
}
