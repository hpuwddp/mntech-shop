--
-- PostgreSQL database dump
--

\restrict 3NxcgW16Yb7zhL8f2c2fA6qg0Ssdru26b9Y4karo4HFDPWE90FJAXxfwW17J6Wq

-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: brand; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.brand (
    id character varying(36) NOT NULL,
    name character varying(100) NOT NULL,
    code character varying(50),
    logo character varying(500),
    description text,
    sort_order integer DEFAULT 0,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted integer DEFAULT 0
);


ALTER TABLE public.brand OWNER TO shopuser;

--
-- Name: cart; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.cart (
    id character varying(36) NOT NULL,
    user_id character varying(36) NOT NULL,
    product_id character varying(36) NOT NULL,
    sku_id character varying(36),
    product_name character varying(200) NOT NULL,
    sku_name character varying(200),
    product_image character varying(500),
    price numeric(19,2) NOT NULL,
    quantity integer DEFAULT 1 NOT NULL,
    stock integer DEFAULT 0 NOT NULL,
    selected boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted integer DEFAULT 0
);


ALTER TABLE public.cart OWNER TO shopuser;

--
-- Name: TABLE cart; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.cart IS '购物车表';


--
-- Name: COLUMN cart.id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.cart.id IS '主键ID';


--
-- Name: COLUMN cart.user_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.cart.user_id IS '用户ID';


--
-- Name: COLUMN cart.product_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.cart.product_id IS '产品ID';


--
-- Name: COLUMN cart.sku_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.cart.sku_id IS 'SKU ID';


--
-- Name: COLUMN cart.product_name; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.cart.product_name IS '产品名称';


--
-- Name: COLUMN cart.sku_name; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.cart.sku_name IS 'SKU名称';


--
-- Name: COLUMN cart.product_image; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.cart.product_image IS '产品图片';


--
-- Name: COLUMN cart.price; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.cart.price IS '单价';


--
-- Name: COLUMN cart.quantity; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.cart.quantity IS '数量';


--
-- Name: COLUMN cart.stock; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.cart.stock IS '库存';


--
-- Name: COLUMN cart.selected; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.cart.selected IS '是否选中';


--
-- Name: COLUMN cart.created_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.cart.created_at IS '创建时间';


--
-- Name: COLUMN cart.updated_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.cart.updated_at IS '更新时间';


--
-- Name: COLUMN cart.deleted; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.cart.deleted IS '逻辑删除: 0-未删除, 1-已删除';


--
-- Name: category; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.category (
    id character varying(36) NOT NULL,
    parent_id character varying(36),
    name character varying(100) NOT NULL,
    code character varying(50) NOT NULL,
    level integer NOT NULL,
    sort_order integer DEFAULT 0,
    icon character varying(500),
    image character varying(500),
    description text,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted integer DEFAULT 0
);


ALTER TABLE public.category OWNER TO shopuser;

--
-- Name: coupon; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.coupon (
    id character varying(36) NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(255),
    type character varying(20) DEFAULT 'cash'::character varying NOT NULL,
    value numeric(10,2),
    discount numeric(3,1),
    min_amount numeric(10,2) DEFAULT 0,
    max_discount numeric(10,2),
    applicable_scope character varying(20) DEFAULT 'all'::character varying,
    applicable_ids text,
    total_quantity integer DEFAULT 0,
    used_quantity integer DEFAULT 0,
    remaining_quantity integer DEFAULT 0,
    user_limit integer DEFAULT 1,
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone NOT NULL,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    sort_order integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted integer DEFAULT 0
);


ALTER TABLE public.coupon OWNER TO shopuser;

--
-- Name: TABLE coupon; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.coupon IS '优惠券表';


--
-- Name: COLUMN coupon.id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.id IS '主键ID';


--
-- Name: COLUMN coupon.name; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.name IS '优惠券名称';


--
-- Name: COLUMN coupon.description; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.description IS '优惠券描述';


--
-- Name: COLUMN coupon.type; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.type IS '类型: DISCOUNT(折扣), CASH(满减), FREE_SHIPPING(免运费)';


--
-- Name: COLUMN coupon.value; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.value IS '满减金额或折扣值(如: 满100减20, value=20; 9折, value=9)';


--
-- Name: COLUMN coupon.discount; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.discount IS '折扣值(如9折, 存9.0)';


--
-- Name: COLUMN coupon.min_amount; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.min_amount IS '使用门槛金额(0表示无门槛)';


--
-- Name: COLUMN coupon.max_discount; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.max_discount IS '最大折扣金额(折扣券专用)';


--
-- Name: COLUMN coupon.applicable_scope; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.applicable_scope IS '适用范围: all-全场, category-指定分类, product-指定商品';


--
-- Name: COLUMN coupon.applicable_ids; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.applicable_ids IS '适用分类ID或商品ID(JSON数组)';


--
-- Name: COLUMN coupon.total_quantity; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.total_quantity IS '总发放数量(0表示不限)';


--
-- Name: COLUMN coupon.used_quantity; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.used_quantity IS '已使用数量';


--
-- Name: COLUMN coupon.remaining_quantity; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.remaining_quantity IS '剩余数量';


--
-- Name: COLUMN coupon.user_limit; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.user_limit IS '每人限领数量';


--
-- Name: COLUMN coupon.start_time; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.start_time IS '开始时间';


--
-- Name: COLUMN coupon.end_time; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.end_time IS '结束时间';


--
-- Name: COLUMN coupon.status; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.status IS '状态: ACTIVE-启用, INACTIVE-禁用';


--
-- Name: COLUMN coupon.sort_order; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.sort_order IS '排序';


--
-- Name: COLUMN coupon.created_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.created_at IS '创建时间';


--
-- Name: COLUMN coupon.updated_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.updated_at IS '更新时间';


