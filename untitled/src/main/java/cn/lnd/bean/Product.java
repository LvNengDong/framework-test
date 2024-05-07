package cn.lnd.bean;

import cn.lnd.refelct.CompareDesc;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author lnd
 * @Description
 * @Date 2024/5/7 16:06
 */
@Data
public class Product<T> {
    private Integer id;

    /**
     * 产品简称（详情接口才有、查询接口没有，酒店名（产品名））
     */
    @CompareDesc(desc = "产品名称")
    private String productName;

    /**
     * 产品分类，取自Product_type表
     */
    private String productType;

    /**
     * 售价
     */
    private BigDecimal sellPrice;

    /**
     * 展示原价
     */
    private BigDecimal showPrice;

    /**
     * 结算价
     */
    private BigDecimal settlePrice;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 售卖开始时间
     */
    private Date saleStartTime;

    /**
     * 售卖结束时间
     */
    private Date saleEndTime;

    /**
     * 最小购买数量
     */
    private int minAmount;

    /**
     * 最大购买数量
     */
    private int maxAmount;

    /**
     * 产品生产券的有效期
     */
    private Date ticketStartTime;

    private Date ticketEndTime;


    /**
     * 版本号
     */
    private int version;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 人工修改时间
     */
    private Date editTime;

    /**
     * 人工修改，编辑人
     */
    private String editor;

    /**
     * json格式，扩展字段，例如文案信息等
     */
    @CompareDesc(desc = "附加信息")
    private T additionInfo;
}
