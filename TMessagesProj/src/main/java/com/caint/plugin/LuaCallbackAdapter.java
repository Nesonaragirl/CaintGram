package com.caint.plugin;

import android.util.Log;

import com.caint.api.CaintCallback;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

/**
 * Adapts a Lua function value into a Java CaintCallback so plugin scripts
 * can be passed anywhere the real Caint API expects a callback
 * (Events.on, component:onClick, Dialogs.confirm, ...).
 *
 * A plugin's function throwing is caught and logged here rather than
 * propagating -- one misbehaving plugin callback should never be able to
 * break the Caint code that invoked it.
 */
final class LuaCallbackAdapter implements CaintCallback {

    private static final String TAG = "Caint.Plugin";

    private final LuaValue function;

    LuaCallbackAdapter(LuaValue function) {
        this.function = function;
    }

    @Override
    public void run(Object data) {
        try {
            LuaValue arg = data == null ? LuaValue.NIL : CoerceJavaToLua.coerce(data);
            function.call(arg);
        } catch (LuaError e) {
            Log.e(TAG, "A plugin callback threw an error", e);
        } catch (Exception e) {
            Log.e(TAG, "A plugin callback failed unexpectedly", e);
        }
    }
}
