package com.activeharmony.sample.model;


import com.activeharmony.Model;
import com.activeharmony.annotation.Column;
import com.activeharmony.annotation.Table;

/**
 * Created by ljw on 2016/11/2.
 * 1，必须继承Model,继承后就不用声明主键了
 * 2，按照表名添加字段
 * 3，添加注释
 */
@Table(name = "user")
public class User extends Model {

    @Column
    private String userName;
    @Column
    private int userId;
    @Column
    private int age;
    @Column
    private String addr;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", userId=" + userId +
                ", age=" + age +
                ", addr='" + addr + '\'' +
                '}';
    }

}