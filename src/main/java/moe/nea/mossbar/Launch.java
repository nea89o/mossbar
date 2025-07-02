package moe.nea.mossbar;

import moe.nea.mossbar.concepts.Bar;
import moe.nea.mossbar.concepts.Display;
import moe.nea.mossbar.concepts.Scope;

public class Launch {
    public static void main(String[] args) {
        var rootScope = Scope.root();
        var display = new Display().bindTo(rootScope);
        for (var output : display.outputs) {
            var bar = new Bar(display, output).bindTo(rootScope);
            bar.renderOnce();
        }
        while (display.proxy.dispatch() >= 0) {
        }

        rootScope.closeObject();
    }
}
