-- =============================================================
-- 商城新增功能模块数据库脚本（优惠券 / 秒杀 / 团购）
-- 数据库：PostgreSQL 16（shopdb）
-- 说明：脚本幂等，可重复执行。
--   - 新建表使用 CREATE TABLE IF NOT EXISTS
--   - 可能缺失的列使用 ALTER TABLE ... ADD COLUMN IF NOT EXISTS
--   - 不使用 FIND_IN_SET / DATE_ADD / valid_start / valid_end 等 MySQL 语法
-- =============================================================

-- -------------------------------------------------------------
-- 1. 团购主活动表
--    status: PENDING(待审核) / APPROVED(已通过) / REJECTED(已驳回) /
--            ONGOING(进行中) / ENDED(已结束)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.group_buy_activity (
    id          character varying(32) NOT NULL,
    name        character varying(100) NOT NULL,
    description text,
    start_time  timestamp without time zone NOT NULL,
    end_time    timestamp without time zone NOT NULL,
    status      character varying(20) DEFAULT 'PENDING',
    created_by  character varying(32),
    created_at  timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at  timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted     smallint DEFAULT 0,
    PRIMARY KEY (id)
);

COMMENT ON TABLE public.group_buy_activity IS '团购活动主表';
COMMENT ON COLUMN public.group_buy_activity.status IS '状态: PENDING(待审核), APPROVED(已通过), REJECTED(已驳回), ONGOING(进行中), ENDED(已结束)';

-- -------------------------------------------------------------
-- 2. 团购商品表
--    一个活动下挂多个团购商品
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.group_buy_product (
    id              character varying(32) NOT NULL,
    activity_id     character varying(32) NOT NULL,
    product_id      character varying(36) NOT NULL,
    sku_id          character varying(36),
    group_price     numeric(10,2) NOT NULL,
    original_price  numeric(10,2),
    stock           integer DEFAULT 0,
    remaining_stock integer DEFAULT 0,
    per_user_limit  integer DEFAULT 1,
    min_people      integer NOT NULL DEFAULT 2,
    group_hours     integer NOT NULL DEFAULT 24,
    sort_order      integer DEFAULT 0,
    sales_count     integer DEFAULT 0,
    locked_stock    integer DEFAULT 0,
    sold_stock      integer DEFAULT 0,
    version         integer DEFAULT 0,
    created_at      timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at      timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted         smallint DEFAULT 0,
    PRIMARY KEY (id)
);

COMMENT ON TABLE public.group_buy_product IS '团购商品表';
COMMENT ON COLUMN public.group_buy_product.group_price IS '拼团价';
COMMENT ON COLUMN public.group_buy_product.original_price IS '商品原价';
COMMENT ON COLUMN public.group_buy_product.min_people IS '成团人数';
COMMENT ON COLUMN public.group_buy_product.group_hours IS '开团后成团时限(小时)';

CREATE INDEX IF NOT EXISTS idx_group_buy_product_activity ON public.group_buy_product (activity_id);

-- -------------------------------------------------------------
-- 3. 团购团（拼团记录）
--    status: ONGOING(拼团中) / SUCCESS(已成团) / FAILED(拼团失败) / CLOSED(已关闭/退款)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.group_buy_team (
    id                    character varying(32) NOT NULL,
    group_no              character varying(32) NOT NULL,
    activity_id           character varying(32) NOT NULL,
    group_buy_product_id  character varying(32) NOT NULL,
    leader_user_id        character varying(32) NOT NULL,
    current_num           integer DEFAULT 1,
    target_num            integer NOT NULL,
    status                character varying(20) DEFAULT 'ONGOING',
    end_time              timestamp without time zone,
    created_at            timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at            timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted               smallint DEFAULT 0,
    PRIMARY KEY (id)
);

COMMENT ON TABLE public.group_buy_team IS '团购团记录表';
COMMENT ON COLUMN public.group_buy_team.status IS '状态: ONGOING(拼团中), SUCCESS(已成团), FAILED(拼团失败), CLOSED(已关闭)';

CREATE INDEX IF NOT EXISTS idx_group_buy_team_product ON public.group_buy_team (group_buy_product_id);
CREATE INDEX IF NOT EXISTS idx_group_buy_team_status  ON public.group_buy_team (status);
CREATE INDEX IF NOT EXISTS idx_group_buy_team_leader  ON public.group_buy_team (leader_user_id);

