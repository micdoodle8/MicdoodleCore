package micdoodle8.mods.miccore;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import cpw.mods.fml.common.versioning.VersionParser;
import cpw.mods.fml.relauncher.FMLInjectionData;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MicdoodleTransformer implements net.minecraft.launchwrapper.IClassTransformer
{
	HashMap<String, ObfuscationEntry> nodemap = new HashMap<String, ObfuscationEntry>();
	private boolean deobfuscated = true;
	private boolean optifinePresent;
    private DefaultArtifactVersion mcVersion;

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
	private static final String KEY_CLASS_WORLD_RENDERER = "worldRendererClass";
	private static final String KEY_CLASS_RENDER_GLOBAL = "renderGlobalClass";
	private static final String KEY_CLASS_RENDER_MANAGER = "renderManagerClass";
	private static final String KEY_CLASS_TESSELLATOR = "tessellatorClass";
	private static final String KEY_CLASS_TILEENTITY_RENDERER = "tileEntityRendererClass";
	private static final String KEY_CLASS_CONTAINER_PLAYER = "containerPlayer";
	private static final String KEY_CLASS_MINECRAFT = "minecraft";
	private static final String KEY_CLASS_SESSION = "session";
	private static final String KEY_CLASS_GUI_SCREEN = "guiScreen";
	private static final String KEY_CLASS_ITEM_RENDERER = "itemRendererClass";
	private static final String KEY_CLASS_VEC3 = "vecClass";
	private static final String KEY_CLASS_ENTITY = "entityClass";
	private static final String KEY_CLASS_TILEENTITY = "tileEntityClass";
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
	private static final String KEY_CLASS_WORLD_CLIENT = "worldClient";
    private static final String KEY_CLASS_MUSIC_TICKER = "musicTicker";
    private static final String KEY_NET_HANDLER_LOGIN_SERVER = "netHandlerLoginServer";

	private static final String KEY_FIELD_THE_PLAYER = "thePlayer";
	private static final String KEY_FIELD_WORLDRENDERER_GLRENDERLIST = "glRenderList";

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
	private static final String KEY_METHOD_BED_ORIENT_CAMERA = "orientBedCamera";
	private static final String KEY_METHOD_RENDER_PARTICLES = "renderParticlesMethod";
	private static final String KEY_METHOD_CUSTOM_PLAYER_MP = "customPlayerMPConstructor";
	private static final String KEY_METHOD_CUSTOM_PLAYER_SP = "customPlayerSPConstructor";
	private static final String KEY_METHOD_ATTEMPT_LOGIN_BUKKIT = "attemptLoginMethodBukkit";
	private static final String KEY_METHOD_HANDLE_SPAWN_PLAYER = "handleSpawnPlayerMethod";
	private static final String KEY_METHOD_ORIENT_CAMERA = "orientCamera";
	private static final String KEY_METHOD_RENDERMANAGER = "renderManagerMethod";
	private static final String KEY_METHOD_PRERENDER_BLOCKS = "preRenderBlocksMethod"; //WorldRenderer.preRenderBlocks(int)
	private static final String KEY_METHOD_SETUP_GL = "setupGLTranslationMethod"; //WorldRenderer.setupGLTranslation()
	private static final String KEY_METHOD_SET_POSITION = "setPositionMethod"; //WorldRenderer.setPosition()
	private static final String KEY_METHOD_WORLDRENDERER_UPDATERENDERER = "updateRendererMethod"; //WorldRenderer.updateRenderer()
	private static final String KEY_METHOD_LOAD_RENDERERS = "loadRenderersMethod"; //RenderGlobal.loadRenderers()
	private static final String KEY_METHOD_RENDERGLOBAL_INIT = "renderGlobalInitMethod"; //RenderGlobal.RenderGlobal()
	private static final String KEY_METHOD_RENDERGLOBAL_SORTANDRENDER = "sortAndRenderMethod"; //RenderGlobal.sortAndRender()
	private static final String KEY_METHOD_TESSELLATOR_ADDVERTEX = "addVertexMethod"; //Tessellator.addVertex()
	private static final String KEY_METHOD_TILERENDERER_RENDERTILEAT = "renderTileAtMethod"; //TileEntityRendererDispatcher.renderTileEntityAt()
	private static final String KEY_METHOD_START_GAME = "startGame";
	private static final String KEY_METHOD_CAN_RENDER_FIRE = "canRenderOnFire";

	private static final String CLASS_RUNTIME_INTERFACE = "micdoodle8/mods/miccore/Annotations$RuntimeInterface";
	private static final String CLASS_ALT_FORVERSION = "micdoodle8/mods/miccore/Annotations$AltForVersion";
	private static final String CLASS_VERSION_SPECIFIC = "micdoodle8/mods/miccore/Annotations$VersionSpecific";
	private static final String CLASS_MICDOODLE_PLUGIN = "micdoodle8/mods/miccore/MicdoodlePlugin";
	private static final String CLASS_CLIENT_PROXY_MAIN = "micdoodle8/mods/galacticraft/core/proxy/ClientProxyCore";
	private static final String CLASS_MUSIC_TICKER_GC = "micdoodle8/mods/galacticraft/core/client/sounds/MusicTickerGC";
	private static final String CLASS_WORLD_UTIL = "micdoodle8/mods/galacticraft/core/util/WorldUtil";
	private static final String CLASS_GL11 = "org/lwjgl/opengl/GL11";

	private static int operationCount = 0;
	private static int injectionCount = 0;

	public MicdoodleTransformer() {
        this.mcVersion = new DefaultArtifactVersion((String) FMLInjectionData.data()[4]);

        try {
            deobfuscated = Launch.classLoader.getClassBytes("net.minecraft.world.World") != null;
            optifinePresent = Launch.classLoader.getClassBytes("CustomColorizer") != null;
        } catch (final Exception e) {
            e.printStackTrace();
        }

        if (this.mcVersionMatches("[1.7.2]"))
        {
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_PLAYER_MP, new ObfuscationEntry("net/minecraft/entity/player/EntityPlayerMP", "mm"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_WORLD, new ObfuscationEntry("net/minecraft/world/World", "afn"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_CONF_MANAGER, new ObfuscationEntry("net/minecraft/server/management/ServerConfigurationManager", "ld"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_GAME_PROFILE, new ObfuscationEntry("com/mojang/authlib/GameProfile"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_ITEM_IN_WORLD_MANAGER, new ObfuscationEntry("net/minecraft/server/management/ItemInWorldManager", "mn"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_PLAYER_CONTROLLER, new ObfuscationEntry("net/minecraft/client/multiplayer/PlayerControllerMP", "biy"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_PLAYER_SP, new ObfuscationEntry("net/minecraft/client/entity/EntityClientPlayerMP", "bje"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_STAT_FILE_WRITER, new ObfuscationEntry("net/minecraft/stats/StatFileWriter", "oe"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_NET_HANDLER_PLAY, new ObfuscationEntry("net/minecraft/client/network/NetHandlerPlayClient", "biv"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_ENTITY_LIVING, new ObfuscationEntry("net/minecraft/entity/EntityLivingBase", "rh"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_ENTITY_ITEM, new ObfuscationEntry("net/minecraft/entity/item/EntityItem", "vw"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_ENTITY_RENDERER, new ObfuscationEntry("net/minecraft/client/renderer/EntityRenderer", "bll"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_WORLD_RENDERER, new ObfuscationEntry("net/minecraft/client/renderer/WorldRenderer", "blg"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_RENDER_GLOBAL, new ObfuscationEntry("net/minecraft/client/renderer/RenderGlobal", "bls"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_TESSELLATOR, new ObfuscationEntry("net/minecraft/client/renderer/Tessellator", "blz"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_RENDER_MANAGER, new ObfuscationEntry("net/minecraft/client/renderer/entity/RenderManager", "bnf"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_TILEENTITY_RENDERER, new ObfuscationEntry("net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher", "bmc"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_CONTAINER_PLAYER, new ObfuscationEntry("net/minecraft/inventory/ContainerPlayer", "zb"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_MINECRAFT, new ObfuscationEntry("net/minecraft/client/Minecraft", "azd"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_SESSION, new ObfuscationEntry("net/minecraft/util/Session", "baf"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_GUI_SCREEN, new ObfuscationEntry("net/minecraft/client/gui/GuiScreen", "bcd"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_ITEM_RENDERER, new ObfuscationEntry("net/minecraft/client/renderer/ItemRenderer", "blq"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_VEC3, new ObfuscationEntry("net/minecraft/util/Vec3", "ayk"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_ENTITY, new ObfuscationEntry("net/minecraft/entity/Entity", "qn"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_GUI_SLEEP, new ObfuscationEntry("net/minecraft/client/gui/GuiSleepMP", "bbp"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_EFFECT_RENDERER, new ObfuscationEntry("net/minecraft/client/particle/EffectRenderer", "bkg"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_FORGE_HOOKS_CLIENT, new ObfuscationEntry("net/minecraftforge/client/ForgeHooksClient"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_CUSTOM_PLAYER_MP, new ObfuscationEntry("micdoodle8/mods/galacticraft/core/entities/player/GCEntityPlayerMP"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_CUSTOM_PLAYER_SP, new ObfuscationEntry("micdoodle8/mods/galacticraft/core/entities/player/GCEntityClientPlayerMP"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_CUSTOM_OTHER_PLAYER, new ObfuscationEntry("micdoodle8/mods/galacticraft/core/entities/player/GCEntityOtherPlayerMP"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_PACKET_SPAWN_PLAYER, new ObfuscationEntry("net/minecraft/network/play/server/S0CPacketSpawnPlayer", "fs"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_ENTITY_OTHER_PLAYER, new ObfuscationEntry("net/minecraft/client/entity/EntityOtherPlayerMP", "bld"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_SERVER, new ObfuscationEntry("net/minecraft/server/MinecraftServer"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_WORLD_SERVER, new ObfuscationEntry("net/minecraft/world/WorldServer", "mj"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_WORLD_CLIENT, new ObfuscationEntry("net/minecraft/client/multiplayer/WorldClient", "biz"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_TILEENTITY, new ObfuscationEntry("net/minecraft/tileentity/TileEntity", "and"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_MUSIC_TICKER, new ObfuscationEntry("net/minecraft/client/audio/MusicTicker", "bst"));
            this.nodemap.put(MicdoodleTransformer.KEY_NET_HANDLER_LOGIN_SERVER, new ObfuscationEntry("net/minecraft/server/network/NetHandlerLoginServer", "nd"));

            this.nodemap.put(MicdoodleTransformer.KEY_FIELD_THE_PLAYER, new FieldObfuscationEntry("thePlayer", "h"));
            this.nodemap.put(MicdoodleTransformer.KEY_FIELD_WORLDRENDERER_GLRENDERLIST, new FieldObfuscationEntry("glRenderList", "z"));

            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_CREATE_PLAYER, new MethodObfuscationEntry("createPlayerForUser", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_GAME_PROFILE) + ";)L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_MP) + ";"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_RESPAWN_PLAYER, new MethodObfuscationEntry("respawnPlayer", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_MP) + ";IZ)L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_MP) + ";"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_CREATE_CLIENT_PLAYER, new MethodObfuscationEntry("func_147493_a", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_STAT_FILE_WRITER) + ";)L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_SP) + ";"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_MOVE_ENTITY, new MethodObfuscationEntry("moveEntityWithHeading", "e", "(FF)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_ON_UPDATE, new MethodObfuscationEntry("onUpdate", "h", "()V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_UPDATE_LIGHTMAP, new MethodObfuscationEntry("updateLightmap", "h", "(F)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_RENDER_OVERLAYS, new MethodObfuscationEntry("renderOverlays", "b", "(F)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_UPDATE_FOG_COLOR, new MethodObfuscationEntry("updateFogColor", "i", "(F)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_GET_FOG_COLOR, new MethodObfuscationEntry("getFogColor", "f", "(F)L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_VEC3) + ";"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_GET_SKY_COLOR, new MethodObfuscationEntry("getSkyColor", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY) + ";F)L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_VEC3) + ";"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_WAKE_ENTITY, new MethodObfuscationEntry("func_146418_g", "g", "()V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_BED_ORIENT_CAMERA, new MethodObfuscationEntry("orientBedCamera", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_MINECRAFT) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY_LIVING) + ";)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_RENDER_PARTICLES, new MethodObfuscationEntry("renderParticles", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY) + ";F)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_CUSTOM_PLAYER_MP, new MethodObfuscationEntry("<init>", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_SERVER) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD_SERVER) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_GAME_PROFILE) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ITEM_IN_WORLD_MANAGER) + ";)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_CUSTOM_PLAYER_SP, new MethodObfuscationEntry("<init>", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_MINECRAFT) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_SESSION) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_NET_HANDLER_PLAY) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_STAT_FILE_WRITER) + ";)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_HANDLE_SPAWN_PLAYER, new MethodObfuscationEntry("handleSpawnPlayer", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PACKET_SPAWN_PLAYER) + ";)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_ORIENT_CAMERA, new MethodObfuscationEntry("orientCamera", "g", "(F)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_RENDERMANAGER, new MethodObfuscationEntry("func_147939_a", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY) + ";DDDFFZ)Z"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_SETUP_GL, new MethodObfuscationEntry("setupGLTranslation", "f", "()V")); //func_78905_g
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_PRERENDER_BLOCKS, new MethodObfuscationEntry("preRenderBlocks", "b", "(I)V")); //func_147890_b
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_SET_POSITION, new MethodObfuscationEntry("setPosition", "a", "(III)V")); //func_78913_a
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_WORLDRENDERER_UPDATERENDERER, new MethodObfuscationEntry("updateRenderer", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY_LIVING) + ";)V")); //func_147892_a
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_LOAD_RENDERERS, new MethodObfuscationEntry("loadRenderers", "a", "()V")); //func_72712_a
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_RENDERGLOBAL_INIT, new MethodObfuscationEntry("<init>", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_MINECRAFT) + ";)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_RENDERGLOBAL_SORTANDRENDER, new MethodObfuscationEntry("sortAndRender", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY_LIVING) + ";ID)I")); //func_72719_a
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_TESSELLATOR_ADDVERTEX, new MethodObfuscationEntry("addVertex", "a", "(DDD)V")); //blz/a (DDD)V net/minecraft/client/renderer/Tessellator/func_78377_a (DDD)V
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_TILERENDERER_RENDERTILEAT, new MethodObfuscationEntry("renderTileEntityAt", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_TILEENTITY) + ";DDDF)V")); //bmc/a (Land;DDDF)V net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher/func_147549_a (Lnet/minecraft/tileentity/TileEntity;DDDF)V
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_START_GAME, new MethodObfuscationEntry("startGame", "Z", "()V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_CAN_RENDER_FIRE, new MethodObfuscationEntry("canRenderOnFire", "aA", "()Z"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_ATTEMPT_LOGIN_BUKKIT, new MethodObfuscationEntry("attemptLogin", "attemptLogin", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_NET_HANDLER_LOGIN_SERVER) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_GAME_PROFILE) + ";Ljava/lang/String;)L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_MP) + ";"));
        }
        else if (this.mcVersionMatches("[1.7.10]"))
        {
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_PLAYER_MP, new ObfuscationEntry("net/minecraft/entity/player/EntityPlayerMP", "mw"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_WORLD, new ObfuscationEntry("net/minecraft/world/World", "ahb"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_CONF_MANAGER, new ObfuscationEntry("net/minecraft/server/management/ServerConfigurationManager", "oi"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_GAME_PROFILE, new ObfuscationEntry("com/mojang/authlib/GameProfile"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_ITEM_IN_WORLD_MANAGER, new ObfuscationEntry("net/minecraft/server/management/ItemInWorldManager", "mx"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_PLAYER_CONTROLLER, new ObfuscationEntry("net/minecraft/client/multiplayer/PlayerControllerMP", "bje"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_PLAYER_SP, new ObfuscationEntry("net/minecraft/client/entity/EntityClientPlayerMP", "bjk"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_STAT_FILE_WRITER, new ObfuscationEntry("net/minecraft/stats/StatFileWriter", "pq"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_NET_HANDLER_PLAY, new ObfuscationEntry("net/minecraft/client/network/NetHandlerPlayClient", "bjb"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_ENTITY_LIVING, new ObfuscationEntry("net/minecraft/entity/EntityLivingBase", "sv"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_ENTITY_ITEM, new ObfuscationEntry("net/minecraft/entity/item/EntityItem", "xk"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_ENTITY_RENDERER, new ObfuscationEntry("net/minecraft/client/renderer/EntityRenderer", "blt"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_WORLD_RENDERER, new ObfuscationEntry("net/minecraft/client/renderer/WorldRenderer", "blo"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_RENDER_GLOBAL, new ObfuscationEntry("net/minecraft/client/renderer/RenderGlobal", "bma"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_TESSELLATOR, new ObfuscationEntry("net/minecraft/client/renderer/Tessellator", "bmh"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_RENDER_MANAGER, new ObfuscationEntry("net/minecraft/client/renderer/entity/RenderManager", "bnn"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_TILEENTITY_RENDERER, new ObfuscationEntry("net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher", "bmk"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_CONTAINER_PLAYER, new ObfuscationEntry("net/minecraft/inventory/ContainerPlayer", "aap"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_MINECRAFT, new ObfuscationEntry("net/minecraft/client/Minecraft", "bao"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_SESSION, new ObfuscationEntry("net/minecraft/util/Session", "bbs"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_GUI_SCREEN, new ObfuscationEntry("net/minecraft/client/gui/GuiScreen", "bdw"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_ITEM_RENDERER, new ObfuscationEntry("net/minecraft/client/renderer/ItemRenderer", "bly"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_VEC3, new ObfuscationEntry("net/minecraft/util/Vec3", "azw"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_ENTITY, new ObfuscationEntry("net/minecraft/entity/Entity", "sa"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_GUI_SLEEP, new ObfuscationEntry("net/minecraft/client/gui/GuiSleepMP", "bdi"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_EFFECT_RENDERER, new ObfuscationEntry("net/minecraft/client/particle/EffectRenderer", "bkn"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_FORGE_HOOKS_CLIENT, new ObfuscationEntry("net/minecraftforge/client/ForgeHooksClient"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_CUSTOM_PLAYER_MP, new ObfuscationEntry("micdoodle8/mods/galacticraft/core/entities/player/GCEntityPlayerMP"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_CUSTOM_PLAYER_SP, new ObfuscationEntry("micdoodle8/mods/galacticraft/core/entities/player/GCEntityClientPlayerMP"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_CUSTOM_OTHER_PLAYER, new ObfuscationEntry("micdoodle8/mods/galacticraft/core/entities/player/GCEntityOtherPlayerMP"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_PACKET_SPAWN_PLAYER, new ObfuscationEntry("net/minecraft/network/play/server/S0CPacketSpawnPlayer", "gb"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_ENTITY_OTHER_PLAYER, new ObfuscationEntry("net/minecraft/client/entity/EntityOtherPlayerMP", "bll"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_SERVER, new ObfuscationEntry("net/minecraft/server/MinecraftServer"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_WORLD_SERVER, new ObfuscationEntry("net/minecraft/world/WorldServer", "mt"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_WORLD_CLIENT, new ObfuscationEntry("net/minecraft/client/multiplayer/WorldClient", "bjf"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_TILEENTITY, new ObfuscationEntry("net/minecraft/tileentity/TileEntity", "aor"));
            this.nodemap.put(MicdoodleTransformer.KEY_CLASS_MUSIC_TICKER, new ObfuscationEntry("net/minecraft/client/audio/MusicTicker", "btg"));
            this.nodemap.put(MicdoodleTransformer.KEY_NET_HANDLER_LOGIN_SERVER, new ObfuscationEntry("net/minecraft/server/network/NetHandlerLoginServer", "nn"));

            this.nodemap.put(MicdoodleTransformer.KEY_FIELD_THE_PLAYER, new FieldObfuscationEntry("thePlayer", "h"));
            this.nodemap.put(MicdoodleTransformer.KEY_FIELD_WORLDRENDERER_GLRENDERLIST, new FieldObfuscationEntry("glRenderList", "z"));

            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_CREATE_PLAYER, new  MethodObfuscationEntry("createPlayerForUser", "f", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_GAME_PROFILE) + ";)L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_MP) + ";"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_RESPAWN_PLAYER, new MethodObfuscationEntry("respawnPlayer", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_MP) + ";IZ)L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_MP) + ";"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_CREATE_CLIENT_PLAYER, new MethodObfuscationEntry("func_147493_a", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_STAT_FILE_WRITER) + ";)L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_SP) + ";"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_MOVE_ENTITY, new MethodObfuscationEntry("moveEntityWithHeading", "e", "(FF)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_ON_UPDATE, new MethodObfuscationEntry("onUpdate", "h", "()V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_UPDATE_LIGHTMAP, new MethodObfuscationEntry("updateLightmap", "i", "(F)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_RENDER_OVERLAYS, new MethodObfuscationEntry("renderOverlays", "b", "(F)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_UPDATE_FOG_COLOR, new MethodObfuscationEntry("updateFogColor", "j", "(F)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_GET_FOG_COLOR, new MethodObfuscationEntry("getFogColor", "f", "(F)L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_VEC3) + ";"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_GET_SKY_COLOR, new MethodObfuscationEntry("getSkyColor", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY) + ";F)L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_VEC3) + ";"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_WAKE_ENTITY, new MethodObfuscationEntry("func_146418_g", "f", "()V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_BED_ORIENT_CAMERA, new MethodObfuscationEntry("orientBedCamera", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_MINECRAFT) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY_LIVING) + ";)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_RENDER_PARTICLES, new MethodObfuscationEntry("renderParticles", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY) + ";F)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_CUSTOM_PLAYER_MP, new MethodObfuscationEntry("<init>", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_SERVER) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD_SERVER) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_GAME_PROFILE) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ITEM_IN_WORLD_MANAGER) + ";)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_CUSTOM_PLAYER_SP, new MethodObfuscationEntry("<init>", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_MINECRAFT) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_SESSION) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_NET_HANDLER_PLAY) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_STAT_FILE_WRITER) + ";)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_HANDLE_SPAWN_PLAYER, new MethodObfuscationEntry("handleSpawnPlayer", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PACKET_SPAWN_PLAYER) + ";)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_ORIENT_CAMERA, new MethodObfuscationEntry("orientCamera", "h", "(F)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_RENDERMANAGER, new MethodObfuscationEntry("func_147939_a", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY) + ";DDDFFZ)Z"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_SETUP_GL, new MethodObfuscationEntry("setupGLTranslation", "f", "()V")); //func_78905_g
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_PRERENDER_BLOCKS, new MethodObfuscationEntry("preRenderBlocks", "b", "(I)V")); //func_147890_b
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_SET_POSITION, new MethodObfuscationEntry("setPosition", "a", "(III)V")); //func_78913_a
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_WORLDRENDERER_UPDATERENDERER, new MethodObfuscationEntry("updateRenderer", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY_LIVING) + ";)V")); //func_147892_a
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_LOAD_RENDERERS, new MethodObfuscationEntry("loadRenderers", "a", "()V")); //func_72712_a
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_RENDERGLOBAL_INIT, new MethodObfuscationEntry("<init>", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_MINECRAFT) + ";)V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_RENDERGLOBAL_SORTANDRENDER, new MethodObfuscationEntry("sortAndRender", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY_LIVING) + ";ID)I")); //func_72719_a
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_TESSELLATOR_ADDVERTEX, new MethodObfuscationEntry("addVertex", "a", "(DDD)V")); //blz/a (DDD)V net/minecraft/client/renderer/Tessellator/func_78377_a (DDD)V
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_TILERENDERER_RENDERTILEAT, new MethodObfuscationEntry("renderTileEntityAt", "a", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_TILEENTITY) + ";DDDF)V")); //bmc/a (Land;DDDF)V net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher/func_147549_a (Lnet/minecraft/tileentity/TileEntity;DDDF)V
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_START_GAME, new MethodObfuscationEntry("startGame", "ag", "()V"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_CAN_RENDER_FIRE, new MethodObfuscationEntry("canRenderOnFire", "aA", "()Z"));
            this.nodemap.put(MicdoodleTransformer.KEY_METHOD_ATTEMPT_LOGIN_BUKKIT, new MethodObfuscationEntry("attemptLogin", "attemptLogin", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_NET_HANDLER_LOGIN_SERVER) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_GAME_PROFILE) + ";Ljava/lang/String;)L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_MP) + ";"));
        }
    }

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		if (name.contains("galacticraft"))
		{
			bytes = this.transformCustomAnnotations(bytes);
		}
		else
		{
			String testName = name.replace('.', '/');
			if (this.deobfuscated)
			{
				this.transformVanillaDeobfuscated(testName, bytes);
			}
			else
			{
				this.transformVanillaObfuscated(testName, bytes);
			}
		}

		return bytes;
	}

	private void transformVanillaDeobfuscated(String testName, byte[] bytes)
	{
		if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_CONF_MANAGER)))
		{
			bytes = this.transformConfigManager(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_PLAYER_CONTROLLER)))
		{
			bytes = this.transformPlayerController(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_ENTITY_LIVING)))
		{
			bytes = this.transformEntityLiving(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_ENTITY_ITEM)))
		{
			bytes = this.transformEntityItem(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_ENTITY_RENDERER)))
		{
			bytes = this.transformEntityRenderer(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_ITEM_RENDERER)))
		{
			bytes = this.transformItemRenderer(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_GUI_SLEEP)))
		{
			bytes = this.transformGuiSleep(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_FORGE_HOOKS_CLIENT)))
		{
			bytes = this.transformForgeHooks(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_EFFECT_RENDERER)))
		{
			bytes = this.transformEffectRenderer(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_NET_HANDLER_PLAY)))
		{
			bytes = this.transformNetHandlerPlay(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_WORLD_RENDERER)))
		{
			bytes = this.transformWorldRenderer(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_RENDER_GLOBAL)))
		{
			bytes = this.transformRenderGlobal(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_RENDER_MANAGER)))
		{
			bytes = this.transformRenderManager(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_TILEENTITY_RENDERER)))
		{
			bytes = this.transformTileEntityRenderer(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_MINECRAFT)))
		{
			bytes = this.transformMinecraftClass(bytes);
		}
		else if (testName.equals(this.getName(MicdoodleTransformer.KEY_CLASS_ENTITY)))
		{
			bytes = this.transformEntityClass(bytes);
		}
	}

	private void transformVanillaObfuscated(String testName, byte[] bytes)
	{
		if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_CONF_MANAGER).obfuscatedName))
		{
			bytes = this.transformConfigManager(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_PLAYER_CONTROLLER).obfuscatedName))
		{
			bytes = this.transformPlayerController(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_ENTITY_LIVING).obfuscatedName))
		{
			bytes = this.transformEntityLiving(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_ENTITY_ITEM).obfuscatedName))
		{
			bytes = this.transformEntityItem(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_ENTITY_RENDERER).obfuscatedName))
		{
			bytes = this.transformEntityRenderer(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_ITEM_RENDERER).obfuscatedName))
		{
			bytes = this.transformItemRenderer(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_GUI_SLEEP).obfuscatedName))
		{
			bytes = this.transformGuiSleep(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_FORGE_HOOKS_CLIENT).obfuscatedName))
		{
			bytes = this.transformForgeHooks(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_EFFECT_RENDERER).obfuscatedName))
		{
			bytes = this.transformEffectRenderer(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_NET_HANDLER_PLAY).obfuscatedName))
		{
			bytes = this.transformNetHandlerPlay(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_WORLD_RENDERER).obfuscatedName))
		{
			bytes = this.transformWorldRenderer(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_RENDER_GLOBAL).obfuscatedName))
		{
			bytes = this.transformRenderGlobal(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_RENDER_MANAGER).obfuscatedName))
		{
			bytes = this.transformRenderManager(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_TILEENTITY_RENDERER).obfuscatedName))
		{
			bytes = this.transformTileEntityRenderer(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_MINECRAFT).obfuscatedName))
		{
			bytes = this.transformMinecraftClass(bytes);
		}
		else if (testName.equals(this.nodemap.get(MicdoodleTransformer.KEY_CLASS_ENTITY).obfuscatedName))
		{
			bytes = this.transformEntityClass(bytes);
		}
	}

	/**
	 * replaces EntityPlayerMP initialization with custom ones
	 */
	public byte[] transformConfigManager(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);

		MicdoodleTransformer.operationCount = 6;

		MethodNode createPlayerMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_CREATE_PLAYER);
		MethodNode respawnPlayerMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_RESPAWN_PLAYER);
		MethodNode attemptLoginMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_ATTEMPT_LOGIN_BUKKIT);

		if (createPlayerMethod != null)
		{
			for (int count = 0; count < createPlayerMethod.instructions.size(); count++)
			{
				final AbstractInsnNode list = createPlayerMethod.instructions.get(count);
				
				if (list instanceof TypeInsnNode)
				{
					final TypeInsnNode nodeAt = (TypeInsnNode) list;

					if (nodeAt.getOpcode() != Opcodes.CHECKCAST && nodeAt.desc.contains(this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_MP)))
					{
						final TypeInsnNode overwriteNode = new TypeInsnNode(Opcodes.NEW, this.getName(MicdoodleTransformer.KEY_CLASS_CUSTOM_PLAYER_MP));

						createPlayerMethod.instructions.set(nodeAt, overwriteNode);
						MicdoodleTransformer.injectionCount++;
					}
				}
				else if (list instanceof MethodInsnNode)
				{
					final MethodInsnNode nodeAt = (MethodInsnNode) list;

					if (nodeAt.owner.contains(this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_MP)) && nodeAt.getOpcode() == Opcodes.INVOKESPECIAL)
					{
						createPlayerMethod.instructions.set(nodeAt, new MethodInsnNode(Opcodes.INVOKESPECIAL, this.getName(MicdoodleTransformer.KEY_CLASS_CUSTOM_PLAYER_MP), this.getName(MicdoodleTransformer.KEY_METHOD_CUSTOM_PLAYER_MP), this.getDescDynamic(MicdoodleTransformer.KEY_METHOD_CUSTOM_PLAYER_MP)));
						MicdoodleTransformer.injectionCount++;
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

					if (nodeAt.getOpcode() != Opcodes.CHECKCAST && nodeAt.desc.contains(this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_MP)))
					{
						final TypeInsnNode overwriteNode = new TypeInsnNode(Opcodes.NEW, this.getName(MicdoodleTransformer.KEY_CLASS_CUSTOM_PLAYER_MP));

						respawnPlayerMethod.instructions.set(nodeAt, overwriteNode);
						MicdoodleTransformer.injectionCount++;
					}
				}
				else if (list instanceof MethodInsnNode)
				{
					final MethodInsnNode nodeAt = (MethodInsnNode) list;

					if (nodeAt.name.equals("<init>") && nodeAt.owner.equals(this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_MP)))
					{
						respawnPlayerMethod.instructions.set(nodeAt, new MethodInsnNode(Opcodes.INVOKESPECIAL, this.getName(MicdoodleTransformer.KEY_CLASS_CUSTOM_PLAYER_MP), this.getName(MicdoodleTransformer.KEY_METHOD_CUSTOM_PLAYER_MP), this.getDescDynamic(MicdoodleTransformer.KEY_METHOD_CUSTOM_PLAYER_MP)));

						MicdoodleTransformer.injectionCount++;
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

					if (nodeAt.getOpcode() == Opcodes.NEW && nodeAt.desc.contains(this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_MP)))
					{
						final TypeInsnNode overwriteNode = new TypeInsnNode(Opcodes.NEW, this.getName(MicdoodleTransformer.KEY_CLASS_CUSTOM_PLAYER_MP));

						attemptLoginMethod.instructions.set(nodeAt, overwriteNode);
						MicdoodleTransformer.injectionCount++;
					}
				}
				else if (list instanceof MethodInsnNode)
				{
					final MethodInsnNode nodeAt = (MethodInsnNode) list;

					if (nodeAt.getOpcode() == Opcodes.INVOKESPECIAL && nodeAt.name.equals("<init>") && nodeAt.owner.equals(this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_MP)))
					{
                        String initDesc = "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_SERVER) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD_SERVER) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_GAME_PROFILE) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ITEM_IN_WORLD_MANAGER) + ";)V";
						attemptLoginMethod.instructions.set(nodeAt, new MethodInsnNode(Opcodes.INVOKESPECIAL, this.getName(MicdoodleTransformer.KEY_CLASS_CUSTOM_PLAYER_MP), this.getName(MicdoodleTransformer.KEY_METHOD_CUSTOM_PLAYER_MP), initDesc));

						MicdoodleTransformer.injectionCount++;
					}
				}
			}
		}

		return this.finishInjection(node);
	}

	public byte[] transformPlayerController(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);

		MicdoodleTransformer.operationCount = 2;

		MethodNode method = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_CREATE_CLIENT_PLAYER);

		if (method != null)
		{
			for (int count = 0; count < method.instructions.size(); count++)
			{
				final AbstractInsnNode list = method.instructions.get(count);

				if (list instanceof TypeInsnNode)
				{
					final TypeInsnNode nodeAt = (TypeInsnNode) list;

					if (nodeAt.desc.contains(this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_SP)))
					{
						final TypeInsnNode overwriteNode = new TypeInsnNode(Opcodes.NEW, this.getName(MicdoodleTransformer.KEY_CLASS_CUSTOM_PLAYER_SP));

						method.instructions.set(nodeAt, overwriteNode);
						MicdoodleTransformer.injectionCount++;
					}
				}
				else if (list instanceof MethodInsnNode)
				{
					final MethodInsnNode nodeAt = (MethodInsnNode) list;

					if (nodeAt.name.equals("<init>") && nodeAt.owner.equals(this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_PLAYER_SP)))
					{
						method.instructions.set(nodeAt, new MethodInsnNode(Opcodes.INVOKESPECIAL, this.getName(MicdoodleTransformer.KEY_CLASS_CUSTOM_PLAYER_SP), this.getName(MicdoodleTransformer.KEY_METHOD_CUSTOM_PLAYER_SP), this.getDescDynamic(MicdoodleTransformer.KEY_METHOD_CUSTOM_PLAYER_SP)));
						MicdoodleTransformer.injectionCount++;
					}
				}
			}
		}

		return this.finishInjection(node);
	}

	public byte[] transformEntityLiving(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);

		MicdoodleTransformer.operationCount = 1;

		MethodNode method = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_MOVE_ENTITY);

		if (method != null)
		{
			for (int count = 0; count < method.instructions.size(); count++)
			{
				final AbstractInsnNode list = method.instructions.get(count);

				if (list instanceof LdcInsnNode)
				{
					final LdcInsnNode nodeAt = (LdcInsnNode) list;

					if (nodeAt.cst.equals(0.08D))
					{
						final VarInsnNode beforeNode = new VarInsnNode(Opcodes.ALOAD, 0);
						final MethodInsnNode overwriteNode = new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_WORLD_UTIL, "getGravityForEntity", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY) + ";)D");

						method.instructions.insertBefore(nodeAt, beforeNode);
						method.instructions.set(nodeAt, overwriteNode);
						MicdoodleTransformer.injectionCount++;
					}
				}
			}
		}

		return this.finishInjection(node);
	}

	public byte[] transformEntityItem(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);

		MicdoodleTransformer.operationCount = 1;

		MethodNode method = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_ON_UPDATE);

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
						final MethodInsnNode overwriteNode = new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_WORLD_UTIL, "getItemGravity", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY_ITEM) + ";)D");

						method.instructions.insertBefore(nodeAt, beforeNode);
						method.instructions.set(nodeAt, overwriteNode);
						MicdoodleTransformer.injectionCount++;
					}
				}
			}
		}

		return this.finishInjection(node);
	}

	public byte[] transformEntityRenderer(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);

		MicdoodleTransformer.operationCount = 5;
		if (this.optifinePresent)
		{
			MicdoodleTransformer.operationCount--;
		}

		MethodNode updateLightMapMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_UPDATE_LIGHTMAP);
		MethodNode updateFogColorMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_UPDATE_FOG_COLOR);
		MethodNode orientCameraMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_ORIENT_CAMERA);

		if (orientCameraMethod != null)
		{
			final InsnList nodesToAdd = new InsnList();

			nodesToAdd.add(new VarInsnNode(Opcodes.FLOAD, 1));
			nodesToAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_CLIENT_PROXY_MAIN, "orientCamera", "(F)V"));
			orientCameraMethod.instructions.insertBefore(orientCameraMethod.instructions.get(orientCameraMethod.instructions.size() - 3), nodesToAdd);
			MicdoodleTransformer.injectionCount++;
			if (ConfigManagerMicCore.enableDebug) System.out.println("bll.OrientCamera done");
		}

		if (updateLightMapMethod != null)
		{
			boolean worldBrightnessInjection = false;

			for (int count = 0; count < updateLightMapMethod.instructions.size(); count++)
			{
				final AbstractInsnNode list = updateLightMapMethod.instructions.get(count);

				if (list instanceof MethodInsnNode)
				{
					MethodInsnNode nodeAt = (MethodInsnNode) list;

					//Original code:  float f1 = worldclient.getSunBrightness(1.0F) * 0.95F + 0.05F;
					if (!worldBrightnessInjection && nodeAt.owner.equals(this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD_CLIENT)))
					{
						updateLightMapMethod.instructions.remove(updateLightMapMethod.instructions.get(count - 1));
						updateLightMapMethod.instructions.remove(updateLightMapMethod.instructions.get(count - 1));
						updateLightMapMethod.instructions.insertBefore(updateLightMapMethod.instructions.get(count - 1), new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_WORLD_UTIL, "getWorldBrightness", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD_CLIENT) + ";)F"));
						MicdoodleTransformer.injectionCount++;
						worldBrightnessInjection = true;
						if (ConfigManagerMicCore.enableDebug) System.out.println("bll.updateLightMap - worldBrightness done");
						continue;
					}
				}

				if (list instanceof IntInsnNode)
				{
					final IntInsnNode nodeAt = (IntInsnNode) list;

					if (nodeAt.operand == 255)
					{
						final InsnList nodesToAdd = new InsnList();

						nodesToAdd.add(new VarInsnNode(Opcodes.FLOAD, 11));
						nodesToAdd.add(new VarInsnNode(Opcodes.ALOAD, 2));
						nodesToAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_WORLD_UTIL, "getColorRed", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD) + ";)F"));
						nodesToAdd.add(new InsnNode(Opcodes.FMUL));
						nodesToAdd.add(new VarInsnNode(Opcodes.FSTORE, 11));

						nodesToAdd.add(new VarInsnNode(Opcodes.FLOAD, 12));
						nodesToAdd.add(new VarInsnNode(Opcodes.ALOAD, 2));
						nodesToAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_WORLD_UTIL, "getColorGreen", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD) + ";)F"));
						nodesToAdd.add(new InsnNode(Opcodes.FMUL));
						nodesToAdd.add(new VarInsnNode(Opcodes.FSTORE, 12));

						nodesToAdd.add(new VarInsnNode(Opcodes.FLOAD, 13));
						nodesToAdd.add(new VarInsnNode(Opcodes.ALOAD, 2));
						nodesToAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_WORLD_UTIL, "getColorBlue", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD) + ";)F"));
						nodesToAdd.add(new InsnNode(Opcodes.FMUL));
						nodesToAdd.add(new VarInsnNode(Opcodes.FSTORE, 13));

						updateLightMapMethod.instructions.insertBefore(nodeAt, nodesToAdd);
						MicdoodleTransformer.injectionCount++;
						if (ConfigManagerMicCore.enableDebug) System.out.println("bll.updateLightMap - getColors done");
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

					if (!this.optifinePresent && this.methodMatches(MicdoodleTransformer.KEY_METHOD_GET_FOG_COLOR, nodeAt))
					{
						InsnList toAdd = new InsnList();

						toAdd.add(new VarInsnNode(Opcodes.ALOAD, 2));
						toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_WORLD_UTIL, "getFogColorHook", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD) + ";)L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_VEC3) + ";"));
						toAdd.add(new VarInsnNode(Opcodes.ASTORE, 9));

						updateFogColorMethod.instructions.insertBefore(updateFogColorMethod.instructions.get(count + 2), toAdd);
						MicdoodleTransformer.injectionCount++;
						if (ConfigManagerMicCore.enableDebug) System.out.println("bll.updateFogColor - getFogColor (no Optifine) done");
					}
					else if (this.methodMatches(MicdoodleTransformer.KEY_METHOD_GET_SKY_COLOR, nodeAt))
					{
						InsnList toAdd = new InsnList();

						toAdd.add(new VarInsnNode(Opcodes.ALOAD, 2));
						toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_WORLD_UTIL, "getSkyColorHook", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD) + ";)L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_VEC3) + ";"));
						toAdd.add(new VarInsnNode(Opcodes.ASTORE, 5));

						updateFogColorMethod.instructions.insertBefore(updateFogColorMethod.instructions.get(count + 2), toAdd);
						MicdoodleTransformer.injectionCount++;
						if (ConfigManagerMicCore.enableDebug) System.out.println("bll.updateFogColor - getSkyColor done");
					}
				}
			}
		}

		return this.finishInjection(node);
	}

	public byte[] transformGuiSleep(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);

		MicdoodleTransformer.operationCount = 1;

		MethodNode method = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_WAKE_ENTITY);

		if (method != null)
		{
			method.instructions.insertBefore(method.instructions.get(method.instructions.size() - 3), new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_MICDOODLE_PLUGIN, "onSleepCancelled", "()V"));
			MicdoodleTransformer.injectionCount++;
		}

		return this.finishInjection(node);
	}

	public byte[] transformForgeHooks(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);

		MicdoodleTransformer.operationCount = 1;

		MethodNode method = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_BED_ORIENT_CAMERA);

		if (method != null)
		{
			method.instructions.insertBefore(method.instructions.get(0), new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_MICDOODLE_PLUGIN, "orientCamera", "()V"));
			MicdoodleTransformer.injectionCount++;
		}

		return this.finishInjection(node);
	}

	@SuppressWarnings("unchecked")
	public byte[] transformCustomAnnotations(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);

		MicdoodleTransformer.operationCount = 0;
		MicdoodleTransformer.injectionCount = 0;

		final Iterator<MethodNode> methods = node.methods.iterator();
		List<String> ignoredMods = new ArrayList<String>();

		while (methods.hasNext())
		{
			MethodNode methodnode = methods.next();

			methodLabel:
			if (methodnode.visibleAnnotations != null && methodnode.visibleAnnotations.size() > 0)
			{
				for (AnnotationNode annotation : methodnode.visibleAnnotations)
				{
                    if (annotation.desc.equals("L" + MicdoodleTransformer.CLASS_VERSION_SPECIFIC + ";"))
                    {
                        String toMatch = null;

                        for (int i = 0; i < annotation.values.size(); i+=2)
                        {
                            if ("version".equals(annotation.values.get(i)))
                            {
                                toMatch = String.valueOf(annotation.values.get(i + 1));
                            }
                        }

                        if (toMatch != null)
                        {
                            boolean doRemove = true;
                            if (mcVersionMatches(toMatch))
                            {
                            	doRemove = false;
                            }

                            if (doRemove)
                            {
	                            methods.remove();
	                            break methodLabel;
                            }
                        }
                    }

                    if (annotation.desc.equals("L" + MicdoodleTransformer.CLASS_ALT_FORVERSION + ";"))
                    {
                        String toMatch = null;

                        for (int i = 0; i < annotation.values.size(); i+=2)
                        {
                            if ("version".equals(annotation.values.get(i)))
                            {
                                toMatch = String.valueOf(annotation.values.get(i + 1));
                            }
                        }

                        if (toMatch != null)
                        {
                            if (mcVersionMatches(toMatch))
                            {
                            	String existing = new String(methodnode.name);
                            	existing = existing.substring(0, existing.length() - 1);
                            	if (ConfigManagerMicCore.enableDebug) this.printLog("Renaming method "+existing+" for version "+toMatch);
                            	methodnode.name = new String(existing);
                            	break;
                            }
                        }
                    }

					if (annotation.desc.equals("L" + MicdoodleTransformer.CLASS_RUNTIME_INTERFACE + ";"))
					{
						List<String> desiredInterfaces = new ArrayList<String>();
						String modID = "";

						for (int i = 0; i < annotation.values.size(); i+=2)
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
										if (ConfigManagerMicCore.enableDebug) this.printLog("Galacticraft ignored missing interface \"" + inter + "\" from mod \"" + modID + "\".");
										continue;
									}

									inter = inter.replace(".", "/");

									if (!node.interfaces.contains(inter))
									{
										if (ConfigManagerMicCore.enableDebug) this.printLog("Galacticraft added interface \"" + inter + "\" dynamically from \"" + modID + "\" to class \"" + node.name + "\".");
										node.interfaces.add(inter);
										MicdoodleTransformer.injectionCount++;
									}

									break;
								}
							}
							else
							{
								ignoredMods.add(modID);
								if (ConfigManagerMicCore.enableDebug) this.printLog("Galacticraft ignored dynamic interface insertion since \"" + modID + "\" was not found.");
							}
						}

						break methodLabel;
					}
				}
			}
		}

		if (MicdoodleTransformer.injectionCount > 0)
		{
			if (ConfigManagerMicCore.enableDebug) this.printLog("Galacticraft successfully injected bytecode into: " + node.name + " (" + MicdoodleTransformer.injectionCount + ")");
		}

		return this.finishInjection(node, false);
	}

	public byte[] transformEffectRenderer(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);

		MicdoodleTransformer.operationCount = 1;

		MethodNode renderParticlesMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_RENDER_PARTICLES);

		if (renderParticlesMethod != null)
		{
			InsnList toAdd = new InsnList();
			toAdd.add(new VarInsnNode(Opcodes.FLOAD, 2));
			toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_CLIENT_PROXY_MAIN, "renderFootprints", "(F)V"));
			renderParticlesMethod.instructions.insert(renderParticlesMethod.instructions.get(0), toAdd);
			MicdoodleTransformer.injectionCount++;
		}

		return this.finishInjection(node);
	}

	public byte[] transformItemRenderer(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);

		MicdoodleTransformer.operationCount = 1;

		MethodNode renderOverlaysMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_RENDER_OVERLAYS);

		if (renderOverlaysMethod != null)
		{
			for (int count = 0; count < renderOverlaysMethod.instructions.size(); count++)
			{
				final AbstractInsnNode glEnable = renderOverlaysMethod.instructions.get(count);

				if (glEnable instanceof MethodInsnNode && ((MethodInsnNode) glEnable).name.equals("glEnable"))
				{
					InsnList toAdd = new InsnList();

					toAdd.add(new VarInsnNode(Opcodes.FLOAD, 1));
					toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_CLIENT_PROXY_MAIN, "renderLiquidOverlays", "(F)V"));

					renderOverlaysMethod.instructions.insertBefore(glEnable, toAdd);
					MicdoodleTransformer.injectionCount++;
					break;
				}
			}
		}

		return this.finishInjection(node);
	}

	public byte[] transformNetHandlerPlay(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);

		MicdoodleTransformer.operationCount = 2;

		MethodNode handleNamedSpawnMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_HANDLE_SPAWN_PLAYER);

		if (handleNamedSpawnMethod != null)
		{
			for (int count = 0; count < handleNamedSpawnMethod.instructions.size(); count++)
			{
				final AbstractInsnNode list = handleNamedSpawnMethod.instructions.get(count);

				if (list instanceof TypeInsnNode)
				{
					final TypeInsnNode nodeAt = (TypeInsnNode) list;

					if (nodeAt.desc.contains(this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY_OTHER_PLAYER)))
					{
						final TypeInsnNode overwriteNode = new TypeInsnNode(Opcodes.NEW, this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_CUSTOM_OTHER_PLAYER));

						handleNamedSpawnMethod.instructions.set(nodeAt, overwriteNode);
						MicdoodleTransformer.injectionCount++;
					}
				}
				else if (list instanceof MethodInsnNode)
				{
					final MethodInsnNode nodeAt = (MethodInsnNode) list;

					if (nodeAt.name.equals("<init>") && nodeAt.owner.equals(this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY_OTHER_PLAYER)))
					{
						handleNamedSpawnMethod.instructions.set(nodeAt, new MethodInsnNode(Opcodes.INVOKESPECIAL, this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_CUSTOM_OTHER_PLAYER), "<init>", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD) + ";L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_GAME_PROFILE) + ";)V"));
						MicdoodleTransformer.injectionCount++;
					}
				}
			}
		}

		return this.finishInjection(node);
	}

	public byte[] transformWorldRenderer(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);
		Boolean smallMoonsEnabled = this.getSmallMoonsEnabled();

		MicdoodleTransformer.operationCount = smallMoonsEnabled ? 2 : 0;
		//if (optifinePresent) operationCount = 2;

		if (smallMoonsEnabled)
		{
			MethodNode setPositionMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_SET_POSITION);

			if (setPositionMethod != null)
			{
				for (int count = 0; count < setPositionMethod.instructions.size(); count++)
				{
					final AbstractInsnNode nodeTest = setPositionMethod.instructions.get(count);

					if (nodeTest instanceof InsnNode && nodeTest.getOpcode() == Opcodes.RETURN)
					{
						//Insert at end: a call to ClientProxyCore.setPositionList(this, this.glRenderList)
						//  aload_0
						//  aload_0
						//  getfield blg/z I
						//  invokestatic ClientProxyCore.setPositionList()
						InsnList toAdd = new InsnList();
						toAdd.add(new VarInsnNode(Opcodes.ALOAD, 0));
						toAdd.add(new VarInsnNode(Opcodes.ALOAD, 0));
						toAdd.add(new FieldInsnNode(Opcodes.GETFIELD, this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD_RENDERER), this.getNameDynamic(MicdoodleTransformer.KEY_FIELD_WORLDRENDERER_GLRENDERLIST), "I"));
						toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_CLIENT_PROXY_MAIN, "setPositionList", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD_RENDERER) + ";I)V"));
						setPositionMethod.instructions.insertBefore(nodeTest, toAdd);
						MicdoodleTransformer.injectionCount++;
						if (ConfigManagerMicCore.enableDebug) System.out.println("blg.setPosition - done");
						break;
					}
				}
			}

			//Insert at start of method:  GL11.glCallList(rend.glRenderList + 3);
			MethodNode setupGLMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_SETUP_GL);
			if (setupGLMethod != null)
			{
				InsnList toAdd = new InsnList();
				//Insert  GL11.glCallList(rend.glRenderList + 3);   before GL11.glTranslatef();
				//  aload_0
				//  getfield blg/z I
				//  iconst_3
				//  iadd
				//  invokestatic org/lwjgl/opengl/GL11/glCallList(I)V
				toAdd.add(new VarInsnNode(Opcodes.ALOAD, 0));
				toAdd.add(new FieldInsnNode(Opcodes.GETFIELD, this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_WORLD_RENDERER), this.getNameDynamic(MicdoodleTransformer.KEY_FIELD_WORLDRENDERER_GLRENDERLIST), "I"));
				toAdd.add(new InsnNode(Opcodes.ICONST_3));
				toAdd.add(new InsnNode(Opcodes.IADD));
				toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_GL11, "glCallList", "(I)V"));
				setupGLMethod.instructions.insertBefore(setupGLMethod.instructions.get(0), toAdd);
				MicdoodleTransformer.injectionCount++;
				if (ConfigManagerMicCore.enableDebug) System.out.println("blg.setupGLMethod - done");
			}

			/*		MethodNode updateRMethod = getMethod(node, KEY_METHOD_WORLDRENDERER_UPDATERENDERER);
					if (updateRMethod != null && !optifinePresent)
			        {
			            for (int count = 0; count < updateRMethod.instructions.size(); count++)
			            {
			                final AbstractInsnNode nodeTest = updateRMethod.instructions.get(count);

			                //Looking for:  invokespecial blg/b(I)V
			                //This corresponds to:  this.preRenderBlocks(k2);   (line 183 in the source code)
			                if (nodeTest instanceof MethodInsnNode && nodeTest.getOpcode() == Opcodes.INVOKESPECIAL)
			                {
			                	MethodInsnNode methodTest = (MethodInsnNode) nodeTest;

			                	if (methodTest.owner.equals(getNameDynamic(KEY_CLASS_WORLD_RENDERER)) && methodTest.desc.equals("(I)V") && methodTest.name.equals(getNameDynamic(KEY_METHOD_PRERENDER_BLOCKS)))
			                	{
			                		InsnList setLastY = new InsnList();

			                		//reset ClientProxyCore.lastY
			                		setLastY.add(new LdcInsnNode(-1));
			                		setLastY.add(new FieldInsnNode(Opcodes.PUTSTATIC, CLASS_CLIENT_PROXY_MAIN, "lastY", "I"));

			                        updateRMethod.instructions.insert(nodeTest, setLastY);
			                		injectionCount++;
			                        if (ConfigManagerMicCore.enableDebug) System.out.println("blg.updateRenderer - first done");
			                	}
			                }

			                //Looking for istore 25
			                //This corresponds to: int k3 = block.getRenderBlockPass();  (line 196 in the source code)
			                if (nodeTest instanceof VarInsnNode && nodeTest.getOpcode() == Opcodes.ISTORE && ((VarInsnNode)nodeTest).var == 25)
			                {
			            		//insert a call to scaleBlock(l2)   //l2 is the block's y value
			            		InsnList callScaleBlock = new InsnList();
			            		callScaleBlock.add(new VarInsnNode(Opcodes.ILOAD, 21));
			            		callScaleBlock.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CLASS_CLIENT_PROXY_MAIN, "scaleBlock", "(I)V"));

			                    updateRMethod.instructions.insert(nodeTest, callScaleBlock);
			            		injectionCount++;
			                    if (ConfigManagerMicCore.enableDebug) System.out.println("blg.updateRenderer - second done");
			            		break;
			                }
			            }
			        }
					*/
		}
		return this.finishInjection(node);
	}

	public byte[] transformRenderGlobal(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);
		Boolean smallMoonsEnabled = this.getSmallMoonsEnabled();

		MicdoodleTransformer.operationCount = smallMoonsEnabled ? 5 : 0;

		if (smallMoonsEnabled)
		{
			MethodNode initMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_RENDERGLOBAL_INIT);

			//Looking for   iconst_3   imul
			//Replace the 3 with a 4
			if (initMethod != null)
			{
				for (int count = 0; count < initMethod.instructions.size(); count++)
				{
					final AbstractInsnNode nodeTest = initMethod.instructions.get(count);
					final AbstractInsnNode nodeTestb = initMethod.instructions.get(count + 1);

					if (nodeTest instanceof InsnNode && nodeTestb instanceof InsnNode)
					{
						if (nodeTest.getOpcode() == Opcodes.ICONST_3 && nodeTestb.getOpcode() == Opcodes.IMUL)
						{
							final InsnNode overwriteNode = new InsnNode(Opcodes.ICONST_4);

							initMethod.instructions.set(nodeTest, overwriteNode);
							MicdoodleTransformer.injectionCount++;
							if (ConfigManagerMicCore.enableDebug) System.out.println("bls.init - done");
							break;
						}
					}
				}
			}

			MethodNode loadMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_LOAD_RENDERERS);

			//Looking for   iinc 2 3  - this is j += 3; at line 418
			//Change this to iinc 2 4  - this makes space for one additional glRenderList per WorldRenderer
			if (loadMethod != null)
			{
				for (int count = 0; count < loadMethod.instructions.size(); count++)
				{
					final AbstractInsnNode nodeTest = loadMethod.instructions.get(count);

					if (nodeTest instanceof IincInsnNode)
					{
						final IincInsnNode nodeAt = (IincInsnNode) nodeTest;

						if (nodeAt.var == 2 && nodeAt.incr == 3 && !this.optifinePresent)
						{
							final IincInsnNode overwriteNode = new IincInsnNode(2, 4);

							loadMethod.instructions.set(nodeAt, overwriteNode);
							MicdoodleTransformer.injectionCount++;
							if (ConfigManagerMicCore.enableDebug) System.out.println("bls.loadRenderers (no Optifine) done");
							break;
						}
						//Optifine 1.7.2 special - same code, different variable id for j
						if (nodeAt.var == 6 && nodeAt.incr == 3 && this.optifinePresent)
						{
							final IincInsnNode overwriteNode = new IincInsnNode(6, 4);

							loadMethod.instructions.set(nodeAt, overwriteNode);
							MicdoodleTransformer.injectionCount++;
							if (ConfigManagerMicCore.enableDebug) System.out.println("bls.loadRenderers (Optifine present) done");
							break;
						}
					}
				}
			}

			MethodNode renderMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_RENDERGLOBAL_SORTANDRENDER);

			//Insertions at start and a GL11.glPopMatrix() at end
			if (renderMethod != null)
			{
				InsnList toAdd = new InsnList();
				toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_CLIENT_PROXY_MAIN, "adjustRenderCamera", "()V"));
				renderMethod.instructions.insertBefore(renderMethod.instructions.get(0), toAdd);
				MicdoodleTransformer.injectionCount++;

				MethodInsnNode toAdd2 = new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_GL11, "glPopMatrix", "()V");
				renderMethod.instructions.insertBefore(renderMethod.instructions.get(renderMethod.instructions.size() - 3), toAdd2);
				MicdoodleTransformer.injectionCount++;

				if (ConfigManagerMicCore.enableDebug) System.out.println("bls.sortAndRender - both done");

				int pos1 = 0; //putfield bls/ac I
				int pos2 = 0; //putfield bls/k I
				int pos3 = 0; //invokespecial bls/c(III)V

				String fieldRenderersSkippingRenderPass = this.deobfuscated ? "renderersSkippingRenderPass" : "ac";
				String fieldPrevChunkSortZ = this.deobfuscated ? "prevChunkSortZ" : "k";
				String methodMarkRenderersForNewPosition = this.deobfuscated ? "markRenderersForNewPosition" : "c";

				for (int count = 0; count < renderMethod.instructions.size(); count++)
				{
					final AbstractInsnNode nodeTest = renderMethod.instructions.get(count);

					if (nodeTest instanceof FieldInsnNode && nodeTest.getOpcode() == Opcodes.PUTFIELD && ((FieldInsnNode) nodeTest).name.equals(fieldRenderersSkippingRenderPass) && ((FieldInsnNode) nodeTest).desc.equals("I"))
					{
						pos1 = count;
						continue;
					}

					if (nodeTest instanceof FieldInsnNode && nodeTest.getOpcode() == Opcodes.PUTFIELD && ((FieldInsnNode) nodeTest).name.equals(fieldPrevChunkSortZ) && ((FieldInsnNode) nodeTest).desc.equals("I"))
					{
						pos2 = count;
						continue;
					}

					if (nodeTest instanceof MethodInsnNode && nodeTest.getOpcode() == Opcodes.INVOKESPECIAL && ((MethodInsnNode) nodeTest).name.equals(methodMarkRenderersForNewPosition) && ((MethodInsnNode) nodeTest).desc.equals("(III)V"))
					{
						pos3 = count;
					}
				}

				//Change the order: moving the following line to before the if() statement at line 728
				//this.markRenderersForNewPosition(MathHelper.floor_double(par1EntityLivingBase.posX), MathHelper.floor_double(par1EntityLivingBase.posY), MathHelper.floor_double(par1EntityLivingBase.posZ));
				if (pos1 > 0 && pos2 > 0 && pos3 > 0)
				{
					AbstractInsnNode[] instructionArray = renderMethod.instructions.toArray();
					renderMethod.instructions.clear();
					int count = 0;
					while (count <= pos1)
					{
						renderMethod.instructions.add(instructionArray[count++]);
					}
					count = pos2 + 1;
					while (count <= pos3)
					{
						renderMethod.instructions.add(instructionArray[count++]);
					}
					count = pos1 + 1;
					while (count <= pos2)
					{
						renderMethod.instructions.add(instructionArray[count++]);
					}
					count = pos3 + 1;
					while (count < instructionArray.length)
					{
						renderMethod.instructions.add(instructionArray[count++]);
					}
					MicdoodleTransformer.injectionCount++;
				}
				else
				{
					System.out.println("[GC] Warning: Unable to modify bytecode for bls.markRenderersForNewPosition");
				}
			}
		}

		return this.finishInjection(node);
	}

	public byte[] transformRenderManager(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);
		Boolean smallMoonsEnabled = this.getSmallMoonsEnabled();

		MicdoodleTransformer.operationCount = smallMoonsEnabled ? 2 : 0;

		if (smallMoonsEnabled)
		{
			MethodNode method = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_RENDERMANAGER);

			//Looking for   render.doRender(p_147939_1_, p_147939_2_, p_147939_4_, p_147939_6_, p_147939_8_, p_147939_9_);
			//that's:  aload 11, aload_1 etc
			if (method != null)
			{
				int count = 0;
				while (count < method.instructions.size())
				{
					final AbstractInsnNode nodeTest = method.instructions.get(count);
					final AbstractInsnNode nodeTestb = method.instructions.get(count + 1);

					if (nodeTest instanceof VarInsnNode && nodeTestb instanceof VarInsnNode)
					{
						if (nodeTest.getOpcode() == Opcodes.ALOAD && nodeTestb.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) nodeTest).var == 11 && ((VarInsnNode) nodeTestb).var == 1)
						{
							InsnList toAdd = new InsnList();
							//aload_1  dload_2  dload 4  dload 6
							toAdd.add(new VarInsnNode(Opcodes.ALOAD, 1));
							toAdd.add(new VarInsnNode(Opcodes.DLOAD, 2));
							toAdd.add(new VarInsnNode(Opcodes.DLOAD, 4));
							toAdd.add(new VarInsnNode(Opcodes.DLOAD, 6));
							toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_CLIENT_PROXY_MAIN, "adjustRenderPos", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY) + ";DDD)V"));

							method.instructions.insertBefore(nodeTest, toAdd);
							MicdoodleTransformer.injectionCount++;
							break;
						}
					}
					count++;
				}

				//Looking for: getstatic bnf/p Z
				for (int i = count; i < method.instructions.size(); i++)
				{
					final AbstractInsnNode nodeTest = method.instructions.get(i);

					if (nodeTest instanceof FieldInsnNode && nodeTest.getOpcode() == Opcodes.GETSTATIC)
					{
						FieldInsnNode f = (FieldInsnNode) nodeTest;
						if (f.owner.equals(this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_RENDER_MANAGER)) && f.desc.equals("Z")) //&& f.name.equals("p")
						{
							MethodInsnNode toAdd = new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_GL11, "glPopMatrix", "()V");
							method.instructions.insertBefore(nodeTest, toAdd);
							MicdoodleTransformer.injectionCount++;
							if (ConfigManagerMicCore.enableDebug) System.out.println("bnf - done2/2");
							break;
						}
					}
				}
			}
		}

		return this.finishInjection(node);
	}

	public byte[] transformTileEntityRenderer(byte[] bytes)
	{
		ClassNode node = this.startInjection(bytes);
		Boolean smallMoonsEnabled = this.getSmallMoonsEnabled();

		MicdoodleTransformer.operationCount = smallMoonsEnabled ? 2 : 0;

		if (smallMoonsEnabled)
		{
			MethodNode renderMethod = this.getMethod(node, MicdoodleTransformer.KEY_METHOD_TILERENDERER_RENDERTILEAT);

			if (renderMethod != null)
			{
				InsnList toAdd = new InsnList();
				toAdd.add(new VarInsnNode(Opcodes.ALOAD, 1));
				toAdd.add(new VarInsnNode(Opcodes.DLOAD, 2));
				toAdd.add(new VarInsnNode(Opcodes.DLOAD, 4));
				toAdd.add(new VarInsnNode(Opcodes.DLOAD, 6));
				toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_CLIENT_PROXY_MAIN, "adjustTileRenderPos", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_TILEENTITY) + ";DDD)V"));
				renderMethod.instructions.insert(toAdd);
				MicdoodleTransformer.injectionCount++;

                AbstractInsnNode returnNode = renderMethod.instructions.get(renderMethod.instructions.size() - 1);
                for (int i = 0; i < renderMethod.instructions.size(); i++)
                {
                    AbstractInsnNode insnAt = renderMethod.instructions.get(i);
                   if (insnAt.getOpcode() == Opcodes.RETURN)
                   {
                       returnNode = insnAt;
                       break;
                   }
                }

				MethodInsnNode toAdd2 = new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_GL11, "glPopMatrix", "()V");
				renderMethod.instructions.insertBefore(returnNode, toAdd2);
				MicdoodleTransformer.injectionCount++;
			}

		}

		return this.finishInjection(node);
	}

    public byte[] transformEntityClass(byte[] bytes)
    {
        ClassNode node = this.startInjection(bytes);

        MicdoodleTransformer.operationCount = 1;

        MethodNode method = this.getMethod(node, KEY_METHOD_CAN_RENDER_FIRE);

        if (method != null)
        {
            for (int i = 0; i < method.instructions.size(); i++)
            {
                AbstractInsnNode nodeAt = method.instructions.get(i);

                if (nodeAt instanceof MethodInsnNode && nodeAt.getOpcode() == Opcodes.INVOKEVIRTUAL)
                {
                    MethodInsnNode overwriteNode = new MethodInsnNode(Opcodes.INVOKESTATIC, MicdoodleTransformer.CLASS_WORLD_UTIL, "shouldRenderFire", "(L" + this.getNameDynamic(MicdoodleTransformer.KEY_CLASS_ENTITY) + ";)Z");

                    method.instructions.set(nodeAt, overwriteNode);
                    MicdoodleTransformer.injectionCount++;
                }
            }
        }

        return this.finishInjection(node);
    }

    public byte[] transformMinecraftClass(byte[] bytes)
    {
        ClassNode node = this.startInjection(bytes);

//        MicdoodleTransformer.operationCount = 2;
//
//        MethodNode init = this.getMethod(node, KEY_METHOD_START_GAME);
//
//        if (init != null)
//        {
//            for (int i = 0; i < init.instructions.size(); i++)
//            {
//                AbstractInsnNode insnAt = init.instructions.get(i);
//
//                if (insnAt instanceof TypeInsnNode && insnAt.getOpcode() == Opcodes.NEW && ((TypeInsnNode) insnAt).desc.equals(this.getNameDynamic(KEY_CLASS_MUSIC_TICKER)))
//                {
//                    ((TypeInsnNode) insnAt).desc = CLASS_MUSIC_TICKER_GC;
//                    MicdoodleTransformer.injectionCount++;
//                }
//
//                if (insnAt instanceof MethodInsnNode && insnAt.getOpcode() == Opcodes.INVOKESPECIAL && ((MethodInsnNode) insnAt).owner.equals(this.getNameDynamic(KEY_CLASS_MUSIC_TICKER)))
//                {
//                    ((MethodInsnNode) insnAt).owner = CLASS_MUSIC_TICKER_GC;
//                    MicdoodleTransformer.injectionCount++;
//                }
//            }
//        } TODO

        return this.finishInjection(node);
    }

	/*    public byte[] transformTessellator(byte[] bytes)
	    {
			ClassNode node = startInjection(bytes);

			operationCount = 1;

			MethodNode vMethod = getMethod(node, KEY_METHOD_TESSELLATOR_ADDVERTEX);


			/*
			 * public void addVertex(double x, double y, double z)
			 * {
			 * 		double var7 = 1 + ((y % 16) + offsetY) / globalRadius;
			 * 		x += ((x % 16 - 8) * var7 ) + 8;
			 * 		z += ((z % 16 - 8) * var7 ) + 8;
			 *		...
			 * }
			 **
	        if (vMethod != null)
	        {
	            InsnList toAdd = new InsnList();
	            toAdd.add(new InsnNode(Opcodes.DCONST_1));		//dconst_1
	            toAdd.add(new VarInsnNode(Opcodes.DLOAD, 3));	//dload_3
	            toAdd.add(new LdcInsnNode(16.0D));				//ldc2_w 16.0
	            toAdd.add(new InsnNode(Opcodes.DREM));			//drem
	            toAdd.add(new FieldInsnNode(Opcodes.GETSTATIC, CLASS_CLIENT_PROXY_MAIN, "offsetY", "D"));	//getstatic micdoodle8/mods/galacticraft/core/proxy/ClientProxyCore/offsetY D
	            toAdd.add(new InsnNode(Opcodes.DADD));	//dadd
	            toAdd.add(new FieldInsnNode(Opcodes.GETSTATIC, CLASS_CLIENT_PROXY_MAIN, "globalRadius", "F"));	//getstatic micdoodle8/mods/galacticraft/core/proxy/ClientProxyCore/globalRadius F
	            toAdd.add(new InsnNode(Opcodes.F2D));			//f2d
	            toAdd.add(new InsnNode(Opcodes.DDIV));			//ddiv
	            toAdd.add(new InsnNode(Opcodes.DADD));			//dadd
	            toAdd.add(new VarInsnNode(Opcodes.DSTORE, 7));	//dstore 7
	            toAdd.add(new VarInsnNode(Opcodes.DLOAD, 1));	//dload_1
	            toAdd.add(new VarInsnNode(Opcodes.DLOAD, 1));	//dload_1
	            toAdd.add(new LdcInsnNode(16.0D));				//ldc2_w 16.0
	            toAdd.add(new InsnNode(Opcodes.DREM));			//drem
	            toAdd.add(new LdcInsnNode(8.0D));				//ldc2_w 8.0
	            toAdd.add(new InsnNode(Opcodes.DSUB));			//dsub
	            toAdd.add(new VarInsnNode(Opcodes.DLOAD, 7));	//dload 7
	            toAdd.add(new InsnNode(Opcodes.DMUL));			//dmul
	            toAdd.add(new LdcInsnNode(8.0D));				//ldc2_w 8.0
	            toAdd.add(new InsnNode(Opcodes.DADD));			//dadd
	            toAdd.add(new InsnNode(Opcodes.DADD));			//dadd
	            toAdd.add(new VarInsnNode(Opcodes.DSTORE, 1));	//dstore_1
	            toAdd.add(new VarInsnNode(Opcodes.DLOAD, 5));	//dload 5
	            toAdd.add(new VarInsnNode(Opcodes.DLOAD, 5));	//dload 5
	            toAdd.add(new LdcInsnNode(16.0D));				//ldc2_w 16.0
	            toAdd.add(new InsnNode(Opcodes.DREM));			//drem
	            toAdd.add(new LdcInsnNode(8.0D));				//ldc2_w 8.0
	            toAdd.add(new InsnNode(Opcodes.DSUB));			//dsub
	            toAdd.add(new VarInsnNode(Opcodes.DLOAD, 7));	//dload 7
	            toAdd.add(new InsnNode(Opcodes.DMUL));			//dmul
	            toAdd.add(new LdcInsnNode(8.0D));				//ldc2_w 8.0
	            toAdd.add(new InsnNode(Opcodes.DADD));			//dadd
	            toAdd.add(new InsnNode(Opcodes.DADD));			//dadd
	            toAdd.add(new VarInsnNode(Opcodes.DSTORE, 5));	//dstore 5
	            vMethod.instructions.insert(toAdd);
	            injectionCount++;
	        }

	        return finishInjection(node);
	    }
	*/
    public static class ObfuscationEntry
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

	public static class MethodObfuscationEntry extends ObfuscationEntry
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

    public static class FieldObfuscationEntry extends ObfuscationEntry
	{
		public FieldObfuscationEntry(String name, String obfuscatedName)
		{
			super(name, obfuscatedName);
		}
	}

	private void printResultsAndReset(String nodeName)
	{
		if (MicdoodleTransformer.operationCount > 0)
		{
			if (MicdoodleTransformer.injectionCount >= MicdoodleTransformer.operationCount)
			{
				this.printLog("Galacticraft successfully injected bytecode into: " + nodeName + " (" + MicdoodleTransformer.injectionCount + " / " + MicdoodleTransformer.operationCount + ")");
			}
			else
			{
				System.err.println("Potential problem: Galacticraft did not complete injection of bytecode into: " + nodeName + " (" + MicdoodleTransformer.injectionCount + " / " + MicdoodleTransformer.operationCount + ")");
			}
		}
	}

	private MethodNode getMethod(ClassNode node, String keyName)
	{
		for (MethodNode methodNode : node.methods)
		{
			if (this.methodMatches(keyName, methodNode))
			{
				return methodNode;
			}
		}

		return null;
	}

	private boolean methodMatches(String keyName, MethodInsnNode node)
	{
		return node.name.equals(this.getNameDynamic(keyName)) && node.desc.equals(this.getDescDynamic(keyName));
	}

	private boolean methodMatches(String keyName, MethodNode node)
	{
		return node.name.equals(this.getNameDynamic(keyName)) && node.desc.equals(this.getDescDynamic(keyName));
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
				return this.getName(keyName);
			}
			else
			{
				return this.getObfName(keyName);
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
		return className.replace('.', '/').equals(this.getNameDynamic(keyName));
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
		MicdoodleTransformer.injectionCount = 0;
		MicdoodleTransformer.operationCount = 0;
		return node;
	}

	private byte[] finishInjection(ClassNode node)
	{
		return this.finishInjection(node, true);
	}

	private byte[] finishInjection(ClassNode node, boolean printToLog)
	{
		final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);

		if (printToLog)
		{
			this.printResultsAndReset(node.name);
		}

		return writer.toByteArray();
	}

	private boolean getSmallMoonsEnabled()
	{
        return false;
//		return ConfigManagerMicCore.enableSmallMoons;
	}

    private boolean mcVersionMatches(String testVersion)
    {
        return VersionParser.parseRange(testVersion).containsVersion(this.mcVersion);
    }
}
