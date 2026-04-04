create table hotels (id varchar(255) not null, address varchar(255), name varchar(255), primary key (id));
create table rooms (id varchar(255) not null, number varchar(255), price float(53) not null, type varchar(255), hotel_id varchar(255), primary key (id));
alter table if exists rooms add constraint FKp5lufxy0ghq53ugm93hdc941k foreign key (hotel_id) references hotels;