-- -------------------------------------------------------------
-- 4. 团购订单表
--    status: PENDING(待支付) / PAID(已支付) / CANCELLED(已取消) / REFUNDED(已退款)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.group_buy_order (
    id                    character varying(32) NOT NULL,
    order_no              character varying(32) NOT NULL,
    team_id               character varying(32),
    activity_id           character varying(32) NOT NULL,
    group_buy_product_id  character varying(32) NOT NULL,
    user_id               character varying(32) NOT NULL,
    quantity              integer DEFAULT 1,
    group_price           numeric(10,2) NOT NULL,
    total_amount          numeric(10,2) NOT NULL,
    discount_amount       numeric(10,2) DEFAULT 0,
    pay_amount            numeric(10,2) DEFAULT 0,
    refund_amount         numeric(10,2),
    refund_time           timestamp without time zone,
    status                character varying(20) DEFAULT 'PENDING',
    pay_time              timestamp without time zone,
    transaction_id        character varying(64),
    receiver_name         character varying(50),
    receiver_phone        character varying(20),
    receiver_address      character varying(255),
    created_at            timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at            timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted               smallint DEFAULT 0,
    PRIMARY KEY (id)
);

COMMENT ON TABLE public.group_buy_order IS '团购订单表';
COMMENT ON COLUMN public.group_buy_order.status IS '状态: PENDING(待支付), PAID(已支付), CANCELLED(已取消), REFUNDED(已退款)';

CREATE INDEX IF NOT EXISTS idx_group_buy_order_user   ON public.group_buy_order (user_id);
CREATE INDEX IF NOT EXISTS idx_group_buy_order_team   ON public.group_buy_order (team_id);
CREATE INDEX IF NOT EXISTS idx_group_buy_order_activity ON public.group_buy_order (activity_id);

-- -------------------------------------------------------------
-- 5. 幂等补齐字段（兼容旧库 / 已有数据库脚本版本差异）
--    若列已存在则跳过，不会报错
-- -------------------------------------------------------------

-- coupon：创建人（admin 端实体需要）
ALTER TABLE public.coupon ADD COLUMN IF NOT EXISTS created_by character varying(32);
-- coupon：描述
ALTER TABLE public.coupon ADD COLUMN IF NOT EXISTS description character varying(255);
-- coupon：满减金额 / 折扣值
ALTER TABLE public.coupon ADD COLUMN IF NOT EXISTS value numeric(10,2);
ALTER TABLE public.coupon ADD COLUMN IF NOT EXISTS discount numeric(3,1);
-- coupon：有效期
ALTER TABLE public.coupon ADD COLUMN IF NOT EXISTS start_time timestamp without time zone;
ALTER TABLE public.coupon ADD COLUMN IF NOT EXISTS end_time timestamp without time zone;
-- coupon：状态（创建即发放 ACTIVE）
ALTER TABLE public.coupon ADD COLUMN IF NOT EXISTS status character varying(20) DEFAULT 'ACTIVE';
-- coupon：适用范围
ALTER TABLE public.coupon ADD COLUMN IF NOT EXISTS applicable_scope character varying(20) DEFAULT 'all';
ALTER TABLE public.coupon ADD COLUMN IF NOT EXISTS applicable_ids text;

-- user_coupon：使用时间 / 有效期
ALTER TABLE public.user_coupon ADD COLUMN IF NOT EXISTS used_time timestamp without time zone;
ALTER TABLE public.user_coupon ADD COLUMN IF NOT EXISTS start_time timestamp without time zone;
ALTER TABLE public.user_coupon ADD COLUMN IF NOT EXISTS end_time timestamp without time zone;

-- seckill_order：支付信息 / 收货信息（兼容旧版本缺列）
ALTER TABLE public.seckill_order ADD COLUMN IF NOT EXISTS pay_time timestamp without time zone;
ALTER TABLE public.seckill_order ADD COLUMN IF NOT EXISTS transaction_id character varying;
ALTER TABLE public.seckill_order ADD COLUMN IF NOT EXISTS receiver_name character varying(255);
ALTER TABLE public.seckill_order ADD COLUMN IF NOT EXISTS receiver_phone character varying(255);
ALTER TABLE public.seckill_order ADD COLUMN IF NOT EXISTS receiver_address character varying(255);

