CREATE TABLE `sys_user`
(
    `id`         int(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'id',
    `username`   varchar(32)      NOT NULL COMMENT '用户名',
    `nickname`   varchar(64)      NOT NULL COMMENT '昵称',
    `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `modifyTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录修改时间'
) CHARSET = utf8 COMMENT '用户表';