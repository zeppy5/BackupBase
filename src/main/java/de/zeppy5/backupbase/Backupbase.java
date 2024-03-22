package de.zeppy5.backupbase;

import de.zeppy5.backupbase.backup.Timer;
import de.zeppy5.backupbase.commands.BackupCommand;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class Backupbase extends JavaPlugin {

    private static int time;
    private static Backupbase instance;

    @Override
    public void onLoad() {
        String name = getConfig().getString("backup");
        File backup = new File(Bukkit.getWorldContainer(), "backup");
        if (getConfig().get("backup") == null) {
            return;
        }
        if (Objects.requireNonNull(name).equals("") || !((new File(backup, name)).exists())) {
            return;
        }
        for (File f : Objects.requireNonNull(backup.listFiles())) {
            if (f.getName().equals(name)) {
                try {
                    File fileZip = new File(backup, name);
                    File destDir = Bukkit.getWorldContainer();
                    byte[] buffer = new byte[1024];
                    ZipInputStream zis = new ZipInputStream(Files.newInputStream(fileZip.toPath()));
                    ZipEntry zipEntry = zis.getNextEntry();
                    while (zipEntry != null) {
                        File newFile = newFile(destDir, zipEntry);
                        if (newFile.exists()) {
                            newFile.delete();
                        }
                        if (zipEntry.isDirectory()) {
                            if (!newFile.isDirectory() && !newFile.mkdirs()) {
                                throw new IOException("Failed to create directory " + newFile);
                            }
                        } else {
                            // fix for Windows-created archives
                            File parent = newFile.getParentFile();
                            if (!parent.isDirectory() && !parent.mkdirs()) {
                                throw new IOException("Failed to create directory " + parent);
                            }

                            // write file content
                            FileOutputStream fos = new FileOutputStream(newFile);
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                            fos.close();
                        }
                        zipEntry = zis.getNextEntry();
                    }
                    zis.closeEntry();
                    zis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!getConfig().contains("time") || !getConfig().contains("autoBackup") || !getConfig().contains(
                "createBackupOnLoad") || !NumberUtils.isNumber(Objects.requireNonNull(getConfig().get("time")).toString()) ||
                getConfig().getInt("time") < 1 || !new File(this.getDataFolder(), "config.yml").isFile() ||
                Objects.equals(getConfig().get("time"), "")) {
            getConfig().set("time", 30);
            getConfig().set("autoBackup", true);
            getConfig().set("createBackupOnLoad", true);
            saveConfig();
        }

        if (!getConfig().contains("worlds") || getConfig().getStringList("worlds").isEmpty()) {
            List<String> worldList = new ArrayList<>();
            worldList.add("world");
            worldList.add("world_nether");
            worldList.add("world_the_end");
            getConfig().set("worlds", worldList);
            saveConfig();
        }

        getConfig().set("backup", "");
        saveConfig();
        time = getConfig().getInt("time");
        if (getConfig().getBoolean("autoBackup")) {
            Timer.timer();
        }

        if (!(new File(Bukkit.getWorldContainer(), "backup").exists())) {
            new File(Bukkit.getWorldContainer(), "backup").mkdirs();
        }
        Objects.requireNonNull(getCommand("backupbase")).setExecutor(new BackupCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Backupbase getInstance() {
        return instance;
    }

    public static Integer getTime() {
        return time;
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
