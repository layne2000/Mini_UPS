CREATE TABLE users
(
    user_id  VARCHAR(255) PRIMARY KEY NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE trucks
(
    truck_id INT PRIMARY KEY,
    status   VARCHAR(255) NOT NULL,
    x        INT          NOT NULL,
    y        INT          NOT NULL
);

CREATE TABLE orders
(
    ship_id         BIGINT PRIMARY KEY,
    user_id         VARCHAR(255) NULL,
    truck_id        INT NULL,
    shipment_status VARCHAR(255) NOT NULL,
    commodity_id    BIGINT       NOT NULL,
    description     VARCHAR(255) NOT NULL,
    x               INT          NOT NULL,
    y               INT          NOT NULL,
    wh_id           INT          NOT NULL,
    created_time    TIMESTAMP    NULL,
    delivering_time TIMESTAMP NULL,
    delivered_time  TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (truck_id) REFERENCES trucks (truck_id)
);
