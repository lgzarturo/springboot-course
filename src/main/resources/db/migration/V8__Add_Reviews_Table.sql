create table reviews (
    id varchar(255) not null primary key,
    reservation_id varchar(255) not null unique,
    hotel_id varchar(255) not null,
    user_id bigint not null,
    rating integer not null,
    comment varchar(2000),
    constraint fk_reviews_reservation foreign key (reservation_id) references reservations,
    constraint fk_reviews_user foreign key (user_id) references users,
    constraint chk_reviews_rating check (rating between 1 and 5)
);
create index idx_reviews_hotel on reviews (hotel_id);
create index idx_reviews_user on reviews (user_id);
