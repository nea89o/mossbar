package moe.nea.mossbar.concepts;

import manifold.ext.props.rt.api.PropOption;
import manifold.ext.props.rt.api.get;
import manifold.ext.props.rt.api.set;
import org.freedesktop.wayland.client.*;
import org.freedesktop.wayland.shared.WlSeatCapability;
import org.freedesktop.wayland.util.EnumUtil;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Display extends Scope {
    private final WlDisplayProxy proxy;
    private final WlRegistryProxy registry;
    private int shmFormat = 0;
    private WlCompositorProxy compositorProxy;
    private final List<WlOutputProxy> outputs = new ArrayList<>();

    public List<WlOutputProxy> getOutputs() {
        return outputs;
    }

    public WlCompositorProxy getCompositorProxy() {
        return compositorProxy;
    }

    @get
    @set(PropOption.Private)
    WlShmProxy shmProxy;
    @get
    @set(PropOption.Private)
    WlSeatProxy seatProxy;
    @get
    @set(PropOption.Private)
    XdgWmBaseProxy xdgWmBaseProxy;
    @get
    @set(PropOption.Private)
    ZwlrLayerShellV1Proxy layerShell;

    public Display() {
        proxy = WlDisplayProxy.connect(Objects.requireNonNull(System.getenv("WAYLAND_DISPLAY"), "No WAYLAND_DISPLAY found"));
        registry = proxy.getRegistry(new WlRegistryEvents() {
            @Override
            public void global(WlRegistryProxy emitter, int name, @NonNull String interfaceName, int version) {
                System.out.println("Receiving interface " + interfaceName + "." + version);
                registerInterface(name, interfaceName);
            }

            @Override
            public void globalRemove(WlRegistryProxy emitter, int name) {
                System.out.println("Removed " + name);
            }
        });

        proxy.roundtrip();
        proxy.roundtrip();

        Objects.requireNonNull(layerShell);
        Objects.requireNonNull(compositorProxy);
        Objects.requireNonNull(xdgWmBaseProxy);
        Objects.requireNonNull(seatProxy);
        if (outputs.isEmpty) // TODO: dynamically find more outputs
            throw new IllegalStateException("No outputs found");
    }


    private <J, T extends Proxy<J>> T bind(int name, Class<T> proxyType, int version, J implementation) {
        var proxy = registry
                .bind(name, proxyType, version, implementation);
        proxy.bindTo(this);
        return proxy;
    }

    private void registerInterface(int name, String interfaceName) {
        switch (interfaceName) {
            case WlCompositorProxy.INTERFACE_NAME ->
                    Display.this.compositorProxy = bind(name, WlCompositorProxy.class, WlCompositorEventsV6.VERSION, new WlCompositorEventsV6() {
                    });
            case WlShmProxy.INTERFACE_NAME ->
                    Display.this.shmProxy = bind(name, WlShmProxy.class, WlShmEvents.VERSION, new WlShmEvents() {
                        @Override
                        public void format(WlShmProxy emitter, int format) {
                            Display.this.shmFormat |= (1 << format);
                        }
                    });
            case XdgWmBaseProxy.INTERFACE_NAME ->
                    Display.this.xdgWmBaseProxy = bind(name, XdgWmBaseProxy.class, XdgWmBaseEventsV5.VERSION, new XdgWmBaseEventsV5() {
                        @Override
                        public void ping(XdgWmBaseProxy emitter, int serial) {
                            emitter.pong(serial);
                        }
                    });
            case ZwlrLayerShellV1Proxy.INTERFACE_NAME ->
                    Display.this.layerShell = bind(name, ZwlrLayerShellV1Proxy.class, ZwlrLayerShellV1EventsV4.VERSION, new ZwlrLayerShellV1EventsV4() {
                    });
            case WlOutputProxy.INTERFACE_NAME ->
                    outputs.add(bind(name, WlOutputProxy.class, WlOutputEvents.VERSION, new WlOutputEvents() {
                        @Override
                        public void geometry(WlOutputProxy emitter, int x, int y, int physicalWidth, int physicalHeight, int subpixel, String make, String model, int transform) {

                        }

                        @Override
                        public void mode(WlOutputProxy emitter, int flags, int width, int height, int refresh) {

                        }
                    }));
            case WlSeatProxy.INTERFACE_NAME ->
                    Display.this.seatProxy = bind(name, WlSeatProxy.class, WlSeatEventsV3.VERSION, new WlSeatEventsV3() {
                        @Override
                        public void capabilities(WlSeatProxy emitter, int capabilities) {
                            var caps = EnumUtil.decode(WlSeatCapability.class, capabilities);
                            System.out.println("capabilities: " + caps);
                        }

                        @Override
                        public void name(WlSeatProxy emitter, @NonNull String name) {
                            System.out.println("Obtained seat " + name);
                        }
                    });
        }
    }

    public WlDisplayProxy getProxy() {
        return proxy;
    }

    @Override
    public void closeObject() {
        super.closeObject();
        System.out.println("Destroying registry");
        registry.destroy();
        System.out.println("Destroyed registry");
        proxy.flush();
        System.out.println("Flushed proxy");
        proxy.disconnect();
        System.out.println("Destroyed proxy");
    }

    public void roundtrip() {
        proxy.roundtrip();
    }
}
