package micdoodle8.mods.miccore;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.launchwrapper.LaunchClassLoader;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.fml.common.Loader;

public class MicdoodleTransformer implements net.minecraft.launchwrapper.IClassTransformer
{
	HashMap<String, ObfuscationEntry> nodemap = new HashMap<String, ObfuscationEntry>();
	private boolean deobfuscated = true;
	private boolean optifinePresent;


    private static final String KEY_CLASS_PLAYER_MP = "PlayerMP";
    private static final String KEY_CLASS_WORLD = "worldClass";
    private static final String KEY_CLASS_CONF_MANAGER = "confManagerClass";
    private static final String KEY_CLASS_GAME_PROFILE = "gameProfileClass";
    private static final String KEY_CLASS_ITEM_IN_WORLD_MANAGER = "itemInWorldManagerClass";
    private static final String KEY_CLASS_PLAYER_CONTROLLER = "playerControllerClass";
    private static final String KEY_CLASS_PLAYER_SP = "playerClient";
    private static final String KEY_CLASS_STAT_FILE_WRITER = "statFileWriterClass";
    private static final String KEY_CLASS_NET_HANDLER_PLAY = "netHandlerPlayClientClass";
    private static final String KEY_CLASS_ENTITY_LIVING = "entityLivingClass";
    private static final String KEY_CLASS_ENTITY_ITEM = "entityItemClass";
    private static final String KEY_CLASS_ENTITY_RENDERER = "entityRendererClass";
    private static final String KEY_CLASS_CONTAINER_PLAYER = "containerPlayer";
    private static final String KEY_CLASS_MINECRAFT = "minecraft";
    private static final String KEY_CLASS_SESSION = "session";
    private static final String KEY_CLASS_GUI_SCREEN = "guiScreen";
    private static final String KEY_CLASS_ITEM_RENDERER = "itemRendererClass";
    private static final String KEY_CLASS_VEC3 = "vecClass";
    private static final String KEY_CLASS_ENTITY = "entityClass";
    private static final String KEY_CLASS_GUI_SLEEP = "guiSleepClass";
    private static final String KEY_CLASS_EFFECT_RENDERER = "effectRendererClass";
    private static final String KEY_CLASS_FORGE_HOOKS_CLIENT = "forgeHooks";
    private static final String KEY_CLASS_CUSTOM_PLAYER_MP = "customPlayerMP";
    private static final String KEY_CLASS_CUSTOM_PLAYER_SP = "customPlayerSP";
    private static final String KEY_CLASS_CUSTOM_OTHER_PLAYER = "customEntityOtherPlayer";
    private static final String KEY_CLASS_PACKET_SPAWN_PLAYER = "packetSpawnPlayer";
    private static final String KEY_CLASS_ENTITY_OTHER_PLAYER = "entityOtherPlayer";
    private static final String KEY_CLASS_SERVER = "minecraftServer";
    private static final String KEY_CLASS_WORLD_SERVER = "worldServer";

    private static final String KEY_FIELD_THE_PLAYER = "thePlayer";

    private static final String KEY_METHOD_CREATE_PLAYER = "createPlayerMethod";
    private static final String KEY_METHOD_RESPAWN_PLAYER = "respawnPlayerMethod";
    private static final String KEY_METHOD_CREATE_CLIENT_PLAYER = "createClientPlayerMethod";
    private static final String KEY_METHOD_MOVE_ENTITY = "moveEntityMethod";
    private static final String KEY_METHOD_ON_UPDATE = "onUpdateMethod";
    private static final String KEY_METHOD_UPDATE_LIGHTMAP = "updateLightmapMethod";
    private static final String KEY_METHOD_RENDER_OVERLAYS = "renderOverlaysMethod";
    private static final String KEY_METHOD_UPDATE_FOG_COLOR = "updateFogColorMethod";
    private static final String KEY_METHOD_GET_FOG_COLOR = "getFogColorMethod";
    private static final String KEY_METHOD_GET_SKY_COLOR = "getSkyColorMethod";
    private static final String KEY_METHOD_WAKE_ENTITY = "wakeEntityMethod";
    private static final String KEY_METHOD_ORIENT_CAMERA = "orientBedCamera";
    private static final String KEY_METHOD_RENDER_PARTICLES = "renderParticlesMethod";
    private static final String KEY_METHOD_CUSTOM_PLAYER_MP = "customPlayerMPConstructor";
    private static final String KEY_METHOD_CUSTOM_PLAYER_SP = "customPlayerSPConstructor";
    private static final String KEY_METHOD_ATTEMPT_LOGIN_BUKKIT = "attemptLoginMethodBukkit";
    private static final String KEY_METHOD_HANDLE_SPAWN_PLAYER = "handleSpawnPlayerMethod";

    private static final String CLASS_RUNTIME_INTERFACE = "micdoodle8/mods/miccore/Annotations$RuntimeInterface";
    private static final String CLASS_MICDOODLE_PLUGIN = "micdoodle8/mods/miccore/MicdoodlePlugin";
    private static final String CLASS_CLIENT_PROXY_MAIN = "micdoodle8/mods/galacticraft/core/proxy/ClientProxyCore";
    private static final String CLASS_WORLD_UTIL = "micdoodle8/mods/galacticraft/core/util/WorldUtil";