--
-- Name: COLUMN coupon.deleted; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.coupon.deleted IS '逻辑删除';


--
-- Name: lottery_record; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.lottery_record (
    id character varying(36) NOT NULL,
    user_id character varying(36) NOT NULL,
    prize_id character varying(36),
    prize_name character varying(100),
    prize_level integer,
    prize_image character varying(500),
    is_win boolean DEFAULT false,
    remark character varying(500),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted integer DEFAULT 0,
    receiver_name character varying(255),
    receiver_phone character varying(255),
    receiver_address character varying(255),
    status character varying(255)
);


ALTER TABLE public.lottery_record OWNER TO shopuser;

--
-- Name: TABLE lottery_record; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.lottery_record IS '抽奖记录表';


--
-- Name: COLUMN lottery_record.id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.lottery_record.id IS '主键ID';


--
-- Name: COLUMN lottery_record.user_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.lottery_record.user_id IS '用户ID';


--
-- Name: COLUMN lottery_record.prize_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.lottery_record.prize_id IS '奖品ID（未中奖为null）';


--
-- Name: COLUMN lottery_record.prize_name; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.lottery_record.prize_name IS '奖品名称';


--
-- Name: COLUMN lottery_record.prize_level; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.lottery_record.prize_level IS '奖项等级';


--
-- Name: COLUMN lottery_record.prize_image; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.lottery_record.prize_image IS '奖品图片';


--
-- Name: COLUMN lottery_record.is_win; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.lottery_record.is_win IS '是否中奖';


--
-- Name: COLUMN lottery_record.remark; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.lottery_record.remark IS '备注';


--
-- Name: COLUMN lottery_record.created_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.lottery_record.created_at IS '创建时间';


--
-- Name: COLUMN lottery_record.deleted; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.lottery_record.deleted IS '逻辑删除：0-未删除，1-已删除';


--
-- Name: order_item; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.order_item (
    id character varying(36) NOT NULL,
    order_no character varying(32) NOT NULL,
    product_id character varying(36) NOT NULL,
    sku_id character varying(36),
    product_name character varying(200) NOT NULL,
    sku_name character varying(200),
    product_image character varying(500),
    quantity integer DEFAULT 1 NOT NULL,
    price numeric(19,2) NOT NULL,
    total_price numeric(19,2) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted integer DEFAULT 0,
    user_id character varying
);


ALTER TABLE public.order_item OWNER TO shopuser;

--
-- Name: TABLE order_item; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.order_item IS '订单明细表';


--
-- Name: COLUMN order_item.id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.order_item.id IS '主键ID';


--
-- Name: COLUMN order_item.order_no; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.order_item.order_no IS '订单号';


--
-- Name: COLUMN order_item.product_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.order_item.product_id IS '产品ID';


--
-- Name: COLUMN order_item.sku_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.order_item.sku_id IS 'SKU ID';


--
-- Name: COLUMN order_item.product_name; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.order_item.product_name IS '产品名称';


--
-- Name: COLUMN order_item.sku_name; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.order_item.sku_name IS 'SKU名称';


--
-- Name: COLUMN order_item.product_image; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.order_item.product_image IS '产品图片';


--
-- Name: COLUMN order_item.quantity; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.order_item.quantity IS '数量';


--
-- Name: COLUMN order_item.price; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.order_item.price IS '单价';


--
-- Name: COLUMN order_item.total_price; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.order_item.total_price IS '小计';


--
-- Name: COLUMN order_item.created_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.order_item.created_at IS '创建时间';


--
-- Name: COLUMN order_item.deleted; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.order_item.deleted IS '逻辑删除: 0-未删除, 1-已删除';


--
-- Name: COLUMN order_item.user_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.order_item.user_id IS '冗余字段';


--
-- Name: orders; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.orders (
    id character varying(36) NOT NULL,
    order_no character varying(32) NOT NULL,
    user_id character varying(36) NOT NULL,
    total_amount numeric(19,2) NOT NULL,
    pay_amount numeric(19,2) NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying,
    pay_time timestamp without time zone,
    transaction_id character varying(64),
    receiver_name character varying(50),
    receiver_phone character varying(20),
    receiver_address character varying(500),
    pay_type character varying(20) DEFAULT 'wechat'::character varying,
    remark character varying(500),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted integer DEFAULT 0,
    wl character varying(255),
    wl_code character varying(255)
);


ALTER TABLE public.orders OWNER TO shopuser;

--
-- Name: TABLE orders; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.orders IS '订单主表';


--
-- Name: COLUMN orders.id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.id IS '主键ID';


--
-- Name: COLUMN orders.order_no; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.order_no IS '订单号';


--
-- Name: COLUMN orders.user_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.user_id IS '用户ID';


--
-- Name: COLUMN orders.total_amount; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.total_amount IS '订单总金额';


--
-- Name: COLUMN orders.pay_amount; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.pay_amount IS '实付金额';


--
-- Name: COLUMN orders.status; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.status IS '状态: PENDING-待支付, PAID-已支付, CANCELLED-已取消, REFUND-已退款 COMPLETED 已完成';


--
-- Name: COLUMN orders.pay_time; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.pay_time IS '支付时间';


--
-- Name: COLUMN orders.transaction_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.transaction_id IS '微信交易号';


--
-- Name: COLUMN orders.receiver_name; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.receiver_name IS '收货人姓名';


--
-- Name: COLUMN orders.receiver_phone; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.receiver_phone IS '收货人电话';


--
-- Name: COLUMN orders.receiver_address; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.receiver_address IS '收货地址';


--
-- Name: COLUMN orders.pay_type; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.pay_type IS '支付方式: wechat-微信, alipay-支付宝';


--
-- Name: COLUMN orders.remark; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.remark IS '订单备注';


