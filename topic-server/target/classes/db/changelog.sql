
--liquibase formatted sql

--changeset melchakov:init_schema

DROP TABLE IF EXISTS TOPICS;
DROP SEQUENCE IF exists topics_seq;
create sequence topics_seq
    increment by 50;

alter sequence topics_seq owner to forum;
create table topics
(
    change_date   timestamp(6),
    creation_date timestamp(6),
    directory_id  bigint,
    post_id       bigint not null
        primary key,
    user_owner_id bigint,
    post_content  varchar(255),
    topic_id      varchar(255)
);
alter table topics
    owner to forum;
insert into topics (  post_id, post_content, topic_id,user_owner_id,directory_id)
values (1, 'user Root dir',      'dc21cf9f-e003-46d7-b33c-59f411453c3f', 1, 101),
       (2, 'admin post content', 'b78da23b-eedb-464f-b29d-e827f2a21445', 2, 202);

