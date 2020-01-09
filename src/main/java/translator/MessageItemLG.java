package translator;

import keys.KeyCommPathOrLink;
import models.AbstractLaneGroup;

public class MessageItemLG extends AbstractMessageItem {

    public models.fluid.LaneGroup lg;

    public MessageItemLG(AbstractLaneGroup lg, KeyCommPathOrLink key) {
        super(key);
        this.lg = (models.fluid.LaneGroup ) lg;
    }

    @Override
    public String toString() {
        return String.format("itemLG:\tlink=%d\tkey:\t{comm %d,ispath %s,pathorlink %d}",lg.link.getId(),key.commodity_id,key.isPath,key.pathOrlink_id);
    }

}
