package translator;

import core.AbstractLaneGroup;
import core.State;
import models.fluid.FluidLaneGroup;

public class MessageItemLG extends AbstractMessageItem {

    public FluidLaneGroup lg;

    public MessageItemLG(AbstractLaneGroup lg, State key) {
        super(key);
        this.lg = (FluidLaneGroup) lg;
    }

    @Override
    public String toString() {
        return String.format("itemLG:\tlink=%d\tkey:\t{comm %d,ispath %s,pathorlink %d}",lg.get_link().getId(),key.commodity_id,key.isPath,key.pathOrlink_id);
    }

}