    private static int operationCount = 0;
    private static int injectionCount = 0;

	@SuppressWarnings("resource")
	public MicdoodleTransformer()
	{
		try
		{
			final URLClassLoader loader = new LaunchClassLoader(((URLClassLoader) this.getClass().getClassLoader()).getURLs());
			URL classResource = loader.findResource(String.valueOf("net.minecraft.world.World").replace('.', '/').concat(".class"));
            this.deobfuscated = classResource != null;

			classResource = loader.findResource(String.valueOf("CustomColorizer").replace('.', '/').concat(".class"));
            this.optifinePresent = classResource != null;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}

        this.nodemap.put(KEY_CLASS_PLAYER_MP, new ObfuscationEntry("net/minecraft/entity/player/EntityPlayerMP", "mm"));
        this.nodemap.put(KEY_CLASS_WORLD, new ObfuscationEntry("net/minecraft/world/World", "afn"));
        this.nodemap.put(KEY_CLASS_CONF_MANAGER, new ObfuscationEntry("net/minecraft/server/management/ServerConfigurationManager", "ld"));
        this.nodemap.put(KEY_CLASS_GAME_PROFILE, new ObfuscationEntry("com/mojang/authlib/GameProfile"));
        this.nodemap.put(KEY_CLASS_ITEM_IN_WORLD_MANAGER, new ObfuscationEntry("net/minecraft/server/management/ItemInWorldManager", "mn"));
        this.nodemap.put(KEY_CLASS_PLAYER_CONTROLLER, new ObfuscationEntry("net/minecraft/client/multiplayer/PlayerControllerMP", "biy"));
        this.nodemap.put(KEY_CLASS_PLAYER_SP, new ObfuscationEntry("net/minecraft/client/entity/EntityClientPlayerMP", "bje"));
        this.nodemap.put(KEY_CLASS_STAT_FILE_WRITER, new ObfuscationEntry("net/minecraft/stats/StatFileWriter", "oe"));
        this.nodemap.put(KEY_CLASS_NET_HANDLER_PLAY, new ObfuscationEntry("net/minecraft/client/network/NetHandlerPlayClient", "biv"));
        this.nodemap.put(KEY_CLASS_ENTITY_LIVING, new ObfuscationEntry("net/minecraft/entity/EntityLivingBase", "rh"));
        this.nodemap.put(KEY_CLASS_ENTITY_ITEM, new ObfuscationEntry("net/minecraft/entity/item/EntityItem", "vw"));
        this.nodemap.put(KEY_CLASS_ENTITY_RENDERER, new ObfuscationEntry("net/minecraft/client/renderer/EntityRenderer", "bll"));
        this.nodemap.put(KEY_CLASS_CONTAINER_PLAYER, new ObfuscationEntry("net/minecraft/inventory/ContainerPlayer", "zb"));
        this.nodemap.put(KEY_CLASS_MINECRAFT, new ObfuscationEntry("net/minecraft/client/Minecraft", "azd"));
        this.nodemap.put(KEY_CLASS_SESSION, new ObfuscationEntry("net/minecraft/util/Session", "baf"));
        this.nodemap.put(KEY_CLASS_GUI_SCREEN, new ObfuscationEntry("net/minecraft/client/gui/GuiScreen", "bcd"));
        this.nodemap.put(KEY_CLASS_ITEM_RENDERER, new ObfuscationEntry("net/minecraft/client/renderer/ItemRenderer", "blq"));
        this.nodemap.put(KEY_CLASS_VEC3, new ObfuscationEntry("net/minecraft/util/Vec3", "ayk"));
        this.nodemap.put(KEY_CLASS_ENTITY, new ObfuscationEntry("net/minecraft/entity/Entity", "qn"));
        this.nodemap.put(KEY_CLASS_GUI_SLEEP, new ObfuscationEntry("net/minecraft/client/gui/GuiSleepMP", "bbp"));
        this.nodemap.put(KEY_CLASS_EFFECT_RENDERER, new ObfuscationEntry("net/minecraft/client/particle/EffectRenderer", "bkg"));
        this.nodemap.put(KEY_CLASS_FORGE_HOOKS_CLIENT, new ObfuscationEntry("net/minecraftforge/client/ForgeHooksClient"));
        this.nodemap.put(KEY_CLASS_CUSTOM_PLAYER_MP, new ObfuscationEntry("micdoodle8/mods/galacticraft/core/entities/player/GCEntityPlayerMP"));
        this.nodemap.put(KEY_CLASS_CUSTOM_PLAYER_SP, new ObfuscationEntry("micdoodle8/mods/galacticraft/core/entities/player/GCEntityClientPlayerMP"));
        this.nodemap.put(KEY_CLASS_CUSTOM_OTHER_PLAYER, new ObfuscationEntry("micdoodle8/mods/galacticraft/core/entities/player/GCEntityOtherPlayerMP"));
        this.nodemap.put(KEY_CLASS_PACKET_SPAWN_PLAYER, new ObfuscationEntry("net/minecraft/network/play/server/S0CPacketSpawnPlayer", "fs"));
        this.nodemap.put(KEY_CLASS_ENTITY_OTHER_PLAYER, new ObfuscationEntry("net/minecraft/client/entity/EntityOtherPlayerMP", "bld"));
        this.nodemap.put(KEY_CLASS_SERVER, new ObfuscationEntry("net/minecraft/server/MinecraftServer"));
        this.nodemap.put(KEY_CLASS_WORLD_SERVER, new ObfuscationEntry("net/minecraft/world/WorldServer", "mj"));

        this.nodemap.put(KEY_FIELD_THE_PLAYER, new FieldObfuscationEntry("thePlayer", "h"));

        this.nodemap.put(KEY_METHOD_CREATE_PLAYER, new MethodObfuscationEntry("createPlayerForUser", "a", "(L" + getNameDynamic(KEY_CLASS_GAME_PROFILE) + ";)L" + getNameDynamic(KEY_CLASS_PLAYER_MP) + ";"));
        this.nodemap.put(KEY_METHOD_RESPAWN_PLAYER, new MethodObfuscationEntry("respawnPlayer", "a", "(L" + getNameDynamic(KEY_CLASS_PLAYER_MP) + ";IZ)L" + getNameDynamic(KEY_CLASS_PLAYER_MP) + ";"));
        this.nodemap.put(KEY_METHOD_CREATE_CLIENT_PLAYER, new MethodObfuscationEntry("func_147493_a", "a", "(L" + getNameDynamic(KEY_CLASS_WORLD) + ";L" + getNameDynamic(KEY_CLASS_STAT_FILE_WRITER) + ";)L" + getNameDynamic(KEY_CLASS_PLAYER_SP) + ";"));
        this.nodemap.put(KEY_METHOD_MOVE_ENTITY, new MethodObfuscationEntry("moveEntityWithHeading", "e", "(FF)V"));
        this.nodemap.put(KEY_METHOD_ON_UPDATE, new MethodObfuscationEntry("onUpdate", "h", "()V"));
        this.nodemap.put(KEY_METHOD_UPDATE_LIGHTMAP, new MethodObfuscationEntry("updateLightmap", "h", "(F)V"));
        this.nodemap.put(KEY_METHOD_RENDER_OVERLAYS, new MethodObfuscationEntry("renderOverlays", "b", "(F)V"));
        this.nodemap.put(KEY_METHOD_UPDATE_FOG_COLOR, new MethodObfuscationEntry("updateFogColor", "i", "(F)V"));
        this.nodemap.put(KEY_METHOD_GET_FOG_COLOR, new MethodObfuscationEntry("getFogColor", "", "(F)L" + getNameDynamic(KEY_CLASS_VEC3) + ";"));
        this.nodemap.put(KEY_METHOD_GET_SKY_COLOR, new MethodObfuscationEntry("getSkyColor", "a", "(L" + getNameDynamic(KEY_CLASS_ENTITY) + ";F)L" + getNameDynamic(KEY_CLASS_VEC3) + ";"));
        this.nodemap.put(KEY_METHOD_WAKE_ENTITY, new MethodObfuscationEntry("func_146418_g", "func_146418_g", "()V"));
        this.nodemap.put(KEY_METHOD_ORIENT_CAMERA, new MethodObfuscationEntry("orientBedCamera", "orientBedCamera", "(L" + getNameDynamic(KEY_CLASS_MINECRAFT) + ";L" + getNameDynamic(KEY_CLASS_ENTITY_LIVING) + ";)V"));
        this.nodemap.put(KEY_METHOD_RENDER_PARTICLES, new MethodObfuscationEntry("renderParticles", "a", "(L" + getNameDynamic(KEY_CLASS_ENTITY) + ";F)V"));
        this.nodemap.put(KEY_METHOD_CUSTOM_PLAYER_MP, new MethodObfuscationEntry("<init>", "(L" + getNameDynamic(KEY_CLASS_SERVER) + ";L" + getNameDynamic(KEY_CLASS_WORLD_SERVER) + ";L" + getNameDynamic(KEY_CLASS_GAME_PROFILE) + ";L" + getNameDynamic(KEY_CLASS_ITEM_IN_WORLD_MANAGER) + ";)V"));
        this.nodemap.put(KEY_METHOD_CUSTOM_PLAYER_SP, new MethodObfuscationEntry("<init>", "(L" + getNameDynamic(KEY_CLASS_MINECRAFT) + ";L" + getNameDynamic(KEY_CLASS_WORLD) + ";L" + getNameDynamic(KEY_CLASS_SESSION) + ";L" + getNameDynamic(KEY_CLASS_NET_HANDLER_PLAY) + ";L" + getNameDynamic(KEY_CLASS_STAT_FILE_WRITER) + ";)V"));
        this.nodemap.put(KEY_METHOD_ATTEMPT_LOGIN_BUKKIT, new MethodObfuscationEntry("", "", ""));
        this.nodemap.put(KEY_METHOD_HANDLE_SPAWN_PLAYER, new MethodObfuscationEntry("handleSpawnPlayer", "a", "(L" + getNameDynamic(KEY_CLASS_PACKET_SPAWN_PLAYER) + ";)V"));
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
        if (classPathMatches(KEY_CLASS_CONF_MANAGER, name))
		{
			bytes = this.transformConfigManager(name, bytes);
		}
		else if (classPathMatches(KEY_CLASS_PLAYER_CONTROLLER, name))
		{
			bytes = this.transformPlayerController(name, bytes);
		}
		else if (classPathMatches(KEY_CLASS_ENTITY_LIVING, name))
		{
			bytes = this.transformEntityLiving(name, bytes);
		}
		else if (classPathMatches(KEY_CLASS_ENTITY_ITEM, name))
		{
			bytes = this.transformEntityItem(name, bytes);
		}
		else if (classPathMatches(KEY_CLASS_ENTITY_RENDERER, name))
		{
			bytes = this.transformEntityRenderer(name, bytes);
		}
		else if (classPathMatches(KEY_CLASS_ITEM_RENDERER, name))
		{
			bytes = this.transformItemRenderer(name, bytes);
		}
		else if (classPathMatches(KEY_CLASS_GUI_SLEEP, name))
		{
			bytes = this.transformGuiSleep(name, bytes);
		}
		else if (classPathMatches(KEY_CLASS_FORGE_HOOKS_CLIENT, name))
		{
			bytes = this.transformForgeHooks(name, bytes);
		}
		else if (classPathMatches(KEY_CLASS_EFFECT_RENDERER, name))
		{
			bytes = this.transformEffectRenderer(name, bytes);
		}
		else if (classPathMatches(KEY_CLASS_NET_HANDLER_PLAY, name))
		{
			bytes = this.transformNetHandlerPlay(name, bytes);
		}

		if (name.contains("galacticraft"))
		{
			bytes = this.transformCustomAnnotations(name, bytes);
		}

		return bytes;
	}