--
-- Name: COLUMN orders.created_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.created_at IS '创建时间';


--
-- Name: COLUMN orders.updated_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.updated_at IS '更新时间';


--
-- Name: COLUMN orders.deleted; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.deleted IS '逻辑删除: 0-未删除, 1-已删除';


--
-- Name: COLUMN orders.wl; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.wl IS '物流公司名称';


--
-- Name: COLUMN orders.wl_code; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.orders.wl_code IS '物流单号';


--
-- Name: prize; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.prize (
    id character varying(36) NOT NULL,
    name character varying(100) NOT NULL,
    image character varying(500),
    description text,
    level integer DEFAULT 0,
    probability numeric(10,6) DEFAULT 0 NOT NULL,
    total_quantity integer DEFAULT 0 NOT NULL,
    remaining_quantity integer DEFAULT 0 NOT NULL,
    sort_order integer DEFAULT 0,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted integer DEFAULT 0
);


ALTER TABLE public.prize OWNER TO shopuser;

--
-- Name: TABLE prize; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.prize IS '奖品表';


--
-- Name: COLUMN prize.id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.prize.id IS '主键ID';


--
-- Name: COLUMN prize.name; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.prize.name IS '奖品名称';


--
-- Name: COLUMN prize.image; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.prize.image IS '奖品图片';


--
-- Name: COLUMN prize.description; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.prize.description IS '奖品描述';


--
-- Name: COLUMN prize.level; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.prize.level IS '奖项等级：1-特等奖，2-一等奖，3-二等奖，4-三等奖，5-参与奖';


--
-- Name: COLUMN prize.probability; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.prize.probability IS '中奖概率（0-1之间）';


--
-- Name: COLUMN prize.total_quantity; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.prize.total_quantity IS '奖品总数量';


--
-- Name: COLUMN prize.remaining_quantity; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.prize.remaining_quantity IS '剩余数量';


--
-- Name: COLUMN prize.sort_order; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.prize.sort_order IS '排序';


--
-- Name: COLUMN prize.status; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.prize.status IS '状态：ACTIVE-启用，INACTIVE-禁用';


--
-- Name: COLUMN prize.created_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.prize.created_at IS '创建时间';


--
-- Name: COLUMN prize.updated_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.prize.updated_at IS '更新时间';


--
-- Name: COLUMN prize.deleted; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.prize.deleted IS '逻辑删除：0-未删除，1-已删除';


--
-- Name: product; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.product (
    id character varying(36) NOT NULL,
    spu_code character varying(50),
    name character varying(200),
    subtitle character varying(500),
    description text,
    brand_id character varying(36),
    category_id character varying(36),
    original_price numeric(19,2),
    selling_price numeric(19,2),
    cost_price numeric(19,2),
    member_price numeric(19,2),
    promotion_price numeric(19,2),
    promotion_start timestamp without time zone,
    promotion_end timestamp without time zone,
    total_stock integer DEFAULT 0,
    available_stock integer DEFAULT 0,
    locked_stock integer DEFAULT 0,
    sold_stock integer DEFAULT 0,
    status character varying(20) DEFAULT 'DRAFT'::character varying,
    audit_status character varying(20) DEFAULT 'PENDING'::character varying,
    audit_remark text,
    auditor_id character varying(36),
    audit_time timestamp without time zone,
    has_sku boolean DEFAULT false,
    sku_template jsonb,
    weight numeric(10,2),
    volume numeric(10,2),
    delivery_time character varying(50),
    main_image character varying(500),
    images character varying[],
    video_url character varying(500),
    tags character varying(50)[],
    sales_count integer DEFAULT 0,
    view_count integer DEFAULT 0,
    favorite_count integer DEFAULT 0,
    review_count integer DEFAULT 0,
    rating numeric(3,2),
    created_by character varying(36),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_by character varying(36),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    version integer DEFAULT 0,
    deleted integer DEFAULT 0
);


ALTER TABLE public.product OWNER TO shopuser;

--
-- Name: product_sku; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.product_sku (
    id character varying(36) NOT NULL,
    product_id character varying(36),
    sku_code character varying(50),
    sku_name character varying(200),
    attributes character varying,
    attribute_hash character varying(64),
    original_price numeric(19,2),
    selling_price numeric(19,2),
    cost_price numeric(19,2),
    promotion_price numeric(19,2),
    promotion_start timestamp without time zone,
    promotion_end timestamp without time zone,
    total_stock integer DEFAULT 0,
    available_stock integer DEFAULT 0,
    locked_stock integer DEFAULT 0,
    sold_stock integer DEFAULT 0,
    weight numeric(10,2),
    volume numeric(10,2),
    image_url character varying(500),
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    sales_count integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    version integer DEFAULT 0,
    deleted integer DEFAULT 0
);


ALTER TABLE public.product_sku OWNER TO shopuser;

--
-- Name: seckill_activity; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.seckill_activity (
    id character varying(32) NOT NULL,
    name character varying(100) NOT NULL,
    description text,
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying,
    created_by character varying(32),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted smallint DEFAULT 0
);


ALTER TABLE public.seckill_activity OWNER TO shopuser;

--
-- Name: TABLE seckill_activity; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.seckill_activity IS '秒杀活动主表';


--
-- Name: COLUMN seckill_activity.id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_activity.id IS '主键ID';


--
-- Name: COLUMN seckill_activity.name; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_activity.name IS '活动名称';


--
-- Name: COLUMN seckill_activity.description; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_activity.description IS '活动描述';


--
-- Name: COLUMN seckill_activity.start_time; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_activity.start_time IS '开始时间';


--
-- Name: COLUMN seckill_activity.end_time; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_activity.end_time IS '结束时间';


--
-- Name: COLUMN seckill_activity.status; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_activity.status IS '状态: PENDING(待审核), APPROVED(已通过), REJECTED(已驳回), ONGOING(进行中), ENDED(已结束)';


