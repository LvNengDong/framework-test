CREATE TABLE dimension
(
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `type` tinyint(4)          NOT NULL DEFAULT '0' COMMENT '业务维度，1用户维度',
    `primary_key` varchar(40)         NOT NULL DEFAULT '' COMMENT '业务主键 1买方在qunar的账户用户名',
    `order_id` varchar(50)         NOT NULL DEFAULT '' COMMENT '订单号',
    `product_type` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '产品类型',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `ext` varchar(255)         NOT NULL DEFAULT '' COMMENT '扩展字段',
    `coupon_id` varchar(64)         NOT NULL DEFAULT '' COMMENT '券ID',
    `hotel_order_no` varchar(64)         NOT NULL DEFAULT '' COMMENT '酒店订单号',
    PRIMARY KEY (id),
    KEY `idx_primary_key` (`primary_key`),
    KEY `idx_hotel_order_no` (`hotel_order_no`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='业务维度表';