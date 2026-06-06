/*------------------------------
-- Default database entries --
------------------------------
-- account group entries
INSERT INTO acc_groups(id, created_user, group_name, group_under, group_type, description, status, app_lock, order_no, tenant_id)
VALUES (10, 1, 'Cash', 1, 'ASSET', 'Cash Group', 'ACTIVE', 0, 0, "-1")
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO acc_groups(id, created_user, group_name, group_under, group_type, description, status, app_lock, order_no, tenant_id)
VALUES (11, 1, 'Bank Account', 1, 'ASSET', 'Bank Accounts', 'ACTIVE', 0, 0, "-1")
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO acc_groups(id, created_user, group_name, group_under, group_type, description, status, app_lock, order_no, tenant_id)
VALUES (12, 1, 'Devotee', 1, 'ASSET', 'Devotee Group', 'ACTIVE', 0, 0, "-1")
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO acc_groups(id, created_user, group_name, group_under, group_type, description, status, app_lock, order_no, tenant_id)
VALUES (13, 1, 'Pooja', 2, 'INCOME', 'Pooja Income', 'ACTIVE', 0, 0, "-1")
ON DUPLICATE KEY UPDATE id = id;

ALTER TABLE acc_groups AUTO_INCREMENT = 50;

-- account ledger entries
INSERT INTO `acc_ledgers` (`id`, `tenant_id`,  `created_user`, `description`, `group_under`, `ledger_name`, `status`, `app_lock`) VALUES (10, '-1', '-1', 'Cash ledger default', '10', 'Cash', 'ACTIVE', 0)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO `acc_ledgers` (`id`, `tenant_id`,  `created_user`, `description`, `group_under`, `ledger_name`, `status`, `app_lock`) VALUES (11, '-1', '-1', 'Pooja Income', '13', 'Pooja', 'ACTIVE', 0)
ON DUPLICATE KEY UPDATE id = id;

ALTER TABLE acc_ledgers AUTO_INCREMENT = 150;

-- user table entries

INSERT INTO users (id, tenant_id, full_name, password, status, trans_id, user_name)
SELECT NULL, 'abcd', 'System Admin', '$2a$10$mo1dE9nX9PAvATdXK74DSOqvvD/J3FDgn5PnZfL0QGqz67Vy80scO', 'ACTIVE', '-1', 'admin'
	FROM dual WHERE NOT EXISTS (SELECT 1 FROM users WHERE tenant_id = 'abcd' AND user_name = 'admin');

*/

/* =========================================================
   H2 DEFAULT DATA FOR KOVIL (OFFLINE DESKTOP APP)
   ========================================================= */

-- ---------------------------------------------------------
-- ACCOUNT GROUPS
-- ---------------------------------------------------------
MERGE INTO acc_groups (
    id,
    created_user,
    group_name,
    group_under,
    group_type,
    description,
    status,
    app_lock,
    order_no,
    tenant_id
)
KEY (id)
VALUES (
    10,
    1,
    'Cash',
    1,
    'ASSET',
    'Cash Group',
    'ACTIVE',
    0,
    0,
    '-1'
);

MERGE INTO acc_groups (
    id,
    created_user,
    group_name,
    group_under,
    group_type,
    description,
    status,
    app_lock,
    order_no,
    tenant_id
)
KEY (id)
VALUES (
    11,
    1,
    'Bank Account',
    1,
    'ASSET',
    'Bank Accounts',
    'ACTIVE',
    0,
    0,
    '-1'
);

MERGE INTO acc_groups (
    id,
    created_user,
    group_name,
    group_under,
    group_type,
    description,
    status,
    app_lock,
    order_no,
    tenant_id
)
KEY (id)
VALUES (
    12,
    1,
    'Devotee',
    1,
    'ASSET',
    'Devotee Group',
    'ACTIVE',
    0,
    0,
    '-1'
);

MERGE INTO acc_groups (
    id,
    created_user,
    group_name,
    group_under,
    group_type,
    description,
    status,
    app_lock,
    order_no,
    tenant_id
)
KEY (id)
VALUES (
    13,
    1,
    'Pooja Income',
    2,
    'INCOME',
    'Pooja Income',
    'ACTIVE',
    0,
    0,
    '-1'
);

MERGE INTO acc_groups (
    id,
    created_user,
    group_name,
    group_under,
    group_type,
    description,
    status,
    app_lock,
    order_no,
    tenant_id
)
KEY (id)
VALUES (
    14,
    1,
    'Pooja Liability',
    2,
    'INCOME',
    'Pooja Liabilities',
    'ACTIVE',
    0,
    0,
    '-1'
);

ALTER TABLE acc_groups
ALTER COLUMN id RESTART WITH 101;
-- ---------------------------------------------------------
-- ACCOUNT LEDGERS
-- ---------------------------------------------------------
MERGE INTO acc_ledgers (
    id,
    tenant_id,
    created_user,
    description,
    group_under,
    ledger_name,
    status,
    app_lock
)
KEY (id)
VALUES (
    10,
    '-1',
    '-1',
    'Cash ledger default',
    10,
    'Cash',
    'ACTIVE',
    0
);

MERGE INTO acc_ledgers (
    id,
    tenant_id,
    created_user,
    description,
    group_under,
    ledger_name,
    status,
    app_lock
)
KEY (id)
VALUES (
    11,
    '-1',
    '-1',
    'Pooja Income',
    13,
    'Pooja',
    'ACTIVE',
    0
);

MERGE INTO acc_ledgers (
    id,
    tenant_id,
    created_user,
    description,
    group_under,
    ledger_name,
    status,
    app_lock
)
KEY (id)
VALUES (
    12,
    '-1',
    '-1',
    'Pooja Advance',
    14,
    'Pooja Advance Liability',
    'ACTIVE',
    0
);

ALTER TABLE acc_ledgers
ALTER COLUMN id RESTART WITH 151;
-- ---------------------------------------------------------
-- DEFAULT ADMIN USER
-- ---------------------------------------------------------
MERGE INTO users (
    tenant_id,
    full_name,
    password,
    status,
    trans_id,
    user_name
)
KEY (tenant_id, user_name)
VALUES (
    'abcd',
    'System Admin',
    '$2a$10$nFrkWU2Wt.RL9ex3ITaIEuvAy09bFTsFrHsJ/SNwFH3FmHjjyJzAS',
    'ACTIVE',
    '-1',
    'pkst'
);


MERGE INTO users (
    tenant_id,
    full_name,
    password,
    status,
    trans_id,
    user_name
)
KEY (tenant_id, user_name)
VALUES (
    'abcd',
    'System Admin',
    '$2a$10$mo1dE9nX9PAvATdXK74DSOqvvD/J3FDgn5PnZfL0QGqz67Vy80scO',
    'ACTIVE',
    '-1',
    'rahul'
);


INSERT INTO inv_unit_conversions (from_unit, to_unit, multiplier, tenant_id) VALUES
('KG', 'G', 1000, '-1'),
('G', 'KG', 0.001, '-1'),
('L', 'ML', 1000, '-1'),
('ML', 'L', 0.001, '-1');


