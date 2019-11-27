package translator;

import keys.KeyCommPathOrLink;
import models.BaseLaneGroup;

public class MessageItemLG extends AbstractMessageItem {

    public models.ctm.LaneGroup lg;

    public MessageItemLG(BaseLaneGroup lg, KeyCommPathOrLink key) {
        super(key);
        this.lg = (models.ctm.LaneGroup ) lg;
    }

    @Override
    public String toString() {
        return String.format("itemLG:\tlink=%d\tkey:\t{comm %d,ispath %s,pathorlink %d}",lg.link.getId(),key.commodity_id,key.isPath,key.pathOrlink_id);
    }

}
