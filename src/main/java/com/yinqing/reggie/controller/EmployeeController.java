package com.yinqing.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yinqing.reggie.common.R;
import com.yinqing.reggie.entity.Employee;
import com.yinqing.reggie.service.impl.EmployeeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController{
    @Autowired
    private EmployeeServiceImpl employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request,@RequestBody Employee employee){
            //1.将页面提交的密码进行md5加密处理
        String password=employee.getPassword();
     password = DigestUtils.md5DigestAsHex(password.getBytes());
            //2.根据页面提交的用户名进行数据库查询
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp=employeeService.getOne(queryWrapper);
        //3.如果没有查询到则返回登录失败结果
        if(emp==null)return R.error("登录失败");
        //4.用户输入密码与数据库密码进行判断
        if(!emp.getPassword().equals(password)) return R.error("登录失败");
        //5.判断用户的账户是否被禁用状态
        if(emp.getStatus()==0)return R.error("账号已禁用");
        //6.登录成功，将用户id存储到session中，并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());

        return  R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
        @RequestMapping("/logout")
    public R<String>logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param request
     * @param employee
     * @return
     */
    @RequestMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
            log.info("新增员工信息：{}",employee.toString());
            //设置初始密码123456,需进行md5加密
            employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
            //自动填充，不需要下面代码
           // employee.setCreateTime(LocalDateTime.now());
           // employee.setUpdateTime(LocalDateTime.now());
            //获取当前登录用户的id
        //Long empid =(Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empid);
       // employee.setUpdateUser(empid);
        employeeService.save(employee);
            return R.success("新增员工成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
        @GetMapping("/page")
    public R<Page>page(int page,int pageSize,String name){
        log.info("页码为：{}，pageSize：{}，name：{}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo=new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee>queryWrapper=new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /***
     * 根据id修改员工信息
     * @param employee
     * @return
     */
        @PutMapping
    public R<String>update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
        //自动填充，不需要下面代码
           // Long empId = (Long) request.getSession().getAttribute("employee");
            //employee.setUpdateUser(empId);
            //employee.setUpdateTime(LocalDateTime.now());
            employeeService.updateById(employee);
            return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息，用于修改员工信息时将信息回显给浏览器
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee>getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if(employee!=null)
        return R.success(employee);
         return R.error("没有查询到员工信息");
    }
}
