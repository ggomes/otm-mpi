package translator;

import keys.KeyCommPathOrLink;

public abstract class AbstractMessageItem {
    public final KeyCommPathOrLink key;

    public AbstractMessageItem(KeyCommPathOrLink key) {
        this.key = key;
    }

}
