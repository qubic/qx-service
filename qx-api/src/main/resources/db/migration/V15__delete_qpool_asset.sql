-- doesn't delete transactions

delete
from trades
where asset_id = (select id
                  from assets
                  where issuer = 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB'
                    and name = 'QPOOL');

delete
from assets
where issuer = 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB'
  and name = 'QPOOL';