	/**
	 * replaces EntityPlayerMP initialization with custom ones
	 */
	public byte[] transformConfigManager(String name, byte[] bytes)
	{
        ClassNode node = startInjection(bytes);

		operationCount = 6;

        MethodNode createPlayerMethod = getMethod(node, KEY_METHOD_CREATE_PLAYER);
        MethodNode respawnPlayerMethod = getMethod(node, KEY_METHOD_RESPAWN_PLAYER);
        MethodNode attemptLoginMethod = getMethod(node, KEY_METHOD_ATTEMPT_LOGIN_BUKKIT);

        if (createPlayerMethod != null)
        {
            for (int count = 0; count < createPlayerMethod.instructions.size(); count++)
            {
                final AbstractInsnNode list = createPlayerMethod.instructions.get(count);

                if (list instanceof TypeInsnNode)
                {
                    final TypeInsnNode nodeAt = (TypeInsnNode) list;

                    if (nodeAt.getOpcode() != Opcodes.CHECKCAST && nodeAt.desc.contains(getNameDynamic(KEY_CLASS_PLAYER_MP)))
                    {
                        final TypeInsnNode overwriteNode = new TypeInsnNode(Opcodes.NEW, getName(KEY_CLASS_CUSTOM_PLAYER_MP));

                        createPlayerMethod.instructions.set(nodeAt, overwriteNode);
                        injectionCount++;
                    }
                }
                else if (list instanceof MethodInsnNode)
                {
                    final MethodInsnNode nodeAt = (MethodInsnNode) list;

                    if (nodeAt.owner.contains(getNameDynamic(KEY_CLASS_PLAYER_MP)) && nodeAt.getOpcode() == Opcodes.INVOKESPECIAL)
                    {
                        createPlayerMethod.instructions.set(nodeAt, new MethodInsnNode(Opcodes.INVOKESPECIAL, getName(KEY_CLASS_CUSTOM_PLAYER_MP), getName(KEY_METHOD_CUSTOM_PLAYER_MP), getDescDynamic(KEY_METHOD_CUSTOM_PLAYER_MP)));
                        injectionCount++;
                    }
                }
            }
        }

        if (respawnPlayerMethod != null)
        {
            for (int count = 0; count < respawnPlayerMethod.instructions.size(); count++)
            {
                final AbstractInsnNode list = respawnPlayerMethod.instructions.get(count);

                if (list instanceof TypeInsnNode)
                {
                    final TypeInsnNode nodeAt = (TypeInsnNode) list;

                    if (nodeAt.getOpcode() != Opcodes.CHECKCAST && nodeAt.desc.contains(getNameDynamic(KEY_CLASS_PLAYER_MP)))
                    {
                        final TypeInsnNode overwriteNode = new TypeInsnNode(Opcodes.NEW, getName(KEY_CLASS_CUSTOM_PLAYER_MP));

                        respawnPlayerMethod.instructions.set(nodeAt, overwriteNode);
                        injectionCount++;
                    }
                }
                else if (list instanceof MethodInsnNode)
                {
                    final MethodInsnNode nodeAt = (MethodInsnNode) list;

                    if (nodeAt.name.equals("<init>") && nodeAt.owner.equals(getNameDynamic(KEY_CLASS_PLAYER_MP)))
                    {
                        respawnPlayerMethod.instructions.set(nodeAt, new MethodInsnNode(Opcodes.INVOKESPECIAL, getName(KEY_CLASS_CUSTOM_PLAYER_MP), getName(KEY_METHOD_CUSTOM_PLAYER_MP), getDescDynamic(KEY_METHOD_CUSTOM_PLAYER_MP)));

                        injectionCount++;
                    }
                }
            }
        }

        if (attemptLoginMethod != null)
        {
            for (int count = 0; count < attemptLoginMethod.instructions.size(); count++)
            {
                final AbstractInsnNode list = attemptLoginMethod.instructions.get(count);

                if (list instanceof TypeInsnNode)
                {
                    final TypeInsnNode nodeAt = (TypeInsnNode) list;

                    if (nodeAt.getOpcode() == Opcodes.NEW && nodeAt.desc.contains(getNameDynamic(KEY_CLASS_PLAYER_MP)))
                    {
                        final TypeInsnNode overwriteNode = new TypeInsnNode(Opcodes.NEW, getName(KEY_CLASS_CUSTOM_PLAYER_MP));

                        attemptLoginMethod.instructions.set(nodeAt, overwriteNode);
                        injectionCount++;
                    }
                }
                else if (list instanceof MethodInsnNode)
                {
                    final MethodInsnNode nodeAt = (MethodInsnNode) list;

                    if (nodeAt.getOpcode() == Opcodes.INVOKESPECIAL && nodeAt.name.equals("<init>") && nodeAt.owner.equals(getNameDynamic(KEY_CLASS_PLAYER_MP)))
                    {
                        attemptLoginMethod.instructions.set(nodeAt, new MethodInsnNode(Opcodes.INVOKESPECIAL, getName(KEY_CLASS_CUSTOM_PLAYER_MP), getName(KEY_METHOD_CUSTOM_PLAYER_MP), "(Lnet/minecraft/server/MinecraftServer;L" + getNameDynamic(KEY_CLASS_WORLD) + ";Ljava/lang/String;L" + getNameDynamic(KEY_CLASS_ITEM_IN_WORLD_MANAGER) + ";)V"));

                        injectionCount++;
                    }
                }
            }
        }

        return finishInjection(node);
	}

