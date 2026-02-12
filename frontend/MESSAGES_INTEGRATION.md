# Messages Integration Guide

## Overview

The GraphQL messages manager has been integrated into the main frontend dashboard. Users can now switch between the Analytics Dashboard and Messages views using tab navigation.

## Features Added

### 📊 Tab Navigation

- **Dashboard Tab**: Shows the existing analytics (map, stocks, word cloud)
- **Messages Tab**: Shows the GraphQL messages manager

### 💬 Messages Manager

- **Query Messages**: Fetch social messages via GraphQL
- **Filter Options**:
  - Origin (Mastodon, LinkedIn, News)
  - Language (en, ja, es, fr)
  - Name search
- **Select & Delete**: Bulk selection and deletion of messages
- **Real-time Stats**: Shows total and selected message counts

## File Structure

```
frontend/src/
├── components/
│   ├── MapWidget.jsx          # Existing - Map visualization
│   ├── StockWidget.jsx         # Existing - Stock chart
│   ├── CloudWidget.jsx         # Existing - Word cloud
│   └── MessagesWidget.jsx      # NEW - Messages manager
├── hooks/
│   └── useDashboardData.js     # Existing - Dashboard data hook
├── App.jsx                     # MODIFIED - Added tab navigation
├── App.css                     # MODIFIED - Added messages styles
├── api.js                      # MODIFIED - Added GraphQL functions
└── main.jsx                    # Existing - Entry point
```

## New Components

### MessagesWidget.jsx

React component that:
- Fetches messages using GraphQL queries
- Displays messages in a table
- Handles filtering and selection
- Deletes messages via GraphQL mutations

**Key Features:**
- State management with React hooks
- Async data fetching
- Interactive table with checkboxes
- Filter controls
- Error handling

### GraphQL API Functions (api.js)

**`fetchSocialMessages(filters)`**
- Queries social messages with optional filters
- Returns array of message objects

**`deleteSocialMessages(ids)`**
- Deletes messages by IDs
- Returns deletion result

## Configuration

### Development Mode

The Vite dev server proxies GraphQL requests:

```javascript
// vite.config.js
server: {
  proxy: {
    '/graphql': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

### Environment Variables

You can override the GraphQL endpoint:

```bash
# .env.local
VITE_GRAPHQL_URL=http://your-graphql-server:8080/graphql
```

## Usage

### Starting the Frontend

```bash
cd frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`

### Backend Requirements

The following backends must be running:

1. **REST API** (port 8080) - For dashboard data:
   - `/stocks` - Stock data
   - `/term/{duration}` - Term frequency data
   - `/messages/analysis` - Map data

2. **GraphQL API** (port 8080) - For messages:
   - `/graphql` - GraphQL endpoint

3. **Analytics gRPC** (port 9090) - For message deletion

### Switching Between Views

1. **Dashboard View** (default):
   - Shows map, stock chart, and word cloud
   - Auto-refreshes every 30 seconds

2. **Messages View**:
   - Click "💬 Messages" tab
   - Apply filters and search
   - Select messages to delete

## GraphQL Schema

### Query: socialMessages

```graphql
query GetMessages($origin: String, $lang: String, $name: String) {
  socialMessages(origin: $origin, lang: $lang, name: $name, first: 100) {
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
  }
}
```

### Mutation: deleteSocialMessages

```graphql
mutation DeleteMessages($ids: [ID!]!) {
  deleteSocialMessages(ids: $ids) {
    message
    deletedCount
  }
}
```

## Styling

All styles are in `App.css`:

- **Navigation Tabs**: `.nav-tabs`, `.nav-tab`
- **Messages Container**: `.messages-container`
- **Filters**: `.messages-controls`, `.filter-group`
- **Table**: `.messages-table`
- **Badges**: `.badge-origin`, `.badge-lang`

### Customization

To customize colors:

```css
:root {
  --primary: #007bff;    /* Change primary color */
  --border: #dfe3e8;     /* Change border color */
  --card-bg: #ffffff;    /* Change card background */
}
```

## Testing

### Manual Testing

1. **View Messages**:
   - Navigate to Messages tab
   - Verify messages load

2. **Apply Filters**:
   - Select origin, language, or name
   - Click Search
   - Verify filtered results

3. **Delete Messages**:
   - Select one or more messages
   - Click "Delete Selected"
   - Confirm deletion
   - Verify messages are removed

### GraphiQL Testing

Test GraphQL queries directly:
```
http://localhost:8080/graphiql
```

## Troubleshooting

### Issue: "Failed to fetch messages"

**Cause**: GraphQL endpoint not available

**Solution**:
1. Verify GraphQL service is running on port 8080
2. Check network tab in browser DevTools
3. Test with GraphiQL: http://localhost:8080/graphiql

### Issue: Messages not deleting

**Cause**: Analytics gRPC service not running

**Solution**:
1. Start analytics module on port 9090
2. Check GraphQL logs for gRPC errors
3. Verify gRPC configuration in GraphQL application.properties

### Issue: Filters not working

**Cause**: Invalid filter values or GraphQL query error

**Solution**:
1. Check browser console for errors
2. Verify filter values match database values
3. Test query in GraphiQL with same filters

## Production Build

### Build Frontend

```bash
cd frontend
npm run build
```

Output: `frontend/dist/`

### Deploy

The built files in `dist/` can be:
1. Served by a static web server (nginx, Apache)
2. Deployed to CDN
3. Served by Spring Boot (copy to `src/main/resources/static`)

### Environment Configuration

Set production GraphQL URL:

```bash
VITE_GRAPHQL_URL=https://your-domain.com/graphql npm run build
```

## Integration Benefits

✅ **Single Application**: One frontend for all features
✅ **Consistent UX**: Same look and feel across views
✅ **Shared State**: Can potentially share data between views
✅ **Easy Navigation**: Simple tab switching
✅ **Maintainable**: All code in one repository
✅ **Deployable**: Single build artifact

## Future Enhancements

Potential improvements:
- Add pagination for large datasets
- Export messages to CSV
- Advanced filtering (date range, text search)
- Bulk edit operations
- Message details modal
- Real-time updates with GraphQL subscriptions
- Unified search across dashboard and messages

## Related Documentation

- [Frontend Guide](../graphql/FRONTEND_GUIDE.md) - Standalone GraphQL frontend
- [Delete API](../graphql/DELETE_API.md) - GraphQL delete API docs
- [Quick Start](../graphql/QUICKSTART.md) - Backend setup guide
