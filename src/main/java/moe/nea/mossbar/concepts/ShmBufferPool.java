package moe.nea.mossbar.concepts;

import manifold.ext.props.rt.api.get;
import org.freedesktop.wayland.client.WlBufferEvents;
import org.freedesktop.wayland.client.WlBufferProxy;
import org.freedesktop.wayland.client.WlShmPoolEvents;
import org.freedesktop.wayland.shared.WlShmFormat;
import org.freedesktop.wayland.util.ShmPool;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class ShmBufferPool extends Scope {
    private boolean destroyed = false;
    private Queue<Buffer> ready = new ArrayDeque<>();

    void enqueue(Buffer buffer) {
        requireNotDestroyed();
        ready.add(buffer);
    }

    public class Buffer extends Scope implements WlBufferEvents, WlShmPoolEvents {
        private final WlBufferProxy bufferProxy;
        private final ShmPool shmPool;
        @get
        final int width;
        @get
        final int height;
        @get
        final int byteWidth;
        @get
        final @NonNull WlShmFormat format;

        Buffer(Display display, int width, int height, int byteWidth, WlShmFormat format) throws IOException {
            this.width = width;
            this.height = height;
            this.byteWidth = byteWidth;
            this.format = format;
            int bufferSize = width * height * byteWidth;
            shmPool = new ShmPool(bufferSize).bindTo(this); // TODO: shouldnt this be a long upstream? maybe wayland is just 32 bit like that
            var poolProxy = display.shmProxy.createPool(this, shmPool.fd, bufferSize);
            bufferProxy = poolProxy.createBuffer(this, 0, width, height, width * byteWidth, format.value).bindTo(this);
            poolProxy.destroy();
        }

        public ByteBuffer getByteBuffer() {
            return shmPool.asByteBuffer();
        }

        @Override
        public void release(WlBufferProxy emitter) {
            enqueue(this);
        }

        public WlBufferProxy getProxy() {
            return bufferProxy;
        }
    }

    private void requireNotDestroyed() {
        if (destroyed)
            throw new IllegalStateException("pool destroyed");
    }

    public @Nullable Buffer poll() {
        requireNotDestroyed();
        return ready.poll();
    }

    @Override
    public void closeObject() {
        super.closeObject();
        destroyed = true;
    }

    public ShmBufferPool(Display display, int width, int height, int bufferCount, int byteWidth, WlShmFormat shmFormat) throws IOException {
        for (int i = 0; i < bufferCount; i++) {
            ready.add(new Buffer(display, width, height, byteWidth, shmFormat).bindTo(this));
        }
    }

    public static ShmBufferPool newARGBPool(Display display, int width, int height, int bufferCount) throws IOException {
        return new ShmBufferPool(display, width, height, bufferCount, 4, WlShmFormat.ARGB8888);
    }
}
