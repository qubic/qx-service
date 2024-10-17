insert into entities (identity) VALUES ('ID1');
insert into entities (identity) VALUES ('ID2');
insert into assets (issuer, name) VALUES ('ISSUER1', 'ASSET1');
insert into assets (issuer, name) VALUES ('ISSUER2', 'ASSET2');

insert into transactions (hash, source_entity_id, destination_entity_id, amount, tick, input_type, input_size, extra_data, money_flew)
values ('hash',
        currval('entities_id_seq') - 1,
        currval('entities_id_seq'),
        1,
        2,
        3,
        4,
        '{"name": "ASSET", "price": 123, "@class": ".QxAssetOrderData", "issuer": "ISSUER", "numberOfShares": 456}',
        true);
