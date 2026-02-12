# Quick Start - Integrated Frontend

## 🚀 Start Everything

### Prerequisites
- Java 21 installed
- Node.js 18+ installed
- PostgreSQL running with data

### Step 1: Start Backend Services

```bash
# Terminal 1: Start Analytics gRPC service (port 9090)
cd analytics
./mvnw spring-boot:run

# Terminal 2: Start GraphQL service (port 8080)
cd graphql
./mvnw spring-boot:run
```

Wait for both services to start completely.

### Step 2: Start Frontend

```bash
# Terminal 3: Start React frontend (port 5173)
cd frontend
npm install
npm run dev
```

### Step 3: Open Browser

Navigate to: **http://localhost:5173**

## 🎯 What You'll See

### Default View: Dashboard
- 🗺️ Map showing message locations
- 📈 Stock price chart (AVGO)
- ☁️ Word cloud from social messages

### Switch to Messages
1. Click **"💬 Messages"** tab at top
2. See table of social messages
3. Use filters to search
4. Select and delete messages

## 📋 Quick Actions

### Filter Messages
```
1. Click "💬 Messages" tab
2. Select origin: "MASTODON" / "LINKEDIN" / "NEWS"
3. Select language: "en" / "ja" / "es" / "fr"
4. Type author name in search box
5. Click "🔍 Search"
```

### Delete Messages
```
1. Check boxes next to messages
2. Or click header checkbox for "Select All"
3. Click "🗑️ Delete Selected (N)"
4. Confirm deletion
5. Messages removed and page refreshes
```

### Switch Views
```
📊 Dashboard → 💬 Messages → 📊 Dashboard
(Click tabs to switch)
```

## 🔧 Configuration

### Change API URLs

Create `.env.local` in `frontend/` directory:

```env
# REST API for dashboard
VITE_API_BASE_URL=http://your-api-host:8080

# GraphQL API for messages
VITE_GRAPHQL_URL=http://your-graphql-host:8080/graphql
```

### Change Ports

**GraphQL Backend:**
```properties
# graphql/src/main/resources/application.properties
server.port=8080
```

**Frontend:**
```bash
npm run dev -- --port 3000
```

## 🐛 Troubleshooting

### Dashboard Shows "Loading..." Forever

**Problem:** REST API not running

**Fix:**
```bash
cd graphql  # or restapi
./mvnw spring-boot:run
```

**Verify:** http://localhost:8080/stocks

### Messages Tab Shows Empty

**Problem:** GraphQL service not running or no data

**Fix:**
1. Check GraphQL is running: http://localhost:8080/graphiql
2. Run test query in GraphiQL:
```graphql
{
  socialMessages(first: 10) {
    edges {
      node {
        id
        text
      }
    }
  }
}
```

### Delete Button Doesn't Work

**Problem:** Analytics gRPC service not running

**Fix:**
```bash
cd analytics
./mvnw spring-boot:run
```

**Verify:** Check logs show "Started AnalyticsApplication"

### Port Already in Use

**Problem:** Service already running on that port

**Fix:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill it (replace PID)
kill -9 <PID>

# Or change port in application.properties
```

## 📱 Access from Mobile

### Local Network Access

1. Find your computer's IP:
```bash
# macOS/Linux
ifconfig | grep "inet "

# Windows
ipconfig
```

2. Update Vite config:
```javascript
// frontend/vite.config.js
server: {
  host: '0.0.0.0',  // Allow external access
  proxy: { /* ... */ }
}
```

3. Access from mobile:
```
http://YOUR_IP:5173
```

## 🏗️ Production Deployment

### Build Frontend
```bash
cd frontend
npm run build
```

### Deploy Options

**Option 1: Static Hosting**
```bash
# Upload dist/ folder to:
- Netlify
- Vercel
- AWS S3 + CloudFront
- GitHub Pages
```

**Option 2: Spring Boot Static**
```bash
# Copy to GraphQL module
cp -r frontend/dist/* graphql/src/main/resources/static/

# Build and run GraphQL
cd graphql
./mvnw clean package
java -jar target/graphql-0.0.1-SNAPSHOT.jar

# Access at http://localhost:8080/
```

**Option 3: Nginx**
```nginx
server {
    listen 80;
    root /path/to/frontend/dist;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /graphql {
        proxy_pass http://localhost:8080;
    }
}
```

## 🎓 Learn More

- **React Basics**: https://react.dev/learn
- **GraphQL**: https://graphql.org/learn/
- **Vite**: https://vitejs.dev/guide/

## 📚 Documentation

- `MESSAGES_INTEGRATION.md` - Integration details
- `INTEGRATION_SUMMARY.md` - Complete summary
- `../graphql/DELETE_API.md` - GraphQL API docs
- `../graphql/FRONTEND_GUIDE.md` - Standalone frontend guide

## ✅ Verification Checklist

After starting everything:

- [ ] Frontend loads at http://localhost:5173
- [ ] Dashboard tab shows map, stocks, word cloud
- [ ] Messages tab shows message table
- [ ] Filters work correctly
- [ ] Delete functionality works
- [ ] No console errors in browser
- [ ] All backend services running

## 🎉 You're Ready!

The integrated frontend is now running with:
- ✅ Analytics Dashboard
- ✅ GraphQL Messages Manager
- ✅ Tab Navigation
- ✅ Full CRUD Operations

Enjoy exploring your data! 🚀