-- seckill_product：锁定库存 / 已售库存 / 乐观锁版本（兼容旧版本缺列）
ALTER TABLE public.seckill_product ADD COLUMN IF NOT EXISTS locked_stock integer DEFAULT 0;
ALTER TABLE public.seckill_product ADD COLUMN IF NOT EXISTS sold_stock integer DEFAULT 0;
ALTER TABLE public.seckill_product ADD COLUMN IF NOT EXISTS version integer DEFAULT 0;

-- orders：优惠券抵扣金额 / 使用的用户优惠券ID / 优惠券名称（订单计价落地）
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS discount_amount numeric(10,2) DEFAULT 0;
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS coupon_id character varying(64);
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS coupon_name character varying(128);

-- group_buy_order：团长9折优惠金额 / 实付金额 / 退款金额 / 退款时间
ALTER TABLE public.group_buy_order ADD COLUMN IF NOT EXISTS discount_amount numeric(10,2) DEFAULT 0;
ALTER TABLE public.group_buy_order ADD COLUMN IF NOT EXISTS pay_amount numeric(10,2) DEFAULT 0;
ALTER TABLE public.group_buy_order ADD COLUMN IF NOT EXISTS refund_amount numeric(10,2);
ALTER TABLE public.group_buy_order ADD COLUMN IF NOT EXISTS refund_time timestamp without time zone;

-- -------------------------------------------------------------
-- 6. 分销模块
--    规则：三级分销；下级支付成功后按当时上级链生成三级佣金（PENDING 待入账）；
--         确认收货后佣金入账（AVAILABLE 可提现）；订单退款/取消则佣金作废（CANCELLED）；
--         满 10 元可申请提现，管理员审核通过后线下打款（MOCK）。
--    绑定关系：独立关系表保存用户最新上级，可随时更换，下单时按最新关系固化佣金链。
-- -------------------------------------------------------------

