DROP TABLE IF EXISTS `server`;
CREATE TABLE `server`
(
    `guild_id_long`   bigint(30) NOT NULL,
    `channel_id` bigint(30) NOT NULL,
    PRIMARY KEY (`guild_id_long`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `language`;
CREATE TABLE `language`
(
    `guild_id` bigint(30) NOT NULL,
    `language` enum ('RU', 'EN') not null,
    PRIMARY KEY (`guild_id`),
    UNIQUE KEY `guild_id` (`guild_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `subs`;
CREATE TABLE `subs`
(
    `id`               bigint(11) NOT NULL AUTO_INCREMENT,
    `guild_id`         bigint(11) NOT NULL,
    `user_id`          bigint(30) NOT NULL,
    `user_tracking_id` bigint(30) NOT NULL,
    PRIMARY KEY (`id`),
    KEY                `FK9edsvtap9uhuikihdsv3c74rv` (`id`),
    CONSTRAINT `FK9edsvtap9uhuikihdsv3c74r5`
        FOREIGN KEY (`guild_id`)
            REFERENCES `server` (`guild_id_long`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `locks`;
CREATE TABLE `locks`
(
    `user_id` bigint(30) NOT NULL,
    `lock_status` enum ('LOCKED', 'UNLOCKED') not null,
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
