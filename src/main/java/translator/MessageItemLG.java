package translator;

import common.AbstractLaneGroup;
import keys.KeyCommPathOrLink;
import models.fluid.FluidLaneGroup;

public class MessageItemLG extends AbstractMessageItem {

    public FluidLaneGroup lg;

    public MessageItemLG(AbstractLaneGroup lg, KeyCommPathOrLink key) {
        super(key);
        this.lg = (FluidLaneGroup) lg;
    }

    @Override
    public String toString() {
        return String.format("itemLG:\tlink=%d\tkey:\t{comm %d,ispath %s,pathorlink %d}",lg.link.getId(),key.commodity_id,key.isPath,key.pathOrlink_id);
    }

}
