/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MySensorsWriter implements Runnable {
    protected Logger logger = LoggerFactory.getLogger(MySensorsWriter.class);

    protected boolean stopWriting = false;
    protected long lastSend = System.currentTimeMillis();
    protected PrintWriter outs = null;
    protected OutputStream outStream = null;
    protected MySensorsBridgeConnection mysCon = null;

    protected ExecutorService executor = Executors.newSingleThreadExecutor();
    protected Future<?> future = null;

    protected int sendDelay = 1000;

    public void startWriter() {
        future = executor.submit(this);
    }

    @Override
    public void run() {
        Thread.currentThread().setName(MySensorsWriter.class.getName());
        while (!stopWriting) {
            if (!mysCon.isWriterPaused()) {
                try {
                    MySensorsMessage msg = mysCon.pollMySensorsOutboundQueue();

                    if (msg != null) {
                        if (msg.getNextSend() < System.currentTimeMillis()
                                && (lastSend + sendDelay) < System.currentTimeMillis()) {
                            // if we request an ACK we will wait for it and keep the message in the queue (at the end)
                            // otherwise we remove the message from the queue
                            if (msg.getAck() == 1) {
                                msg.setRetries(msg.getRetries() + 1);
                                if (!(msg.getRetries() > MySensorsBindingConstants.MYSENSORS_NUMBER_OF_RETRIES)) {
                                    msg.setNextSend(System.currentTimeMillis()
                                            + MySensorsBindingConstants.MYSENSORS_RETRY_TIMES[msg.getRetries() - 1]);
                                    mysCon.addMySensorsOutboundMessage(msg);
                                } else {
                                    logger.warn("NO ACK from nodeId: " + msg.getNodeId());
                                    if (msg.getOldMsg().isEmpty()) {
                                        logger.debug("No old status know to revert to!");
                                    } else if (msg.getRevert()) {
                                        logger.debug("Reverting status!");
                                        msg.setMsg(msg.getOldMsg());
                                        msg.setAck(0);
                                        mysCon.broadCastEvent(msg);
                                    } else if (!msg.getRevert()) {
                                        logger.debug("Not reverted due to configuration!");
                                    }
                                    continue;
                                }
                            }
                            String output = MySensorsMessageParser.generateAPIString(msg);
                            logger.debug("Sending to MySensors: {}", output.trim());

                            sendMessage(output);
                            lastSend = System.currentTimeMillis();
                        } else {
                            // Is not time for send again...
                            mysCon.addMySensorsOutboundMessage(msg);
                        }
                    } else {
                        logger.warn("Message returned from queue is null");
                    }

                } catch (Exception e) {
                    logger.error("({}) on writing from serial port, message: {}", e, getClass(), e.getMessage());
                }
            }
        }
    }

    protected void sendMessage(String output) {
        outs.println(output);
        outs.flush();
    }

    public void stopWriting() {

        logger.debug("Stopping Writer thread");

        this.stopWriting = true;

        if (future != null) {
            future.cancel(true);
            future = null;
        }

        if (executor != null) {
            executor.shutdown();
            executor.shutdownNow();
            executor = null;
        }

        try {
            if (outs != null) {
                outs.flush();
                outs.close();
                outs = null;
            }

            if (outStream != null) {
                outStream.close();
                outStream = null;
            }
        } catch (IOException e) {
            logger.error("Cannot close writer stream");
        }

    }
}
