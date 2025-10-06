create table if not exists users (
    user_id          bigserial primary key,
    user_name        varchar(255) not null,
    user_email       varchar(255) not null unique,
    user_created_at  timestamptz  not null default now()
);

create table if not exists groups (
    group_id          bigserial primary key,
    group_name        varchar(255) not null,
    group_created_at  timestamptz  not null default now()
);

create table if not exists membership (
    membership_id  bigserial primary key,
    user_id        bigint      not null,
    group_id       bigint      not null,
    joined_at      timestamptz not null default now(),
    role           text        not null default 'MEMBER' check (role in ('ADMIN','MEMBER')),
    constraint fk_membership_user
        foreign key (user_id) references users (user_id) on delete cascade,
    constraint fk_membership_group
        foreign key (group_id) references groups (group_id) on delete cascade,
    constraint uq_membership_user_group unique (user_id, group_id)
);

create index if not exists idx_membership_user  on membership(user_id);
create index if not exists idx_membership_group on membership(group_id);

create table if not exists expenses (
    expense_id   bigserial primary key,
    payer_id     bigint      not null,
    group_id     bigint      not null,
    amount       numeric(10,2) not null check (amount >= 0),
    currency     varchar(3)  not null default 'CAD' 
                 check (char_length(currency)=3 and currency = upper(currency)),
    description  text,
    occurred_at  timestamptz not null default now(),
    created_at   timestamptz not null default now(),
    constraint fk_expenses_payer
        foreign key (payer_id) references users (user_id) on delete cascade,
    constraint fk_expenses_group
        foreign key (group_id) references groups (group_id) on delete cascade
);

create index if not exists idx_expenses_payer        on expenses(payer_id);
create index if not exists idx_expenses_group        on expenses(group_id);
create index if not exists idx_expenses_occurred_at  on expenses(occurred_at);
create index if not exists idx_expenses_group_time   on expenses(group_id, occurred_at);

create table if not exists expense_share (
    expense_share_id bigserial primary key,
    expense_id       bigint not null,
    participant_id   bigint not null,
    share_amount     numeric(10,2) not null check (share_amount >= 0),
    share_ratio      numeric(6,4),
    constraint fk_expense_share_expense
        foreign key (expense_id) references expenses (expense_id) on delete cascade,
    constraint fk_expense_share_participant
        foreign key (participant_id) references users (user_id),
    constraint uq_expense_share unique (expense_id, participant_id)
);

create index if not exists idx_expense_share_expense     on expense_share(expense_id);
create index if not exists idx_expense_share_participant on expense_share(participant_id);

create table if not exists settlements (
    settlement_id   bigserial primary key,
    payer_id        bigint      not null,
    payee_id        bigint      not null,
    group_id        bigint      not null,
    amount          numeric(10,2) not null check (amount > 0),
    currency        varchar(3)  not null default 'CAD'
                   check (char_length(currency)=3 and currency = upper(currency)),
    settled_at      timestamptz not null default now(),
    constraint ck_settlement_not_self check (payer_id <> payee_id),
    constraint fk_settlement_payer  foreign key (payer_id) references users (user_id) on delete cascade,
    constraint fk_settlement_payee  foreign key (payee_id) references users (user_id) on delete cascade,
    constraint fk_settlement_group  foreign key (group_id) references groups (group_id) on delete cascade
);

create index if not exists idx_settlement_payer on settlements(payer_id);
create index if not exists idx_settlement_payee on settlements(payee_id);
create index if not exists idx_settlement_group on settlements(group_id);
