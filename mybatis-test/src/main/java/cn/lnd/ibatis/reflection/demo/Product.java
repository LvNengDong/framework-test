package cn.lnd.ibatis.reflection.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/4 21:53
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Product {
    private Integer id;

    private String productName;
    /**
     * 基础产品类别，查询优先以供应商为准
     */
    private int category;
    /**
     * 售价
     */
    private BigDecimal sellPrice;
    /**
     * 售卖开始时间
     */
    private Date saleStartTime;
    /**
     * 售卖结束时间
     */
    private Date saleEndTime;
    /**
     * 版本号
     */
    private int version;
}
