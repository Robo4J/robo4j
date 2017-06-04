CREATE TABLE ROBO_UNIT (
  ID BIGSERIAL primary key,
  UID VARCHAR(255) NOT NULL,
  CONFIG VARCHAR(255) NOT NULL
);

create sequence robo_unit_sequence start with 1 increment by 1;

CREATE TABLE ROBO_SYSTEM (
  ID BIGSERIAL primary key,
  UID VARCHAR(255) NOT NULL,
);

create sequence robo_system_sequence start with 1 increment by 1;

CREATE TABLE ROBO_UNIT_POINT (
  ID BIGSERIAL primary key,
  UNIT BIGSERIAL REFERENCES ROBO_UNIT(ID)
  VALUE_TYPE VARCHAR (255) NOT NULL,
  VALUES VARCHAR (255) NOT NULL,
)

create sequence robo_unit_point_sequence start with 1 increment by 1;
