--liquibase formatted sql

--changeset melchakov:init_schema

DROP TABLE IF EXISTS posts;
create table posts
(
    post_id         uuid not null
        primary key,
    change_date     timestamp(6),
    creation_date   timestamp(6),
    number_of_likes bigint,
    topic_id        bigint,
    user_creator_id bigint,
    user_owner_id   bigint,
    post_content    varchar(255)
);

alter table posts
    owner to forum;
insert into posts ( number_of_likes, post_id, post_content, topic_id,user_creator_id,user_owner_id)
values (11, 'b78da23b-eedb-464f-b29d-e827f2a21445', 'Root dir',   1, 1, 1),
       (22, 'b21ecc4a-7751-4ea9-beae-2d9358ccc8f2', 'Sub_dir dir',2, 2, 2);




