package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    // 注意需要在mapper接口上使用@mapper注解(来自mybatis)，将动态创建的mapper代理对象注册到容器中
    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传过来的明文密码进行md5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);

        //设置账号的状态，默认正常状态 1表示正常 0表示锁定
        employee.setStatus(StatusConstant.ENABLE);

        //设置密码，默认密码123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置当前记录的创建时间和修改时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());

        // 记录当前操作者的id信息
        // 因为是在同一个线程中，所以可以获取到这次访问的token所解析出来的员工id。
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        // 最后调用mapper层完成对数据库的操作
        employeeMapper.insert(employee);
    }

    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 1. 设置分页参数
        // 调用PageHelper的静态方法startPage，开启分页功能。
        // PageHelper会通过线程局部变量（ThreadLocal）来存储分页参数。
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        // 2. 执行查询
        // 紧跟在startPage方法后的第一个MyBatis查询方法会被自动进行分页。
        // PageHelper的拦截器会自动在原始SQL语句后面追加LIMIT子句。
        // Page继承了ArrayList
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        // 3. 获取分页结果
        // Page对象是PageHelper提供的实现，它包含了分页信息和查询结果。
        long total = page.getTotal(); // 从Page对象中获取总记录数
        List<Employee> records = page.getResult(); // 获取当前页的数据列表

        // 4. 封装成统一的PageResult对象并返回
        return new PageResult(total, records);
    }

    /**
     * 启用禁用员工账号
     *
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工
     *
     * @param id
     * @return
     */
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        // 修改返回给前端的敏感数据
        employee.setPassword("****");
        return employee;
    }

    /**
     * 编辑员工信息
     *
     * @param employeeDTO
     */
    public void update(EmployeeDTO employeeDTO) {
        // 1. 创建用于数据库更新的实体对象
        // DTO (Data Transfer Object) 用于接收前端数据，而Entity用于与数据库交互。
        // 这是一个良好的分层习惯，避免将数据库实体直接暴露给前端。
        Employee employee = new Employee();

        // 2. 使用BeanUtils.copyProperties进行对象属性拷贝
        // 这是一个非常实用的工具，它可以自动将employeeDTO中的同名属性值，拷贝到employee对象中。
        // 这避免了我们手动编写大量的 employee.setName(employeeDTO.getName()); 这样的代码，
        // 使代码更简洁，且当未来增加字段时，无需修改这里的代码。
        BeanUtils.copyProperties(employeeDTO, employee);

        // 3. 设置数据审计（Auditing）字段
        // 更新“最后修改时间”为当前时间，这是数据追踪的重要一环。
//        employee.setUpdateTime(LocalDateTime.now());

        // 4. 设置“最后修改人”的ID
        // BaseContext.getCurrentId() 从ThreadLocal中获取当前登录用户的ID。
        // 这个ID是在JWT拦截器中，当用户请求被验证通过时，从Token中解析并存入的。
        // 这同样是数据审计的关键，记录下是谁执行了这次修改操作。
//        employee.setUpdateUser(BaseContext.getCurrentId());

        // 5. 调用Mapper层执行数据库更新
        // employeeMapper.update 会根据传入的employee对象的ID（ID也通过属性拷贝设置好了），
        // 去更新数据库中对应记录的其他字段。
        employeeMapper.update(employee);
    }
}
