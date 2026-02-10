```
source /usr/local/greenplum-db/greenplum_path.sh
gpconfig -c cron.database_name -v 'devdb' --skipvalidation
gpconfig -c shared_preload_libraries -v "`gpconfig -s shared_preload_libraries | grep "Coordinator value" | awk '{printf $3}'`,pg_cron"
gpstop -ar
```

```
cf update-quota default --reserved-route-ports 10
cf cups fluentd-drain -l syslog://tcp.app.tas.vpsmart.aws.lespaulstudioplus.info:25140
```