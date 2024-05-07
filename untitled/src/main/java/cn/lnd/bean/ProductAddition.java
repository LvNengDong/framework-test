package cn.lnd.bean;

import cn.lnd.refelct.CompareDesc;
import lombok.Data;

/**
 * @Author lnd
 * @Description
 * @Date 2024/5/7 17:00
 */
@Data
public class ProductAddition {
    /**
     * 城市编码
     */
    private String cityCode;
    /**
     * 地址
     */
    @CompareDesc(desc = "地址信息")
    private String address;
}
