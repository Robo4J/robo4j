CREATE TABLE ROBO_UNIT (
  id BIGSERIAL primary key,
  uid VARCHAR(255) NOT NULL,
  config VARCHAR(255) NOT NULL
);

create sequence robo_unit_sequence start with 1 increment by 1;

INSERT INTO ROBO_UNIT (uid, config) VALUES ('system', 'httpUnit,serverUni,cameraUnit');