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

## 📂 Project Structure
```shell
📦 Quicksilver
├── 📂 src
│   ├── 📂 main
│   │   ├── 📂 io.github.udayhe.quicksilver
│   │   │   ├── 📂 client
│   │   │   │   ├── ClientHandler.java      # Handles client connections
│   │   │   ├── 📂 command
│   │   │   │   ├── 📂 enums
│   │   │   │   │   ├── Command.java        # Enum for supported commands
│   │   │   │   ├── 📂 implementation
│   │   │   │   │   ├── Command.java        # Command interface
│   │   │   │   │   ├── CommandRegistry.java # Manages command execution
│   │   │   ├── 📂 config
│   │   │   │   ├── Config.java             # Reads and manages configuration
│   │   │   ├── 📂 constant
│   │   │   │   ├── Constants.java          # Application-wide constants
│   │   │   ├── 📂 db
│   │   │   │   ├── 📂 enums
│   │   │   │   │   ├── DBType.java         # Enum for database types
│   │   │   │   ├── 📂 implementation
│   │   │   │   │   ├── InMemoryDB.java     # In-memory key-value store
│   │   │   │   │   ├── ShardedDB.java      # Sharded database implementation
│   │   │   │   │   ├── DatabaseFactory.java # Factory to create DB instances
│   │   │   │   │   ├── DB.java             # Generic database interface
│   │   │   ├── 📂 threadpool
│   │   │   │   ├── ThreadPoolManager.java  # Centralized thread pool manager
│   │   │   ├── 📂 util
│   │   │   │   ├── LogoUtil.java           # Prints QuickSilver logo
│   │   │   ├── Server.java                 # Main server entry point
│   │   ├── 📂 resources
│   │   │   ├── config.properties           # Configurations (port, shards, etc.)
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
|`SET key value` | Stores a value |SET username Uday
|`GET key` | Retrieves a value|GET username
|`DEL key` | Deletes a key |DEL username
|`FLUSH` | Clears all data |FLUSH
|`EXIT` | Closes the connection|EXIT

## 📜 License
Apache License Version 2.0 <br>
https://github.com/UdayHE/Quicksilver/blob/master/LICENSE