	public byte[] transformPlayerController(String name, byte[] bytes)
	{
        ClassNode node = startInjection(bytes);

		operationCount = 2;

        MethodNode method = getMethod(node, KEY_METHOD_CREATE_CLIENT_PLAYER);

        if (method != null)
        {
            for (int count = 0; count < method.instructions.size(); count++)
            {
                final AbstractInsnNode list = method.instructions.get(count);

                if (list instanceof TypeInsnNode)
                {
                    final TypeInsnNode nodeAt = (TypeInsnNode) list;

                    if (nodeAt.desc.contains(getNameDynamic(KEY_CLASS_PLAYER_SP)))
                    {
                        final TypeInsnNode overwriteNode = new TypeInsnNode(Opcodes.NEW, getName(KEY_CLASS_CUSTOM_PLAYER_SP));

                        method.instructions.set(nodeAt, overwriteNode);
                        injectionCount++;
                    }
                }
                else if (list instanceof MethodInsnNode)
                {
                    final MethodInsnNode nodeAt = (MethodInsnNode) list;

                    if (nodeAt.name.equals("<init>") && nodeAt.owner.equals(getNameDynamic(KEY_CLASS_PLAYER_SP)))
                    {
                        method.instructions.set(nodeAt, new MethodInsnNode(Opcodes.INVOKESPECIAL, getName(KEY_CLASS_CUSTOM_PLAYER_SP), getName(KEY_METHOD_CUSTOM_PLAYER_SP), getDescDynamic(KEY_METHOD_CUSTOM_PLAYER_SP)));
                        injectionCount++;
                    }
                }
            }
        }

        return finishInjection(node);
	}

