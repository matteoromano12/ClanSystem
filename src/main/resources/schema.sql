CREATE TABLE IF NOT EXISTS clans (
                                     id          INT AUTO_INCREMENT PRIMARY KEY,
                                     name        VARCHAR(16)  NOT NULL UNIQUE,
    tag         VARCHAR(8)   NOT NULL UNIQUE,
    created_at  BIGINT       NOT NULL,
    home_world  VARCHAR(64)  NULL,
    home_x      DOUBLE       NULL,
    home_y      DOUBLE       NULL,
    home_z      DOUBLE       NULL,
    home_yaw    FLOAT        NULL,
    home_pitch  FLOAT        NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clan_members (
                                            uuid        CHAR(36)     NOT NULL PRIMARY KEY,
    clan_id     INT          NOT NULL,
    name        VARCHAR(16)  NOT NULL,
    role        VARCHAR(16)  NOT NULL,
    joined_at   BIGINT       NOT NULL,
    CONSTRAINT fk_member_clan FOREIGN KEY (clan_id)
    REFERENCES clans(id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clan_claims (
                                           id          INT AUTO_INCREMENT PRIMARY KEY,
                                           clan_id     INT          NOT NULL,
                                           world       VARCHAR(64)  NOT NULL,
    chunk_x     INT          NOT NULL,
    chunk_z     INT          NOT NULL,
    claimed_at  BIGINT       NOT NULL,
    CONSTRAINT fk_claim_clan FOREIGN KEY (clan_id)
    REFERENCES clans(id) ON DELETE CASCADE,
    CONSTRAINT uq_claim UNIQUE (world, chunk_x, chunk_z)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_members_clan ON clan_members(clan_id);
CREATE INDEX idx_claims_clan ON clan_claims(clan_id);