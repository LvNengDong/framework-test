use prmt_raven;

drop table if exists `product_price`;
CREATE TABLE `product_price`
(
    `id`                    int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id`            int(10)          NOT NULL DEFAULT '0' COMMENT '预售产品id',
    `product_name`          text COMMENT '预售产品名称',
    `product_type`          varchar(32)      NOT NULL DEFAULT '' COMMENT '产品类型',
    `member_level`          varchar(32)      NOT NULL DEFAULT '' COMMENT '会员等级',
    `rtid`                  bigint(20)       NOT NULL DEFAULT '0' COMMENT 'rtid',
    `from_date`             timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入住日期',
    `to_date`               timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '离店日期',
    `night_num`             int(10)          NOT NULL DEFAULT '0' COMMENT '晚数',
    `presale_sell_price`    decimal(10, 2)   NOT NULL DEFAULT '0.00' COMMENT '预售卖价',
    `presale_settle_price`  decimal(10, 2)   NOT NULL DEFAULT '0.00' COMMENT '预售结算价',
    `presale_profit`        decimal(10, 2)   NOT NULL DEFAULT '0.00' COMMENT '预售收益',
    `presale_commission`    decimal(10, 2)   NOT NULL DEFAULT '0.00' COMMENT '预售佣金',
    `presale_x`             text COMMENT '预售X信息',
    `calendar_sell_price`   decimal(10, 2)   NOT NULL DEFAULT '0.00' COMMENT '日历房卖价',
    `calendar_settle_price` decimal(10, 2)   NOT NULL DEFAULT '0.00' COMMENT '日历房结算价',
    `calendar_profit`       decimal(10, 2)   NOT NULL DEFAULT '0.00' COMMENT '日历房收益',
    `calendar_commission`   decimal(10, 2)   NOT NULL DEFAULT '0.00' COMMENT '日历房佣金',
    `calendar_x`            text COMMENT '日历房X信息',
    `create_time`           timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_presale_sell_price_presale_settle_price_presale_profit` (`presale_sell_price`, `presale_settle_price`, `presale_profit`),
    KEY `idx_from_date_to_date_product_type_member_level` (`from_date`, `to_date`, `product_type`, `member_level`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_product_id` (`product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='产品报价表';



drop table if exists `product_price_stat`;
CREATE TABLE `product_price_stat`
(
    `id`                    int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `from_date`             timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入住日期',
    `to_date`               timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '离店日期',
    `product_type`          varchar(32)      NOT NULL DEFAULT '' COMMENT '产品类型',
    `member_level`          varchar(32)      NOT NULL DEFAULT '' COMMENT '会员等级',
    `night_num`             int(10)          NOT NULL DEFAULT '0' COMMENT '晚数',
    `total_num`             int(10)          NOT NULL DEFAULT '0' COMMENT '比较总数',
    `pay_price_lose_num`    int(10)          NOT NULL DEFAULT '0' COMMENT '预售支付价lose',
    `settle_price_lose_num` int(10)          NOT NULL DEFAULT '0' COMMENT '预售结算价lose',
    `profit_lose_num`       int(10)          NOT NULL DEFAULT '0' COMMENT '预售收益lose',
    `commission_lose_num`   int(10)          NOT NULL DEFAULT '0' COMMENT '预售佣金lose',
    `x_lose_num`            int(10)          NOT NULL DEFAULT '0' COMMENT '预售Xlose',
    `pay_price_meat_num`    int(10)          NOT NULL DEFAULT '0' COMMENT '预售支付价meat',
    `settle_price_meat_num` int(10)          NOT NULL DEFAULT '0' COMMENT '预售支付价meat',
    `profit_meat_num`       int(10)          NOT NULL DEFAULT '0' COMMENT '预售收益meat',
    `commission_meat_num`   int(10)          NOT NULL DEFAULT '0' COMMENT '预售佣金meat',
    `x_meat_num`            int(10)          NOT NULL DEFAULT '0' COMMENT '预售Xmeat',
    `pay_price_beat_num`    int(10)          NOT NULL DEFAULT '0' COMMENT '预售支付价beat',
    `settle_price_beat_num` int(10)          NOT NULL DEFAULT '0' COMMENT '预售支付价beat',
    `profit_beat_num`       int(10)          NOT NULL DEFAULT '0' COMMENT '预售收益beat',
    `commission_beat_num`   int(10)          NOT NULL DEFAULT '0' COMMENT '预售佣金beat',
    `x_beat_num`            int(10)          NOT NULL DEFAULT '0' COMMENT '预售Xbeat',
    `extra_kv`              text COMMENT '补充信息',
    `create_time`           timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='产品报价统计表';