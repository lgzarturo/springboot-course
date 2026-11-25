create table revoked_tokens (
    jti uuid primary key,
    user_id bigint not null references users(id) on delete cascade,
    expires_at timestamp with time zone not null,
    revoked_at timestamp with time zone not null default now(),
    reason varchar(20) not null
);

create index idx_revoked_tokens_expires_at on revoked_tokens(expires_at);
