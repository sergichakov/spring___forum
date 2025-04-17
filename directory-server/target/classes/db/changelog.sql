--liquibase formatted sql

--changeset melchakov:init_schema
-- drop table if exists directories;
-- DROP SEQUENCE IF EXISTS directories_directory_id_seq;
-- create sequence directories_directory_id_seq;
--
-- alter sequence directories_directory_id_seq owner to forum;
--
-- alter sequence directories_directory_id_seq owned by directories.directory_id;
--
--
-- create table directories
-- (
--     creation_date timestamp(6),
--     directory_id  bigserial
--         primary key,
--     order_num     bigint,
--     sub_dir_id    bigint,
--     topic_id      bigint,
--     dir_name      varchar(255)
--     , killthis_string varchar(255)
-- );
--
-- alter table directories
--     owner to forum;
--
-- insert into DIRECTORIES (order_num, topic_id, dir_name)
-- values (1, 1, 'Root dir'),
--        (2, 2, 'Sub_dir dir');
drop table if exists directories;
DROP SEQUENCE IF EXISTS directories_directory_id_seq;

create table directories
(
    directory_id  bigserial
        primary key,
    creation_date timestamp(6),
    dir_name      varchar(255),
    order_num     bigint,
    sub_dir_id    bigint,
    topic_id      bigint
);
-- create sequence directories_directory_id_seq;

alter sequence directories_directory_id_seq owner to forum;

alter sequence directories_directory_id_seq owned by directories.directory_id;
alter table directories
    owner to forum;

insert into DIRECTORIES (order_num, topic_id, dir_name)
values (1, 1, 'Root dir'),
       (2, 2, 'Sub_dir dir');