package de.zeppy5.backupbase.commands;

import de.zeppy5.backupbase.backup.BackupManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class BackupCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("backupbase.backup")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
            return false;
        }

        if (args.length < 1 || args.length > 2) {
            syntax(sender);
            return false;
        }

        if (args[0].equalsIgnoreCase("backup")) {
            if (args.length == 1) {
                DecimalFormat df = new DecimalFormat("#.####");
                df.setRoundingMode(RoundingMode.CEILING);
                long size = BackupManager.timeBackup();
                if (size == 0) {
                    sender.sendMessage(ChatColor.RED + "Failed to create a backup!(" + df.format((double) size / 1000000) + "MB)");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "A Backup was successfully created!(" + df.format((double) size / 1000000) + "MB)");
                }
            } else {
                File backup = new File(Bukkit.getWorldContainer(), "backup");
                if (new File(backup, args[1] + ".zip").exists()) {
                    sender.sendMessage(ChatColor.RED + "A backup called " + ChatColor.GOLD + args[1] + ChatColor.RED + " already exists!");
                    return false;
                }
                DecimalFormat df = new DecimalFormat("#.####");
                df.setRoundingMode(RoundingMode.CEILING);
                long size = BackupManager.backup(args[1]);
                if (size == 0) {
                    sender.sendMessage(ChatColor.RED + "Failed to create a backup called " + ChatColor.GOLD + args[1] + ChatColor.RED + "!(" + df.format((double) size / 1000000) + "MB)");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "A backup called " + ChatColor.GOLD + args[1] + ChatColor.GREEN + " was successfully created!(" + df.format((double) size / 1000000) + "MB)");
                }
            }
        } else if (args[0].equalsIgnoreCase("load")) {
            if (!sender.hasPermission("backupbase.load")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
                return false;
            }
            if (args.length != 2) {
                syntax(sender);
                return false;
            }

            File backup = new File(Bukkit.getWorldContainer(), "backup");
            if (!(new File(backup, args[1] + ".zip").exists())) {
                sender.sendMessage(ChatColor.RED + "Backup named " + ChatColor.GOLD + args[1] + ChatColor.RED + " doesn't exist!");
                return false;
            }
            Bukkit.getLogger().log(Level.INFO, ChatColor.GREEN + sender.getName() + ChatColor.RESET + " loaded backup " + args[1] + "!");
            BackupManager.load(args[1]);

        } else {
            syntax(sender);
            return false;
        }

        return false;
    }

    public void syntax(CommandSender user) {
        user.sendMessage(ChatColor.RED + "Possible Commands are:");
        user.sendMessage(ChatColor.RED + "/backupbase backup [<name>]");
        user.sendMessage(ChatColor.RED + "/backupbase load <name>");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (!sender.hasPermission("backupbase.backup")) {
            return null;
        }

        if (args.length == 0) return list;

        if (args.length == 1) {
            list.add("backup");
            list.add("load");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("backup")) {
                list.add("name");
            } else if (args[0].equalsIgnoreCase("load")) {
                File backup = new File(Bukkit.getWorldContainer(), "backup");
                if (backup.listFiles() == null) return list;
                for (File name : Objects.requireNonNull(backup.listFiles())) {
                    list.add(name.getName().substring(0, name.getName().length() - 4));
                }
            }
        }

        ArrayList<String> completerList = new ArrayList<>();
        String currentArg = args[args.length - 1].toLowerCase();
        for (String s : list) {
            String s1 = s.toLowerCase();
            if(s1.startsWith(currentArg)) {
                completerList.add(s);
            }
        }
        return completerList;
    }
}
