# Social Messages Manager - Frontend Guide

## Overview

A modern, responsive web interface for managing social media messages using GraphQL queries and mutations.

## Features

### 🔍 Query & Filter
- **Origin Filter**: Filter by source (Mastodon, LinkedIn, News)
- **Language Filter**: Filter by language (English, Japanese, Spanish, French)
- **Name Search**: Search messages by author name
- **Pagination Support**: Built-in support for paginated results

### 📝 Message Management
- **View Messages**: Display messages in a clean, organized table
- **Select Messages**: Individual or bulk selection with "Select All"
- **Delete Messages**: Remove selected messages with confirmation
- **Live Updates**: Automatic refresh after deletion

### 🎨 User Experience
- Modern gradient design with smooth animations
- Responsive layout for all screen sizes
- Real-time selection counter
- Loading states and error handling
- Success/error notifications

## Accessing the Frontend

### Local Development

1. Start the GraphQL module:
   ```bash
   cd graphql
   ./mvnw spring-boot:run
   ```

2. Open your browser and navigate to:
   ```
   http://localhost:8080/
   ```

### Production

The frontend is automatically bundled with the GraphQL module and served as a static resource.

Access at: `http://<your-host>:<port>/`

## GraphQL Integration

### Queries Used

**Fetch Messages:**
```graphql
query GetMessages($origin: String, $lang: String, $name: String) {
    socialMessages(origin: $origin, lang: $lang, name: $name, first: 50) {
        edges {
            node {
                id
                origin
                text
                lang
                name
                url
                createDateTime
            }
        }
        pageInfo {
            hasNextPage
            hasPreviousPage
        }
    }
}
```

**Delete Messages:**
```graphql
mutation DeleteMessages($ids: [ID!]!) {
    deleteSocialMessages(ids: $ids) {
        message
        deletedCount
    }
}
```

## Usage Guide

### Viewing Messages

1. **Initial Load**: Messages are automatically loaded when the page opens
2. **Apply Filters**: Select origin, language, or enter a name to filter
3. **Click Search**: Apply the selected filters

### Deleting Messages

1. **Select Messages**:
   - Check individual message checkboxes
   - Or use "Select All" to select all visible messages
2. **Click Delete**: The "Delete Selected" button activates when messages are selected
3. **Confirm**: Confirm the deletion in the dialog
4. **Automatic Refresh**: Messages are reloaded after successful deletion

### Filter Options

| Filter | Options | Description |
|--------|---------|-------------|
| Origin | All Sources, Mastodon, LinkedIn, News | Filter by message source platform |
| Language | All Languages, en, ja, es, fr | Filter by message language |
| Name | Text input | Search by author name (partial match) |

## Technical Details

### Technology Stack
- **Vanilla JavaScript**: No frameworks, fast and lightweight
- **CSS3**: Modern styling with gradients and animations
- **Fetch API**: Native HTTP requests for GraphQL
- **HTML5**: Semantic markup

### Browser Compatibility
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

### Performance
- **Initial Load**: ~200ms (depending on data size)
- **Filter/Search**: Instant UI update + GraphQL query time
- **Delete Operation**: ~100ms + backend processing time

## GraphQL Endpoint Configuration

The frontend expects the GraphQL endpoint at:
```
/graphql
```

This is the default endpoint provided by Spring Boot GraphQL starter.

### CORS Configuration

If accessing from a different origin, ensure CORS is configured in the backend:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/graphql")
                .allowedOrigins("http://localhost:3000", "http://localhost:8080")
                .allowedMethods("POST", "GET", "OPTIONS");
    }
}
```

## Customization

### Styling

Modify the `<style>` section in `index.html`:

```css
/* Change primary color */
.btn-primary {
    background: #your-color;
}

/* Change gradient */
body {
    background: linear-gradient(135deg, #color1 0%, #color2 100%);
}
```

### Page Size

Modify the GraphQL query in JavaScript:

```javascript
socialMessages(origin: $origin, lang: $lang, name: $name, first: 100) {
    // Changed from 50 to 100
```

### Add New Filters

1. Add HTML filter control
2. Add filter variable to GraphQL query
3. Read filter value in `loadMessages()` function

## Troubleshooting

### Issue: "Failed to load messages"
- **Solution**: Check that the GraphQL module is running on port 8080
- Verify: Open http://localhost:8080/graphiql in your browser

### Issue: "Delete failed"
- **Solution**: Ensure analytics gRPC service is running on port 9090
- Check logs: `tail -f graphql/target/logs/application.log`

### Issue: No messages displayed
- **Solution**: Check that data exists in the database
- Use GraphiQL to test: http://localhost:8080/graphiql

### Issue: CORS errors
- **Solution**: Verify CORS configuration in WebConfig.java
- Ensure the origin is whitelisted

## Screenshots

### Main View
- Clean table layout with filtering options
- Selection checkboxes for bulk operations
- Color-coded badges for origin and language

### After Selection
- Active delete button when messages are selected
- Real-time counter showing selected count

### After Deletion
- Success notification
- Automatic page refresh with updated data

## API Documentation

For detailed GraphQL API documentation, see:
- [DELETE_API.md](./DELETE_API.md) - Delete mutation details
- GraphiQL Interface: http://localhost:8080/graphiql

## Development

To modify the frontend:

1. Edit `src/main/resources/static/index.html`
2. Restart the application or use Spring Boot DevTools for hot reload
3. Refresh your browser

No build step required - changes are immediately reflected!
