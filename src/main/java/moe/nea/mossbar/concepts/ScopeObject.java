package moe.nea.mossbar.concepts;

import manifold.ext.rt.api.Self;

public interface ScopeObject {

    default @Self ScopeObject bindTo(Scope scope) {
        return scope.bind(this);
    }

    void closeObject();
}
