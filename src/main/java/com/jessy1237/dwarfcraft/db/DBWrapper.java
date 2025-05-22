package com.jessy1237.dwarfcraft.data;

import java.util.UUID;

import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.jessy1237.dwarfcraft.models.DwarfSkill;

public interface DBWrapper
{

    void dbInitialize();

    void dbFinalize();

    void createDwarfData(DwarfPlayer dCPlayer);

    boolean checkDwarfData(DwarfPlayer player);

    /**
     * Used for creating and populating a dwarf with a null(off line) player
     *
     * @param player
     * @param uuid
     */
    boolean checkDwarfData( DwarfPlayer player, UUID uuid );

    boolean saveDwarfData(DwarfPlayer dwarfPlayer, DwarfSkill[] skills);

}
