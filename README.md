# QuickSilver
## In-Memory Distributed Key-Value Store

QuickSilver is a high-performance, in-memory key-value store with full **RESP (Redis Serialization Protocol)** support, sharding, clustering, pub/sub messaging, TTL expiration, and disk persistence.

Because it speaks the Redis wire protocol, any Redis client — including `redis-cli` — works with QuickSilver out of the box.

---

## Features

- **RESP Protocol** — Full Redis wire-protocol support; compatible with `redis-cli` and any Redis client library
- **Inline command fallback** — Plain-text commands via `telnet`/`nc` also accepted
- **In-Memory Storage** — Sub-millisecond GET/SET latency
- **LRU Eviction** — Evicts least-recently used entries when the store is full
- **TTL Support** — Automatic key expiration via background sweep
- **Sharding** — Distributes data across `N` in-memory shards
- **Consistent Hashing** — Efficient key routing across cluster nodes with minimal rehashing
- **Clustering** — Multi-node deployment with automatic data sync on startup
- **Pub/Sub** — Topic-based real-time message fan-out
- **Persistence** — Save/restore the data set to/from disk
- **Security limits** — Max key size, bulk-string size, array depth, and idle-timeout enforced at the protocol layer
- **Command Pattern** — Stateless, extensible command implementations

---

## Architecture

### Layers

```
Client (redis-cli / telnet / any RESP client)
        │  TCP
        ▼
  ClientHandler          ← RESP parse → dispatch → RESP encode
        │
        ├─ CommandRegistry   ← Command Pattern; routes to command implementations
        │       └─ Set / Get / Del / Flush / Dump / Subscribe / Unsubscribe / Publish / Exit
        │
        ├─ ClusterService    ← consistent-hashing ring; forwards to remote node if needed
        │       └─ ClusterClient  ← inter-node RESP client (fire-and-forget / response)
        │
        └─ DB (InMemoryDB / ShardedDB)
                └─ LRU eviction + TTL background sweep + disk persistence
```

### Design Patterns Used

| Pattern | Where |
|---|---|
| **Command** | `Command<K,V>` interface; one class per operation |
| **Factory Method** | `RespParser.parse()` dispatches by first byte; `DatabaseFactory` creates DB instances |
| **Sealed Interface + Records** | `RespValue` type hierarchy — exhaustive pattern matching via `switch` |
| **Facade** | `RespEncoder` — single API to encode any `RespValue` to a stream |
| **Strategy** | `DB` interface with `InMemoryDB` and `ShardedDB` implementations |
| **Singleton** | `Config`, `ThreadPoolManager` |

### RESP Type Hierarchy

```
RespValue (sealed interface)
├── SimpleString   →  +OK\r\n
├── RespError      →  -ERR message\r\n
├── RespInteger    →  :42\r\n
├── BulkString     →  $6\r\nfoobar\r\n  (or $-1\r\n for nil)
└── RespArray      →  *3\r\n...         (or *-1\r\n for nil)
```

---

## Project Structure

```
quicksilver/
├── src/main/java/io/github/udayhe/quicksilver/
│   ├── Server.java                          # Entry point; wires DB, cluster, thread pool
│   ├── client/
│   │   └── ClientHandler.java               # Per-connection lifecycle; RESP parse/encode; cluster routing
│   ├── cluster/
│   │   ├── ClusterClient.java               # Inter-node RESP client (sendCommand / sendCommandWithResponse)
│   │   ├── ClusterManager.java              # Node registry
│   │   ├── ClusterNode.java                 # Record: host + port
│   │   ├── ClusterService.java              # Cluster lifecycle and data sync
│   │   └── ConsistentHashing.java           # TreeMap-based hash ring
│   ├── command/
│   │   ├── Command.java                     # Functional interface returning RespValue
│   │   ├── CommandRegistry.java             # Dispatch table; case-insensitive lookup
│   │   └── implementation/
│   │       ├── Set.java                     # → SimpleString("OK")
│   │       ├── Get.java                     # → BulkString | NIL
│   │       ├── Del.java                     # → RespInteger(0|1)
│   │       ├── Flush.java                   # → SimpleString("OK"); cluster broadcast
│   │       ├── Dump.java                    # → BulkString (serialized store)
│   │       ├── Subscribe.java               # → RespArray [subscribe, topic, count]
│   │       ├── Unsubscribe.java             # → RespArray [unsubscribe, topic, count]
│   │       ├── Publish.java                 # → RespInteger (delivery count)
│   │       └── Exit.java                    # → SimpleString("BYE")
│   ├── config/
│   │   └── Config.java                      # Singleton; reads config.properties
│   ├── db/
│   │   ├── DB.java                          # Storage interface
│   │   ├── DatabaseFactory.java             # Creates InMemoryDB or ShardedDB
│   │   └── implementation/
│   │       ├── InMemoryDB.java              # LinkedHashMap LRU + ScheduledExecutor TTL sweep
│   │       └── ShardedDB.java               # N InMemoryDB shards; key → abs(hashCode) % N
│   ├── pubsub/
│   │   └── PubSubManager.java               # ConcurrentHashMap<topic, Set<OutputStream>>; dead-subscriber cleanup
│   ├── resp/
│   │   ├── RespParser.java                  # Stateful; handles RESP binary framing + inline plain-text
│   │   ├── RespEncoder.java                 # Stateless; encode() (no flush) + write() (encode + flush)
│   │   ├── RespException.java               # Protocol / security violation
│   │   └── value/
│   │       ├── RespValue.java               # Sealed interface
│   │       ├── SimpleString.java            # Record; rejects CR/LF in value
│   │       ├── RespError.java               # Record; factory methods err() / wrongArgs()
│   │       ├── RespInteger.java             # Record
│   │       ├── BulkString.java              # Record; overrides equals/hashCode for byte[]
│   │       └── RespArray.java               # Record; nil sentinel
│   ├── security/
│   │   └── ConnectionLimits.java            # Protocol security constants
│   ├── threads/
│   │   └── ThreadPoolManager.java           # Singleton; cached thread pool with graceful shutdown
│   └── util/
│       ├── ClusterUtil.java
│       └── Util.java
└── src/main/resources/
    └── config.properties
```

