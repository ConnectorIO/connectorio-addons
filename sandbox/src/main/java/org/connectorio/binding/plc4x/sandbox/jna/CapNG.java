package org.connectorio.binding.plc4x.sandbox.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public interface CapNG extends Library {

    CapNG INSTANCE = (CapNG) Native.loadLibrary("cap-ng", CapNG.class);

    int capng_get_caps_process();
}