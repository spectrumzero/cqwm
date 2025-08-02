package com.sky.mapper;

import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

// 注入ioc容器
@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     *
     * @param username
     * @return
     */
    // 直接使用@Select注解进行方法绑定，其中省略了resultType，mybatis会根据方法的返回值类型自动推断
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

}
