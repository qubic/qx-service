-- change assetName to name
update transactions
set extra_data = (replace(extra_data::text, '"assetName"', '"name"'))::jsonb
where input_type = 2;

-- change numberOfUnits to numberOfShares
update transactions
set extra_data = (replace(extra_data::text, '"numberOfUnits"', '"numberOfShares"'))::jsonb
where input_type in (1, 2)
returning *;

