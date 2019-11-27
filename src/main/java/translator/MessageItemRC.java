package translator;

import keys.KeyCommPathOrLink;

public class MessageItemRC extends AbstractMessageItem {

    public Long rc_id;

    public MessageItemRC(Long rc_id,KeyCommPathOrLink key) {
        super(key);
        this.rc_id = rc_id;
    }

    @Override
    public String toString() {
        return String.format("itemRC:\trcid=%d\tkey:\t{comm %d,ispath %s,pathorlink %d}",rc_id,key.commodity_id,key.isPath,key.pathOrlink_id);
    }

}
