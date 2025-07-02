package moe.nea.mossbar.concepts;

import org.freedesktop.wayland.client.*;
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.freedesktop.wayland.shared.ZwlrLayerShellV1Layer;
import org.freedesktop.wayland.shared.ZwlrLayerSurfaceV1Anchor;
import org.freedesktop.wayland.util.EnumUtil;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.EnumSet;

public class Bar extends Scope {
    private final Display display;
    private final WlSurfaceProxy surface;
    private final ZwlrLayerSurfaceV1Proxy layer;
    private final ShmBufferPool bufferPool;
    private WlCallbackProxy callbackProxy;

    public Bar(Display display, Output output) {
        this.display = display;
        this.surface = display.compositorProxy.createSurface(new WlSurfaceEventsV6() {
            @Override
            public void enter(WlSurfaceProxy emitter, WlOutputProxy output) {
                System.out.println("Surface entered");
            }

            @Override
            public void leave(WlSurfaceProxy emitter, WlOutputProxy output) {
                System.out.println("Left surface");
            }

            @Override
            public void preferredBufferScale(WlSurfaceProxy emitter, int factor) {
                System.out.println("Preferred buffer scale ${factor}");
            }

            @Override
            public void preferredBufferTransform(WlSurfaceProxy emitter, int transform) {
                var transforms = EnumUtil.decode(WlOutputTransform.class, transform);
                System.out.println("Preferred buffer transform ${transforms}");
            }
        }).bindTo(this);
        this.layer = display.layerShell.getLayerSurface(new ZwlrLayerSurfaceV1EventsV4() {
                    @Override
                    public void configure(ZwlrLayerSurfaceV1Proxy emitter, int serial, int width, int height) {
                        System.out.println("Configure layer ${serial} ${width}x${height}");
                        emitter.ackConfigure(serial);
                    }

                    @Override
                    public void closed(ZwlrLayerSurfaceV1Proxy emitter) {
                        System.out.println("layer closed");
                    }
                }, this.surface, output.proxy, ZwlrLayerShellV1Layer.OVERLAY.getValue(), "mossbar")
                .bindTo(this);
        if (output.width <= 0)
            throw new IllegalStateException("Monitor with 0 sized output");
        int width = output.width;
        int height = 30;
        layer.setSize(width, height); // TODO??? this should really not be a hardcoded width
        layer.setAnchor(EnumUtil.encode(EnumSet.of(ZwlrLayerSurfaceV1Anchor.TOP, ZwlrLayerSurfaceV1Anchor.RIGHT, ZwlrLayerSurfaceV1Anchor.LEFT)));
        layer.setExclusiveZone(height);
        surface.commit();
        try {
            bufferPool = ShmBufferPool.newARGBPool(display, width, height, 2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        display.roundtrip();
    }

    public void renderOnce() {
        var nextBuffer = bufferPool.poll();
        assert nextBuffer != null;
        var pixels = nextBuffer.getByteBuffer().asIntBuffer();
        fill(pixels, 0xFFFF00A0);
        surface.attach(nextBuffer.getProxy(), 0, 0);
        surface.damage(0, 0, nextBuffer.width, nextBuffer.height);
        queueNextFrame();
        surface.commit();
    }

    private void queueNextFrame() {
        if (callbackProxy != null)
            callbackProxy.destroy();
        callbackProxy = surface.frame((_, _) -> renderOnce());
    }

    @Override
    public void closeObject() {
        callbackProxy.destroy();
        super.closeObject();
    }

    private static void fill(IntBuffer buffer, int value) {
        buffer.clear();
        buffer.limit(buffer.capacity());
        while (buffer.hasRemaining()) {
            buffer.put(value);
        }
    }
}
