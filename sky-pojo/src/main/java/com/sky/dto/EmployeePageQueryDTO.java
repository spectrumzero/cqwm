package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeePageQueryDTO implements Serializable {

    //员工姓名
    private String name;

    //当前页码(需要进一步计算OFFSET,也可以直接交给分页插件)
    private int page;

    //每页显示记录数(LIMIT)
    private int pageSize;

}
