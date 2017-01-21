package org.openhab.binding.mysensors.adapter;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;

public class MySensorsStringTypeAdapter implements MySensorsTypeAdapter {

    @Override
    public State fromString(String s) {
        return new StringType(s);
    }

}
