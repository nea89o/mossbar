package moe.nea.mossbar.concepts;

import org.freedesktop.wayland.client.WlOutputEvents;
import org.freedesktop.wayland.client.WlOutputProxy;
import org.freedesktop.wayland.shared.WlOutputMode;
import org.freedesktop.wayland.util.EnumUtil;

import java.util.EnumSet;

public class Output extends Scope implements WlOutputEvents {
    private final WlOutputProxy proxy;
    int width = -1, height = -1;
    EnumSet<WlOutputMode> flags;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public EnumSet<WlOutputMode> getFlags() {
        return flags;
    }

    public Output(Display display, int name) {
        proxy = display.registry.bind(name, WlOutputProxy.class, WlOutputEvents.VERSION, this).bindTo(this);
    }

    @Override
    public void geometry(WlOutputProxy emitter, int x, int y, int physicalWidth, int physicalHeight, int subpixel, String make, String model, int transform) {
        System.out.println("${make} ${model} is at $x,$y ${physicalWidth}x${physicalHeight}");
    }

    @Override
    public void mode(WlOutputProxy emitter, int flags, int width, int height, int refresh) {
        var encodedFlags = EnumUtil.decode(WlOutputMode.class, flags);
        if (encodedFlags.contains(WlOutputMode.CURRENT)) {
            this.width = width;
            this.height = height;
            this.flags = encodedFlags;
        }
        System.out.println("mode ${width}x${height} ${this.flags}");
    }

    public WlOutputProxy getProxy() {
        return proxy;
    }
}
