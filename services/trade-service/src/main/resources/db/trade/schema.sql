-- 第一阶段空库建表；不导入单体种子数据，不连接其他服务数据库。
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address_id` bigint DEFAULT NULL,
  `amount_cent` int NOT NULL,
  `buyer_id` bigint NOT NULL,
  `cancelled_at` datetime(6) DEFAULT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `paid_at` datetime(6) DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `receiver_address` varchar(255) DEFAULT NULL,
  `receiver_name` varchar(255) DEFAULT NULL,
  `receiver_phone` varchar(255) DEFAULT NULL,
  `seller_id` bigint NOT NULL,
  `settled_at` datetime(6) DEFAULT NULL,
  `shipped_at` datetime(6) DEFAULT NULL,
  `status` enum('AFTER_SALE','CANCELLED','COMPLETED','SETTLED','WAIT_DELIVER','WAIT_PAY','WAIT_RECEIVE') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `order_events` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `from_status` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `order_id` bigint NOT NULL,
  `to_status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `offers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `buyer_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `message` varchar(500) DEFAULT NULL,
  `offered_price_cent` int NOT NULL,
  `order_id` bigint DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  `status` enum('ACCEPTED','CANCELLED','PENDING','REJECTED') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shipments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `carrier_code` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `order_id` bigint NOT NULL,
  `status` enum('CREATED','DELIVERED','IN_TRANSIT') DEFAULT NULL,
  `tracking_no` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `after_sale_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `arbitration_result` text,
  `buyer_evidence` text,
  `buyer_id` bigint DEFAULT NULL,
  `closed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `deadline_at` datetime(6) DEFAULT NULL,
  `handled_at` datetime(6) DEFAULT NULL,
  `order_completed_at` datetime(6) DEFAULT NULL,
  `order_id` bigint NOT NULL,
  `reason` text,
  `refund_amount_cent` int DEFAULT NULL,
  `refunded_at` datetime(6) DEFAULT NULL,
  `request_type` enum('PARTIAL_REFUND','REFUND_NOT_SHIPPED','REFUND_RECEIVED','RETURN_REFUND') NOT NULL,
  `requested_at` datetime(6) DEFAULT NULL,
  `responsibility` varchar(16) DEFAULT NULL,
  `return_carrier_code` varchar(32) DEFAULT NULL,
  `return_tracking_no` varchar(64) DEFAULT NULL,
  `returned_at` datetime(6) DEFAULT NULL,
  `seller_evidence` text,
  `seller_id` bigint DEFAULT NULL,
  `seller_response` text,
  `shipping_cost_cent` int DEFAULT NULL,
  `shipping_paid_by` varchar(16) DEFAULT NULL,
  `status` enum('APPROVED','CLOSED','PLATFORM_ARBITRATION','REFUNDED','REJECTED','REQUESTED','RETURN_CONFIRMED','RETURN_SHIPPED') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ratings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `reviewer_id` bigint NOT NULL,
  `score` int NOT NULL,
  `seller_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4b8f5fs6fguy7a8ygpimjelms` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE orders MODIFY status ENUM('CREATING','CREATE_FAILED','AFTER_SALE','CANCELLED','COMPLETED','SETTLED','WAIT_DELIVER','WAIT_PAY','WAIT_RECEIVE') NOT NULL, ADD COLUMN product_title VARCHAR(100), ADD COLUMN product_version BIGINT;
CREATE TABLE trade_operations(id BIGINT PRIMARY KEY AUTO_INCREMENT,actor_id BIGINT NOT NULL,idempotency_key VARCHAR(80) NOT NULL,payload_hash CHAR(64) NOT NULL,order_id BIGINT,phase VARCHAR(32) NOT NULL,attempts INT NOT NULL DEFAULT 0,last_error VARCHAR(80),created_at DATETIME NOT NULL,updated_at DATETIME NOT NULL,UNIQUE KEY uk_actor_key(actor_id,idempotency_key),UNIQUE KEY uk_order(order_id));

CREATE TABLE outbox_events(id VARCHAR(64) PRIMARY KEY,recipient_id BIGINT NOT NULL,kind VARCHAR(32) NOT NULL,payload JSON NOT NULL,attempts INT NOT NULL DEFAULT 0,next_attempt_at DATETIME NOT NULL,lease_until DATETIME NULL,lease_owner VARCHAR(64),published_at DATETIME,last_error VARCHAR(200),created_at DATETIME NOT NULL,KEY ix_outbox_pending(published_at,next_attempt_at));

ALTER TABLE orders ADD COLUMN list_price_cent INT, ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE offers MODIFY status ENUM('ACCEPTED','ACCEPTING','CANCELLED','PENDING','REJECTED') NOT NULL, ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE after_sale_requests ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE shipments ADD UNIQUE KEY uk_shipment_order(order_id);
CREATE TABLE payments(payment_no VARCHAR(64) PRIMARY KEY,order_id BIGINT NOT NULL UNIQUE,buyer_id BIGINT NOT NULL,amount_cent INT NOT NULL,status VARCHAR(20) NOT NULL,refunded_cent INT NOT NULL DEFAULT 0,method VARCHAR(20),created_at DATETIME NOT NULL,updated_at DATETIME NOT NULL);
CREATE TABLE refunds(refund_no VARCHAR(64) PRIMARY KEY,after_sale_id BIGINT NOT NULL UNIQUE,order_id BIGINT NOT NULL,amount_cent INT NOT NULL,created_at DATETIME NOT NULL);

ALTER TABLE trade_operations ADD COLUMN next_attempt_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, ADD COLUMN lease_until DATETIME, ADD COLUMN lease_owner VARCHAR(64), ADD KEY ix_recovery(phase,next_attempt_at);
