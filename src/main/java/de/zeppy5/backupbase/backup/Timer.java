package de.zeppy5.backupbase.backup;

import de.zeppy5.backupbase.Backupbase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Timer {

    public static void timer() {
        final int[] time = {Backupbase.getTime()*60};
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Backupbase.getInstance(), () -> {
            switch (time[0]) {
                case 10:
                    Bukkit.broadcastMessage(ChatColor.GRAY + "The server will create a backup in 10 seconds!");
                    break;
                case 0:
                    DecimalFormat df = new DecimalFormat("#.####");
                    df.setRoundingMode(RoundingMode.CEILING);
                    long size = BackupManager.timeBackup();
                    Bukkit.broadcastMessage(ChatColor.GRAY + "The server successfully created a backup!(" + df.format((double) size / 1000000) + "MB)");
                    time[0] = Backupbase.getTime()*60;
                    break;
                default:
                    break;
            }
            time[0] -= 1;
        }, 0, 20);
    }
}
