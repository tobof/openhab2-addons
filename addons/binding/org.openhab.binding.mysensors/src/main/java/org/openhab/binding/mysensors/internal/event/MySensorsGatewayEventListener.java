package org.openhab.binding.mysensors.internal.event;

import java.util.EventListener;

import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

/**
 * Class that implement (and register) this interface receive update events from the MySensors network.
 * Default (Java8) was added to allow the class that will implement this interface, to choose
 * only the method in which is interested.
 *
 * @author Tim Oberföll
 * @author Andrea Cioni
 *
 */
public interface MySensorsGatewayEventListener extends EventListener {

    /**
     * Triggered when gateway reserve (and send) an ID for a new network device.
     * A new ,empty, device is created before this method is triggered
     */
    default public void nodeIdReservationDone(Integer reservedId) throws Throwable {
    }

    /**
     * Triggered when new node ID is discovered in the network
     * A new ,empty, device is created before this method is triggered
     */
    default public void newNodeDiscovered(MySensorsNode node) throws Throwable {
    }

    /**
     * When a message of type SET has processed correctly (node/child/variable found in gateway)
     * the new value is sent to every observer. The @isRevert parameter is set to true to indicate that
     * channel update was triggered by the
     */
    default public void channelUpdateEvent(MySensorsNode node, MySensorsChild child, MySensorsVariable var,
            boolean isRevert) throws Throwable {
    }

    /**
     * When a node is not more reachable this method is triggered.
     * Reachability changes when connection go down or (TODO) NetworkSanityChecker tells us the
     * device is not responding
     */
    default public void nodeReachStatusChanged(MySensorsNode node, boolean reach) throws Throwable {
    }

    /**
     * Procedure to notify new message from MySensorsNetwork.
     */
    default public void messageReceived(MySensorsMessage message) throws Throwable {
    }

    /**
     * Triggered when connection update its status
     */
    default public void connectionStatusUpdate(MySensorsAbstractConnection connection, boolean connected)
            throws Throwable {

    }

    default public void ackNotReceived(MySensorsMessage msg) throws Throwable {

    }
}