---

## Getting Started

### 1. Clone

```sh
git clone https://github.com/UdayHE/Quicksilver.git
cd Quicksilver
```

### 2. Build

```sh
./gradlew build
```

### 3. Run

```sh
# Default port 6379
java -jar build/libs/Quicksilver-1.0-SNAPSHOT.jar

# Custom port
java -jar build/libs/Quicksilver-1.0-SNAPSHOT.jar 7000
```

### 4. Connect

**redis-cli** (recommended — full RESP protocol):
```sh
redis-cli -p 6379
```

**telnet / nc** (plain-text inline mode — backward compat):
```sh
telnet localhost 6379
```

---

## Commands

| Command | Description | Response |
|---|---|---|
| `SET key value` | Store a value | `+OK` |
| `GET key` | Retrieve a value | Bulk string or nil |
| `DEL key` | Delete a key | `:1` (deleted) or `:0` (not found) |
| `FLUSH` | Clear all data (cluster-wide) | `+OK` |
| `DUMP` | Serialize entire store to a string | Bulk string |
| `SUBSCRIBE topic` | Subscribe to a topic | Array `[subscribe, topic, count]` |
| `UNSUBSCRIBE topic` | Unsubscribe from a topic | Array `[unsubscribe, topic, count]` |
| `PUBLISH topic message` | Publish a message to a topic | `:N` (delivery count) |
| `EXIT` | Close the connection | `+BYE` |

### Examples (redis-cli)

```
127.0.0.1:6379> SET user:1001 "Alice"
OK
127.0.0.1:6379> GET user:1001
"Alice"
127.0.0.1:6379> DEL user:1001
(integer) 1
127.0.0.1:6379> GET user:1001
(nil)

# Pub/Sub (open two redis-cli sessions)
# Session A:
127.0.0.1:6379> SUBSCRIBE news
# Session B:
127.0.0.1:6379> PUBLISH news "Breaking: QuickSilver now speaks RESP"
(integer) 1
```

---

## Configuration

Edit `src/main/resources/config.properties`:

```properties
server.port=6379
db.type=SHARDED       # SHARDED or IN_MEMORY
db.shard.total=4
db.shard.size=100
```

---

## Security Limits

Enforced at the protocol layer in [`ConnectionLimits`](src/main/java/io/github/udayhe/quicksilver/security/ConnectionLimits.java):

| Limit | Value |
|---|---|
| Max inline command bytes | 64 KB |
| Max bulk-string size | 512 MB |
| Max array elements | 1,048,576 |
| Socket idle timeout | 5 minutes |
| Max key size | 512 KB |

---

## Cluster Setup

### Start multiple nodes

```sh
java -jar build/libs/Quicksilver-1.0-SNAPSHOT.jar 6379
java -jar build/libs/Quicksilver-1.0-SNAPSHOT.jar 6380
java -jar build/libs/Quicksilver-1.0-SNAPSHOT.jar 6381
```

### How it works

- Nodes register in a **consistent hashing ring** on startup.
- Each node syncs data from peers via RESP `DUMP` on startup.
- `ClientHandler` computes the responsible node for every key. If the key belongs to another node, the request is forwarded transparently and the reply is relayed to the client.
- `FLUSH` is broadcast to all nodes.

```sh
# Connect to any node — routing is automatic
redis-cli -p 6379
SET order:9001 "shipped"    # stored on whichever node owns this key
GET order:9001              # routed to the same node automatically
```

---

## Sharding

Configure in `config.properties`:

```properties
db.type=SHARDED
db.shard.total=4
db.shard.size=100
```

Keys are routed to shards by `abs(key.hashCode()) % shardCount`. Each shard is an independent `InMemoryDB` with its own LRU cache and TTL sweep, enabling parallel in-process operations.

---

## Testing

```sh
./gradlew test

# Run only the RESP / command suite
./gradlew test \
  --tests "io.github.udayhe.quicksilver.command.CommandRegistryTest" \
  --tests "io.github.udayhe.quicksilver.integration.ServerIntegrationTest"
```

---

## Docker

```sh
# Single instance
docker build -t quicksilver .
docker run -p 6379:6379 quicksilver

# Cluster (3 nodes)
docker run -p 6379:6379 --name qs-1 quicksilver
docker run -p 6380:6380 --name qs-2 quicksilver
docker run -p 6381:6381 --name qs-3 quicksilver
```

---

## License

Apache License 2.0 — see [LICENSE](LICENSE)
