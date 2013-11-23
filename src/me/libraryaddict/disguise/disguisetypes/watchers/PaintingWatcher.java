package me.libraryaddict.disguise.disguisetypes.watchers;

import org.bukkit.Art;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;

public class PaintingWatcher extends FlagWatcher {
    private Art painting;

    public PaintingWatcher(Disguise disguise) {
        super(disguise);
    }

    public Art getPainting() {
        return painting;
    }

    @Override
    public PaintingWatcher clone(Disguise disguise) {
        PaintingWatcher watcher = (PaintingWatcher) super.clone(disguise);
        watcher.setPainting(getPainting());
        return watcher;
    }

    public void setPainting(Art newPainting) {
        this.painting = newPainting;
        if (getDisguise().getEntity() != null && getDisguise().getWatcher() == this) {
            DisguiseUtilities.refreshTrackers(getDisguise().getEntity());
        }
    }

}
