DROP TABLE IF EXISTS `advertisement`;
CREATE TABLE `advertisement`
(
    `guild_id` bigint(20) NOT NULL,
    `status`   enum('DISABLED') NOT NULL,
    PRIMARY KEY (`guild_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;

DROP TABLE IF EXISTS `entries`;
CREATE TABLE `entries`
(
    `id`               bigint(11) NOT NULL AUTO_INCREMENT,
    `user_id`          bigint(11) NOT NULL,
    `guild_id`         bigint(30) NOT NULL,
    `channel_id`       bigint(30) NOT NULL,
    `users_in_channel` mediumtext         DEFAULT NULL,
    `join_time`        timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    PRIMARY KEY (`id`),
    UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27016 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `language`;
CREATE TABLE `language`
(
    `guild_id` bigint(30) NOT NULL,
    `language` enum('RU','EN') NOT NULL,
    PRIMARY KEY (`guild_id`),
    UNIQUE KEY `guild_id` (`guild_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `locks`;
CREATE TABLE `locks`
(
    `user_id`     bigint(30) NOT NULL,
    `lock_status` enum('LOCKED','UNLOCKED') NOT NULL,
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `server`;
CREATE TABLE `server`
(
    `guild_id_long` bigint(30) NOT NULL,
    `channel_id`    bigint(30) NOT NULL,
    PRIMARY KEY (`guild_id_long`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;

DROP TABLE IF EXISTS `subs`;
CREATE TABLE `subs`
(
    `id`               bigint(11) NOT NULL AUTO_INCREMENT,
    `guild_id`         bigint(11) NOT NULL,
    `user_id`          bigint(30) NOT NULL,
    `user_tracking_id` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    KEY                `FK9edsvtap9uhuikihdsv3c74rv` (`id`),
    KEY                `FK9edsvtap9uhuikihdsv3c74r5` (`guild_id`),
    CONSTRAINT `FK9edsvtap9uhuikihdsv3c74r5` FOREIGN KEY (`guild_id`) REFERENCES `server` (`guild_id_long`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=473 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;