--
-- Name: COLUMN seckill_activity.created_by; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_activity.created_by IS '创建人';


--
-- Name: COLUMN seckill_activity.created_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_activity.created_at IS '创建时间';


--
-- Name: COLUMN seckill_activity.updated_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_activity.updated_at IS '更新时间';


--
-- Name: COLUMN seckill_activity.deleted; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_activity.deleted IS '逻辑删除: 0=未删除, 1=已删除';


--
-- Name: seckill_order; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.seckill_order (
    id character varying(32) NOT NULL,
    activity_id character varying(32) NOT NULL,
    seckill_product_id character varying(32) NOT NULL,
    user_id character varying(32) NOT NULL,
    order_no character varying(32),
    quantity integer NOT NULL,
    seckill_price numeric(10,2) NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted smallint DEFAULT 0,
    pay_time timestamp without time zone,
    transaction_id character varying,
    receiver_name character varying(255),
    receiver_phone character varying(255),
    receiver_address character varying(255)
);


ALTER TABLE public.seckill_order OWNER TO shopuser;

--
-- Name: TABLE seckill_order; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.seckill_order IS '秒杀订单表';


--
-- Name: COLUMN seckill_order.id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_order.id IS '主键ID';


--
-- Name: COLUMN seckill_order.activity_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_order.activity_id IS '活动ID';


--
-- Name: COLUMN seckill_order.seckill_product_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_order.seckill_product_id IS '秒杀商品ID';


--
-- Name: COLUMN seckill_order.user_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_order.user_id IS '用户ID';


--
-- Name: COLUMN seckill_order.order_no; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_order.order_no IS '订单号';


--
-- Name: COLUMN seckill_order.quantity; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_order.quantity IS '购买数量';


--
-- Name: COLUMN seckill_order.seckill_price; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_order.seckill_price IS '秒杀价';


--
-- Name: COLUMN seckill_order.status; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_order.status IS '状态: PENDING(待支付), PAID(已支付), CANCELLED(已取消)';


--
-- Name: COLUMN seckill_order.created_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_order.created_at IS '创建时间';


--
-- Name: COLUMN seckill_order.updated_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_order.updated_at IS '更新时间';


--
-- Name: COLUMN seckill_order.deleted; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_order.deleted IS '逻辑删除: 0=未删除, 1=已删除';


--
-- Name: seckill_product; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.seckill_product (
    id character varying(32) NOT NULL,
    activity_id character varying(32) NOT NULL,
    product_id character varying(32) NOT NULL,
    sku_id character varying(32),
    seckill_price numeric(10,2) NOT NULL,
    stock integer NOT NULL,
    remaining_stock integer NOT NULL,
    per_user_limit integer DEFAULT 1,
    sales_count integer DEFAULT 0,
    sort_order integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted smallint DEFAULT 0,
    locked_stock integer,
    sold_stock integer,
    version integer
);


ALTER TABLE public.seckill_product OWNER TO shopuser;

--
-- Name: TABLE seckill_product; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.seckill_product IS '秒杀商品表';


--
-- Name: COLUMN seckill_product.id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_product.id IS '主键ID';


--
-- Name: COLUMN seckill_product.activity_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_product.activity_id IS '活动ID';


--
-- Name: COLUMN seckill_product.product_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_product.product_id IS '商品ID';


--
-- Name: COLUMN seckill_product.sku_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_product.sku_id IS 'SKU ID';


--
-- Name: COLUMN seckill_product.seckill_price; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_product.seckill_price IS '秒杀价';


--
-- Name: COLUMN seckill_product.stock; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_product.stock IS '秒杀库存';


--
-- Name: COLUMN seckill_product.remaining_stock; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_product.remaining_stock IS '剩余库存';


--
-- Name: COLUMN seckill_product.per_user_limit; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_product.per_user_limit IS '每人限购数量';


--
-- Name: COLUMN seckill_product.sales_count; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_product.sales_count IS '销量';


--
-- Name: COLUMN seckill_product.sort_order; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_product.sort_order IS '排序';


--
-- Name: COLUMN seckill_product.created_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_product.created_at IS '创建时间';


--
-- Name: COLUMN seckill_product.updated_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_product.updated_at IS '更新时间';


--
-- Name: COLUMN seckill_product.deleted; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.seckill_product.deleted IS '逻辑删除: 0=未删除, 1=已删除';


--
-- Name: shipping_template; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.shipping_template (
    id character varying(36) NOT NULL,
    name character varying(100) NOT NULL,
    template_type character varying(20) DEFAULT 'WEIGHT'::character varying,
    first_weight numeric(10,2),
    first_price numeric(10,2),
    additional_weight numeric(10,2),
    additional_price numeric(10,2),
    is_default boolean DEFAULT false,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted integer DEFAULT 0
);


ALTER TABLE public.shipping_template OWNER TO shopuser;

