-- asset id and bid is implicitly similar if transaction id is same
ALTER TABLE trades ADD CONSTRAINT unique_trade UNIQUE (tick_time, transaction_id, price, number_of_shares, maker_id);