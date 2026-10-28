package com.longjie.wepaste;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * WorldEdit->Paste
 * WorldEdit 的附属扩展：粘贴时按玩家当前朝向自动旋转建筑。
 */
@Mod(WePaste.MODID)
public class WePaste {
    public static final String MODID = "worldeditpaste";

    public WePaste(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(WePaste::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(JoinHint::onLogin);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        PasteFacedCommand.register(event.getDispatcher());
    }
}
