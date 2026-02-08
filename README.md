# 🚀 QuickSilver
## In-Memory Distributed Database
QuickSilver is an open-source high-performance, in-memory key-value store with sharding, persistence, and multi-threaded client handling. <br> 
Designed for speed, scalability, and flexibility, it supports multiple database backends (InMemoryDB, ShardedDB), and persists data to disk.<br>

## 📌 Features
✅ In-Memory Storage – Fast key-value operations  <br>
✅ Sharding Support – Distributes data across multiple instances  <br>
✅ LRU Eviction – Removes least-recently used entries when full  <br>
✅ Persistence – Saves and loads data from disk  <br>
✅ Multi-threaded – Uses a thread pool for efficient client handling  <br>
✅ Command Pattern – Extensible command execution  <br>
✅ Cluster Support – Distribute data across multiple nodes  <br>
✅ Pub/Sub System – Publish and subscribe to topics for real-time messaging  <br>
✅ Consistent Hashing – Efficiently distributes keys across cluster nodes  <br>
✅ TTL Support – Automatic expiration of keys after a specified time  <br>

## 🏗️ Architecture Overview

QuickSilver follows a modular, layered architecture designed for high performance and scalability:

### Core Components

1. **Server Layer**
   - Main entry point (`Server.java`) that initializes the database, cluster service, and client handling
   - Manages server lifecycle, including graceful shutdown with data persistence
   - Handles client connections through a thread pool for concurrent request processing

2. **Client Handling**
   - `ClientHandler.java` manages individual client connections
   - Implements command routing and cluster redirection logic
   - Handles special commands (FLUSH, DUMP) and client lifecycle management

3. **Command Processing**
   - `CommandRegistry.java` implements the Command Pattern for extensible operations
   - Supports both database operations (SET, GET, DEL) and pub/sub operations (PUBLISH, SUBSCRIBE, UNSUBSCRIBE)
   - Commands are stateless and can be easily extended

4. **Database Layer**
   - **InMemoryDB**: LRU-eviction based in-memory store with TTL support
   - **ShardedDB**: Distributes data across multiple InMemoryDB instances using consistent hashing
   - Both implementations support persistence through serialization

5. **Clustering System**
   - `ClusterService`: Manages cluster-wide operations and data synchronization
   - `ConsistentHashing`: Distributes keys across cluster nodes efficiently
   - `ClusterClient`: Handles inter-node communication for distributed operations
   - Automatic data synchronization across cluster nodes during startup

6. **Pub/Sub System**
   - `PubSubManager`: Manages topic-based publish/subscribe messaging
   - Real-time message distribution to subscribed clients
   - Thread-safe subscriber management using concurrent collections

7. **Configuration & Utilities**
   - `Config.java`: Singleton configuration manager with property file support
   - `ThreadPoolManager`: Centralized thread pool management with graceful shutdown
   - Utility classes for cluster operations and common functionality

## 📂 Project Structure
```shell
📦 quicksilver
├── 📂 src
│   ├── 📂 main
│   │   ├── 📂 io.github.udayhe.quicksilver
│   │   │   ├── 📂 client
│   │   │   │   ├── ClientHandler.java       # Handles client connections
│   │   │   ├── 📂 cluster
│   │   │   │   ├── ClusterClient.java       # Sends commands to cluster nodes
│   │   │   │   ├── ClusterManager.java      # Manages Cluster nodes
│   │   │   │   ├── ClusterNode.java         # Cluster node
│   │   │   │   ├── ClusterService.java      # Serves the cluster
│   │   │   │   ├── ConsistentHashing.java   # ConsistentHashing
│   │   │   ├── 📂 command
│   │   │   │   ├── 📂 implementation
│   │   │   │   │   ├── Del.java             # DELETE command
│   │   │   │   │   ├── Exit.java            # EXIT command
│   │   │   │   │   ├── Flush.java           # FLUSH command
│   │   │   │   │   ├── Get.java             # GET command
│   │   │   │   │   ├── Set.java             # SET command
│   │   │   │   │   ├── Subscribe.java       # SUBSCRIBE command
│   │   │   │   │   ├── Unsubscribe.java     # UNSUBSCRIBE command
│   │   │   │   │   ├── Publish.java         # PUBLISH command
│   │   │   │   ├── Command.java             # Command interface
│   │   │   │   ├── CommandRegistry.java     # Manages command execution
│   │   │   ├── 📂 config
│   │   │   │   ├── Config.java              # Reads and manages configuration
│   │   │   ├── 📂 constant
│   │   │   │   ├── Constants.java           # Application-wide constants
│   │   │   ├── 📂 db
│   │   │   │   ├── 📂 implementation
│   │   │   │   │   ├── InMemoryDB.java      # In-memory key-value store
│   │   │   │   │   ├── ShardedDB.java       # Sharded database implementation
│   │   │   │   ├── DatabaseFactory.java     # Factory to create DB instances
│   │   │   │   ├── DB.java                  # Generic database interface
│   │   │   ├── 📂 enums                     
│   │   │   │   ├── Command.java             # Enum for commands  
│   │   │   │   ├── DBType.java              # Enum for database types
│   │   │   ├── 📂 threads
│   │   │   │   ├── ThreadPoolManager.java   # Centralized thread pool manager
│   │   │   ├── 📂 util
│   │   │   │   ├── ClusterUtil.java         # Cluster related utility methods
│   │   │   │   ├── Util.java                # Utility class
│   │   │   ├── Server.java                  # Main server entry point
│   │   ├── 📂 resources
│   │   │   ├── config.properties            # Configurations (port, shards, etc.)
├── 📂 test                                  # Unit tests
├── 📜 .gitignore                            # Git ignore rules
├── 📜 build.gradle                          # Gradle build file
├── 📜 Dockerfile                            # Docker configuration
├── 📜 gradlew                               # Gradle wrapper
├── 📜 LICENSE                               # License file
├── 📜 README.md                             # Project documentation
├── 📜 settings.gradle                       # Gradle settings

```

