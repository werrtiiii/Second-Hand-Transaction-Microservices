-- 第一阶段空库建表；不导入单体种子数据，不连接其他服务数据库。
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `icon_url` varchar(512) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `sort_order` int DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint DEFAULT NULL,
  `product_condition` enum('EIGHT_TENTHS','LIKE_NEW','NEW','NINE_TENTHS','SEVEN_TENTHS','SIX_TENTHS_AND_BELOW') DEFAULT NULL,
  `cover_image_url` varchar(512) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text,
  `free_shipping` tinyint(1) NOT NULL DEFAULT '0',
  `price_cent` int NOT NULL,
  `quantity` int NOT NULL,
  `seller_id` bigint NOT NULL,
  `shipping_fee_cent` int DEFAULT NULL,
  `status` enum('DRAFT','OFF_SALE','ON_SALE') NOT NULL,
  `title` varchar(100) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `product_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `sort_order` int DEFAULT NULL,
  `thumbnail_url` varchar(512) NOT NULL,
  `url` varchar(512) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `comments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `favorites` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKgh1s14hhb9qb8p2do933hscsf` (`user_id`,`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `reports` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text,
  `handle_note` text,
  `handled_at` datetime(6) DEFAULT NULL,
  `handled_by` bigint DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `reason_type` enum('COUNTERFEIT','FALSE_DESC','OTHER','PRICE_FRAUD','PRIVACY','PROHIBITED') NOT NULL,
  `reporter_id` bigint NOT NULL,
  `status` enum('DISMISSED','HANDLED','PENDING') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE products ADD COLUMN version BIGINT NOT NULL DEFAULT 0, ADD COLUMN off_shelf_reason VARCHAR(32);
CREATE TABLE inventory_reservations(operation_id VARCHAR(80) PRIMARY KEY,order_id BIGINT NOT NULL,product_id BIGINT NULL,quantity INT NOT NULL,status VARCHAR(24) NOT NULL,payload_hash CHAR(64),product_snapshot MEDIUMTEXT,created_at DATETIME NOT NULL,updated_at DATETIME NOT NULL,UNIQUE KEY uk_order(order_id));

CREATE TABLE outbox_events(id VARCHAR(64) PRIMARY KEY,recipient_id BIGINT NOT NULL,kind VARCHAR(32) NOT NULL,payload JSON NOT NULL,attempts INT NOT NULL DEFAULT 0,next_attempt_at DATETIME NOT NULL,lease_until DATETIME NULL,lease_owner VARCHAR(64),published_at DATETIME,last_error VARCHAR(200),created_at DATETIME NOT NULL,KEY ix_outbox_pending(published_at,next_attempt_at));
