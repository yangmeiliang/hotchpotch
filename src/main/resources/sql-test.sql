CREATE TABLE `sns_email_log`
(
    `id`         int(10) UNSIGNED
        NOT NULL AUTO_INCREMENT COMMENT 'id',
    `handler`     varchar(32) NOT NULL COMMENT '处理程序',
    `reciever`   varchar(64)         NOT NULL COMMENT '邮件接收者',
    `status`     tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '发送状态0初始，1发送成功，2发送失败,3校验失败',
    `createTime` datetime                     DEFAULT NULL COMMENT '记录创建时间',
    `modifyTime` datetime                     DEFAULT NULL COMMENT '记录修改时间',
    PRIMARY KEY (`id`),
    KEY `reciever` (`reciever`, `handler`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 125
  DEFAULT CHARSET = utf8 COMMENT ='Email发送日志表, sns00库';

CREATE TABLE `sns_email_log`
(
    `id`         int(10) UNSIGNED    NOT NULL AUTO_INCREMENT COMMENT 'id',
    `handler`    varchar(32)         NOT NULL COMMENT '处理程序',
    `reciever`   varchar(64)         NOT NULL COMMENT '邮件接收者',
    `status`     tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '发送状态0初始，1发送成功，2发送失败,3校验失败',
    `createTime` datetime                     DEFAULT NULL COMMENT '记录创建时间',
    `modifyTime` datetime                     DEFAULT NULL COMMENT '记录修改时间',
    PRIMARY KEY (`id`),
    KEY `reciever` (`reciever`, `handler`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 125
  DEFAULT CHARSET = utf8 COMMENT 'Email发送日志表, sns00库';