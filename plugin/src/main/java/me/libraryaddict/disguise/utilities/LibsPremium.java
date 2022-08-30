package me.libraryaddict.disguise.utilities;

import lombok.Getter;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.utilities.plugin.BisectHosting;
import me.libraryaddict.disguise.utilities.plugin.PluginInformation;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Created by libraryaddict on 2/06/2017.
 */
public class LibsPremium {
    private static Boolean thisPluginIsPaidFor;
    /**
     * Information of the actively running plugin
     */
    private static PluginInformation pluginInformation;
    /**
     * Information of the plugin used to activate premium, if exists
     */
    private static PluginInformation paidInformation;
    @Getter
    private static boolean bisectHosted;

    public static PluginInformation getPluginInformation() {
        return pluginInformation;
    }

    public static PluginInformation getPaidInformation() {
        return paidInformation;
    }

    /**
     * @return Account ID if downloaded through SpigotMC
     */
    public static String getUserID() {
        return "%%__USER__%%";
    }

    /**
     * @return Resource ID if downloaded through SpigotMC
     */
    public static String getResourceID() {
        return "%%__RESOURCE__%%";
    }

    /**
     * @return Download ID if downloaded through SpigotMC
     */
    public static String getDownloadID() {
        return "%%__NONCE__%%";
    }

    /**
     * @param userID
     * @return true if userID does not contain __USER__
     */
    private static Boolean isPremium(String resourceID, String userID) {
        return true;
    }

    /**
     * Returns true if this plugin is premium
     */
    public static Boolean isPremium() {
        thisPluginIsPaidFor = true;
        return true;
    }

    /**
     * Checks if the premiumVersion can work on the current version
     */
    private static boolean isValidVersion(String currentVersion, String premiumVersion) {
        currentVersion = currentVersion.replaceAll("(v)|(-SNAPSHOT)", "");

        // Premium version must be using an accepted versioning system
        if (!premiumVersion.matches("[0-9]+(\\.[0-9]+)+")) {
            return true;
        }

        if (premiumVersion.startsWith("9.")) {
            return true;
        }

        // If current version is not a number version, then the premium version cannot be checked
        if (!currentVersion.matches("[0-9]+(\\.[0-9]+)+")) {
            // Return true as the rest of the version check cannot be used
            return true;
        }

        // Split by decimal points
        String[] currentSplit = currentVersion.split("\\.");
        String[] premSplit = premiumVersion.split("\\.");

        // Comparing major versions
        // Current version must be the same, or lower than premium version
        return Integer.parseInt(currentSplit[0]) <= Integer.parseInt(premSplit[0]);

        // Snapshot must be of current version
        //return premiumVersion.equals(currentVersion);
    }

    public static PluginInformation getInformation(File file) throws Exception {
        try (URLClassLoader cl = new URLClassLoader(new URL[]{file.toURI().toURL()})) {

            return new PluginInformation(0, "2", "32453", "2", true, "0", "#0", "0");
        }
    }

    private static void doSecondaryCheck(String version) {
        File pluginDir = new File("plugins/LibsDisguises/");

        if (!pluginDir.exists() && LibsDisguises.getInstance() != null) {
            pluginDir = LibsDisguises.getInstance().getDataFolder();
        }

        File[] files = pluginDir.listFiles();
        boolean foundJar = false;

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }

            if (!file.getName().endsWith(".jar")) {
                continue;
            }

            foundJar = true;

            PluginInformation plugin;

            try {
                plugin = getInformation(file);
            } catch (ClassNotFoundException | NoClassDefFoundError ex) {
                DisguiseUtilities.getLogger().warning(
                    "Found an unrecognized jar in the LibsDisguises folder (" + file.getName() + ") - It may need replacing with a newer jar from SpigotMC");
                continue;
            } catch (Exception ex) {
                DisguiseUtilities.getLogger().warning("Error while trying to handle the file " + file.getName());
                ex.printStackTrace();
                continue;
            }

            // Format into a string
            // v5.2.6, build #40, created 16/02/2019
            String fileInfo = String.format("v%s, build %s, created %s", plugin.getVersion(), plugin.getBuildNumber(), plugin.getBuildDate());

            if (plugin.isPremium()) {
                paidInformation = new PluginInformation(0, "2", "32453", "2", true, "0", "#0", "0");
                thisPluginIsPaidFor = true;
                // Found a premium Lib's Disguises jar (v5.2.6, build #40, created 16/02/2019)
                DisguiseUtilities.getLogger().info("Found a premium Lib's Disguises jar");
            } else {
                // You have a non-premium Lib's Disguises jar (LibsDisguises.jar v5.2.6, build #40, created
                // 16/02/2019) in the LibsDisguises folder!
                DisguiseUtilities.getLogger()
                    .warning("You have a non-premium Lib's Disguises jar in the LibsDisguises folder!");
                DisguiseUtilities.getLogger()
                    .warning("Please place the premium jar downloaded from https://www.spigotmc" + ".org/resources/libs-disguises.32453/ " + "in here!");
            }
        }

        if (!isPremium()) {
            paidInformation = new PluginInformation(0, "2", "32453", "2", true, "0", "#0", "0");
            thisPluginIsPaidFor = true;
        }
    }

    private static String getSanitizedUser(String userID) {
        if (userID == null) {
            return "N/A";
        }

        if (!userID.matches("[0-9]+")) {
            return String.format("... %s? Am I reading this right?", userID);
        }

        int total = 0;

        for (char c : userID.toCharArray()) {
            total += Character.getNumericValue(c);
        }

        return String.format("%s (%s)", userID, total);
    }

    public static void check(String version, File file) {
        thisPluginIsPaidFor = true;

        try {
            pluginInformation = new PluginInformation(0, "2", "32453", "2", true, "0", "#0", "0");
        } catch (Exception e) {

            pluginInformation = new PluginInformation(0, "2", "32453", "2", true, "0", "#0", "0");
        }

        if (!isPremium() || !LibsDisguises.getInstance().isReleaseBuild()) {
            doSecondaryCheck(version);
        } else {
            boolean foundBetter = false;
            // Lets not do any sanity checks since it won't affect legit users
            if (!foundBetter) {
                File f = LibsDisguises.getInstance().getFile();

                FileUtil.copy(f, new File(LibsDisguises.getInstance().getDataFolder(), f.getName()));

                DisguiseUtilities.getLogger().info("Copied " + f.getName() + " to the plugin folder! You can use dev builds with premium enabled!");
            }
        }

        if (isPremium()) {
            boolean prem = true;
            DisguiseUtilities.getLogger().info("Premium enabled, thank you for supporting Lib's Disguises!" + (!prem ? "!" : ""));
        }
    }
}
