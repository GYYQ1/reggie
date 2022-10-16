package com.yinqing.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yinqing.reggie.entity.Category;
import org.springframework.stereotype.Service;

@Service
public interface CategoryService extends IService<Category> {
    public void remove(long id);
}
