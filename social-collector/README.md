# Deploy

## To Cloud Foundry

### Check RabbitMQ service is enabled
```
% cf marketplace -e p.rabbitmq
Getting service plan information for service offering p.rabbitmq in org system / space social-wordcloud as admin...

broker: rabbitmq-odb
   plan             description                              free or paid   costs
   on-demand-plan   This plan provides a RabbitMQ instance   free    
```

### Check MySQL service is enabled

```
% cf marketplace -e p.mysql   
Getting service plan information for service offering p.mysql in org system / space social-wordcloud as admin...

broker: dedicated-mysql-broker
   plan          description                                             free or paid   costs
   db-small      This plan provides a small dedicated MySQL instance.    free           
   db-medium     This plan provides a medium dedicated MySQL instance.   free           
   db-large      This plan provides a large dedicated MySQL instance.    free           
   ha-db-small   This plan provides a large dedicated MySQL instance.    free  
```

### Check credhub is enabled

```
% cf m -e credhub
Getting service plan information for service offering credhub in org system / space social-wordcloud as admin...

broker: credhub-broker
   plan      description                                           free or paid   costs
   default   Stores configuration parameters securely in CredHub   free   
```

### Create services

```
% cf create-service p.rabbitmq on-demand-plan rabbitmq-broker
```

```
% cf create-service p.mysql db-small offset-db    
```

```
cf create-service credhub default secret-store -c '{"newsapi-key": "", "mastodon-token": "", "apifylinkedin-token": "", "stocksapi-key": ""}' 
```