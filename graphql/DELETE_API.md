# GraphQL Delete API

## Overview

The GraphQL module now includes a delete mutation that executes a gRPC call to the analytics module to delete social messages from the database.

## Implementation

### GraphQL Schema

```graphql
type Mutation {
    deleteSocialMessages(ids: [ID!]!): DeleteResult
}

type DeleteResult {
    message: String
    deletedCount: Int
}
```

### gRPC Integration

- **Proto file**: `src/main/proto/deleteMessages.proto`
- **Service**: `Delete` gRPC service from analytics module
- **Method**: `DeleteMessages` - accepts a list of IDs and returns a confirmation message

### Configuration

The gRPC client connection can be configured in `application.properties`:

```properties
grpc.client.analytics.host=localhost
grpc.client.analytics.port=9090
```

## Usage

### GraphQL Mutation

To delete social messages using GraphiQL or any GraphQL client:

```graphql
mutation {
  deleteSocialMessages(ids: ["message-id-1", "message-id-2", "message-id-3"]) {
    message
    deletedCount
  }
}
```

### Example Response

```json
{
  "data": {
    "deleteSocialMessages": {
      "message": "Deleted",
      "deletedCount": 3
    }
  }
}
```

### Using curl

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { deleteSocialMessages(ids: [\"id1\", \"id2\"]) { message deletedCount } }"
  }'
```

## Architecture

1. **GraphQL Layer**: Receives the delete mutation request
2. **gRPC Client**: Forwards the request to the analytics module via gRPC
3. **Analytics Module**: Processes the deletion and removes entries from the database
4. **Response**: Returns confirmation back through the gRPC client to GraphQL

## Files Modified/Added

- `graphql/src/main/proto/deleteMessages.proto` - Proto definition (copied from analytics)
- `graphql/src/main/resources/graphql/schema.graphqls` - Added mutation and DeleteResult type
- `graphql/src/main/java/.../SocialMessageConfig.java` - Added gRPC client beans
- `graphql/src/main/java/.../SocialMessageController.java` - Added mutation handler
- `graphql/src/main/resources/application-local.properties` - Added gRPC client configuration
- `graphql/pom.xml` - Added spring-boot-starter-web dependency

## Prerequisites

- Analytics module must be running and accessible at the configured host:port
- Analytics module must have the gRPC Delete service running on port 9090 (default)

## Error Handling

The gRPC client will throw exceptions if:
- The analytics service is not reachable
- The deletion fails in the analytics module
- Invalid IDs are provided

These exceptions will be propagated to the GraphQL response as errors.
