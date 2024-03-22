package de.zeppy5.backupbase.backup;


import de.zeppy5.backupbase.Backupbase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupManager {

    public static long timeBackup() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
        Calendar cal = Calendar.getInstance();
        String time = dateFormat.format(cal.getTime());
        return backup(time);
    }

    public static long backup(String name) {
        long size = 0;
        try {

            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "save-all");

            List<String> worldListNames = Backupbase.getInstance().getConfig().getStringList("worlds");

            ArrayList<File> worldList = new ArrayList<>();

            for (String world : worldListNames) {
                worldList.add(new File(Bukkit.getWorldContainer(), world));
            }

            for (File w : worldList) {
                for (File f : Objects.requireNonNull(w.listFiles())) {
                    if (f.getName().equals("session.lock")) {
                        f.delete();
                    }
                }
            }

            File backup = new File(Bukkit.getWorldContainer(), "backup");

            ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(new File(backup, name + ".zip").toPath()));
            for (File w : worldList) {
                zipFile(w, w.getName(), zip);
            }

            zip.close();
            size = Files.size(new File(backup, name + ".zip").toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public static void load(String name) {
        if (Backupbase.getInstance().getConfig().getBoolean("createBackupOnLoad")) {
            timeBackup();
        }
        Backupbase.getInstance().getConfig().set("backup", name + ".zip");
        Backupbase.getInstance().saveConfig();
        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer(ChatColor.GREEN + "The server is loading a backup!"));
        Bukkit.spigot().restart();
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
            }
            zipOut.closeEntry();
            File[] children = fileToZip.listFiles();
            for (File childFile : Objects.requireNonNull(children)) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
}
