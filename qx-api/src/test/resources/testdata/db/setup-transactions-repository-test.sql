insert into entities (identity) VALUES ('ID1');
insert into entities (identity) VALUES ('ID2');
insert into entities (identity) VALUES ('ISSUER1');
insert into entities (identity) VALUES ('ISSUER2');
insert into assets (issuer, name) VALUES ('ISSUER1', 'ASSET1');
insert into assets (issuer, name) VALUES ('ISSUER2', 'ASSET2');

insert into transactions (hash, source_entity_id, destination_entity_id, amount, tick, input_type, input_size, extra_data, money_flew)
values ('hash1',
        currval('entities_id_seq') - 3, -- ID1
        currval('entities_id_seq') - 2, -- ID2
        1,
        1,
        5,
        0,
        '{"name": "ASSET1", "price": 123, "@class": ".QxAssetOrderData", "issuer": "ISSUER1", "numberOfShares": 456}',
        false);

insert into transactions (hash, source_entity_id, destination_entity_id, amount, tick, input_type, input_size, extra_data, money_flew)
values ('hash2',
        currval('entities_id_seq') - 1, -- ISSUER1
        currval('entities_id_seq') - 2,
        2,
        2,
        1,
        0,
        '{"name": "ASSET1", "numberOfShares": 1000000000, "@class": ".QxIssueAssetData", "unitOfMeasurement": "00000100", "numberOfDecimalPlaces": 10}',
        true);

insert into transactions (hash, source_entity_id, destination_entity_id, amount, tick, input_type, input_size, extra_data, money_flew)
values ('hash3',
        currval('entities_id_seq') - 3, -- ID1
        currval('entities_id_seq') - 2,
        3,
        3,
        2,
        0,
        '{"@class": ".QxTransferAssetData", "issuer": "ISSUER1", "newOwner": "ID2", "name": "ASSET1", "numberOfShares": 8239152}',
        true);

insert into transactions (hash, source_entity_id, destination_entity_id, amount, tick, input_type, input_size, extra_data, money_flew)
values ('hash4',
        currval('entities_id_seq') - 3, -- ID1
        currval('entities_id_seq') - 2, -- ID2
        4,
        4,
        6,
        0,
        '{"name": "ASSET1", "price": 123, "@class": ".QxAssetOrderData", "issuer": "ISSUER1", "numberOfShares": 456}',
        null);

insert into transactions (hash, source_entity_id, destination_entity_id, amount, tick, input_type, input_size, extra_data, money_flew)
values ('hash5',
        currval('entities_id_seq') - 3, -- ID1
        currval('entities_id_seq') - 2, -- ID2
        5,
        5,
        7,
        0,
        '{"name": "ASSET2", "price": 123, "@class": ".QxAssetOrderData", "issuer": "ISSUER2", "numberOfShares": 456}',
        false);

insert into transactions (hash, source_entity_id, destination_entity_id, amount, tick, input_type, input_size, extra_data, money_flew)
values ('hash6',
        currval('entities_id_seq') - 3, -- ID1
        currval('entities_id_seq') - 2, -- ID2
        6,
        5,
        8,
        0,
        '{"name": "ASSET1", "price": 123, "@class": ".QxAssetOrderData", "issuer": "ISSUER1", "numberOfShares": 456}',
        true);
