--liquibase formatted sql
--changeset artem_kuchumov:init_table
CREATE SEQUENCE data_model_sequence
INCREMENT BY 1
START WITH 1;

CREATE TABLE data_model (
    id BIGINT PRIMARY KEY,
    upload_date TIMESTAMP WITHOUT TIME ZONE,
    change_date TIMESTAMP WITHOUT TIME ZONE,
    name VARCHAR(255),
    type VARCHAR(255),
    size BIGINT,
    comment VARCHAR(255),
    file OID
);