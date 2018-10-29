package translator;

import keys.KeyCommPathOrLink;

public class MessageItemRC extends AbstractMessageItem {

    public Long rc_id;

    public MessageItemRC(Long rc_id,KeyCommPathOrLink key) {
        super(key);
        this.rc_id = rc_id;
    }

}
