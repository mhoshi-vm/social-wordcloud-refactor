# Quick Start Guide - Social Messages Manager

## Prerequisites

1. **Analytics Module** running on port 9090 (for gRPC)
2. **PostgreSQL** database with social messages data
3. **Java 21** installed

## Starting the Application

### Option 1: Using Maven (Development)

```bash
cd graphql
./mvnw spring-boot:run
```

### Option 2: Using JAR (Production)

```bash
cd graphql
./mvnw clean package
java -jar target/graphql-0.0.1-SNAPSHOT.jar
```

## Accessing the Application

Once the application is running, you can access:

### 🌐 Frontend Web Interface
```
http://localhost:8080/
```
The main user interface for viewing and managing social messages.

### 📊 GraphiQL IDE
```
http://localhost:8080/graphiql
```
Interactive GraphQL query editor for testing queries and mutations.

## Using the Frontend

### 1. View Messages

When you first open the page, messages will load automatically.

### 2. Filter Messages

- **By Origin**: Select from dropdown (Mastodon, LinkedIn, News)
- **By Language**: Select language code (en, ja, es, fr)
- **By Name**: Type author name in search box
- Click **🔍 Search** to apply filters

### 3. Select Messages

- Click individual checkboxes to select specific messages
- Click the checkbox in the header to select all visible messages
- Selected count shows in the stats bar

### 4. Delete Messages

1. Select one or more messages
2. Click **🗑️ Delete Selected** button
3. Confirm the deletion
4. Messages will be deleted via GraphQL mutation
5. Page refreshes automatically to show updated list

## Example GraphQL Queries

### Query All Messages

```graphql
{
  socialMessages(first: 10) {
    edges {
      node {
        id
        origin
        text
        name
        lang
        createDateTime
      }
    }
  }
}
```

### Query with Filters

```graphql
{
  socialMessages(origin: "MASTODON", lang: "en", first: 20) {
    edges {
      node {
        id
        text
        name
      }
    }
    pageInfo {
      hasNextPage
    }
  }
}
```

### Delete Mutation

```graphql
mutation {
  deleteSocialMessages(ids: ["message-id-1", "message-id-2"]) {
    message
    deletedCount
  }
}
```

## Configuration

### Database Connection

Edit `application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your-database
spring.datasource.username=your-username
spring.datasource.password=your-password
```

### gRPC Client Connection

Edit `application-local.properties`:

```properties
grpc.client.analytics.host=localhost
grpc.client.analytics.port=9090
```

### Server Port

Default port is 8080. To change:

```properties
server.port=8081
```

## Troubleshooting

### Issue: "Failed to load messages"

**Cause**: GraphQL service not responding

**Solution**:
1. Check application logs
2. Verify database connection
3. Test with GraphiQL at http://localhost:8080/graphiql

### Issue: "Delete failed"

**Cause**: Analytics gRPC service not reachable

**Solution**:
1. Start analytics module on port 9090
2. Check gRPC configuration in application-local.properties
3. Verify network connectivity

### Issue: Page shows blank

**Cause**: Static resources not loading

**Solution**:
1. Clear browser cache
2. Check browser console for errors
3. Verify `static/index.html` exists in JAR

### Issue: CORS errors in browser console

**Cause**: Cross-origin request blocked

**Solution**:
Add CORS configuration (already included in spring-boot-starter-web)

## API Documentation

- **Frontend Guide**: [FRONTEND_GUIDE.md](./FRONTEND_GUIDE.md)
- **Delete API**: [DELETE_API.md](./DELETE_API.md)
- **Test Summary**: [TEST_SUMMARY.md](./TEST_SUMMARY.md)

## Project Structure

```
graphql/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── .../graphql/
│   │   │       └── messages/
│   │   │           ├── SocialMessageController.java
│   │   │           ├── SocialMessageRepository.java
│   │   │           └── SocialMessageConfig.java
│   │   ├── proto/
│   │   │   └── deleteMessages.proto
│   │   └── resources/
│   │       ├── graphql/
│   │       │   └── schema.graphqls
│   │       ├── static/
│   │       │   └── index.html          ← Frontend
│   │       ├── application.properties
│   │       └── application-local.properties
│   └── test/
└── pom.xml
```

## Next Steps

1. **Customize UI**: Edit `src/main/resources/static/index.html`
2. **Add Features**: Extend GraphQL schema in `schema.graphqls`
3. **Add Tests**: Create integration tests for GraphQL queries
4. **Deploy**: Package and deploy to your environment

## Support

For issues or questions:
1. Check the documentation in the project
2. Review application logs
3. Test queries in GraphiQL
4. Check database and gRPC service connectivity

Happy message managing! 🚀
