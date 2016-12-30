/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.event;

public enum MySensorsEventType {
    INCOMING_MESSAGE,
    NODE_ID_RESERVATION,
    NEW_NODE_DISCOVERED,
    NODE_STATUS_UPDATE,
    CHILD_VALUE_CHANGED,
    CHILD_LAST_UPDATE_CHANGED,
    BRIDGE_STATUS_UPDATE
}