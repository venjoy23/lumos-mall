# Lumos 商城（网络商品贸易平台）

这是我的毕业设计项目，一个基于 SpringBoot 的网上商城系统，前后端分离。

后端：SpringBoot + MyBatis-Plus + MySQL
前端：Vue3 + Element Plus
数据库：MySQL 8

## 主要功能

前台：
- 用户注册、登录（用 JWT 做登录验证，密码 MD5 加盐存库）
- 首页轮播图、热门商品、商城资讯
- 商品搜索：按名称、品牌、价格区间、分类查，支持按价格/人气排序
- 商品详情：加入购物车、立即购买、收藏、评论
- 购物车结算，选择收货地址，用账户余额支付（模拟支付）
- 个人中心：改资料、改密码、收货地址、我的订单、我的收藏、余额充值、充值记录

后台：
- 用户管理、管理员管理
- 商品分类、商品信息管理（上架下架、限购、图片上传）
- 商品入库（入库后库存自动增加）
- 订单管理：按状态筛选、发货、退款（退款自动退余额和回补库存）
- 评论管理：回复、删除
- 轮播图、资讯分类、商城资讯管理
- 充值记录查看

## 运行方法

1. 建数据库（先装好 MySQL 8）：

```
mysql -uroot -p < sql/lumos.sql
```

2. 启动后端（需要 JDK 8+ 和 Maven）：

先改 `lumos-server/src/main/resources/application.yml` 里的数据库账号密码，然后：

```
cd lumos-server
mvn spring-boot:run
```

后端跑在 8080 端口。

3. 启动前端（需要 Node.js）：

```
cd lumos-web
npm install
npm run dev
```

浏览器打开 http://localhost:5173

## 测试账号

- 管理员：admin / admin123
- 用户：user / 123456

## 目录说明

- `lumos-server`：后端代码
- `lumos-web`：前端代码
- `sql/lumos.sql`：数据库脚本，带一些测试数据
- `scripts`：生成演示图片的脚本

注意：商品图片是程序生成的占位图，正式使用可以在后台重新上传。
