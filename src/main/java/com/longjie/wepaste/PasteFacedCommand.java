package com.longjie.wepaste;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.neoforge.NeoForgeAdapter;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * /wp —— 按玩家当前朝向旋转剪贴板后粘贴（WorldEdit->Paste 的核心功能）。
 *
 * 和 //paste 的区别：//paste 永远用剪贴板原始方向，
 * 这里先根据玩家 yaw 算出旋转角度，再交给 WorldEdit 的粘贴引擎。
 *
 * /wp            按朝向粘贴
 * /wp <角度>      强制指定旋转角度（调试用，0/90/180/270 或 -90 这类负值都行）
 */
public final class PasteFacedCommand {

    /** 主指令名 */
    private static final String MAIN = "wp";
    /** 备用长别名，防止 /wp 和其他 mod 撞名 */
    private static final String ALIAS = "wepaste";

    private PasteFacedCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(build(MAIN));
        dispatcher.register(build(ALIAS));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> build(String name) {
        return Commands.literal(name)
                .requires(src -> src.getEntity() instanceof ServerPlayer)
                .executes(PasteFacedCommand::runFaced)
                .then(Commands.argument("degrees", IntegerArgumentType.integer(-360, 360))
                        .executes(ctx -> run(
                                ctx,
                                IntegerArgumentType.getInteger(ctx, "degrees"),
                                "手动指定")));
    }

    /** 按玩家朝向自动算角度 */
    private static int runFaced(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player actor = adapt(ctx);
        // Minecraft yaw：0=南 90=西 180=北 270=东
        // WorldEdit 的 rotateY 与 yaw 转向相反，且整体差半圈，
        // 实测正确的旋转角 = 180 - yaw：南→180、西→90、北→0、东→270。
        float yaw = actor.getLocation().getYaw();
        int yawTurns = Math.round(yaw / 90.0f) & 3;
        int degrees = ((2 - yawTurns) & 3) * 90;
        return run(ctx, degrees, facingName(yawTurns));
    }

    private static int run(CommandContext<CommandSourceStack> ctx, int degrees, String label)
            throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer mcPlayer)) {
            source.sendFailure(Component.literal("[WE->Paste] 只有玩家能执行这个指令"));
            return 0;
        }

        Player actor = NeoForgeAdapter.get().fromNativePlayer(mcPlayer);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);

        ClipboardHolder holder;
        try {
            holder = session.getClipboard();
        } catch (Throwable t) {
            source.sendFailure(Component.literal("[WE->Paste] 剪贴板是空的，先用 //copy 或 //schem load 加载一份"));
            return 0;
        }

        // 覆盖式设置旋转，避免和上一次粘贴叠加
        holder.setTransform(new AffineTransform().rotateY(degrees));

        BlockVector3 to;
        try {
            to = session.getPlacementPosition(actor);
        } catch (Throwable t) {
            to = BlockVector3.at(
                    (int) Math.floor(mcPlayer.getX()),
                    (int) Math.floor(mcPlayer.getY()),
                    (int) Math.floor(mcPlayer.getZ()));
        }

        EditSession editSession = session.createEditSession(actor);
        try {
            Operation op = holder.createPaste(editSession)
                    .to(to)
                    .ignoreAirBlocks(false)
                    .build();
            Operations.completeLegacy(op);
        } catch (Throwable t) {
            source.sendFailure(Component.literal("[WE->Paste] 粘贴失败：" + t.getMessage()));
            return 0;
        } finally {
            session.remember(editSession);
            try {
                editSession.close();
            } catch (Throwable ignored) {
            }
        }

        source.sendSuccess(
                () -> Component.literal("[WE->Paste] 已粘贴，旋转 " + degrees + "°（" + label + "）"),
                false);
        return 1;
    }

    private static Player adapt(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return NeoForgeAdapter.get().fromNativePlayer(ctx.getSource().getPlayerOrException());
    }

    private static String facingName(int yawTurns) {
        return switch (yawTurns) {
            case 0 -> "面朝南";
            case 1 -> "面朝西";
            case 2 -> "面朝北";
            default -> "面朝东";
        };
    }
}
