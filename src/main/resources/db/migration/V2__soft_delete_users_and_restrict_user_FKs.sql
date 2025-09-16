alter table users
  add column if not exists deleted_at timestamptz;

alter table expenses
  drop constraint if exists fk_expenses_payer;
alter table expenses
  add constraint fk_expenses_payer
    foreign key (payer_id) references users(user_id) on delete restrict;

alter table expense_share
  drop constraint if exists fk_expense_share_participant;
alter table expense_share
  add constraint fk_expense_share_participant
    foreign key (participant_id) references users(user_id) on delete restrict;

alter table settlements
  drop constraint if exists fk_settlement_payer;
alter table settlements
  add constraint fk_settlement_payer
    foreign key (payer_id) references users(user_id) on delete restrict;

alter table settlements
  drop constraint if exists fk_settlement_payee;
alter table settlements
  add constraint fk_settlement_payee
    foreign key (payee_id) references users(user_id) on delete restrict;