-- 6.1 分销关系表（每个用户最新一条上级，换绑即更新 parent_id）
CREATE TABLE IF NOT EXISTS public.distribution_relation (
    id         character varying(32) NOT NULL,
    user_id    character varying(32) NOT NULL,
    parent_id  character varying(32) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted    smallint DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE public.distribution_relation IS '分销上下级关系表';
COMMENT ON COLUMN public.distribution_relation.user_id IS '当前用户（下级）';
COMMENT ON COLUMN public.distribution_relation.parent_id IS '上级（推荐人）';

CREATE UNIQUE INDEX IF NOT EXISTS uk_distribution_relation_user ON public.distribution_relation (user_id);
CREATE INDEX IF NOT EXISTS idx_distribution_relation_parent ON public.distribution_relation (parent_id);

-- 6.2 佣金记录表（每笔订单按层级各生成一条）
CREATE TABLE IF NOT EXISTS public.commission_record (
    id               character varying(32) NOT NULL,
    order_no         character varying(64) NOT NULL,
    buyer_user_id    character varying(32) NOT NULL,
    level            smallint NOT NULL DEFAULT 1,
    user_id          character varying(32) NOT NULL,
    order_amount     numeric(10,2) NOT NULL DEFAULT 0,
    rate             numeric(5,4) NOT NULL DEFAULT 0,
    commission_amount numeric(10,2) NOT NULL DEFAULT 0,
    status           character varying(20) NOT NULL DEFAULT 'PENDING',
    withdrawal_id    character varying(32),
    settled_time     timestamp without time zone,
    created_at       timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at       timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted          smallint DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE public.commission_record IS '分销佣金记录表';
COMMENT ON COLUMN public.commission_record.level IS '分销层级: 1/2/3';
COMMENT ON COLUMN public.commission_record.status IS '状态: PENDING(待入账), AVAILABLE(可提现), LOCKED(提现中), WITHDRAWN(已提现), CANCELLED(已作废)';
COMMENT ON COLUMN public.commission_record.order_amount IS '下单时订单实付金额快照';
COMMENT ON COLUMN public.commission_record.rate IS '佣金比例（如 0.10 表示 10%）';

CREATE UNIQUE INDEX IF NOT EXISTS uk_commission_record_order_level ON public.commission_record (order_no, level);
CREATE INDEX IF NOT EXISTS idx_commission_record_user ON public.commission_record (user_id);
CREATE INDEX IF NOT EXISTS idx_commission_record_status ON public.commission_record (status);
CREATE INDEX IF NOT EXISTS idx_commission_record_withdrawal ON public.commission_record (withdrawal_id);

-- 6.3 提现单表
CREATE TABLE IF NOT EXISTS public.distribution_withdrawal (
    id          character varying(32) NOT NULL,
    user_id     character varying(32) NOT NULL,
    amount      numeric(10,2) NOT NULL DEFAULT 0,
    status      character varying(20) NOT NULL DEFAULT 'PENDING',
    apply_time  timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    review_time timestamp without time zone,
    reviewer    character varying(32),
    remark      character varying(255),
    created_at  timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at  timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted     smallint DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE public.distribution_withdrawal IS '分销提现申请表';
COMMENT ON COLUMN public.distribution_withdrawal.status IS '状态: PENDING(待审核), APPROVED(已通过), REJECTED(已拒绝)';

CREATE INDEX IF NOT EXISTS idx_distribution_withdrawal_user ON public.distribution_withdrawal (user_id);
CREATE INDEX IF NOT EXISTS idx_distribution_withdrawal_status ON public.distribution_withdrawal (status);

-- 6.4 分销配置表（各级佣金比例，可后台调整）
CREATE TABLE IF NOT EXISTS public.distribution_config (
    id         character varying(32) NOT NULL,
    level      smallint NOT NULL,
    rate       numeric(5,4) NOT NULL DEFAULT 0,
    enabled    smallint DEFAULT 1,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
COMMENT ON TABLE public.distribution_config IS '分销佣金比例配置表';
COMMENT ON COLUMN public.distribution_config.level IS '分销层级: 1/2/3';
COMMENT ON COLUMN public.distribution_config.rate IS '佣金比例（如 0.10 表示 10%）';

CREATE UNIQUE INDEX IF NOT EXISTS uk_distribution_config_level ON public.distribution_config (level);

-- 默认三级比例：一级 10% / 二级 5% / 三级 2%（幂等插入）
INSERT INTO public.distribution_config (id, level, rate, enabled) VALUES ('dist_cfg_1', 1, 0.1000, 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO public.distribution_config (id, level, rate, enabled) VALUES ('dist_cfg_2', 2, 0.0500, 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO public.distribution_config (id, level, rate, enabled) VALUES ('dist_cfg_3', 3, 0.0200, 1) ON CONFLICT (id) DO NOTHING;

-- -------------------------------------------------------------
-- 7. 提现打款字段 + 用户签到积分
--    提现打款：管理员审核通过后自动向打款渠道发起打款（当前 MOCK 渠道，可替换为微信商家转账/银行代付）；
--            payout_status: NONE(未打款) / PENDING(打款中) / SUCCESS(打款成功) / FAILED(打款失败，可重试)
--    签到积分：每日签到 +5 积分；连续签到满 7 天额外奖励 +20 积分；断签则连续天数重置。
-- -------------------------------------------------------------

-- 7.1 提现单表补充打款字段（幂等加列）
ALTER TABLE public.distribution_withdrawal ADD COLUMN IF NOT EXISTS payout_status character varying(20) NOT NULL DEFAULT 'NONE';
ALTER TABLE public.distribution_withdrawal ADD COLUMN IF NOT EXISTS payout_time timestamp without time zone;
ALTER TABLE public.distribution_withdrawal ADD COLUMN IF NOT EXISTS payout_no character varying(64);
ALTER TABLE public.distribution_withdrawal ADD COLUMN IF NOT EXISTS fail_reason character varying(255);
COMMENT ON COLUMN public.distribution_withdrawal.payout_status IS '打款状态: NONE(未打款)/PENDING(打款中)/SUCCESS(打款成功)/FAILED(打款失败)';
COMMENT ON COLUMN public.distribution_withdrawal.payout_no IS '打款流水号（渠道返回）';
COMMENT ON COLUMN public.distribution_withdrawal.fail_reason IS '打款失败原因';

-- 7.2 积分账户表（每个用户一行，累计总积分）
CREATE TABLE IF NOT EXISTS public.points_account (
    id          character varying(32) NOT NULL,
    user_id     character varying(32) NOT NULL,
    total_points integer NOT NULL DEFAULT 0,
    created_at  timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at  timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted     smallint DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE public.points_account IS '用户积分账户表';
COMMENT ON COLUMN public.points_account.total_points IS '累计总积分（只增不减，用于展示与后续消耗场景）';

CREATE UNIQUE INDEX IF NOT EXISTS uk_points_account_user ON public.points_account (user_id);

-- 7.3 积分流水表（签到、奖励等变动明细）
CREATE TABLE IF NOT EXISTS public.points_record (
    id          character varying(32) NOT NULL,
    user_id     character varying(32) NOT NULL,
    change_type character varying(20) NOT NULL DEFAULT 'SIGN',
    points      integer NOT NULL DEFAULT 0,
    remark      character varying(255),
    created_at  timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted     smallint DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE public.points_record IS '积分变动流水表';
COMMENT ON COLUMN public.points_record.change_type IS '类型: SIGN(每日签到)/SIGN_BONUS(连续签到奖励)';

CREATE INDEX IF NOT EXISTS idx_points_record_user ON public.points_record (user_id);
CREATE INDEX IF NOT EXISTS idx_points_record_created ON public.points_record (created_at);

-- 7.4 签到记录表（同一用户同一天只能签一次）
CREATE TABLE IF NOT EXISTS public.points_sign (
    id         character varying(32) NOT NULL,
    user_id    character varying(32) NOT NULL,
    sign_date  date NOT NULL,
    points     integer NOT NULL DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted    smallint DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE public.points_sign IS '用户签到记录表';
COMMENT ON COLUMN public.points_sign.sign_date IS '签到日期（服务器时区）';

CREATE UNIQUE INDEX IF NOT EXISTS uk_points_sign_user_date ON public.points_sign (user_id, sign_date);

-- =============================================================
-- 8. 分销分享/海报/微信小程序配置（后台管理系统可视化配置位）
-- =============================================================

-- 8.1 平台配置表（key-value，供后台管理系统统一配置分享、海报、微信小程序等参数）
CREATE TABLE IF NOT EXISTS public.shop_config (
    id           character varying(32) NOT NULL,
    config_key   character varying(64) NOT NULL,
    config_value text,
    remark       character varying(255),
    created_at   timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at   timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    deleted      smallint DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE public.shop_config IS '平台配置表(key-value)，分享/海报/微信参数后台化配置';
COMMENT ON COLUMN public.shop_config.config_key IS '配置键';
COMMENT ON COLUMN public.shop_config.config_value IS '配置值';

CREATE UNIQUE INDEX IF NOT EXISTS uk_shop_config_key ON public.shop_config (config_key);

-- 8.2 种子配置（只插入缺失项，不覆盖后台已修改的值）
INSERT INTO public.shop_config (id, config_key, config_value, remark) VALUES
('shop_cfg_wx_appid', 'wechat.appid', '', '微信小程序 AppID（留空则使用后端 application.yml 的 loginwechat.appid）'),
('shop_cfg_wx_secret', 'wechat.secret', '', '微信小程序 AppSecret（留空则使用后端 application.yml 的 loginwechat.secret）'),
('shop_cfg_poster_shop', 'poster.shopName', '琼浆玉液官方旗舰店', '分销海报店铺名称'),
('shop_cfg_poster_page', 'poster.qrPage', 'pages/index/index', '分销海报二维码/小程序码落地页路径'),
('shop_cfg_share_title', 'share.title', '邀请你一起买好酒，分享赚佣金', '小程序分享标题'),
('shop_cfg_share_desc', 'share.desc', '好友下单，你享返利，快来一起选购吧', '小程序分享描述'),
('shop_cfg_share_image', 'share.imageUrl', '', '小程序分享封面图 URL（留空使用系统默认截图）'),
('shop_cfg_share_path', 'share.path', 'pages/index/index', '小程序分享/海报落地页路径')
ON CONFLICT (config_key) DO NOTHING;

-- =============================================================
-- 脚本结束
-- =============================================================