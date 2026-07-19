package com.fluxgram.plugin;

import com.fluxgram.api.Flux;
import com.fluxgram.api.FluxCallback;
import com.fluxgram.api.FluxComponent;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds the `Flux` table handed to a plugin's Lua state.
 *
 * This is a deliberately curated subset of the real Flux Java API -- only
 * what's explicitly wired up here is reachable from plugin code. Nothing
 * about Telegram internals, or Java itself, leaks through: every function
 * below only ever takes/returns strings, numbers, booleans, Lua functions,
 * or a plain component table.
 *
 * Container support (Flux.UI.ChatHeader etc) and full permission checks
 * aren't wired in yet -- this is the loading mechanism, not the sandbox.
 */
final class FluxLuaBridge {

    // event name -> (the plugin's lua function -> the FluxCallback we
    // registered for it), so Events.off can find and remove the right one.
    private final Map<String, Map<LuaValue, FluxCallback>> eventCallbacks = new HashMap<>();

    LuaTable asLuaTable() {
        LuaTable root = new LuaTable();
        root.set("UI", buildUiTable());
        root.set("Events", buildEventsTable());
        root.set("Settings", buildSettingsTable());
        root.set("Dialogs", buildDialogsTable());
        return root;
    }

    private LuaTable buildUiTable() {
        LuaTable ui = new LuaTable();

        ui.set("createButton", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                String text = args.optjstring(1, "");
                return componentToLua(Flux.UI.createButton(text));
            }
        });

        ui.set("createMenuItem", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                String text = args.optjstring(1, "");
                int icon = args.optint(2, 0);
                return componentToLua(Flux.UI.createMenuItem(text, icon));
            }
        });

        ui.set("createDialog", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                String title = args.optjstring(1, null);
                String content = args.optjstring(2, null);
                return componentToLua(Flux.UI.createDialog(title, content));
            }
        });

        return ui;
    }

    private LuaTable buildEventsTable() {
        LuaTable events = new LuaTable();

        events.set("on", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                String event = args.checkjstring(1);
                LuaValue fn = args.checkfunction(2);
                FluxCallback callback = new LuaCallbackAdapter(fn);
                Map<LuaValue, FluxCallback> forEvent = eventCallbacks.get(event);
                if (forEvent == null) {
                    forEvent = new HashMap<>();
                    eventCallbacks.put(event, forEvent);
                }
                forEvent.put(fn, callback);
                Flux.Events.on(event, callback);
                return LuaValue.NIL;
            }
        });

        events.set("off", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                String event = args.checkjstring(1);
                LuaValue fn = args.checkfunction(2);
                Map<LuaValue, FluxCallback> forEvent = eventCallbacks.get(event);
                if (forEvent != null) {
                    FluxCallback callback = forEvent.remove(fn);
                    if (callback != null) {
                        Flux.Events.off(event, callback);
                    }
                }
                return LuaValue.NIL;
            }
        });

        events.set("emit", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                String event = args.checkjstring(1);
                LuaValue data = args.arg(2);
                Flux.Events.emit(event, data.isnil() ? null : data);
                return LuaValue.NIL;
            }
        });

        return events;
    }

    private LuaTable buildSettingsTable() {
        LuaTable settings = new LuaTable();

        settings.set("get", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                String key = args.checkjstring(1);
                Object value = Flux.Settings.get(key);
                return value == null ? LuaValue.NIL : LuaValue.valueOf(value.toString());
            }
        });

        settings.set("set", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                String key = args.checkjstring(1);
                LuaValue value = args.arg(2);
                Flux.Settings.set(key, value.isnil() ? null : value.tojstring());
                return LuaValue.NIL;
            }
        });

        return settings;
    }

    private LuaTable buildDialogsTable() {
        LuaTable dialogs = new LuaTable();

        dialogs.set("alert", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                String title = args.optjstring(1, null);
                String text = args.optjstring(2, null);
                Flux.Dialogs.alert(title, text);
                return LuaValue.NIL;
            }
        });

        dialogs.set("confirm", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                String title = args.optjstring(1, null);
                String text = args.optjstring(2, null);
                LuaValue fn = args.isfunction(3) ? args.checkfunction(3) : null;
                Flux.Dialogs.confirm(title, text, fn != null ? new LuaCallbackAdapter(fn) : null);
                return LuaValue.NIL;
            }
        });

        return dialogs;
    }

    /**
     * Wraps a FluxComponent as a Lua table exposing the same method names
     * as the Java FluxComponent interface, callable with colon syntax
     * (button:setText("hi"):setVisible(true)) the same way the requirements
     * originally described. Each method returns the table itself so calls
     * chain the same way the Java API chains.
     */
    private static LuaTable componentToLua(FluxComponent component) {
        LuaTable table = new LuaTable();

        table.set("setText", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                component.setText(args.optjstring(2, null));
                return table;
            }
        });
        table.set("setIcon", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                component.setIcon(args.optint(2, 0));
                return table;
            }
        });
        table.set("setVisible", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                component.setVisible(args.optboolean(2, true));
                return table;
            }
        });
        table.set("setEnabled", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                component.setEnabled(args.optboolean(2, true));
                return table;
            }
        });
        table.set("onClick", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                LuaValue fn = args.checkfunction(2);
                component.onClick(new LuaCallbackAdapter(fn));
                return table;
            }
        });
        table.set("attach", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                component.attach();
                return table;
            }
        });
        table.set("remove", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                component.remove();
                return LuaValue.NIL;
            }
        });

        return table;
    }
}
