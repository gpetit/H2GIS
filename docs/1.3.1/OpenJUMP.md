Work in progress. Do not take account of this page.

```sql
drop alias if exists postgis_version;
CREATE ALIAS postgis_version AS $$
String nextPrime() {
    return "1.3 USE_GEOS=1 USE_PROJ=1 USE_STATS=1";
}
$$;
drop alias if exists has_schema_privilege;

CREATE ALIAS has_schema_privilege AS $$
Boolean nextPrime(String a, String b) {
    return true;
}
$$;
```


```sql
select the_geom::varbinary the_geom from BATI_INDIFFERENCIE where the_geom && ${view:0}
```
```sql
SELECT ST_Extent(the_geom)::varbinary from BATI_INDIFFERENCIE;
```

