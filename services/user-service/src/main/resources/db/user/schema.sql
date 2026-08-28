-- 第一阶段空库建表；不导入单体种子数据，不连接其他服务数据库。
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avatar_url` varchar(512) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(128) DEFAULT NULL,
  `nickname` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` enum('ADMIN','USER') NOT NULL,
  `status` enum('ACTIVE','DISABLED') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_identities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `identifier` varchar(128) NOT NULL,
  `identity_type` enum('EMAIL','PHONE') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `verified` bit(1) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhhnqu0l61unw3qwlltpithsu6` (`identity_type`,`identifier`),
  KEY `FKl8i188j5rgpteq6erbt6x1h0m` (`user_id`),
  CONSTRAINT `FKl8i188j5rgpteq6erbt6x1h0m` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_addresses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `city` varchar(30) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `detail_address` varchar(200) NOT NULL,
  `district` varchar(30) NOT NULL,
  `is_default` bit(1) DEFAULT NULL,
  `province` varchar(30) NOT NULL,
  `receiver_name` varchar(50) NOT NULL,
  `receiver_phone` varchar(20) NOT NULL,
  `tag` varchar(20) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `chat_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  `product_id` bigint NOT NULL,
  `receiver_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_security_state(user_id BIGINT PRIMARY KEY, token_version BIGINT NOT NULL DEFAULT 0);
