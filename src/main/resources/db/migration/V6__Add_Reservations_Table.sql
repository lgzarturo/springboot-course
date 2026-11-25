create table reservations (
    id varchar(255) not null primary key,
    room_id varchar(255) not null,
    user_id bigint not null,
    guest_name varchar(255) not null,
    check_in date not null,
    check_out date not null,
    total_price float(53) not null,
    status varchar(20) not null,
    constraint fk_reservations_room foreign key (room_id) references rooms,
    constraint fk_reservations_user foreign key (user_id) references users
);
create index idx_reservations_room_dates on reservations (room_id, check_in, check_out);
create index idx_reservations_user on reservations (user_id);
create index idx_reservations_status on reservations (status);
