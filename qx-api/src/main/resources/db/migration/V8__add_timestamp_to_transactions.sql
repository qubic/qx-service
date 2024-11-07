alter table transactions add tick_time timestamptz;
create index on transactions (tick_time);
