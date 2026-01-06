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
Getting service plan information for service offering credhub in org system / space social-wordcloud as admin...

broker: credhub-broker
   plan      description                                           free or paid   costs
   default   Stores configuration parameters securely in CredHub   free   
```

### Create services

```
% cf create-service genai cpu-embeddings cpu-embeddings
```

```
% cf create-service genai gpt-oss gpt-oss
```

```
cf create-service credhub default greenplum -c '{"url": "", "password": "", "username": ""}' 
```