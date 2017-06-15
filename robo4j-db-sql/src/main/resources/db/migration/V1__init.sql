CREATE TABLE robo_unit (
  id BIGINT PRIMARY KEY,
  created_on TIMESTAMP NOT NULL,
  updated_on TIMESTAMP NOT NULL,
  uid VARCHAR(255) NOT NULL,
  config VARCHAR(255) NOT NULL,
  units BIGINT REFERENCES robo_unit(ID),
  parent_id BIGINT
);

create sequence robo_unit_sequence start with 1 increment by 1;

CREATE TABLE robo_point (
  id BIGINT primary key,
  created_on TIMESTAMP NOT NULL,
  updated_on TIMESTAMP NOT NULL,
  robo_unit_id BIGINT REFERENCES ROBO_UNIT(ID),
  value_type VARCHAR (255) NOT NULL,
  values VARCHAR (800000) NOT NULL
);

create sequence robo_point_sequence start with 1 increment by 1;

ALTER TABLE robo_unit ADD COLUMN points BIGINT;
ALTER TABLE robo_unit ADD CONSTRAINT fk_rup FOREIGN KEY (parent_id) REFERENCES robo_point(id);
ALTER TABLE robo_point ADD CONSTRAINT fk_rpu FOREIGN KEY (robo_unit_id) REFERENCES robo_unit(id);
