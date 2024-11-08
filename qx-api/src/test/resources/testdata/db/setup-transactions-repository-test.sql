insert into entities (identity) VALUES ('CONTRACT_ADDRESS');
insert into entities (identity) VALUES ('ID1');
insert into entities (identity) VALUES ('ID2');
insert into entities (identity) VALUES ('ISSUER1');
insert into entities (identity) VALUES ('ISSUER2');
insert into assets (issuer, name) VALUES ('ISSUER1', 'ASSET1');
insert into assets (issuer, name) VALUES ('ISSUER2', 'ASSET2');

insert into transactions (tick_time, hash, source_entity_id, destination_entity_id, amount, tick, input_type, input_size, extra_data, money_flew)
values (null,
        'hash1',
        currval('entities_id_seq') - 3, -- ID1
        currval('entities_id_seq') - 4, -- CONTRACT_ADDRESS
        1,
        1,
        5,
        0,
        '{"name": "ASSET1", "price": 123, "@class": ".QxAssetOrderData", "issuer": "ISSUER1", "numberOfShares": 456}',
        false);

insert into transactions (tick_time, hash, source_entity_id, destination_entity_id, amount, tick, input_type, input_size, extra_data, money_flew)
values ('2024-01-01T10:10:10+00:00',
        'hash7',
        currval('entities_id_seq') - 2, -- ID2
        currval('entities_id_seq') - 4, -- CONTRACT_ADDRESS
        7,
        1,
        2,
        0,
        '{"@class": ".QxTransferAssetData", "issuer": "ISSUER1", "newOwner": "ID1", "name": "ASSET2", "numberOfShares": 8239152}',
        true);

insert into transactions (tick_time, hash, source_entity_id, destination_entity_id, amount, tick, input_type, input_size, extra_data, money_flew)
values (null,
        'hash2',
        currval('entities_id_seq') - 1, -- ISSUER1
        currval('entities_id_seq') - 4, -- CONTRACT_ADDRESS
        2,
        2,
        1,
        0,
        '{"name": "ASSET1", "numberOfShares": 1000000000, "@class": ".QxIssueAssetData", "unitOfMeasurement": "00000100", "numberOfDecimalPlaces": 10}',
        true);

insert into transactions (tick_time, hash, source_entity_id, destination_entity_id, amount, tick, input_type, input_size, extra_data, money_flew)
values ('2024-01-01T10:10:10+00:00',
        'hash3',
        currval('entities_id_seq') - 3, -- ID1
        currval('entities_id_seq') - 4, -- CONTRACT_ADDRESS
        3,
        3,
        2,
        0,
        '{"@class": ".QxTransferAssetData", "issuer": "ISSUER1", "newOwner": "ID2", "name": "ASSET1", "numberOfShares": 8239152}',
        true);

insert into transactions (tick_time, hash, source_entity_id, destination_entity_id, amount, tick, input_type, input_size, extra_data, money_flew)
values ('2024-01-01T10:10:10+00:00',
        'hash4',
        currval('entities_id_seq') - 3, -- ID1
        currval('entities_id_seq') - 4, -- CONTRACT_ADDRESS
        4,
        4,
        6,
        0,
        '{"name": "ASSET1", "price": 123, "@class": ".QxAssetOrderData", "issuer": "ISSUER1", "numberOfShares": 456}',
        null);

insert into transactions (tick_time, hash, source_entity_id, destination_entity_id, amount, tick, input_type, input_size, extra_data, money_flew)
values ('2024-01-01T10:10:10+00:00',
        'hash5',
        currval('entities_id_seq') - 3, -- ID1
        currval('entities_id_seq') - 4, -- CONTRACT_ADDRESS
        5,
        5,
        7,
        0,
        '{"name": "ASSET2", "price": 123, "@class": ".QxAssetOrderData", "issuer": "ISSUER2", "numberOfShares": 456}',
        false);

insert into transactions (tick_time, hash, source_entity_id, destination_entity_id, amount, tick, input_type, input_size, extra_data, money_flew)
values ('2024-01-01T10:10:10+00:00',
        'hash6',
        currval('entities_id_seq') - 3, -- ID1
        currval('entities_id_seq') - 4, -- CONTRACT_ADDRESS
        6,
        5,
        8,
        0,
        '{"name": "ASSET1", "price": 123, "@class": ".QxAssetOrderData", "issuer": "ISSUER1", "numberOfShares": 456}',
        true);

