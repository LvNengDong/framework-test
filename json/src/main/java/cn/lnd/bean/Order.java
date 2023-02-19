package cn.lnd.bean;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2023/2/19 15:39
 */
@Data
@Builder
public class Order {

    private List<User> users;

    private List<Product> products;
}
