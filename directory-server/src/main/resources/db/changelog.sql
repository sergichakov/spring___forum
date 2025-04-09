--liquibase formatted sql

DROP SEQUENCE IF EXISTS directories_directory_id_seq;
create table directories
(
    creation_date timestamp(6),
    directory_id  bigserial
        primary key,
    order_num     bigint,
    sub_dir_id    bigint,
    topic_id      bigint,
    dir_name      varchar(255)
);

alter table directories
    owner to forum;

insert into DIRECTORIES (order_num, topic_id, dir_name)
values (1, 1, 'Root dir'),
       (2, 2, 'Sub_dir dir');