	public byte[] transformEntityLiving(String name, byte[] bytes)
	{
        ClassNode node = startInjection(bytes);

		operationCount = 1;

        MethodNode method = getMethod(node, KEY_METHOD_MOVE_ENTITY);

        if (method != null)
        {
            for (int count = 0; count < method.instructions.size(); count++)
            {
                final AbstractInsnNode list = method.instructions.get(count);

                if (list instanceof LdcInsnNode)
                {
                    final LdcInsnNode nodeAt = (LdcInsnNode) list;

                    if (nodeAt.cst.equals(Double.valueOf(0.08D)))
                    {
                        final VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
                        final MethodInsnNode overwriteNode = new MethodInsnNode(Opcodes.INVOKESTATIC, CLASS_WORLD_UTIL, "getGravityForEntity", "(L" + getNameDynamic(KEY_CLASS_ENTITY) + ";)D");

                        method.instructions.insertBefore(nodeAt, beforeNode);
                        method.instructions.set(nodeAt, overwriteNode);
                        injectionCount++;
                    }
                }
            }
        }

        return finishInjection(node);
	}

	public byte[] transformEntityItem(String name, byte[] bytes)
	{
        ClassNode node = startInjection(bytes);

		operationCount = 1;

        MethodNode method = getMethod(node, KEY_METHOD_ON_UPDATE);

        if (method != null)
        {
            for (int count = 0; count < method.instructions.size(); count++)
            {
                final AbstractInsnNode list = method.instructions.get(count);

                if (list instanceof LdcInsnNode)
                {
                    final LdcInsnNode nodeAt = (LdcInsnNode) list;

                    if (nodeAt.cst.equals(0.03999999910593033D))
                    {
                        final VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
                        final MethodInsnNode overwriteNode = new MethodInsnNode(Opcodes.INVOKESTATIC, CLASS_WORLD_UTIL, "getItemGravity", "(L" + getNameDynamic(KEY_CLASS_ENTITY_ITEM) + ";)D");

                        method.instructions.insertBefore(nodeAt, beforeNode);
                        method.instructions.set(nodeAt, overwriteNode);
                        injectionCount++;
                    }
                }
            }
        }

        return finishInjection(node);
	}

