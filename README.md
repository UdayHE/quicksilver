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
| ------ | ------ | ------ |
| `SET key value` | Stores a value |SET username Uday
| `GET key` | Retrieves a value|GET username
|` DEL key` | Deletes a key |DEL username
| `FLUSH` | Clears all data |FLUSH
| `EXIT` | Closes the connection|EXIT

## 📜 License
Apache License Version 2.0 <br>
https://github.com/UdayHE/Quicksilver/blob/master/LICENSE