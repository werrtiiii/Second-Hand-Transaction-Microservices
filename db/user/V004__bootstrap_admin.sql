-- 仅用于实验、开发环境初始化。
-- 密码必须保存为 BCrypt 哈希，不能保存明文。

START TRANSACTION;

INSERT INTO users (
    avatar_url,
    created_at,
    email,
    nickname,
    password_hash,
    phone,
    role,
    status,
    updated_at
)
VALUES (
    NULL,
    NOW(6),
    NULL,
    '管理员',
    '$2a$10$bPWq0ITZZF1b9neuN5P9FOz9A/IPBEpLsn/CgHo7x2cf2P09f7ER.',
    '13800000000',
    'ADMIN',
    'ACTIVE',
    NOW(6)
);

SET @admin_user_id = LAST_INSERT_ID();

INSERT INTO user_identities (
    created_at,
    identifier,
    identity_type,
    updated_at,
    verified,
    user_id
)
VALUES (
    NOW(6),
    '13800000000',
    'PHONE',
    NOW(6),
    b'1',
    @admin_user_id
);

INSERT INTO user_security_state (
    user_id,
    token_version
)
VALUES (
    @admin_user_id,
    0
);

COMMIT;