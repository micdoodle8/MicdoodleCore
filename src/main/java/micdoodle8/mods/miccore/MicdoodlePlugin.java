package micdoodle8.mods.miccore;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.LoaderException;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import cpw.mods.fml.common.versioning.VersionParser;
import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@TransformerExclusions(value = { "micdoodle8.mods.miccore" })
public class MicdoodlePlugin implements IFMLLoadingPlugin, IFMLCallHook
{
	public static boolean hasRegistered = false;
	public static final String mcVersion = "[1.7.2],[1.7.10]";
	public static File mcDir;
    public static File canonicalConfigDir;
    private static boolean checkedVersions = false;

	public static void versionCheck(String reqVersion, String mod)
	{
		final String mcVersion = (String) FMLInjectionData.data()[4];

		if (!VersionParser.parseRange(reqVersion).containsVersion(new DefaultArtifactVersion(mcVersion)))
		{
			final String err = "This version of " + mod + " does not support minecraft version " + mcVersion;
			System.err.println(err);

			final JEditorPane ep = new JEditorPane("text/html", "<html>" + err + "<br>Remove it from your mods folder and check <a href=\"http://micdoodle8.com\">here</a> for updates" + "</html>");

			ep.setEditable(false);
			ep.setOpaque(false);
			ep.addHyperlinkListener(new HyperlinkListener()
			{
				@Override
				public void hyperlinkUpdate(HyperlinkEvent event)
				{
					try
					{
						if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
						{
							Desktop.getDesktop().browse(event.getURL().toURI());
						}
					}
					catch (final Exception e)
					{
					}
				}
			});

			JOptionPane.showMessageDialog(null, ep, "Fatal error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	@Override
	public String[] getASMTransformerClass()
	{
		MicdoodlePlugin.versionCheck(MicdoodlePlugin.mcVersion, "MicdoodleCore");
		final String[] asmStrings = new String[] { "micdoodle8.mods.miccore.MicdoodleTransformer" };

		if (!MicdoodlePlugin.hasRegistered)
		{
			final List<String> asm = Arrays.asList(asmStrings);

			for (final String s : asm)
			{
				try
				{
					final Class<?> c = Class.forName(s);

					if (c != null)
					{
						System.out.println("Successfully Registered Transformer");
					}
				}
				catch (final Exception ex)
				{
					System.out.println("Error while running transformer " + s);
					return null;
				}
			}

			MicdoodlePlugin.hasRegistered = true;
		}

		return asmStrings;
	}

	@Override
	public String getModContainerClass()
	{
		return "micdoodle8.mods.miccore.MicdoodleModContainer";
	}

	@Override
	public String getSetupClass()
	{
		return "micdoodle8.mods.miccore.MicdoodlePlugin";
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		if (data.containsKey("mcLocation"))
		{
			MicdoodlePlugin.mcDir = (File) data.get("mcLocation");
            File configDir = new File(mcDir, "config");
            File modsDir = new File(mcDir, "mods");
        	String minecraftVersion = (String) FMLInjectionData.data()[4];
        	File subDir = new File(modsDir, minecraftVersion);
            String canonicalConfigPath;

            if (!checkedVersions)
            {
                checkedVersions = true;
                boolean obfuscated = false;

                try
                {
                    obfuscated = Launch.classLoader.getClassBytes("net.minecraft.world.World") == null;
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }

                if (obfuscated)
                {
                	Collection<File> fileList = FileUtils.listFiles(modsDir, new String[] {"jar", "zip"}, false);

                    String[] micCoreVersion = null;
                    String[] gcVersion = null;
                    if (fileList != null)
                    {
                    	if (subDir.isDirectory()) fileList.addAll(FileUtils.listFiles(subDir, new String[] {"jar", "zip"}, false));

                    	for (File file : fileList)
                        {
                            if (file.getName().toLowerCase().contains("micdoodlecore"))
                            {
                                String fileName = file.getName().toLowerCase();

                                String[] split0 = fileName.split("\\-");

                                if (split0.length == 4)
                                {
                                    String micVersion = split0[3].replace(".jar", "").replace(".zip", "");

                                    micCoreVersion = micVersion.split("\\.");
                                }
                                else if (split0.length == 3)
                                {
                                    String micVersion = split0[2].replace(".jar", "").replace(".zip", "");

                                    micCoreVersion = micVersion.split("\\.");
                                }
                            }

                            if (file.getName().toLowerCase().contains("galacticraftcore"))
                            {
                                String fileName = file.getName().toLowerCase();

                                String[] split0 = fileName.split("\\-");

                                if (split0.length == 4)
                                {
                                    String micVersion = split0[3].replace(".jar", "").replace(".zip", "");

                                    gcVersion = micVersion.split("\\.");
                                }
                                else if (split0.length == 3)
                                {
                                    String micVersion = split0[2].replace(".jar", "").replace(".zip", "");

                                    gcVersion = micVersion.split("\\.");
                                }
                            }
                        }
                    }

                    if (micCoreVersion == null)
                    {
                        FMLLog.info("Failed to find MicdoodleCore file in mods folder, skipping GC version check.");
                    }
                    else if (gcVersion == null)
                    {
                        this.showErrorDialog(new Object[]{"Install", "Ignore"}, "Failed to find Galacticraft file in mods folder!");
                    }
                    else
                    {
                        if (micCoreVersion.length != gcVersion.length)
                        {
                            this.showErrorDialog(new Object[]{"Reinstall", "Ignore"}, "Failed to match Galacticraft version to MicdoodleCore version!");
                        }
                        else
                        {
                            for (int i = 0; i < (micCoreVersion.length & gcVersion.length); i++)
                            {
                                micCoreVersion[i] = trimInvalidIntegers(micCoreVersion[i]);
                                gcVersion[i] = trimInvalidIntegers(gcVersion[i]);
                            }

                            for (int i = 0; i < micCoreVersion.length; i++)
                            {
                                if (!micCoreVersion[i].equals(gcVersion[i]))
                                {
                                    int micCoreVersionI = Integer.parseInt(micCoreVersion[i]);
                                    int gcVersionI = Integer.parseInt(gcVersion[i]);

                                    if (micCoreVersionI < gcVersionI)
                                    {
                                        this.showErrorDialog(new Object[]{"Update", "Ignore"}, "MicdoodleCore Update Required!", "Galacticraft and MicdoodleCore should always be at the same version", "Severe issues can be caused from not updating");
                                    }
                                    else
                                    {
                                        this.showErrorDialog(new Object[]{"Update", "Ignore"}, "Galacticraft Update Required!", "Galacticraft and MicdoodleCore should always be at the same version", "Severe issues can be caused from not updating");
                                    }
                                }
                            }
                        }
                    }
                }
            }

            try
            {
                canonicalConfigPath = configDir.getCanonicalPath();
                canonicalConfigDir = configDir.getCanonicalFile();
            }
            catch (IOException ioe)
            {
                throw new LoaderException(ioe);
            }

            if (!canonicalConfigDir.exists())
            {
                FMLLog.fine("No config directory found, creating one: %s", canonicalConfigPath);
                boolean dirMade = canonicalConfigDir.mkdir();

                if (!dirMade)
                {
                    FMLLog.severe("Unable to create the config directory %s", canonicalConfigPath);
                    throw new LoaderException();
                }

                FMLLog.info("Config directory created successfully");
            }

            ConfigManagerMicCore.init();
		}

		System.out.println("[Micdoodle8Core]: " + "Patching game...");
	}

    private void showErrorDialog(Object[] options, String... messages)
    {
        String err = "<html>";
        for (String s : messages)
        {
            System.err.print(s);
            err = err.concat(s + "<br />");
        }
        err = err.concat("</html>");

        final JEditorPane ep = new JEditorPane("text/html", err);

        ep.setEditable(false);
        ep.setOpaque(false);
        ep.addHyperlinkListener(new HyperlinkListener()
        {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent event)
            {
                try
                {
                    if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                    {
                        Desktop.getDesktop().browse(event.getURL().toURI());
                    }
                }
                catch (final Exception e)
                {
                }
            }
        });

        int ret = JOptionPane.showOptionDialog(null, ep, "Fatal error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
        System.err.println(ret);

        switch (ret)
        {
            case 0:
                try
                {
                    Desktop.getDesktop().browse(new URL("http://micdoodle8.com/mods/galacticraft/downloads").toURI());
                }
                catch (final Exception e)
                {
                }
                System.exit(0);
                break;
            case 1:
                break;
            case JOptionPane.CLOSED_OPTION:
                break;
        }
    }

    private String trimInvalidIntegers(String toTrim)
    {
        String newString = "";
        for (int j = 0; j < toTrim.length(); j++)
        {
            String c = toTrim.substring(j, j + 1);
            if ("0123456789".contains(c))
            {
                newString = newString.concat(c);
            }
            else
            {
                break;
            }
        }
        return newString;
    }

	@Override
	public Void call() throws Exception
	{
		return null;
	}

	private static Constructor<?> sleepCancelledConstructor;
	private static Constructor<?> orientCameraConstructor;
	private static String galacticraftCoreClass = "micdoodle8.mods.galacticraft.core.event.EventHandlerGC";

	public static void onSleepCancelled()
	{
		try
		{
			if (MicdoodlePlugin.sleepCancelledConstructor == null)
			{
				MicdoodlePlugin.sleepCancelledConstructor = Class.forName(MicdoodlePlugin.galacticraftCoreClass + "$SleepCancelledEvent").getConstructor();
			}

			MinecraftForge.EVENT_BUS.post((Event) MicdoodlePlugin.sleepCancelledConstructor.newInstance());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void orientCamera()
	{
		try
		{
			if (MicdoodlePlugin.orientCameraConstructor == null)
			{
				MicdoodlePlugin.orientCameraConstructor = Class.forName(MicdoodlePlugin.galacticraftCoreClass + "$OrientCameraEvent").getConstructor();
			}

			MinecraftForge.EVENT_BUS.post((Event) MicdoodlePlugin.orientCameraConstructor.newInstance());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public String getAccessTransformerClass()
	{
        return "micdoodle8.mods.miccore.MicdoodleAccessTransformer";
	}
}
