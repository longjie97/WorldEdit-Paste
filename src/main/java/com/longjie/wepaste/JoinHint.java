package com.longjie.wepaste;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 进游戏时在聊天框打一张用法卡。
 *
 * 只在每次启动游戏后的第一次进入世界时出现（同一次启动内换存档不再重复），
 * 蓝色文字可以点击，直接填进聊天框，省得手打指令。
 */
public final class JoinHint {

    /** 同一次游戏启动内只提示一次 */
    private static boolean shown = false;

    private JoinHint() {
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (shown) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        shown = true;

        player.sendSystemMessage(header());
        player.sendSystemMessage(line(" 1. 先加载建筑：").append(cmd("//schem load 文件名", "//schem load "))
                .append(hint("（或 //copy 复制选区）")));
        player.sendSystemMessage(line(" 2. 站到位置、面朝想要的方向，输入 ").append(cmd("/wp", "/wp"))
                .append(hint(" 或者 ")).append(cmd("/wepaste", "/wepaste")));
        player.sendSystemMessage(line(" 3. 想手动指定角度：").append(cmd("/wp 90", "/wp 90")));
    }

    private static MutableComponent header() {
        return Component.literal(" WorldEdit->Paste ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("用法  点击蓝色文字可自动填入聊天框").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static MutableComponent line(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GRAY);
    }

    /** 灰色小字注释 */
    private static MutableComponent hint(String text) {
        return Component.literal(text).withStyle(ChatFormatting.DARK_GRAY);
    }

    /** 蓝色可点击的指令，悬停显示说明 */
    private static MutableComponent cmd(String shown, String suggest) {
        return Component.literal(shown)
                .withStyle(style -> style
                        .withColor(ChatFormatting.AQUA)
                        .withClickEvent(new ClickEvent.SuggestCommand(suggest))
                        .withHoverEvent(new HoverEvent.ShowText(
                                Component.literal("点击填入：" + suggest))));
    }
}
