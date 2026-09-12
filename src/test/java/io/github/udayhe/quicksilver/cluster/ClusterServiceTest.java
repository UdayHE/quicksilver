package io.github.udayhe.quicksilver.cluster;

import io.github.udayhe.quicksilver.config.Config;
import io.github.udayhe.quicksilver.db.DB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ClusterService
 */
@ExtendWith(MockitoExtension.class)
public class ClusterServiceTest {

    private ClusterService<String> clusterService;
    private ClusterNode localNode;
    
    @Mock
    private DB<String, Object> mockDB;
    
    @Mock
    private Config mockConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clusterService = new ClusterService<>();
        localNode = new ClusterNode("localhost", 6379);
        
        // Register local node
        clusterService.registerInCluster(6379);
    }

    @Test
    void testRegisterInCluster() {
        ClusterManager clusterManager = clusterService.getClusterManager();
        ConsistentHashing<String> consistentHashing = clusterService.getConsistentHashing();
        
        assertEquals(1, clusterManager.getNodes().size());
        assertEquals(1, consistentHashing.getRingSize());
        
        assertTrue(clusterManager.getNodes().contains(localNode));
    }

    @Test
    void testAddMultipleNodes() {
        ClusterNode node2 = new ClusterNode("localhost", 6380);
        ClusterNode node3 = new ClusterNode("localhost", 6381);
        
        clusterService.getClusterManager().addNode(node2);
        clusterService.getConsistentHashing().addNode(node2);
        
        clusterService.getClusterManager().addNode(node3);
        clusterService.getConsistentHashing().addNode(node3);
        
        assertEquals(3, clusterService.getClusterManager().getNodes().size());
        assertEquals(3, clusterService.getConsistentHashing().getRingSize());
    }

    @Test
    void testGetResponsibleNode() {
        ClusterNode node2 = new ClusterNode("localhost", 6380);
        clusterService.getClusterManager().addNode(node2);
        clusterService.getConsistentHashing().addNode(node2);
        
        ClusterNode responsibleNode1 = clusterService.getResponsibleNode("key1");
        ClusterNode responsibleNode2 = clusterService.getResponsibleNode("key2");
        
        assertNotNull(responsibleNode1);
        assertNotNull(responsibleNode2);
        
        // Keys should be distributed across nodes
        assertTrue(clusterService.getClusterManager().getNodes().contains(responsibleNode1));
        assertTrue(clusterService.getClusterManager().getNodes().contains(responsibleNode2));
    }

    @Test
    void testGetResponsibleNodeWithEmptyCluster() {
        ClusterService<String> emptyClusterService = new ClusterService<>();
        
        ClusterNode responsibleNode = emptyClusterService.getResponsibleNode("key1");
        assertNull(responsibleNode);
    }

    @Test
    void testConsistentHashingDistribution() {
        // Add multiple nodes
        for (int i = 1; i <= 5; i++) {
            ClusterNode node = new ClusterNode("localhost", 6379 + i);
            clusterService.getClusterManager().addNode(node);
            clusterService.getConsistentHashing().addNode(node);
        }
        
        // Test that keys are distributed
        String[] testKeys = {"key1", "key2", "key3", "key4", "key5", "key6", "key7", "key8", "key9", "key10"};
        ClusterNode[] responsibleNodes = new ClusterNode[testKeys.length];
        
        for (int i = 0; i < testKeys.length; i++) {
            responsibleNodes[i] = clusterService.getResponsibleNode(testKeys[i]);
            assertNotNull(responsibleNodes[i]);
        }
        
        // Verify that not all keys go to the same node
        boolean allSameNode = true;
        for (int i = 1; i < responsibleNodes.length; i++) {
            if (!responsibleNodes[0].equals(responsibleNodes[i])) {
                allSameNode = false;
                break;
            }
        }
        
        assertFalse(allSameNode, "Keys should be distributed across different nodes");
    }

    @Test
    void testNodeRemoval() {
        ClusterNode node2 = new ClusterNode("localhost", 6380);
        clusterService.getClusterManager().addNode(node2);
        clusterService.getConsistentHashing().addNode(node2);
        
        assertEquals(2, clusterService.getClusterManager().getNodes().size());
        assertEquals(2, clusterService.getConsistentHashing().getRingSize());
        
        // Remove node
        clusterService.getClusterManager().removeNode(node2);
        clusterService.getConsistentHashing().removeNode(node2);
        
        assertEquals(1, clusterService.getClusterManager().getNodes().size());
        assertEquals(1, clusterService.getConsistentHashing().getRingSize());
        assertFalse(clusterService.getClusterManager().getNodes().contains(node2));
    }

    @Test
    void testClusterManagerAccess() {
        ClusterManager clusterManager = clusterService.getClusterManager();
        assertNotNull(clusterManager);
        
        // Test adding and removing nodes through ClusterManager
        ClusterNode node = new ClusterNode("localhost", 6380);
        clusterManager.addNode(node);
        
        assertEquals(2, clusterManager.getNodes().size());
        assertTrue(clusterManager.getNodes().contains(node));
        
        clusterManager.removeNode(node);
        assertEquals(1, clusterManager.getNodes().size());
        assertFalse(clusterManager.getNodes().contains(node));
    }

    @Test
    void testConsistentHashingAccess() {
        ConsistentHashing<String> consistentHashing = clusterService.getConsistentHashing();
        assertNotNull(consistentHashing);
        
        // Test adding and removing nodes through ConsistentHashing
        ClusterNode node = new ClusterNode("localhost", 6380);
        consistentHashing.addNode(node);
        
        assertEquals(2, consistentHashing.getRingSize());
        
        consistentHashing.removeNode(node);
        assertEquals(1, consistentHashing.getRingSize());
    }

    @Test
    void testNodeEquality() {
        ClusterNode node1 = new ClusterNode("localhost", 6379);
        ClusterNode node2 = new ClusterNode("localhost", 6379);
        ClusterNode node3 = new ClusterNode("localhost", 6380);
        
        assertEquals(node1, node2);
        assertNotEquals(node1, node3);
        assertEquals(node1.hashCode(), node2.hashCode());
    }

    @Test
    void testNodeStringRepresentation() {
        String nodeString = localNode.toString();
        assertTrue(nodeString.contains("localhost"));
        assertTrue(nodeString.contains("6379"));
    }
}
