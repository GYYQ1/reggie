package com.yinqing.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinqing.reggie.entity.AddressBook;
import com.yinqing.reggie.mapper.AddressBookMapper;
import com.yinqing.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook>implements AddressBookService {
}