	public byte[] transformEntityRenderer(String name, byte[] bytes)
	{
        ClassNode node = startInjection(bytes);

		operationCount = 3;

        MethodNode updateLightMapMethod = getMethod(node, KEY_METHOD_UPDATE_LIGHTMAP);
        MethodNode updateFogColorMethod = getMethod(node, KEY_METHOD_UPDATE_FOG_COLOR);

        if (updateLightMapMethod != null)
        {
            for (int count = 0; count < updateLightMapMethod.instructions.size(); count++)
            {
                final AbstractInsnNode list = updateLightMapMethod.instructions.get(count);

                if (list instanceof IntInsnNode)
                {
                    final IntInsnNode nodeAt = (IntInsnNode) list;

                    if (nodeAt.operand == 255)
                    {
                        final InsnList nodesToAdd = new InsnList();

                        nodesToAdd.add(new VarInsnNode(Opcodes.FLOAD, 11));
                        nodesToAdd.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        nodesToAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CLASS_WORLD_UTIL, "getColorRed", "(L" + getNameDynamic(KEY_CLASS_WORLD) + ";)F"));
                        nodesToAdd.add(new InsnNode(Opcodes.FMUL));
                        nodesToAdd.add(new VarInsnNode(Opcodes.FSTORE, 11));

                        nodesToAdd.add(new VarInsnNode(Opcodes.FLOAD, 12));
                        nodesToAdd.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        nodesToAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CLASS_WORLD_UTIL, "getColorGreen", "(L" + getNameDynamic(KEY_CLASS_WORLD) + ";)F"));
                        nodesToAdd.add(new InsnNode(Opcodes.FMUL));
                        nodesToAdd.add(new VarInsnNode(Opcodes.FSTORE, 12));

                        nodesToAdd.add(new VarInsnNode(Opcodes.FLOAD, 13));
                        nodesToAdd.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        nodesToAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CLASS_WORLD_UTIL, "getColorBlue", "(L" + getNameDynamic(KEY_CLASS_WORLD) + ";)F"));
                        nodesToAdd.add(new InsnNode(Opcodes.FMUL));
                        nodesToAdd.add(new VarInsnNode(Opcodes.FSTORE, 13));

                        updateLightMapMethod.instructions.insertBefore(nodeAt, nodesToAdd);
                        injectionCount++;
                        break;
                    }
                }
            }
        }

        if (updateFogColorMethod != null)
        {
            for (int count = 0; count < updateFogColorMethod.instructions.size(); count++)
            {
                final AbstractInsnNode list = updateFogColorMethod.instructions.get(count);

                if (list instanceof MethodInsnNode)
                {
                    final MethodInsnNode nodeAt = (MethodInsnNode) list;

                    if (!this.optifinePresent && methodMatches(KEY_METHOD_GET_FOG_COLOR, nodeAt))
                    {
                        InsnList toAdd = new InsnList();

                        toAdd.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CLASS_WORLD_UTIL, "getFogColorHook", "(L" + getNameDynamic(KEY_CLASS_WORLD) + ";)L" + getNameDynamic(KEY_CLASS_VEC3) + ";"));
                        toAdd.add(new VarInsnNode(Opcodes.ASTORE, 9));

                        updateFogColorMethod.instructions.insertBefore(updateFogColorMethod.instructions.get(count + 2), toAdd);
                        injectionCount++;
                    }
                    else if (methodMatches(KEY_METHOD_GET_SKY_COLOR, nodeAt))
                    {
                        InsnList toAdd = new InsnList();

                        toAdd.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CLASS_WORLD_UTIL, "getSkyColorHook", "(L" + getNameDynamic(KEY_CLASS_WORLD) + ";)L" + getNameDynamic(KEY_CLASS_VEC3) + ";"));
                        toAdd.add(new VarInsnNode(Opcodes.ASTORE, 5));

                        updateFogColorMethod.instructions.insertBefore(updateFogColorMethod.instructions.get(count + 2), toAdd);
                        injectionCount++;
                    }
                }
            }
        }

        return finishInjection(node);
	}

	public byte[] transformGuiSleep(String name, byte[] bytes)
	{
        ClassNode node = startInjection(bytes);

		operationCount = 1;

        MethodNode method = getMethod(node, KEY_METHOD_WAKE_ENTITY);

        if (method != null)
        {
            method.instructions.insertBefore(method.instructions.get(method.instructions.size() - 3), new MethodInsnNode(Opcodes.INVOKESTATIC, CLASS_MICDOODLE_PLUGIN, "onSleepCancelled", "()V"));
            injectionCount++;
        }

        return finishInjection(node);
	}

	public byte[] transformForgeHooks(String name, byte[] bytes)
	{
        ClassNode node = startInjection(bytes);

		operationCount = 1;

        MethodNode method = getMethod(node, KEY_METHOD_ORIENT_CAMERA);

        if (method != null)
        {
            method.instructions.insertBefore(method.instructions.get(0), new MethodInsnNode(Opcodes.INVOKESTATIC, CLASS_MICDOODLE_PLUGIN, "orientCamera", "()V"));
            injectionCount++;
        }

        return finishInjection(node);
	}

	@SuppressWarnings("unchecked")
	public byte[] transformCustomAnnotations(String name, byte[] bytes)
	{
        ClassNode node = startInjection(bytes);

        operationCount = 0;
		injectionCount = 0;

		final Iterator<MethodNode> methods = node.methods.iterator();
		List<String> ignoredMods = new ArrayList<String>();

		while (methods.hasNext())
		{
			MethodNode methodnode = methods.next();

			methodLabel: if (methodnode.visibleAnnotations != null && methodnode.visibleAnnotations.size() > 0)
			{
				final Iterator<AnnotationNode> annotations = methodnode.visibleAnnotations.iterator();

				while (annotations.hasNext())
				{
					AnnotationNode annotation = annotations.next();

					if (annotation.desc.equals("L" + CLASS_RUNTIME_INTERFACE + ";"))
					{
						List<String> desiredInterfaces = new ArrayList<String>();
						String modID = "";

						for (int i = 0; i < annotation.values.size(); i++)
						{
							Object value = annotation.values.get(i);

							if (value.equals("clazz"))
							{
								desiredInterfaces.add(String.valueOf(annotation.values.get(i + 1)));
							}
							else if (value.equals("modID"))
							{
								modID = String.valueOf(annotation.values.get(i + 1));
							}
							else if (value.equals("altClasses"))
							{
								desiredInterfaces.addAll((ArrayList<String>) annotation.values.get(i + 1));
							}
						}

						if (!ignoredMods.contains(modID))
						{
							boolean modFound = Loader.isModLoaded(modID);

							if (modFound)
							{
								for (String inter : desiredInterfaces)
								{
									try
									{
										Class.forName(inter);
									}
									catch (ClassNotFoundException e)
									{
                                        printLog("Galacticraft ignored missing interface \"" + inter + "\" from mod \"" + modID + "\".");
										continue;
									}

									inter = inter.replace(".", "/");

									if (!node.interfaces.contains(inter))
									{
                                        printLog("Galacticraft added interface \"" + inter + "\" dynamically from \"" + modID + "\" to class \"" + node.name + "\".");
										node.interfaces.add(inter);
										injectionCount++;
									}

									break;
								}
							}
							else
							{
								ignoredMods.add(modID);
                                printLog("Galacticraft ignored dynamic interface insertion since \"" + modID + "\" was not found.");
							}
						}

						break methodLabel;
					}
				}
			}
		}

        if (injectionCount > 0)
        {
            printLog("Galacticraft successfully injected bytecode into: " + node.name + " (" + injectionCount + ")");
        }

        return finishInjection(node, false);
	}

	public byte[] transformEffectRenderer(String name, byte[] bytes)
	{
        ClassNode node = startInjection(bytes);

		operationCount = 1;

        MethodNode renderParticlesMethod = getMethod(node, KEY_METHOD_RENDER_PARTICLES);

        if (renderParticlesMethod != null)
        {
            InsnList toAdd = new InsnList();
            toAdd.add(new VarInsnNode(Opcodes.FLOAD, 2));
            toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CLASS_CLIENT_PROXY_MAIN, "renderFootprints", "(F)V"));
            renderParticlesMethod.instructions.insert(renderParticlesMethod.instructions.get(0), toAdd);
            injectionCount++;
        }

        return finishInjection(node);
	}

	public byte[] transformItemRenderer(String name, byte[] bytes)
	{
		ClassNode node = startInjection(bytes);

		operationCount = 1;

        MethodNode renderOverlaysMethod = getMethod(node, KEY_METHOD_RENDER_OVERLAYS);

        if (renderOverlaysMethod != null)
        {
            instructionLoop:
            for (int count = 0; count < renderOverlaysMethod.instructions.size(); count++)
            {
                final AbstractInsnNode glEnable = renderOverlaysMethod.instructions.get(count);

                if (glEnable instanceof MethodInsnNode && ((MethodInsnNode) glEnable).name.equals("glEnable"))
                {
                    InsnList toAdd = new InsnList();

                    toAdd.add(new VarInsnNode(Opcodes.FLOAD, 1));
                    toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CLASS_CLIENT_PROXY_MAIN, "renderLiquidOverlays", "(F)V"));

                    renderOverlaysMethod.instructions.insertBefore(glEnable, toAdd);
                    injectionCount++;
                    break instructionLoop;
                }
            }
        }

        return finishInjection(node);
	}


    public byte[] transformNetHandlerPlay(String name, byte[] bytes)
    {
		ClassNode node = startInjection(bytes);

		operationCount = 2;

        MethodNode handleNamedSpawnMethod = getMethod(node, KEY_METHOD_HANDLE_SPAWN_PLAYER);

        if (handleNamedSpawnMethod != null)
        {
            for (int count = 0; count < handleNamedSpawnMethod.instructions.size(); count++)
            {
                final AbstractInsnNode list = handleNamedSpawnMethod.instructions.get(count);

                if (list instanceof TypeInsnNode)
                {
                    final TypeInsnNode nodeAt = (TypeInsnNode) list;

                    if (nodeAt.desc.contains(getNameDynamic(KEY_CLASS_ENTITY_OTHER_PLAYER)))
                    {
                        final TypeInsnNode overwriteNode = new TypeInsnNode(Opcodes.NEW, getNameDynamic(KEY_CLASS_CUSTOM_OTHER_PLAYER));

                        handleNamedSpawnMethod.instructions.set(nodeAt, overwriteNode);
                        injectionCount++;
                    }
                }
                else if (list instanceof MethodInsnNode)
                {
                    final MethodInsnNode nodeAt = (MethodInsnNode) list;

                    if (nodeAt.name.equals("<init>") && nodeAt.owner.equals(getNameDynamic(KEY_CLASS_ENTITY_OTHER_PLAYER)))
                    {
                    	handleNamedSpawnMethod.instructions.set(nodeAt, new MethodInsnNode(Opcodes.INVOKESPECIAL, getNameDynamic(KEY_CLASS_CUSTOM_OTHER_PLAYER), "<init>", "(L" + getNameDynamic(KEY_CLASS_WORLD) + ";L" + getNameDynamic(KEY_CLASS_GAME_PROFILE) + ";)V"));
                        injectionCount++;
                    }
                }
            }
        }

        return finishInjection(node);
    }

    private class ObfuscationEntry
    {
        public String name;
        public String obfuscatedName;

        public ObfuscationEntry(String name, String obfuscatedName)
        {
            this.name = name;
            this.obfuscatedName = obfuscatedName;
        }
        
        public ObfuscationEntry(String commonName)
        {
        	this(commonName, commonName);
        }
    }

    private class MethodObfuscationEntry extends ObfuscationEntry
    {
        public String methodDesc;

        public MethodObfuscationEntry(String name, String obfuscatedName, String methodDesc)
        {
            super(name, obfuscatedName);
            this.methodDesc = methodDesc;
        }
        
        public MethodObfuscationEntry(String commonName, String methodDesc)
        {
        	this(commonName, commonName, methodDesc);
        }
    }

    private class FieldObfuscationEntry extends ObfuscationEntry
    {
        public FieldObfuscationEntry(String name, String obfuscatedName)
        {
            super(name, obfuscatedName);
        }
    }

    private void printResultsAndReset(String nodeName)
    {
        if (injectionCount >= operationCount)
        {
            printLog("Galacticraft successfully injected bytecode into: " + nodeName + " (" + injectionCount + " / " + operationCount + ")");
        }
        else
        {
            System.err.println("Galacticraft successfully injected bytecode into: " + nodeName + " (" + injectionCount + " / " + operationCount + ")");
        }
    }

    private MethodNode getMethod(ClassNode node, String keyName)
    {
        Iterator<MethodNode> methods = node.methods.iterator();

        while (methods.hasNext())
        {
            MethodNode methodNode = methods.next();

            if (methodMatches(keyName, methodNode))
            {
                return methodNode;
            }
        }

        return null;
    }

    private boolean methodMatches(String keyName, MethodInsnNode node)
    {
        return node.name.equals(getNameDynamic(keyName)) && node.desc.equals(getDescDynamic(keyName));
    }

    private boolean methodMatches(String keyName, MethodNode node)
    {
        return node.name.equals(getNameDynamic(keyName)) && node.desc.equals(getDescDynamic(keyName));
    }

    public String getName(String keyName)
    {
        return this.nodemap.get(keyName).name;
    }

    public String getObfName(String keyName)
    {
        return this.nodemap.get(keyName).obfuscatedName;
    }

    public String getNameDynamic(String keyName)
    {
        try
        {
            if (this.deobfuscated)
            {
                return this.nodemap.get(keyName).name;
            }
            else
            {
                return this.nodemap.get(keyName).obfuscatedName;
            }
        }
        catch (NullPointerException e)
        {
            System.err.println("Could not find key: " + keyName);
            throw e;
        }
    }

    public String getDescDynamic(String keyName)
    {
        return ((MethodObfuscationEntry) this.nodemap.get(keyName)).methodDesc;
    }

    private boolean classPathMatches(String keyName, String className)
    {
        return className.replace('.', '/').equals(getNameDynamic(keyName));
    }

    private void printLog(String message)
    {
        // TODO: Add custom log file
        System.out.println(message);
    }

    private ClassNode startInjection(byte[] bytes)
    {
        final ClassNode node = new ClassNode();
        final ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);
        injectionCount = 0;
        operationCount = 0;
        return node;
    }

    private byte[] finishInjection(ClassNode node)
    {
        return finishInjection(node, true);
    }

    private byte[] finishInjection(ClassNode node, boolean printLog)
    {
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);

        if (printLog)
        {
            printResultsAndReset(node.name);
        }

        return writer.toByteArray();
    }
}
