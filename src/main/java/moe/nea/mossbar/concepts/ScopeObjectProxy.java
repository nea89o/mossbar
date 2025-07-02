package moe.nea.mossbar.concepts;

import org.freedesktop.wayland.client.Proxy;

public class ScopeObjectProxy implements ScopeObject {
    private final Proxy<?> proxy;

    public ScopeObjectProxy(Proxy<?> proxy) {
        this.proxy = proxy;
    }

    @Override
    public void closeObject() {
        proxy.destroy();
    }
}
