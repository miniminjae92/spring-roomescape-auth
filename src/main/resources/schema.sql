CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (start_at)
);

CREATE TABLE theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    image_path  VARCHAR(512) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE store
(
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE member
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name     VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL DEFAULT 'USER',
    PRIMARY KEY (id),
    UNIQUE (login_id)
);

CREATE TABLE store_manager
(
    id        BIGINT NOT NULL AUTO_INCREMENT,
    store_id  BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (store_id, member_id),
    FOREIGN KEY (store_id) REFERENCES store (id),
    FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    store_id BIGINT       NOT NULL,
    member_id BIGINT       NOT NULL,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    time_id  BIGINT       NOT NULL,
    theme_id BIGINT       NOT NULL,
    status   VARCHAR(20)  NOT NULL DEFAULT 'RESERVED',
    active_slot BOOLEAN GENERATED ALWAYS AS (
        CASE WHEN status = 'RESERVED' THEN TRUE ELSE NULL END
    ),
    PRIMARY KEY (id),
    CONSTRAINT unique_active_reservation_slot UNIQUE (store_id, date, time_id, theme_id, active_slot),
    FOREIGN KEY (store_id) REFERENCES store (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);
