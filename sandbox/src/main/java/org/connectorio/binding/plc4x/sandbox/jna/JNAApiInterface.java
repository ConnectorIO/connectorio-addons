package org.connectorio.binding.plc4x.sandbox.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public interface JNAApiInterface extends Library {

    JNAApiInterface INSTANCE = (JNAApiInterface) Native.loadLibrary((Platform.isWindows() ? "msvcrt" : "c"), JNAApiInterface.class);

    void printf(String format, Object... args);
    int sprintf(byte[] buffer, String format, Object... args);
    int scanf(String format, Object... args);
}