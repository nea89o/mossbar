package mossbar.extensions.org.freedesktop.wayland.client.Proxy;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import moe.nea.mossbar.concepts.Scope;
import moe.nea.mossbar.concepts.ScopeObjectProxy;
import org.freedesktop.wayland.client.Proxy;

@Extension
public class ProxyExtension {
    public static <I> ScopeObjectProxy asScopeObject(@This Proxy<I> thiz) {
        return new ScopeObjectProxy(thiz);
    }

    public static <I> @Self Proxy<I> bindTo(@Self @This Proxy<I> thiz, Scope scope) {
        asScopeObject(thiz).bindTo(scope);
        return thiz;
    }
}