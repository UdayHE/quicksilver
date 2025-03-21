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

## 📜 License
Apache License Version 2.0 <br>
https://github.com/UdayHE/Quicksilver/blob/master/LICENSE