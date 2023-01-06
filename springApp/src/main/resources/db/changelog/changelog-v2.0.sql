--liquibase formatted sql
--changeset artem_kuchumov:file_to_file_path

-- ALTER TABLE data_model
-- ALTER COLUMN file TYPE VARCHAR(255);
--
-- ALTER TABLE data_model
-- RENAME COLUMN file TO file_path;

ALTER TABLE data_model
DROP COLUMN file;

ALTER TABLE data_model
ADD COLUMN file_path VARCHAR(255);