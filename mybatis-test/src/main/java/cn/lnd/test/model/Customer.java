package cn.lnd.test.model;

import lombok.Data;

import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/27 20:54
 */
@Data
public class Customer {
    private int id;
    private String name;
    private String phone;
    private List<Address> addresses;
}