--
-- Name: stock_deduct_record; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.stock_deduct_record (
    id bigint NOT NULL,
    biz_no character varying(64) NOT NULL,
    item_count integer DEFAULT 0 NOT NULL,
    detail_json jsonb,
    status smallint DEFAULT 1 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.stock_deduct_record OWNER TO shopuser;

--
-- Name: TABLE stock_deduct_record; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.stock_deduct_record IS '库存扣减记录表（幂等）';


--
-- Name: COLUMN stock_deduct_record.biz_no; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.stock_deduct_record.biz_no IS '幂等键（订单号/业务单号）';


--
-- Name: COLUMN stock_deduct_record.item_count; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.stock_deduct_record.item_count IS '本次扣减 SKU 条数';


--
-- Name: COLUMN stock_deduct_record.detail_json; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.stock_deduct_record.detail_json IS '扣减明细快照（JSON）';


--
-- Name: COLUMN stock_deduct_record.status; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.stock_deduct_record.status IS '状态：1-成功 0-失败';


--
-- Name: stock_deduct_record_id_seq; Type: SEQUENCE; Schema: public; Owner: shopuser
--

CREATE SEQUENCE public.stock_deduct_record_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.stock_deduct_record_id_seq OWNER TO shopuser;

--
-- Name: stock_deduct_record_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: shopuser
--

ALTER SEQUENCE public.stock_deduct_record_id_seq OWNED BY public.stock_deduct_record.id;


--
-- Name: sys_user; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.sys_user (
    id bigint NOT NULL,
    user_name character varying(50) NOT NULL,
    pass_word character varying(255) NOT NULL,
    real_name character varying(50),
    email character varying(100),
    phone character varying(20),
    avatar character varying(255),
    gender smallint DEFAULT 0,
    status smallint DEFAULT 1,
    role character varying(20) DEFAULT 'user'::character varying,
    dept_id bigint,
    last_login_ip character varying(50),
    last_login_time timestamp without time zone,
    login_count integer DEFAULT 0,
    remark text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted smallint DEFAULT 0
);


ALTER TABLE public.sys_user OWNER TO shopuser;

--
-- Name: TABLE sys_user; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.sys_user IS '系统用户表';


--
-- Name: COLUMN sys_user.id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.id IS '主键ID';


--
-- Name: COLUMN sys_user.user_name; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.user_name IS '用户名';


--
-- Name: COLUMN sys_user.pass_word; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.pass_word IS '密码(加密存储)';


--
-- Name: COLUMN sys_user.real_name; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.real_name IS '真实姓名';


--
-- Name: COLUMN sys_user.email; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.email IS '邮箱';


--
-- Name: COLUMN sys_user.phone; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.phone IS '手机号';


--
-- Name: COLUMN sys_user.avatar; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.avatar IS '头像URL';


--
-- Name: COLUMN sys_user.gender; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.gender IS '性别: 0-未知, 1-男, 2-女';


--
-- Name: COLUMN sys_user.status; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.status IS '用户状态: 0-禁用, 1-启用';


--
-- Name: COLUMN sys_user.role; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.role IS '角色: admin-管理员, user-普通用户';


--
-- Name: COLUMN sys_user.dept_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.dept_id IS '部门ID';


--
-- Name: COLUMN sys_user.last_login_ip; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.last_login_ip IS '最后登录IP';


--
-- Name: COLUMN sys_user.last_login_time; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.last_login_time IS '最后登录时间';


--
-- Name: COLUMN sys_user.login_count; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.login_count IS '登录次数';


--
-- Name: COLUMN sys_user.remark; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.remark IS '备注';


--
-- Name: COLUMN sys_user.created_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.created_at IS '创建时间';


--
-- Name: COLUMN sys_user.updated_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.updated_at IS '更新时间';


--
-- Name: COLUMN sys_user.deleted; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.sys_user.deleted IS '删除标记: 0-未删除, 1-已删除';


--
-- Name: sys_user_id_seq; Type: SEQUENCE; Schema: public; Owner: shopuser
--

CREATE SEQUENCE public.sys_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_user_id_seq OWNER TO shopuser;

--
-- Name: sys_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: shopuser
--

ALTER SEQUENCE public.sys_user_id_seq OWNED BY public.sys_user.id;


--
-- Name: user_address; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.user_address (
    id character varying(36) NOT NULL,
    user_id character varying(36) NOT NULL,
    receiver_name character varying(50) NOT NULL,
    receiver_phone character varying(20) NOT NULL,
    province character varying(50),
    city character varying(50),
    district character varying(50),
    detail_address character varying(200) NOT NULL,
    postal_code character varying(10),
    is_default boolean DEFAULT false,
    tag character varying(20),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted integer DEFAULT 0
);


ALTER TABLE public.user_address OWNER TO shopuser;

--
-- Name: TABLE user_address; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.user_address IS '用户收货地址表';


--
-- Name: COLUMN user_address.id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_address.id IS '主键ID';


--
-- Name: COLUMN user_address.user_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_address.user_id IS '用户ID';


--
-- Name: COLUMN user_address.receiver_name; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_address.receiver_name IS '收货人姓名';


--
-- Name: COLUMN user_address.receiver_phone; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_address.receiver_phone IS '收货人电话';


--
-- Name: COLUMN user_address.province; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_address.province IS '省份';


--
-- Name: COLUMN user_address.city; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_address.city IS '城市';


--
-- Name: COLUMN user_address.district; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_address.district IS '区/县';


--
-- Name: COLUMN user_address.detail_address; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_address.detail_address IS '详细地址';


--
-- Name: COLUMN user_address.postal_code; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_address.postal_code IS '邮政编码';


--
-- Name: COLUMN user_address.is_default; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_address.is_default IS '是否默认地址';


--
-- Name: COLUMN user_address.tag; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_address.tag IS '标签: 家, 公司, 学校等';


--
-- Name: COLUMN user_address.created_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_address.created_at IS '创建时间';


--
-- Name: COLUMN user_address.updated_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_address.updated_at IS '更新时间';


--
-- Name: COLUMN user_address.deleted; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_address.deleted IS '逻辑删除: 0-未删除, 1-已删除';


--
-- Name: user_coupon; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.user_coupon (
    id character varying(36) NOT NULL,
    user_id character varying(36) NOT NULL,
    coupon_id character varying(36) NOT NULL,
    coupon_code character varying(32),
    status character varying(20) DEFAULT 'UNUSED'::character varying,
    used_time timestamp without time zone,
    order_no character varying(32),
    start_time timestamp without time zone,
    end_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted integer DEFAULT 0
);


ALTER TABLE public.user_coupon OWNER TO shopuser;

--
-- Name: TABLE user_coupon; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.user_coupon IS '用户优惠券领取记录表';


--
-- Name: COLUMN user_coupon.id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_coupon.id IS '主键ID';


--
-- Name: COLUMN user_coupon.user_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_coupon.user_id IS '用户ID';


--
-- Name: COLUMN user_coupon.coupon_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_coupon.coupon_id IS '优惠券ID';


--
-- Name: COLUMN user_coupon.coupon_code; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_coupon.coupon_code IS '优惠券编码';


--
-- Name: COLUMN user_coupon.status; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_coupon.status IS '状态: UNUSED-未使用, USED-已使用, EXPIRED-已过期';


--
-- Name: COLUMN user_coupon.used_time; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_coupon.used_time IS '使用时间';


--
-- Name: COLUMN user_coupon.order_no; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_coupon.order_no IS '使用订单号';


--
-- Name: COLUMN user_coupon.start_time; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_coupon.start_time IS '生效时间';


--
-- Name: COLUMN user_coupon.end_time; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_coupon.end_time IS '过期时间';


--
-- Name: COLUMN user_coupon.created_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_coupon.created_at IS '创建时间';


--
-- Name: COLUMN user_coupon.updated_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_coupon.updated_at IS '更新时间';


--
-- Name: COLUMN user_coupon.deleted; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_coupon.deleted IS '逻辑删除';


--
-- Name: user_lottery_quota; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.user_lottery_quota (
    id character varying(36) NOT NULL,
    user_id character varying(36) NOT NULL,
    total_quota integer DEFAULT 0 NOT NULL,
    used_quota integer DEFAULT 0 NOT NULL,
    remaining_quota integer DEFAULT 0 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted integer DEFAULT 0
);


ALTER TABLE public.user_lottery_quota OWNER TO shopuser;

--
-- Name: TABLE user_lottery_quota; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.user_lottery_quota IS '用户抽奖次数表';


--
-- Name: COLUMN user_lottery_quota.id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_lottery_quota.id IS '主键ID';


--
-- Name: COLUMN user_lottery_quota.user_id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_lottery_quota.user_id IS '用户ID';


--
-- Name: COLUMN user_lottery_quota.total_quota; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_lottery_quota.total_quota IS '总抽奖次数';


--
-- Name: COLUMN user_lottery_quota.used_quota; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_lottery_quota.used_quota IS '已使用次数';


--
-- Name: COLUMN user_lottery_quota.remaining_quota; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_lottery_quota.remaining_quota IS '剩余次数';


--
-- Name: COLUMN user_lottery_quota.created_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_lottery_quota.created_at IS '创建时间';


--
-- Name: COLUMN user_lottery_quota.updated_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_lottery_quota.updated_at IS '更新时间';


--
-- Name: COLUMN user_lottery_quota.deleted; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.user_lottery_quota.deleted IS '逻辑删除：0-未删除，1-已删除';


--
-- Name: users; Type: TABLE; Schema: public; Owner: shopuser
--

CREATE TABLE public.users (
    id character varying(36) NOT NULL,
    openid character varying(64) NOT NULL,
    unionid character varying(64),
    nickname character varying(100),
    avatar character varying(500),
    phone character varying(20),
    gender smallint DEFAULT 0,
    province character varying(50),
    city character varying(50),
    country character varying(50),
    language character varying(20),
    session_key character varying(64),
    token character varying(500),
    token_expire_time timestamp without time zone,
    status character varying(20) DEFAULT 'ACTIVE'::character varying,
    last_login_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted integer DEFAULT 0
);


ALTER TABLE public.users OWNER TO shopuser;

--
-- Name: TABLE users; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON TABLE public.users IS '用户表';


--
-- Name: COLUMN users.id; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.id IS '用户ID';


--
-- Name: COLUMN users.openid; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.openid IS '微信openid';


--
-- Name: COLUMN users.unionid; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.unionid IS '微信unionid';


--
-- Name: COLUMN users.nickname; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.nickname IS '用户昵称';


--
-- Name: COLUMN users.avatar; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.avatar IS '头像URL';


--
-- Name: COLUMN users.phone; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.phone IS '手机号';


--
-- Name: COLUMN users.gender; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.gender IS '性别: 0-未知, 1-男, 2-女';


--
-- Name: COLUMN users.province; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.province IS '省份';


--
-- Name: COLUMN users.city; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.city IS '城市';


--
-- Name: COLUMN users.country; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.country IS '国家';


--
-- Name: COLUMN users.language; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.language IS '语言';


--
-- Name: COLUMN users.session_key; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.session_key IS '微信session_key';


--
-- Name: COLUMN users.token; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.token IS 'JWT token';


--
-- Name: COLUMN users.token_expire_time; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.token_expire_time IS 'token过期时间';


--
-- Name: COLUMN users.status; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.status IS '状态: ACTIVE-正常, INACTIVE-禁用';


--
-- Name: COLUMN users.last_login_time; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.last_login_time IS '最后登录时间';


--
-- Name: COLUMN users.created_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.created_at IS '创建时间';


--
-- Name: COLUMN users.updated_at; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.updated_at IS '更新时间';


--
-- Name: COLUMN users.deleted; Type: COMMENT; Schema: public; Owner: shopuser
--

COMMENT ON COLUMN public.users.deleted IS '逻辑删除';


--
-- Name: stock_deduct_record id; Type: DEFAULT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.stock_deduct_record ALTER COLUMN id SET DEFAULT nextval('public.stock_deduct_record_id_seq'::regclass);


--
-- Name: sys_user id; Type: DEFAULT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.sys_user ALTER COLUMN id SET DEFAULT nextval('public.sys_user_id_seq'::regclass);


--
-- Name: brand brand_code_key; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.brand
    ADD CONSTRAINT brand_code_key UNIQUE (code);


--
-- Name: brand brand_name_key; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.brand
    ADD CONSTRAINT brand_name_key UNIQUE (name);


--
-- Name: brand brand_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.brand
    ADD CONSTRAINT brand_pkey PRIMARY KEY (id);


--
-- Name: cart cart_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.cart
    ADD CONSTRAINT cart_pkey PRIMARY KEY (id);


--
-- Name: category category_code_key; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_code_key UNIQUE (code);


--
-- Name: category category_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_pkey PRIMARY KEY (id);


--
-- Name: coupon coupon_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.coupon
    ADD CONSTRAINT coupon_pkey PRIMARY KEY (id);


--
-- Name: lottery_record lottery_record_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.lottery_record
    ADD CONSTRAINT lottery_record_pkey PRIMARY KEY (id);


--
-- Name: order_item order_item_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.order_item
    ADD CONSTRAINT order_item_pkey PRIMARY KEY (id);


--
-- Name: orders orders_order_no_key; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_order_no_key UNIQUE (order_no);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: prize prize_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.prize
    ADD CONSTRAINT prize_pkey PRIMARY KEY (id);


--
-- Name: product product_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_pkey PRIMARY KEY (id);


--
-- Name: product_sku product_sku_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.product_sku
    ADD CONSTRAINT product_sku_pkey PRIMARY KEY (id);


--
-- Name: product_sku product_sku_sku_code_key; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.product_sku
    ADD CONSTRAINT product_sku_sku_code_key UNIQUE (sku_code);


--
-- Name: product product_spu_code_key; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_spu_code_key UNIQUE (spu_code);


--
-- Name: seckill_activity seckill_activity_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.seckill_activity
    ADD CONSTRAINT seckill_activity_pkey PRIMARY KEY (id);


--
-- Name: seckill_order seckill_order_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.seckill_order
    ADD CONSTRAINT seckill_order_pkey PRIMARY KEY (id);


--
-- Name: seckill_product seckill_product_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.seckill_product
    ADD CONSTRAINT seckill_product_pkey PRIMARY KEY (id);


--
-- Name: shipping_template shipping_template_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.shipping_template
    ADD CONSTRAINT shipping_template_pkey PRIMARY KEY (id);


--
-- Name: stock_deduct_record stock_deduct_record_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.stock_deduct_record
    ADD CONSTRAINT stock_deduct_record_pkey PRIMARY KEY (id);


--
-- Name: sys_user sys_user_email_key; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.sys_user
    ADD CONSTRAINT sys_user_email_key UNIQUE (email);


--
-- Name: sys_user sys_user_phone_key; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.sys_user
    ADD CONSTRAINT sys_user_phone_key UNIQUE (phone);


--
-- Name: sys_user sys_user_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.sys_user
    ADD CONSTRAINT sys_user_pkey PRIMARY KEY (id);


--
-- Name: sys_user sys_user_username_key; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.sys_user
    ADD CONSTRAINT sys_user_username_key UNIQUE (user_name);


--
-- Name: product_sku uk_sku_product_hash; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.product_sku
    ADD CONSTRAINT uk_sku_product_hash UNIQUE (product_id, attribute_hash);


--
-- Name: user_address user_address_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.user_address
    ADD CONSTRAINT user_address_pkey PRIMARY KEY (id);


--
-- Name: user_coupon user_coupon_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.user_coupon
    ADD CONSTRAINT user_coupon_pkey PRIMARY KEY (id);


--
-- Name: user_lottery_quota user_lottery_quota_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.user_lottery_quota
    ADD CONSTRAINT user_lottery_quota_pkey PRIMARY KEY (id);


--
-- Name: user_lottery_quota user_lottery_quota_user_id_key; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.user_lottery_quota
    ADD CONSTRAINT user_lottery_quota_user_id_key UNIQUE (user_id);


--
-- Name: users users_openid_key; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_openid_key UNIQUE (openid);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: idx_cart_product_id; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_cart_product_id ON public.cart USING btree (product_id);


--
-- Name: idx_cart_user_id; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_cart_user_id ON public.cart USING btree (user_id);


--
-- Name: idx_coupon_deleted; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_coupon_deleted ON public.coupon USING btree (deleted);


--
-- Name: idx_coupon_start_end; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_coupon_start_end ON public.coupon USING btree (start_time, end_time);


--
-- Name: idx_coupon_status; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_coupon_status ON public.coupon USING btree (status);


--
-- Name: idx_lottery_record_created_at; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_lottery_record_created_at ON public.lottery_record USING btree (created_at DESC);


--
-- Name: idx_lottery_record_deleted; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_lottery_record_deleted ON public.lottery_record USING btree (deleted);


--
-- Name: idx_lottery_record_user_id; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_lottery_record_user_id ON public.lottery_record USING btree (user_id);


--
-- Name: idx_order_item_order_no; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_order_item_order_no ON public.order_item USING btree (order_no);


--
-- Name: idx_order_item_product_id; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_order_item_product_id ON public.order_item USING btree (product_id);


--
-- Name: idx_orders_created_at; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_orders_created_at ON public.orders USING btree (created_at DESC);


--
-- Name: idx_orders_order_no; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_orders_order_no ON public.orders USING btree (order_no);


--
-- Name: idx_orders_status; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_orders_status ON public.orders USING btree (status);


--
-- Name: idx_orders_user_id; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_orders_user_id ON public.orders USING btree (user_id);


--
-- Name: idx_prize_deleted; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_prize_deleted ON public.prize USING btree (deleted);


--
-- Name: idx_prize_probability; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_prize_probability ON public.prize USING btree (probability DESC);


--
-- Name: idx_prize_status; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_prize_status ON public.prize USING btree (status);


--
-- Name: idx_product_brand; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_product_brand ON public.product USING btree (brand_id);


--
-- Name: idx_product_category; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_product_category ON public.product USING btree (category_id);


--
-- Name: idx_product_created_at; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_product_created_at ON public.product USING btree (created_at DESC);


--
-- Name: idx_product_deleted; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_product_deleted ON public.product USING btree (deleted);


--
-- Name: idx_product_sales_count; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_product_sales_count ON public.product USING btree (sales_count DESC);


--
-- Name: idx_product_selling_price; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_product_selling_price ON public.product USING btree (selling_price);


--
-- Name: idx_product_spu_code; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_product_spu_code ON public.product USING btree (spu_code);


--
-- Name: idx_product_status; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_product_status ON public.product USING btree (status);


--
-- Name: idx_seckill_activity_status; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_seckill_activity_status ON public.seckill_activity USING btree (status);


--
-- Name: idx_seckill_activity_time; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_seckill_activity_time ON public.seckill_activity USING btree (start_time, end_time);


--
-- Name: idx_seckill_order_activity_id; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_seckill_order_activity_id ON public.seckill_order USING btree (activity_id);


--
-- Name: idx_seckill_order_order_no; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_seckill_order_order_no ON public.seckill_order USING btree (order_no);


--
-- Name: idx_seckill_order_status; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_seckill_order_status ON public.seckill_order USING btree (status);


--
-- Name: idx_seckill_order_unique; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE UNIQUE INDEX idx_seckill_order_unique ON public.seckill_order USING btree (user_id, seckill_product_id);


--
-- Name: idx_seckill_order_user_id; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_seckill_order_user_id ON public.seckill_order USING btree (user_id);


--
-- Name: idx_seckill_product_activity_id; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_seckill_product_activity_id ON public.seckill_product USING btree (activity_id);


--
-- Name: idx_seckill_product_product_id; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_seckill_product_product_id ON public.seckill_product USING btree (product_id);


--
-- Name: idx_seckill_product_unique; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE UNIQUE INDEX idx_seckill_product_unique ON public.seckill_product USING btree (activity_id, product_id, sku_id) WHERE (deleted = 0);


--
-- Name: idx_sku_code; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_sku_code ON public.product_sku USING btree (sku_code);


--
-- Name: idx_sku_deleted; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_sku_deleted ON public.product_sku USING btree (deleted);


--
-- Name: idx_sku_product; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_sku_product ON public.product_sku USING btree (product_id);


--
-- Name: idx_sku_status; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_sku_status ON public.product_sku USING btree (status);


--
-- Name: idx_sys_user_created_at; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_sys_user_created_at ON public.sys_user USING btree (created_at);


--
-- Name: idx_sys_user_dept_id; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_sys_user_dept_id ON public.sys_user USING btree (dept_id);


--
-- Name: idx_sys_user_email; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_sys_user_email ON public.sys_user USING btree (email);


--
-- Name: idx_sys_user_phone; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_sys_user_phone ON public.sys_user USING btree (phone);


--
-- Name: idx_sys_user_role; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_sys_user_role ON public.sys_user USING btree (role);


--
-- Name: idx_sys_user_status; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_sys_user_status ON public.sys_user USING btree (status);


--
-- Name: idx_sys_user_status_role; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_sys_user_status_role ON public.sys_user USING btree (status, role);


--
-- Name: idx_sys_user_username; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_sys_user_username ON public.sys_user USING btree (user_name);


--
-- Name: idx_user_address_deleted; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_user_address_deleted ON public.user_address USING btree (deleted);


--
-- Name: idx_user_address_is_default; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_user_address_is_default ON public.user_address USING btree (is_default);


--
-- Name: idx_user_address_user_id; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_user_address_user_id ON public.user_address USING btree (user_id);


--
-- Name: idx_user_coupon_coupon_id; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_user_coupon_coupon_id ON public.user_coupon USING btree (coupon_id);


--
-- Name: idx_user_coupon_deleted; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_user_coupon_deleted ON public.user_coupon USING btree (deleted);


--
-- Name: idx_user_coupon_status; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_user_coupon_status ON public.user_coupon USING btree (status);


--
-- Name: idx_user_coupon_user_id; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_user_coupon_user_id ON public.user_coupon USING btree (user_id);


--
-- Name: idx_user_coupon_user_status; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_user_coupon_user_status ON public.user_coupon USING btree (user_id, status);


--
-- Name: idx_user_lottery_quota_deleted; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_user_lottery_quota_deleted ON public.user_lottery_quota USING btree (deleted);


--
-- Name: idx_users_deleted; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_users_deleted ON public.users USING btree (deleted);


--
-- Name: idx_users_openid; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_users_openid ON public.users USING btree (openid);


--
-- Name: idx_users_phone; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_users_phone ON public.users USING btree (phone);


--
-- Name: idx_users_token; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE INDEX idx_users_token ON public.users USING btree (token);


--
-- Name: uk_stock_deduct_record_biz_no; Type: INDEX; Schema: public; Owner: shopuser
--

CREATE UNIQUE INDEX uk_stock_deduct_record_biz_no ON public.stock_deduct_record USING btree (biz_no);


--
-- Name: product fk_product_brand; Type: FK CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES public.brand(id);


--
-- Name: product fk_product_category; Type: FK CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES public.category(id);


--
-- Name: product_sku fk_sku_product; Type: FK CONSTRAINT; Schema: public; Owner: shopuser
--

ALTER TABLE ONLY public.product_sku
    ADD CONSTRAINT fk_sku_product FOREIGN KEY (product_id) REFERENCES public.product(id);


--
-- PostgreSQL database dump complete
--

\unrestrict 3NxcgW16Yb7zhL8f2c2fA6qg0Ssdru26b9Y4karo4HFDPWE90FJAXxfwW17J6Wq

