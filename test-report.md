### Test

#### Test SQL

```sql
use tpch;
select * from lineorder where lo_partkey = 999;
```

#### Without PerfLA

- 11/16

  234.306 seconds, 3.8 min (1.3 h)

- 11/19

  198.441 seconds, 3.2 min (1.0 h)

#### With PerfLA turn on

- 11/16

  158.941 seconds, 2.6 min (41 min)
  
- 11/19

  202.625 seconds, 3.3 min (1.0 h)
  
  - Checkpoint
  
    212.993 seconds, 3.4 min (1.2 h)

#### With PerfLA turn off

- 11/16

  204.412 seconds, 3.3 min (1.1 h)
  
- 11/19

  217.039 seconds, 3.5 min (1.2 h)
  
  - Checkpoint
  
    202.538 seconds, 3.3 min (1.0 h)	
