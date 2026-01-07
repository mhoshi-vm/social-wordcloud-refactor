# Deploy

## To Cloud Foundry

### Check GenAI service is enabled
```
% cf marketplace -e genai
```

### Check RabbitMQ service is already deployed

```
 % cf service rabbitmq-broker
```

### Check credhub is enabled

```
% cf m -e credhub
Getting service plan information for service offering credhub in org system / space social-devdb as admin...

broker: credhub-broker
   plan      description                                           free or paid   costs
   default   Stores configuration parameters securely in CredHub   free   
```

### Setup greenplum

```
createdb devdb
# The following is only for demo purpose
psql -d devdb -c "CREATE USER wcadmin SUPERUSER LOGIN PASSWORD 'wcadmin'"
psql -d devdb -c "GRANT ALL PRIVILEGES ON DATABASE devdb TO wcadmin"
echo 'host  devdb  wcadmin  0.0.0.0/0  password' >> /gpdata/coordinator/gpseg-1/pg_hba.conf
gpstop -u
```

### Create services

```
% cf create-service genai on-prem-cpu on-prem-cpu
```

```
cf create-service credhub default greenplum -c '{"url": "", "password": "", "username": ""}' 
```