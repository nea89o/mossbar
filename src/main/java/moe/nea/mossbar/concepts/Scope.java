package moe.nea.mossbar.concepts;

import java.util.ArrayList;
import java.util.List;

public abstract class Scope implements ScopeObject {
    List<ScopeObject> scopedObjects = new ArrayList<>();

    public static Scope root() {
        return new Scope() {
        };
    }

    protected <T extends ScopeObject> T bind(T scopeObject) {
        scopedObjects.add(scopeObject);
        return scopeObject;
    }

    @Override
    public void closeObject() {
        for (int i = scopedObjects.size() - 1; i >= 0; i--) {
            ScopeObject scopeObject = scopedObjects.get(i);
            scopeObject.closeObject();
        }
    }

}
