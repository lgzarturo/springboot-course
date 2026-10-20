alter table if exists rooms add column if not exists status varchar(20) not null default 'AVAILABLE';
alter table if exists rooms add column if not exists theme varchar(20) not null default 'PIKACHU';
alter table if exists rooms add column if not exists capacity int not null default 2;
alter table if exists rooms add column if not exists description text;
