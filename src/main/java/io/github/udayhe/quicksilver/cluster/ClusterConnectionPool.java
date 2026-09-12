package io.github.udayhe.quicksilver.cluster;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton that owns one {@link GenericObjectPool} per {@link ClusterNode}.
 * Pool configuration per node:
 * <ul>
 *   <li>maxTotal  8  — max live connections to a single node</li>
 *   <li>maxIdle   4  — idle connections kept warm</li>
 *   <li>minIdle   1  — at least one connection pre-warmed</li>
 *   <li>maxWait   5 s — borrow blocks at most this long before throwing</li>
 *   <li>testOnBorrow   true — validate socket before handing it out</li>
 *   <li>testWhileIdle  true — eviction thread removes stale sockets</li>
 *   <li>eviction period 30 s</li>
 * </ul>
 *
 * Call {@link #close()} during server shutdown to drain all pools.
 */
public final class ClusterConnectionPool implements AutoCloseable {

    private static final Logger log = Logger.getLogger(ClusterConnectionPool.class.getName());

    private static final int  MAX_TOTAL   = 8;
    private static final int  MAX_IDLE    = 4;
    private static final int  MIN_IDLE    = 1;
    private static final long MAX_WAIT_MS = 5_000;

    private static final ClusterConnectionPool INSTANCE = new ClusterConnectionPool();

    private final ConcurrentHashMap<ClusterNode, GenericObjectPool<ClusterConnection>> pools =
            new ConcurrentHashMap<>();

    private ClusterConnectionPool() {}

    public static ClusterConnectionPool getInstance() {
        return INSTANCE;
    }

    /**
     * Borrows a connection for {@code node}, blocking up to {@code maxWait} ms.
     *
     * @throws Exception if the pool is exhausted or a new connection cannot be created
     */
    public ClusterConnection borrow(ClusterNode node) throws Exception {
        return poolFor(node).borrowObject();
    }

    /** Returns a healthy connection back to the pool. */
    public void release(ClusterNode node, ClusterConnection conn) {
        GenericObjectPool<ClusterConnection> pool = pools.get(node);
        if (pool != null) pool.returnObject(conn);
    }

    /** Destroys a broken connection so the pool can replace it. */
    public void invalidate(ClusterNode node, ClusterConnection conn) {
        GenericObjectPool<ClusterConnection> pool = pools.get(node);
        if (pool == null) return;
        try {
            pool.invalidateObject(conn);
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to invalidate connection for node {0}: {1}",
                    new Object[]{node, e.getMessage()});
        }
    }

    /** Closes all pools and their underlying sockets. Call once during shutdown. */
    @Override
    public void close() {
        pools.values().forEach(GenericObjectPool::close);
        pools.clear();
    }

    private GenericObjectPool<ClusterConnection> poolFor(ClusterNode node) {
        return pools.computeIfAbsent(node,
                n -> new GenericObjectPool<>(new ClusterConnectionFactory(n), poolConfig()));
    }

    private static GenericObjectPoolConfig<ClusterConnection> poolConfig() {
        GenericObjectPoolConfig<ClusterConnection> cfg = new GenericObjectPoolConfig<>();
        cfg.setMaxTotal(MAX_TOTAL);
        cfg.setMaxIdle(MAX_IDLE);
        cfg.setMinIdle(MIN_IDLE);
        cfg.setMaxWait(Duration.ofMillis(MAX_WAIT_MS));
        cfg.setTestOnBorrow(true);
        cfg.setTestWhileIdle(true);
        cfg.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
        cfg.setBlockWhenExhausted(true);
        return cfg;
    }
}
