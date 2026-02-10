```
source /usr/local/greenplum-db/greenplum_path.sh
gpconfig -c cron.database_name -v 'devdb' --skipvalidation
gpconfig -c shared_preload_libraries -v "`gpconfig -s shared_preload_libraries | grep "Coordinator value" | awk '{printf $3}'`,pg_cron"
gpstop -ar
```

cf create-org-quota 100 --reserved-route-ports 100  