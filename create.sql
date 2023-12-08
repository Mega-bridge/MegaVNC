create sequence remote_pc_seq start with 1 increment by 50;
create sequence users_seq start with 1 increment by 50;
create table remote_pc (status smallint check (status between 0 and 2), created_at timestamp(6), id bigint not null, owner_id bigint, repeater_id bigint unique, name varchar(255), primary key (id));
create table users (created_at timestamp(6), id bigint not null, password varchar(255), username varchar(255) unique, roles varchar(255) array, primary key (id));
alter table if exists remote_pc add constraint FK4eyifhukd8vdjrjtta8tidlu foreign key (owner_id) references users;
