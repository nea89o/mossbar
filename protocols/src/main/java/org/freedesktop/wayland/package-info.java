
@WaylandCustomProtocols({
        @WaylandCustomProtocol(
                path = "core/wayland.xml",
                generateServer = false
        ),
        @WaylandCustomProtocol(
                path = "wlr/unstable/wlr-layer-shell-unstable-v1.xml",
                generateServer = false
        )
})
@WaylandProtocols(
        pkgConfig = "",
        path = "extra",
        generateServer = false,
        withUnstable = false
)
package org.freedesktop.wayland;

import org.freedesktop.wayland.generator.api.WaylandCustomProtocol;
import org.freedesktop.wayland.generator.api.WaylandCustomProtocols;
import org.freedesktop.wayland.generator.api.WaylandProtocols;