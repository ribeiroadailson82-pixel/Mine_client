package com.mineclient.command;

import com.mineclient.config.ConfigManager;
import com.mineclient.module.Module;
import com.mineclient.module.ModuleManager;
import com.mineclient.settings.BooleanSetting;
import com.mineclient.settings.ModeSetting;
import com.mineclient.settings.NumberSetting;
import com.mineclient.settings.Setting;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;

/**
 * Comando de chat do client: /mineclient (alias /mcl).
 *
 * /mcl list
 * /mcl toggle <modulo>
 * /mcl set <modulo> <setting> <valor>
 * /mcl bind <modulo> <tecla|none>
 * /mcl save
 */
public class ClientCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "mineclient";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("mcl");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/mineclient <list|toggle|set|bind|save>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        String sub = args[0].toLowerCase();
        if ("list".equals(sub)) {
            for (Module module : ModuleManager.getModules()) {
                EnumChatFormatting color = module.isEnabled() ? EnumChatFormatting.GREEN : EnumChatFormatting.RED;
                message(sender, color + module.getName() + EnumChatFormatting.GRAY + " - " + module.getDescription());
            }
            return;
        }

        if ("save".equals(sub)) {
            ConfigManager.save();
            message(sender, EnumChatFormatting.GREEN + "Config salva.");
            return;
        }

        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        Module module = ModuleManager.getModule(args[1]);
        if (module == null) {
            message(sender, EnumChatFormatting.RED + "Modulo desconhecido: " + args[1]);
            return;
        }

        if ("toggle".equals(sub)) {
            module.toggle();
            EnumChatFormatting color = module.isEnabled() ? EnumChatFormatting.GREEN : EnumChatFormatting.RED;
            message(sender, module.getName() + " agora esta " + color + (module.isEnabled() ? "ativado" : "desativado"));
            return;
        }

        if ("bind".equals(sub)) {
            if (args.length < 3) {
                message(sender, EnumChatFormatting.RED + "Uso: /mineclient bind <modulo> <tecla|none>");
                return;
            }
            if ("none".equalsIgnoreCase(args[2])) {
                module.setKeyCode(0);
                message(sender, module.getName() + ": bind removido.");
                return;
            }
            int key = Keyboard.getKeyIndex(args[2].toUpperCase());
            if (key == Keyboard.KEY_NONE) {
                message(sender, EnumChatFormatting.RED + "Tecla desconhecida: " + args[2]);
                return;
            }
            module.setKeyCode(key);
            message(sender, module.getName() + " vinculado a tecla " + Keyboard.getKeyName(key));
            return;
        }

        if ("set".equals(sub)) {
            if (args.length < 4) {
                message(sender, EnumChatFormatting.RED + "Uso: /mineclient set <modulo> <setting> <valor>");
                return;
            }
            Setting setting = module.getSetting(args[2]);
            if (setting == null) {
                message(sender, EnumChatFormatting.RED + "Setting desconhecida: " + args[2]);
                return;
            }
            String value = args[3];
            try {
                if (setting instanceof BooleanSetting) {
                    ((BooleanSetting) setting).setValue(Boolean.parseBoolean(value));
                } else if (setting instanceof NumberSetting) {
                    ((NumberSetting) setting).setValue(Double.parseDouble(value));
                } else if (setting instanceof ModeSetting) {
                    ((ModeSetting) setting).setMode(value);
                }
                message(sender, module.getName() + "." + setting.getName() + " = " + value);
            } catch (NumberFormatException e) {
                message(sender, EnumChatFormatting.RED + "Valor invalido: " + value);
            }
            return;
        }

        sendUsage(sender);
    }

    private void sendUsage(ICommandSender sender) {
        message(sender, EnumChatFormatting.GOLD + "MineClient" + EnumChatFormatting.GRAY
                + " - /mineclient <list|toggle|set|bind|save> (GUI: Right Shift)");
    }

    private void message(ICommandSender sender, String text) {
        sender.addChatMessage(new ChatComponentText(text));
    }
}
