create table payments (
    id varchar(255) not null primary key,
    reservation_id varchar(255) not null,
    amount float(53) not null,
    status varchar(20) not null,
    method varchar(50) not null,
    gateway_reference varchar(255) not null,
    constraint fk_payments_reservation foreign key (reservation_id) references reservations
);
create index idx_payments_reservation on payments (reservation_id);
create index idx_payments_status on payments (status);
