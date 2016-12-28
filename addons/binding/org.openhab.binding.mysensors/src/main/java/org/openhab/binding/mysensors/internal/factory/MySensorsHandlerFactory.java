/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.factory;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.mysensors.discovery.MySensorsDiscoveryService;
import org.openhab.binding.mysensors.internal.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.internal.handler.MySensorsThingHandler;
import org.openhab.binding.mysensors.internal.sensors.MySensorsDeviceManager;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

/**
 * The {@link MySensorsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tim Oberföll
 */
public class MySensorsHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(getClass());

    // Discovery services
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    // Device manager
    private MySensorsDeviceManager deviceManager;

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        logger.debug("Initializing MySensorsHandlerFactory");
        Map<Integer, MySensorsNode> nodes = loadCacheFile();
        deviceManager = new MySensorsDeviceManager(nodes);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        logger.trace("Looking for support of: {}", thingTypeUID);
        return SUPPORTED_DEVICE_TYPES_UIDS.contains(thingTypeUID);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory#createHandler(org.eclipse.smarthome.core.thing.
     * Thing)
     */
    @Override
    protected ThingHandler createHandler(Thing thing) {
        logger.trace("Creating handler for thing: {}", thing.getUID());
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        ThingHandler handler = null;

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            handler = new MySensorsThingHandler(deviceManager, thing);
            addIntoDeviceManager(thing);
        } else if (thingTypeUID.equals(THING_TYPE_BRIDGE_SER) || thingTypeUID.equals(THING_TYPE_BRIDGE_ETH)) {
            handler = new MySensorsBridgeHandler(deviceManager, (Bridge) thing);
            registerDeviceDiscoveryService((MySensorsBridgeHandler) handler);
        } else {
            logger.error("Thing {} cannot be configured, is this thing supported by the binding?", thingTypeUID);
        }

        return handler;
    }

    private void addIntoDeviceManager(Thing thing) {
        try {
            deviceManager.addNode((MySensorsSensorsFactory.buildNodeFromThing(thing)), true);
        } catch (Throwable e) {
            logger.error("Build node throw and exception({}), message: {}", e.getClass(), e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory#createThing(org.eclipse.smarthome.core.thing.
     * ThingTypeUID, org.eclipse.smarthome.config.core.Configuration, org.eclipse.smarthome.core.thing.ThingUID)
     */
    @Override
    protected Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID) {
        logger.trace("Create thing: {}", thingUID);
        return super.createThing(thingTypeUID, configuration, thingUID);
    }

    private void registerDeviceDiscoveryService(MySensorsBridgeHandler mySensorsBridgeHandler) {
        MySensorsDiscoveryService discoveryService = new MySensorsDiscoveryService(mySensorsBridgeHandler);
        this.discoveryServiceRegs.put(mySensorsBridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    public void removeThing(ThingUID thingUID) {
        logger.trace("Removing thing: {}", thingUID);
        super.removeThing(thingUID);
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        logger.trace("Removing handler: {}", thingHandler);
        if (thingHandler instanceof MySensorsBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

    private Map<Integer, MySensorsNode> loadCacheFile() {
        MySensorsCacheFactory cacheFactory = MySensorsCacheFactory.getCacheFactory();
        Map<Integer, MySensorsNode> nodes = new HashMap<Integer, MySensorsNode>();

        List<Integer> givenIds = cacheFactory.readCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE,
                new ArrayList<Integer>(), new TypeToken<ArrayList<Integer>>() {
                }.getType());

        for (Integer i : givenIds) {
            if (i != null) {
                nodes.put(i, new MySensorsNode(i));
            }
        }

        return nodes;
    }
}
