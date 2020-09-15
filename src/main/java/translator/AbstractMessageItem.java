package translator;

import keys.State;

public abstract class AbstractMessageItem {
    public final State key;

    public AbstractMessageItem(State key) {
        this.key = key;
    }

}