## 🚀 Getting Started

📦 1. Clone the Repository
```sh
git clone https://github.com/UdayHE/Quicksilver.git
cd Quicksilver
```

🔧 2. Build the Project
```sh
./gradlew build
```

⚡ 3. Run the Server
```sh
java -jar build/libs/Quicksilver-1.0-SNAPSHOT.jar
```

🔌 4. Default Port: `6379` <br>
Set custom port:
```sh
java -jar build/libs/Quicksilver-1.0-SNAPSHOT.jar 7000
```

## 🛠 Configuration
Modify config.properties in src/main/resources/:
```
server.port=7000
db.type=SHARDED
shard.count=4
shard.size=100
```

## 📝 Commands

| Command | Description | Examle |
| ----- | ------ | ------ |
|`SET key value` | Stores a value |SET username uday
|`GET key` | Retrieves a value|GET username
|`DEL key` | Deletes a key |DEL username
|`FLUSH` | Clears all data |FLUSH
|`EXIT` | Closes the connection|EXIT
|`SUBSCRIBE topic` | Subscribes to a topic |SUBSCRIBE news
|`UNSUBSCRIBE topic` | Unsubscribes from a topic |UNSUBSCRIBE news
|`PUBLISH topic message` | Publishes a message to a topic |PUBLISH news "Breaking: ..."

## 🔗 Cluster Setup

QuickSilver supports distributed clustering for high availability and horizontal scaling:

### Starting Multiple Nodes
```bash
# Node 1 (Port 6379)
java -jar build/libs/Quicksilver-1.0-SNAPSHOT.jar 6379

# Node 2 (Port 6380)  
java -jar build/libs/Quicksilver-1.0-SNAPSHOT.jar 6380

# Node 3 (Port 6381)
java -jar build/libs/Quicksilver-1.0-SNAPSHOT.jar 6381
```

### Cluster Features
- **Automatic Discovery**: Nodes automatically discover and register with each other
- **Data Synchronization**: Initial data sync across all cluster nodes on startup
- **Consistent Hashing**: Keys are distributed across nodes using consistent hashing
- **Load Distribution**: Client requests are automatically routed to the appropriate node
- **Fault Tolerance**: Cluster continues operating even if individual nodes fail

### Cluster Commands
All standard database commands work in cluster mode:
```bash
# Connect to any node and commands will be routed appropriately
telnet localhost 6379
SET user:1001 "John Doe"
GET user:1001
```

## 🔄 Sharding Configuration

Configure sharding in `config.properties`:
```
db.type=SHARDED
db.shard.total=4
db.shard.size=100
```

### Sharding Benefits
- **Horizontal Scaling**: Distribute data across multiple shards
- **Memory Efficiency**: Each shard has its own LRU cache
- **Parallel Processing**: Operations can be performed in parallel across shards
- **Consistent Performance**: Even distribution prevents hotspots

## 📊 Performance Characteristics

### In-Memory Performance
- **Sub-millisecond latency** for GET/SET operations
- **High throughput** with thread pool optimization
- **LRU eviction** prevents memory exhaustion
- **TTL support** for automatic data expiration

### Cluster Performance
- **Linear scaling** with additional nodes
- **Consistent hashing** minimizes data movement during scaling
- **Network optimization** with efficient inter-node communication
- **Load balancing** across cluster nodes

## 🧪 Testing

Run the test suite:
```bash
./gradlew test
```

## 🐳 Docker Support

Build and run with Docker:
```bash
# Build the image
docker build -t quicksilver .

# Run a single instance
docker run -p 6379:6379 quicksilver

# Run multiple instances for clustering
docker run -p 6379:6379 --name quicksilver-1 quicksilver
docker run -p 6380:6379 --name quicksilver-2 quicksilver
```

## 📜 License
Apache License Version 2.0 <br>
https://github.com/UdayHE/Quicksilver/blob/master/LICENSE
