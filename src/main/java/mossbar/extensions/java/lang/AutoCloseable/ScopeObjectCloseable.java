package mossbar.extensions.java.lang.AutoCloseable;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import moe.nea.mossbar.concepts.Scope;
import moe.nea.mossbar.concepts.ScopeObject;

@Extension
public class ScopeObjectCloseable {
    public static ScopeObject asScopeObject(@This AutoCloseable closeable) {
        return () -> {
            try {
                closeable.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static @Self AutoCloseable bindTo(@Self @This AutoCloseable thiz, Scope scope) {
        thiz.asScopeObject().bindTo(scope);
        return thiz;
    }
}