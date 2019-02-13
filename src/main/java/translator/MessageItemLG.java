package translator;

import keys.KeyCommPathOrLink;
import models.AbstractLaneGroup;

public class MessageItemLG extends AbstractMessageItem {

    public models.ctm.LaneGroup lg;

    public MessageItemLG(AbstractLaneGroup lg, KeyCommPathOrLink key) {
        super(key);
        this.lg = (models.ctm.LaneGroup ) lg;
    }

}
