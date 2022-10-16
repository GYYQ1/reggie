package com.yinqing.reggie.controller;

import com.yinqing.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * 文件的上传和下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

   //从配置文件中获取
    @Value("${reggie.path}")
    private String basePath;
    /**
     * 文件的上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file)  {
        log.info(file.toString());
        //file是一个临时文件，存储在某个地方，需要转储到某个位置，否则本次请求完成后自动删除
            //获取原始文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring((originalFilename.lastIndexOf(".")));
        //使用UUID重新生成文件名，防止文件名重复造成文件覆盖
        String fileName = UUID.randomUUID().toString()+suffix;
       //创建一个目录对象
        File dir = new File(basePath);
        //判断配置文件中的目录是否存在
        if(!dir.exists()){
            //目录不存在，则创建
            dir.mkdir();
        }
        try {
            //将临时文件转存到指定地方
            //用原始文件名去命名转存文件,这种方式可能会有重名，导致文件被覆盖
            //file.transferTo(new File(basePath+originalFilename));
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    /**
     * 下载图片，保存到服务器，回显到浏览器
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        try {
            //输入流，通过输入流获取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //输出流，通过输出流将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");

            int len=0;
            byte[] bytes = new byte[1024];
            while((len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
