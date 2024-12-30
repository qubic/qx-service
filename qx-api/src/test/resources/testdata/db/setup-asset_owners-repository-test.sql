insert into entities (identity) VALUES ('ID1');
insert into entities (identity) VALUES ('ID2');
insert into assets (issuer, name) VALUES ('ISSUER1', 'ASSET1');
insert into assets (issuer, name) VALUES ('ISSUER2', 'ASSET2');

insert into asset_owners (entity_id, asset_id, amount)
values ((select id from entities where identity = 'ID1'), (select id from assets where name = 'ASSET1'), 12345);
insert into asset_owners (entity_id, asset_id, amount)
values ((select id from entities where identity = 'ID1'), (select id from assets where name = 'ASSET2'), 54321);

insert into asset_owners (entity_id, asset_id, amount)
values ((select id from entities where identity = 'ID2'), (select id from assets where name = 'ASSET1'), 123);
