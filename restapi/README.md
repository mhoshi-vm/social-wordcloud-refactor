# Deploy

## To Cloud Foundry


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

```
cf create-service credhub default greenplum -c '{"url": "", "password": "", "username": "", "hostname":"", "database": ""}' 
```