--liquibase formatted sql

DROP TABLE IF EXISTS topics;
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
