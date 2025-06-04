package Shoot;

import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent; // 引入ProjectileHitEvent用于处理火箭弹命中事件
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent; // 用于检测物品切换的事件
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.particle.ParticleEffect;
import com.connorlinfoot.titleapi.TitleAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Shoot 插件主类
 */
public class Shoot extends JavaPlugin implements Listener {

    // 存储玩家每把武器的弹药数量
    private Map<Player, Map<String, Integer>> ammoMap = new HashMap<>();

    // 存储玩家每把武器的射击计数
    private Map<Player, Map<String, Integer>> shootCountMap = new HashMap<>();

    // 存储玩家每把武器的换弹状态
    private Map<Player, Map<String, Boolean>> isReloadingMap = new HashMap<>();

    // 存储玩家上一次左键点击的时间（用于双击检测）
    private Map<Player, Long> lastLeftClickMap = new HashMap<>();

    // 存储玩家的显示设置
    private Map<Player, DisplaySettings> displaySettingsMap = new HashMap<>();

    // 存储玩家当前手持的武器ID
    private Map<Player, String> currentGunMap = new HashMap<>();
    // 存储玩家的快速传送次数
    private Map<Player, Integer> teleportCountMap = new HashMap<>();

    // 存储玩家每把武器的冷却状态
    private Map<Player, Map<String, Boolean>> isCooldownMap = new HashMap<>();

    // 存储由插件发射的火焰弹及其对应的玩家
    private Map<Fireball, Player> fireballMap = new HashMap<>();

    private FileConfiguration config;
    // 新增配置文件
    private File playerDataFile;
    private File buyFile;
    private File locFile;
    private FileConfiguration playerDataConfig;
    private FileConfiguration buyConfig;
    private FileConfiguration locConfig;

    @Override
    public void onEnable() {
        // 注册事件监听器和加载配置文件
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig(); // 保存默认配置文件（如果不存在）
        config = getConfig(); // 获取配置文件
        getLogger().info(ChatColor.GREEN + "Shoot 插件已成功加载！");
// 初始化快速传送次数
        for (Player p : Bukkit.getOnlinePlayers()) {
            teleportCountMap.put(p, 0);
        }
        // 初始化新的配置文件
        initializeConfigs();
    }

    /**
     * 初始化玩家数据、购买配置和位置配置文件
     */
    private void initializeConfigs() {
        // 初始化playerdata.yml
        playerDataFile = new File(getDataFolder(), "playerdata.yml");
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            saveResource("playerdata.yml", false);
        }
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);

        // 初始化Buy.yml
        buyFile = new File(getDataFolder(), "Buy.yml");
        if (!buyFile.exists()) {
            buyFile.getParentFile().mkdirs();
            saveResource("Buy.yml", false);
        }
        buyConfig = YamlConfiguration.loadConfiguration(buyFile);

        // 初始化loc.yml
        locFile = new File(getDataFolder(), "loc.yml");
        if (!locFile.exists()) {
            locFile.getParentFile().mkdirs();
            saveResource("loc.yml", false);
        }
        locConfig = YamlConfiguration.loadConfiguration(locFile);
    }

    /**
     * 保存所有配置文件
     */
    private void saveAllConfigs() {
        try {
            playerDataConfig.save(playerDataFile);
            buyConfig.save(buyFile);
            locConfig.save(locFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDisable() {
        // 插件禁用时的清理工作
        getLogger().info(ChatColor.RED + "Shoot 插件已卸载！");
        // 保存所有配置文件
        saveAllConfigs();
    }

    /**
     * 处理插件相关的指令
     *
     * @param sender  发送指令的执行者
     * @param command 执行的指令
     * @param label   指令标签
     * @param args    指令参数
     * @return 是否成功处理指令
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 仅允许玩家执行指令
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以执行该指令！");
            return true;
        }

        Player player = (Player) sender;

        // 检查指令是否为 /shoot
        if (label.equalsIgnoreCase("shoot")) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.YELLOW + "使用: /shoot help " + ChatColor.WHITE + "查看指令帮助！");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "help":
                    // 显示帮助信息
                    player.sendMessage(ChatColor.AQUA + "====== " + ChatColor.GREEN + "Shoot 插件帮助" + ChatColor.AQUA + " ======");
                    player.sendMessage(ChatColor.YELLOW + "/shoot help" + ChatColor.WHITE + " - 查看帮助信息");
                    player.sendMessage(ChatColor.YELLOW + "/shoot get <id>" + ChatColor.WHITE + " - 获取一把枪");
                    player.sendMessage(ChatColor.YELLOW + "/shoot up" + ChatColor.WHITE + " - 补充当前武器的子弹");
                    player.sendMessage(ChatColor.YELLOW + "/shoot display <start|hit> <on|off>" + ChatColor.WHITE + " - 控制标题显示");
                    player.sendMessage(ChatColor.YELLOW + "/shoot reload" + ChatColor.WHITE + " - 重新加载配置文件");
                    player.sendMessage(ChatColor.AQUA + "=======================================");
                    break;

                case "get":
                    // 获取一把指定ID的武器
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "请指定武器ID！如: /shoot get id1");
                        return true;
                    }

                    String id = args[1];
                    if (config.contains("guns." + id)) {
                        // 从配置中加载武器信息
                        String name = config.getString("guns." + id + ".name", "未知武器");
                        String materialName = config.getString("guns." + id + ".material", "WOOD_SPADE");
                        Material material = Material.getMaterial(materialName);
                        if (material == null) {
                            material = Material.WOOD_SPADE; // 默认材质
                        }

                        // 创建武器物品
                        ItemStack gun = new ItemStack(material);
                        ItemMeta meta = gun.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(ChatColor.GOLD + name); // 设置武器名称
                            gun.setItemMeta(meta); // 应用修改后的元数据
                        }

                        // 将武器添加到玩家的物品栏
                        player.getInventory().addItem(gun);
                        // 初始化弹药数量
                        ammoMap.putIfAbsent(player, new HashMap<>());
                        ammoMap.get(player).put(id, config.getInt("guns." + id + ".ammo", 10));
                        // 初始化射击次数
                        shootCountMap.putIfAbsent(player, new HashMap<>());
                        shootCountMap.get(player).put(id, 0);
                        // 初始化换弹状态
                        isReloadingMap.putIfAbsent(player, new HashMap<>());
                        isReloadingMap.get(player).put(id, false);
                        // 初始化冷却状态
                        isCooldownMap.putIfAbsent(player, new HashMap<>());
                        isCooldownMap.get(player).put(id, false);
                        // 初始化显示设置
                        displaySettingsMap.put(player, new DisplaySettings(true, true));
                        player.sendMessage(ChatColor.GREEN + "已获得武器: " + ChatColor.GOLD + name);
                        // 自动切换到新获得的武器
                        currentGunMap.put(player, id);
                        updatePlayerXP(player, id);
                    } else {
                        player.sendMessage(ChatColor.RED + "无效的武器ID！");
                    }
                    break;

                case "up":
                    // 补充当前武器的弹药
                    String currentGunId = currentGunMap.get(player);
                    if (currentGunId == null) {
                        player.sendMessage(ChatColor.RED + "你手上没有武器！");
                        return true;
                    }
                    if (!ammoMap.containsKey(player) || !ammoMap.get(player).containsKey(currentGunId)) {
                        player.sendMessage(ChatColor.RED + "你的武器数据有误！");
                        return true;
                    }
                    int maxAmmo = config.getInt("guns." + currentGunId + ".ammo", 10);
                    ammoMap.get(player).put(currentGunId, maxAmmo);
                    player.sendMessage(ChatColor.GREEN + "你的武器已补充满子弹！");
                    updatePlayerXP(player, currentGunId);
                    break;

                case "display":
                    // 控制标题显示
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "指令格式错误！正确格式: /shoot display <start|hit> <on|off>");
                        return true;
                    }

                    String displayType = args[1].toLowerCase();
                    String displayAction = args[2].toLowerCase();

                    DisplaySettings settings = displaySettingsMap.getOrDefault(player, new DisplaySettings(true, true));

                    switch (displayType) {
                        case "start":
                            if (displayAction.equals("on")) {
                                settings.setStartTitle(true);
                                player.sendMessage(ChatColor.GREEN + "已开启射击开始时的标题显示！");
                            } else if (displayAction.equals("off")) {
                                settings.setStartTitle(false);
                                player.sendMessage(ChatColor.GREEN + "已关闭射击开始时的标题显示！");
                            } else {
                                player.sendMessage(ChatColor.RED + "无效的操作！使用 on 或 off。");
                            }
                            break;

                        case "hit":
                            if (displayAction.equals("on")) {
                                settings.setHitTitle(true);
                                player.sendMessage(ChatColor.GREEN + "已开启命中目标时的标题显示！");
                            } else if (displayAction.equals("off")) {
                                settings.setHitTitle(false);
                                player.sendMessage(ChatColor.GREEN + "已关闭命中目标时的标题显示！");
                            } else {
                                player.sendMessage(ChatColor.RED + "无效的操作！使用 on 或 off。");
                            }
                            break;

                        default:
                            player.sendMessage(ChatColor.RED + "无效的显示类型！使用 start 或 hit。");
                            return true;
                    }

                    displaySettingsMap.put(player, settings);
                    break;

                case "reload":
                    // 重新加载配置文件
                    reloadConfig();
                    config = getConfig();
                    // 重新加载其他配置文件
                    initializeConfigs();
                    player.sendMessage(ChatColor.GREEN + "配置文件已重新加载！");
                    break;
                case "gui":
                    // 打开枪支和弹药的 GUI 界面
                    openShootGUI(player);
                    break;

                default:
                    // 未知指令
                    player.sendMessage(ChatColor.RED + "未知指令！使用 " + ChatColor.YELLOW + "/shoot help " + ChatColor.RED + "查看帮助信息。");
                    break;
            }
        }

        if (label.equalsIgnoreCase("byr")) {
            // 仅允许管理员执行指令
            if (!sender.hasPermission("shoot.byr")) {
                sender.sendMessage(ChatColor.RED + "你没有权限执行此指令！");
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage(ChatColor.YELLOW + "使用: /byr <set|remove> ...");
                return true;
            }

            Player admin = (Player) sender;

            switch (args[0].toLowerCase()) {
                case "set":
                    // /byr set <type> <id> <NPC名称>
                    if (args.length < 4) {
                        admin.sendMessage(ChatColor.RED + "指令格式错误！正确格式: /byr set <type> <id> <NPC名称>");
                        return true;
                    }

                    String type = args[1].toLowerCase();
                    String itemId = args[2];
                    String npcName = args[3];

//                    if (type.equals("wp")) {
//                        // 确保非枪支武器 id1 不能作为枪支类型设置
//                        if (itemId.equals("id1")) {
//                            admin.sendMessage(ChatColor.RED + "id1 是非枪支武器，无法作为武器类型设置！");
//                            return true;
//                        }
//                    }

                    if (!type.equals("ar") && !type.equals("wp") && !type.equals("it") && !type.equals("sp")) {
                        admin.sendMessage(ChatColor.RED + "无效的类型！可用类型: ar, wp, it, sp");
                        return true;
                    }

                    // **修复Bug：确保id2为手枪，id3为步枪，id4为霰弹枪，id5为机枪**
                    if (type.equals("wp")) {
                        switch (itemId) {
                            case "id1": // 手枪
                                if (!buyConfig.contains("wp.id1")) {
                                    admin.sendMessage(ChatColor.RED + "Buy.yml中未找到手枪(id1)！");
                                    return true;
                                }
                                break;
                            case "id2": // 步枪
                                if (!buyConfig.contains("wp.id2")) {
                                    admin.sendMessage(ChatColor.RED + "Buy.yml中未找到步枪(id2)！");
                                    return true;
                                }
                                break;
                            case "id3": // 霰弹枪
                                if (!buyConfig.contains("wp.id3")) {
                                    admin.sendMessage(ChatColor.RED + "Buy.yml中未找到霰弹枪(id3)！");
                                    return true;
                                }
                                break;
                            case "id4": // 机枪
                                if (!buyConfig.contains("wp.id4")) {
                                    admin.sendMessage(ChatColor.RED + "Buy.yml中未找到机枪(id4)！");
                                    return true;
                                }
                                break;
                            case "id5": // 火箭筒
                                if (!buyConfig.contains("wp.id5")) {
                                    admin.sendMessage(ChatColor.RED + "Buy.yml中未找到火箭筒(id5)！");
                                    return true;
                                }
                                break;
                            case "id6": // 电击枪
                                if (!buyConfig.contains("wp.id6")) {
                                    admin.sendMessage(ChatColor.RED + "Buy.yml中未找到电击枪(id6)！");
                                    return true;
                                }
                                break;
                            case "id7": // 狙击步枪
                                if (!buyConfig.contains("wp.id7")) {
                                    admin.sendMessage(ChatColor.RED + "Buy.yml中未找到狙击步枪(id7)！");
                                    return true;
                                }
                                break;
                            case "id8": // 冷冻枪
                                if (!buyConfig.contains("wp.id8")) {
                                    admin.sendMessage(ChatColor.RED + "Buy.yml中未找到冷冻枪(id8)！");
                                    return true;
                                }
                                break;
                            case "id9": // 雷击枪
                                if (!buyConfig.contains("wp.id9")) {
                                    admin.sendMessage(ChatColor.RED + "Buy.yml中未找到雷击枪(id9)！");
                                    return true;
                                }
                                break;
                            case "id10": // 压强枪
                                if (!buyConfig.contains("wp.id10")) {
                                    admin.sendMessage(ChatColor.RED + "Buy.yml中未找到压强枪(id10)！");
                                    return true;
                                }
                            case "id11": // 铁剑
                                if (!buyConfig.contains("wp.id11")) {
                                    admin.sendMessage(ChatColor.RED + "Buy.yml中未找到铁剑(id11)！");
                                    return true;
                                }
                                break;
                            case "id12": // 钻石剑
                                if (!buyConfig.contains("wp.id12")) {
                                    admin.sendMessage(ChatColor.RED + "Buy.yml中未找到钻石剑(id12)！");
                                    return true;
                                }
                                break;
                            default:
                                admin.sendMessage(ChatColor.RED + "无效的武器ID！");
                                return true;
                        }
                    }

                    // 检查Buy.yml中是否存在该物品
                    if (!buyConfig.contains(type + "." + itemId)) {
                        admin.sendMessage(ChatColor.RED + "Buy.yml中未找到该物品！");
                        return true;
                    }

                    // 获取玩家当前位置
                    Location adminLoc = admin.getLocation();

                    // 创建NPC，确保类型为 PLAYER
                    NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npcName);
                    npc.spawn(adminLoc);
                    npc.setProtected(false); // 允许被攻击

                    // 保存NPC位置
                    String npcUUID = npc.getUniqueId().toString();
                    locConfig.set("npcs." + npcUUID + ".type", type);
                    locConfig.set("npcs." + npcUUID + ".itemId", itemId);
                    locConfig.set("npcs." + npcUUID + ".name", npcName);
                    locConfig.set("npcs." + npcUUID + ".location.world", adminLoc.getWorld().getName());
                    locConfig.set("npcs." + npcUUID + ".location.x", adminLoc.getX());
                    locConfig.set("npcs." + npcUUID + ".location.y", adminLoc.getY());
                    locConfig.set("npcs." + npcUUID + ".location.z", adminLoc.getZ());
                    locConfig.set("npcs." + npcUUID + ".location.yaw", adminLoc.getYaw());
                    locConfig.set("npcs." + npcUUID + ".location.pitch", adminLoc.getPitch());

                    saveAllConfigs();

                    // 添加价格浮动文字（使用ArmorStand作为悬浮文字）
                    ArmorStand priceStand = admin.getWorld().spawn(adminLoc.clone().add(0, 1.5, 0), ArmorStand.class);
                    priceStand.setVisible(false);
                    priceStand.setCustomName(ChatColor.GOLD + "价格: " + buyConfig.getInt(type + "." + itemId + ".price"));
                    priceStand.setCustomNameVisible(true);
                    priceStand.setGravity(false);

                    admin.sendMessage(ChatColor.GREEN + "成功创建购买点 NPC: " + ChatColor.AQUA + npcName);
                    break;

                case "remove":
                    // /byr remove
                    if (args.length < 1) {
                        admin.sendMessage(ChatColor.RED + "指令格式错误！正确格式: /byr remove");
                        return true;
                    }

                    // 获取管理员当前位置
                    Location loc = admin.getLocation();

                    // 找到最近的NPC
                    NPC nearestNPC = null;
                    double nearestDistance = Double.MAX_VALUE;
                    for (NPC existingNPC : CitizensAPI.getNPCRegistry()) {
                        // 获取NPC实体的位置
                        Location npcLoc = existingNPC.getEntity().getLocation();  // 通过 getEntity() 获取实体后调用 getLocation()
                        double distance = loc.distance(npcLoc);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestNPC = existingNPC;
                        }
                    }

                    if (nearestNPC != null && nearestDistance <= 5.0) { // 设定查找半径为5块
                        String npcUUIDToRemove = nearestNPC.getUniqueId().toString();
                        nearestNPC.destroy();

                        // 移除loc.yml中的记录
                        locConfig.set("npcs." + npcUUIDToRemove, null);
                        saveAllConfigs();

                        admin.sendMessage(ChatColor.GREEN + "已移除最近的购买点 NPC: " + ChatColor.AQUA + nearestNPC.getName());
                    } else {
                        admin.sendMessage(ChatColor.RED + "附近没有可移除的购买点 NPC！");
                    }
                    break;
                case "check":
                    // 新增的 check 指令处理
                    if (args.length < 2) {
                        admin.sendMessage(ChatColor.RED + "指令格式错误！正确格式: /byr check <玩家名字>");
                        return true;
                    }

                    String targetName = args[1];
                    Player targetPlayer = Bukkit.getPlayerExact(targetName);
                    if (targetPlayer == null) {
                        admin.sendMessage(ChatColor.RED + "找不到玩家: " + ChatColor.YELLOW + targetName);
                        return true;
                    }

                    double money = getPlayerMoney(targetPlayer);
                    admin.sendMessage(ChatColor.GREEN + targetPlayer.getName() + ChatColor.GOLD + " 当前金钱: " + ChatColor.AQUA + money + " 金币");
                    break;

                case "add":
                    // 新增的 add 指令处理
                    if (args.length < 3) {
                        admin.sendMessage(ChatColor.RED + "指令格式错误！正确格式: /byr add <玩家名字> <金币数量>");
                        return true;
                    }

                    String addTargetName = args[1];
                    Player addTargetPlayer = Bukkit.getPlayerExact(addTargetName);
                    if (addTargetPlayer == null) {
                        admin.sendMessage(ChatColor.RED + "找不到玩家: " + ChatColor.YELLOW + addTargetName);
                        return true;
                    }

                    double addAmount;
                    try {
                        addAmount = Double.parseDouble(args[2]);
                    } catch (NumberFormatException e) {
                        admin.sendMessage(ChatColor.RED + "金币数量必须是数字！");
                        return true;
                    }

                    if (addAmount < 0) {
                        admin.sendMessage(ChatColor.RED + "金币数量必须是正数！");
                        return true;
                    }

                    addPlayerMoney(addTargetPlayer, addAmount);
                    admin.sendMessage(ChatColor.GREEN + "已成功为 " + ChatColor.AQUA + addTargetPlayer.getName() + ChatColor.GREEN + " 添加 " + ChatColor.GOLD + addAmount + " 金币！");
                    break;
                case "gui":
                    // 打开购买物品的 GUI 界面
                    openBuyGUI(player);
                    break;

                default:
                    admin.sendMessage(ChatColor.RED + "未知的子指令！使用: /byr <set|remove>");
                    break;
            }
        }


        return true;
    }

    /**
     * 监听玩家与实体交互事件，处理购买逻辑
     *
     * @param event 玩家与实体交互事件
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        // 检查被点击的实体是否为NPC
        if (!CitizensAPI.getNPCRegistry().isNPC(entity)) {
            return;
        }

        NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
        if (npc == null) {
            return;
        }

        String npcUUID = npc.getUniqueId().toString();
        if (!locConfig.contains("npcs." + npcUUID)) {
            return;
        }

        String type = locConfig.getString("npcs." + npcUUID + ".type");
        String itemId = locConfig.getString("npcs." + npcUUID + ".itemId");

        // 检查玩家是否已经购买过（除非类型为it或sp）
        if (!type.equals("it") && !type.equals("sp")) {
            String playerUUID = player.getUniqueId().toString();
            String purchaseKey = "purchased." + npcUUID + "." + playerUUID;
            if (playerDataConfig.getBoolean(purchaseKey, false)) {
                player.sendMessage(ChatColor.RED + "你已经购买过此物品！");
                return;
            }
        }

        // 获取物品价格
        int price = buyConfig.getInt(type + "." + itemId + ".price", 0);
        if (price <= 0) {
            player.sendMessage(ChatColor.RED + "此物品不可购买或价格未设置！");
            return;
        }

        // 获取玩家余额
        double playerMoney = getPlayerMoney(player);

        if (playerMoney < price) {
            player.sendMessage(ChatColor.RED + "你的余额不足以购买此物品！");
            return;
        }

        // 扣除玩家金额
        subtractPlayerMoney(player, price);

        // 根据类型和itemId处理购买逻辑
        if (type.equals("wp")) {
            if (itemId.equals("id1")) {
                // 处理非枪支武器（如铁剑）
                giveNonGunWeaponToPlayer(player, itemId);
            } else {
                // 处理枪支武器
                giveGunToPlayer(player, itemId);
            }
        } else if (type.equals("ar")) {
            // 护甲类型购买
            giveArmorToPlayer(player, itemId);
        } else if (type.equals("it")) {
            // 道具类型购买
            giveItemToPlayer(player, itemId);
        } else if (type.equals("sp")) {
            // 特殊功能类型购买
            activateSpecialFunction(itemId);
        }

        // 标记玩家已购买（除非类型为it或sp）
        if (!type.equals("it") && !type.equals("sp")) {
            String playerUUID = player.getUniqueId().toString();
            String purchaseKey = "purchased." + npcUUID + "." + playerUUID;
            playerDataConfig.set(purchaseKey, true);
        }

        // 保存配置
        saveAllConfigs();

        // 发送购买成功消息
        player.sendMessage(ChatColor.GREEN + "你已成功购买 " + ChatColor.AQUA + buyConfig.getString(type + "." + itemId + ".name", "未知物品") + ChatColor.GREEN + "！");

        // 发送声音效果（可选）
        String purchaseSound = buyConfig.getString(type + "." + itemId + ".purchase_sound", "");
        if (!purchaseSound.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(purchaseSound);
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                getLogger().warning("无效的购买音效: " + purchaseSound);
            }
        }
    }

    /**
     * 给玩家提供非枪支武器（如铁剑）
     *
     * @param player 目标玩家
     * @param itemId 武器ID
     */
    public void giveNonGunWeaponToPlayer(Player player, String itemId) {
        if (buyConfig.contains("wp." + itemId)) {
            // 仅处理 id1 为非枪支武器
            if (!itemId.equals("id1")) {
                player.sendMessage(ChatColor.RED + "此 ID 不是非枪支武器！");
                return;
            }

            // 获取物品名称和材质
            String itemName = buyConfig.getString("wp." + itemId + ".name", "未知武器");
            String materialName = buyConfig.getString("wp." + itemId + ".material", "IRON_SWORD");
            Material material = Material.getMaterial(materialName);
            if (material == null) {
                material = Material.IRON_SWORD; // 默认材质
            }

            // 创建武器物品
            ItemStack weapon = new ItemStack(material);
            ItemMeta meta = weapon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + itemName); // 设置武器名称
                weapon.setItemMeta(meta); // 应用修改后的元数据
            }

            // 将武器添加到玩家的物品栏
            player.getInventory().addItem(weapon);
            player.sendMessage(ChatColor.GREEN + "已获得非枪支武器: " + ChatColor.GOLD + itemName);

            // 初始化弹药数量为 0（非枪支武器无需弹药）
            ammoMap.putIfAbsent(player, new HashMap<>());
            ammoMap.get(player).put(itemId, 0);

            // 设置当前手持武器
            currentGunMap.put(player, itemId);
            updatePlayerXP(player, itemId);
        } else {
            player.sendMessage(ChatColor.RED + "无效的非枪支武器ID！");
        }
    }



    /**
     * 给玩家装备指定ID的护甲
     *
     * @param player 目标玩家
     * @param itemId 护甲ID
     */
    public void giveArmorToPlayer(Player player, String itemId) {
        if (buyConfig.contains("ar." + itemId)) {
            // 获取护甲套装信息
            String name = buyConfig.getString("ar." + itemId + ".name", "未知护甲");

            if (itemId.equals("id1")) { // 皮革套装(上)
                // 创建皮革头盔
                ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
                ItemMeta helmetMeta = helmet.getItemMeta();
                if (helmetMeta != null) {
                    helmetMeta.setDisplayName(ChatColor.GOLD + name + " 头盔");
                    helmet.setItemMeta(helmetMeta);
                }

                // 创建皮革胸甲
                ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                ItemMeta chestMeta = chestplate.getItemMeta();
                if (chestMeta != null) {
                    chestMeta.setDisplayName(ChatColor.GOLD + name + " 胸甲");
                    chestplate.setItemMeta(chestMeta);
                }

                // 装备护甲（替换原有的头盔和胸甲）
                player.getInventory().setHelmet(helmet);
                player.getInventory().setChestplate(chestplate);

                player.sendMessage(ChatColor.GREEN + "已装备护甲套装: " + ChatColor.GOLD + name);
            } else if (itemId.equals("id2")) { // 皮革套装(下)
                // 创建皮革护腿
                ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
                ItemMeta leggingsMeta = leggings.getItemMeta();
                if (leggingsMeta != null) {
                    leggingsMeta.setDisplayName(ChatColor.GOLD + name + " 护腿");
                    leggings.setItemMeta(leggingsMeta);
                }

                // 创建皮革靴子
                ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
                ItemMeta bootsMeta = boots.getItemMeta();
                if (bootsMeta != null) {
                    bootsMeta.setDisplayName(ChatColor.GOLD + name + " 靴子");
                    boots.setItemMeta(bootsMeta);
                }

                // 装备护甲（替换原有的护腿和靴子）
                player.getInventory().setLeggings(leggings);
                player.getInventory().setBoots(boots);

                player.sendMessage(ChatColor.GREEN + "已装备护甲套装: " + ChatColor.GOLD + name);
            } else if (itemId.equals("id3")) { // 锁链套装(上)
                // 创建锁链头盔
                ItemStack helmet = new ItemStack(Material.CHAINMAIL_HELMET);
                ItemMeta helmetMeta = helmet.getItemMeta();
                if (helmetMeta != null) {
                    helmetMeta.setDisplayName(ChatColor.GOLD + name + " 头盔");
                    helmet.setItemMeta(helmetMeta);
                }

                // 创建锁链胸甲
                ItemStack chestplate = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
                ItemMeta chestMeta = chestplate.getItemMeta();
                if (chestMeta != null) {
                    chestMeta.setDisplayName(ChatColor.GOLD + name + " 胸甲");
                    chestplate.setItemMeta(chestMeta);
                }

                // 装备护甲（替换原有的头盔和胸甲）
                player.getInventory().setHelmet(helmet);
                player.getInventory().setChestplate(chestplate);

                player.sendMessage(ChatColor.GREEN + "已装备锁链护甲套装: " + ChatColor.GOLD + name);
            }else if (itemId.equals("id4")) { // 锁链套装(下)
                // 创建锁链护腿
                ItemStack leggings = new ItemStack(Material.CHAINMAIL_LEGGINGS);
                ItemMeta leggingsMeta = leggings.getItemMeta();
                if (leggingsMeta != null) {
                    leggingsMeta.setDisplayName(ChatColor.GOLD + name + " 护腿");
                    leggings.setItemMeta(leggingsMeta);
                }

                // 创建锁链靴子
                ItemStack boots = new ItemStack(Material.CHAINMAIL_BOOTS);
                ItemMeta bootsMeta = boots.getItemMeta();
                if (bootsMeta != null) {
                    bootsMeta.setDisplayName(ChatColor.GOLD + name + " 靴子");
                    boots.setItemMeta(bootsMeta);
                }

                // 装备护甲（替换原有的护腿和靴子）
                player.getInventory().setLeggings(leggings);
                player.getInventory().setBoots(boots);

                player.sendMessage(ChatColor.GREEN + "已装备锁链护甲套装: " + ChatColor.GOLD + name);
            }else if (itemId.equals("id5")) { // 铁套装(上)
                // 创建铁头盔
                ItemStack helmet = new ItemStack(Material.IRON_HELMET);
                ItemMeta helmetMeta = helmet.getItemMeta();
                if (helmetMeta != null) {
                    helmetMeta.setDisplayName(ChatColor.GOLD + name + " 铁头盔");
                    helmet.setItemMeta(helmetMeta);
                }

                // 创建铁胸甲
                ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
                ItemMeta chestMeta = chestplate.getItemMeta();
                if (chestMeta != null) {
                    chestMeta.setDisplayName(ChatColor.GOLD + name + " 铁胸甲");
                    chestplate.setItemMeta(chestMeta);
                }

                // 装备护甲（替换原有的头盔和胸甲）
                player.getInventory().setHelmet(helmet);
                player.getInventory().setChestplate(chestplate);

                player.sendMessage(ChatColor.GREEN + "已装备铁套装: " + ChatColor.GOLD + name);
            } // 新增：铁套装(下)
            else if (itemId.equals("id6")) { // 铁套装(下)
                // 创建铁护腿
                ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
                ItemMeta leggingsMeta = leggings.getItemMeta();
                if (leggingsMeta != null) {
                    leggingsMeta.setDisplayName(ChatColor.GOLD + name + " 铁护腿");
                    leggings.setItemMeta(leggingsMeta);
                }

                // 创建铁靴子
                ItemStack boots = new ItemStack(Material.IRON_BOOTS);
                ItemMeta bootsMeta = boots.getItemMeta();
                if (bootsMeta != null) {
                    bootsMeta.setDisplayName(ChatColor.GOLD + name + " 铁靴子");
                    boots.setItemMeta(bootsMeta);
                }

                // 装备护甲（替换原有的护腿和靴子）
                player.getInventory().setLeggings(leggings);
                player.getInventory().setBoots(boots);

                player.sendMessage(ChatColor.GREEN + "已装备铁套装(下): " + ChatColor.GOLD + name);
            }else if (itemId.equals("id7")) { // 钻石套装(上)
                // 创建钻石头盔
                ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
                ItemMeta helmetMeta = helmet.getItemMeta();
                if (helmetMeta != null) {
                    helmetMeta.setDisplayName(ChatColor.GOLD + name + " 钻石头盔");
                    helmet.setItemMeta(helmetMeta);
                }

                // 创建钻石胸甲
                ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
                ItemMeta chestMeta = chestplate.getItemMeta();
                if (chestMeta != null) {
                    chestMeta.setDisplayName(ChatColor.GOLD + name + " 钻石胸甲");
                    chestplate.setItemMeta(chestMeta);
                }

                // 装备护甲（替换原有的头盔和胸甲）
                player.getInventory().setHelmet(helmet);
                player.getInventory().setChestplate(chestplate);

                player.sendMessage(ChatColor.GREEN + "已装备钻石护甲套装: " + ChatColor.GOLD + name);
            }else if (itemId.equals("id8")) { // 钻石套装(下)
                // 创建钻石护腿
                ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
                ItemMeta leggingsMeta = leggings.getItemMeta();
                if (leggingsMeta != null) {
                    leggingsMeta.setDisplayName(ChatColor.GOLD + name + " 钻石护腿");
                    leggings.setItemMeta(leggingsMeta);
                }

                // 创建钻石靴子
                ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
                ItemMeta bootsMeta = boots.getItemMeta();
                if (bootsMeta != null) {
                    bootsMeta.setDisplayName(ChatColor.GOLD + name + " 钻石靴子");
                    boots.setItemMeta(bootsMeta);
                }

                // 装备护甲（替换原有的护腿和靴子）
                player.getInventory().setLeggings(leggings);
                player.getInventory().setBoots(boots);

                player.sendMessage(ChatColor.GREEN + "已装备钻石套装(下): " + ChatColor.GOLD + name);
            }else if (itemId.equals("id9")) { // 附魔钻石套装1号(上)
                // 创建附魔钻石头盔
                ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
                ItemMeta helmetMeta = helmet.getItemMeta();
                if (helmetMeta != null) {
                    helmetMeta.setDisplayName(ChatColor.GOLD + name + " 附魔头盔");
                    helmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true); // 添加保护1附魔
                    helmet.setItemMeta(helmetMeta);
                }

                // 创建附魔钻石胸甲
                ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
                ItemMeta chestMeta = chestplate.getItemMeta();
                if (chestMeta != null) {
                    chestMeta.setDisplayName(ChatColor.GOLD + name + " 附魔胸甲");
                    chestMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true); // 添加保护1附魔
                    chestplate.setItemMeta(chestMeta);
                }

                // 装备护甲（替换原有的头盔和胸甲）
                player.getInventory().setHelmet(helmet);
                player.getInventory().setChestplate(chestplate);

                player.sendMessage(ChatColor.GREEN + "已装备附魔钻石护甲套装: " + ChatColor.GOLD + name);
            }else if (itemId.equals("id10")) { // 附魔钻石套装1号(下)
                // 创建附魔钻石护腿
                ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
                ItemMeta leggingsMeta = leggings.getItemMeta();
                if (leggingsMeta != null) {
                    leggingsMeta.setDisplayName(ChatColor.GOLD + name + " 附魔护腿");
                    leggingsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true); // 添加保护1附魔
                    leggings.setItemMeta(leggingsMeta);
                }

                // 创建附魔钻石靴子
                ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
                ItemMeta bootsMeta = boots.getItemMeta();
                if (bootsMeta != null) {
                    bootsMeta.setDisplayName(ChatColor.GOLD + name + " 附魔靴子");
                    bootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true); // 添加保护1附魔
                    boots.setItemMeta(bootsMeta);
                }

                // 装备护甲（替换原有的护腿和靴子）
                player.getInventory().setLeggings(leggings);
                player.getInventory().setBoots(boots);

                player.sendMessage(ChatColor.GREEN + "已装备附魔钻石护甲套装: " + ChatColor.GOLD + name);
            }else if (itemId.equals("id11")) { // 附魔钻石套装2号(上)
                // 创建附魔钻石头盔
                ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
                ItemMeta helmetMeta = helmet.getItemMeta();
                if (helmetMeta != null) {
                    helmetMeta.setDisplayName(ChatColor.GOLD + name + " 附魔头盔");
                    helmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true); // 添加保护2附魔
                    helmet.setItemMeta(helmetMeta);
                }

                // 创建附魔钻石胸甲
                ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
                ItemMeta chestMeta = chestplate.getItemMeta();
                if (chestMeta != null) {
                    chestMeta.setDisplayName(ChatColor.GOLD + name + " 附魔胸甲");
                    chestMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true); // 添加保护2附魔
                    chestplate.setItemMeta(chestMeta);
                }

                // 装备护甲（替换原有的头盔和胸甲）
                player.getInventory().setHelmet(helmet);
                player.getInventory().setChestplate(chestplate);

                player.sendMessage(ChatColor.GREEN + "已装备附魔钻石护甲套装: " + ChatColor.GOLD + name);
            }else if (itemId.equals("id12")) { // 附魔钻石套装2号(下)
                // 创建附魔钻石护腿
                ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
                ItemMeta leggingsMeta = leggings.getItemMeta();
                if (leggingsMeta != null) {
                    leggingsMeta.setDisplayName(ChatColor.GOLD + name + " 附魔护腿");
                    leggingsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true); // 添加保护2附魔
                    leggings.setItemMeta(leggingsMeta);
                }

                // 创建附魔钻石靴子
                ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
                ItemMeta bootsMeta = boots.getItemMeta();
                if (bootsMeta != null) {
                    bootsMeta.setDisplayName(ChatColor.GOLD + name + " 附魔靴子");
                    bootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true); // 添加保护2附魔
                    boots.setItemMeta(bootsMeta);
                }

                // 装备护甲（替换原有的护腿和靴子）
                player.getInventory().setLeggings(leggings);
                player.getInventory().setBoots(boots);

                player.sendMessage(ChatColor.GREEN + "已装备附魔钻石护甲套装: " + ChatColor.GOLD + name);
            }



            else {
                player.sendMessage(ChatColor.RED + "无效的护甲ID！");
            }
        } else {
            player.sendMessage(ChatColor.RED + "无效的护甲ID！");
        }
    }


    /**
     * 给玩家添加指定ID的道具
     *
     * @param player 目标玩家
     * @param itemId 道具ID
     */
    public void giveItemToPlayer(Player player, String itemId) {
        if (buyConfig.contains("it." + itemId)) {
            String itemName = buyConfig.getString("it." + itemId + ".name", "未知道具");
            Material material = Material.getMaterial(buyConfig.getString("it." + itemId + ".material", "POTION"));
            if (material == null) {
                material = Material.POTION;
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + itemName);
                item.setItemMeta(meta);
            }

            // 如果是药水，设置效果
            if (material == Material.POTION) {
                Potion pot = Potion.fromItemStack(item);
                if (itemId.equals("id1")) { // 速度buff1药水
                    pot.setType(PotionType.SPEED);
                    pot.setLevel(1);
                } else if (itemId.equals("id2")) { // 跳跃buff1药水
                    pot.setType(PotionType.JUMP);
                    pot.setLevel(1);
                } else if (itemId.equals("id3")) { // 手枪弹药
                    // 不需要设置药水效果，作为道具使用
                } else if (itemId.equals("id4")) { // 步枪弹药
                    String currentGunId = currentGunMap.get(player);
                    if (currentGunId != null && currentGunId.equals("id2")) { // 确认当前手持武器是步枪
                        ammoMap.get(player).put("id2", config.getInt("guns.id2.ammo", 100));
                        player.sendMessage(ChatColor.GREEN + "步枪弹药已补满！");
                    } else {
                        player.sendMessage(ChatColor.RED + "你需要持有步枪才能使用此弹药！");
                        return;
                    }
                } else if (itemId.equals("id5")) { // 霰弹枪弹药
                    String gunId = "id3"; // 确认对应的霰弹枪ID为id4
                    if (currentGunMap.containsKey(player) && currentGunMap.get(player).equals(gunId)) {
                        ammoMap.get(player).put(gunId, config.getInt("guns." + gunId + ".ammo", 30));
                        player.sendMessage(ChatColor.GREEN + "霰弹枪弹药已补满！");
                    } else {
                        player.sendMessage(ChatColor.RED + "你需要持有霰弹枪才能使用此弹药！");
                        return;
                    }
                } else if (itemId.equals("id6")) { // 机枪弹药
                    String currentGunId = currentGunMap.get(player);
                    if (currentGunId != null && currentGunId.equals("id4")) { // 确认当前手持武器是机枪
                        ammoMap.get(player).put("id4", config.getInt("guns.id4.ammo", 200)); // 假设机枪弹药为200
                        player.sendMessage(ChatColor.GREEN + "机枪弹药已补满！");
                    } else {
                        player.sendMessage(ChatColor.RED + "你需要持有机枪才能使用此弹药！");
                        return;
                    }
                } else if (itemId.equals("id7")) { // 火箭筒弹药
                    String currentGunId = currentGunMap.get(player);
                    if (currentGunId != null && currentGunId.equals("id5")) { // 确认当前手持武器是火箭筒
                        ammoMap.get(player).put("id5", config.getInt("guns.id5.ammo", 400)); // 假设火箭筒弹药为400
                        player.sendMessage(ChatColor.GREEN + "火箭筒弹药已补满！");
                    } else {
                        player.sendMessage(ChatColor.RED + "你需要持有火箭筒才能使用此弹药！");
                        return;
                    }
                } else if (itemId.equals("id8")) { // 电击枪弹药
                    String currentGunId = currentGunMap.get(player);
                    if (currentGunId != null && currentGunId.equals("id6")) { // 确认当前手持武器是电击枪
                        ammoMap.get(player).put("id6", config.getInt("guns.id6.ammo", 600)); // 假设电击枪弹药为600
                        player.sendMessage(ChatColor.GREEN + "电击枪弹药已补满！");
                    } else {
                        player.sendMessage(ChatColor.RED + "你需要持有电击枪才能使用此弹药！");
                        return;
                    }
                } else if (itemId.equals("id9")) { // 狙击步枪弹药
                    String currentGunId = currentGunMap.get(player);
                    if (currentGunId != null && currentGunId.equals("id7")) { // 确认当前手持武器是狙击步枪
                        ammoMap.get(player).put("id7", config.getInt("guns.id7.ammo", 500));
                        player.sendMessage(ChatColor.GREEN + "狙击步枪弹药已补满！");
                    } else {
                        player.sendMessage(ChatColor.RED + "你需要持有狙击步枪才能使用此弹药！");
                        return;
                    }
                } else if (itemId.equals("id10")) { // 冰冻枪弹药
                    String currentGunId = currentGunMap.get(player);
                    if (currentGunId != null && currentGunId.equals("id8")) { // 确认当前手持武器是冷冻枪
                        ammoMap.get(player).put("id8", config.getInt("guns.id8.ammo", 300)); // 假设冷冻枪弹药为300
                        player.sendMessage(ChatColor.GREEN + "冷冻枪弹药已补满！");
                    } else {
                        player.sendMessage(ChatColor.RED + "你需要持有冷冻枪才能使用此弹药！");
                        return;
                    }
                } else if (itemId.equals("id11")) { // 雷击枪弹药
                    String currentGunId = currentGunMap.get(player);
                    if (currentGunId != null && currentGunId.equals("id9")) { // 确认当前手持武器是雷击枪
                        ammoMap.get(player).put("id9", config.getInt("guns.id9.ammo", 15));
                        player.sendMessage(ChatColor.GREEN + "雷击枪弹药已补满！");
                    } else {
                        player.sendMessage(ChatColor.RED + "你需要持有雷击枪才能使用此弹药！");
                        return;
                    }
                } else if (itemId.equals("id12")) { // 压强枪弹药
                    String currentGunId = currentGunMap.get(player);
                    if (currentGunId != null && currentGunId.equals("id10")) { // 确认当前手持武器是压强枪
                        ammoMap.get(player).put("id10", config.getInt("guns.id10.ammo", 1000)); // 假设压强枪弹药为1000
                        player.sendMessage(ChatColor.GREEN + "压强枪弹药已补满！");
                    } else {
                        player.sendMessage(ChatColor.RED + "你需要持有压强枪才能使用此弹药！");
                        return;
                    }
                } else if (itemId.equals("id13")) { // 金苹果
                    player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
                    player.sendMessage(ChatColor.GREEN + "已获得金苹果！");
                    return;
                }

                pot.apply(item);
            }

            // 处理手枪弹药的特殊逻辑
            if (itemId.equals("id2")) {
                String currentGunId = currentGunMap.get(player);
                if (currentGunId != null && currentGunId.equals("id1")) { // 确认当前手持武器是手枪
                    ammoMap.get(player).put("id1", config.getInt("guns.id1.ammo", 10));
                    player.sendMessage(ChatColor.GREEN + "手枪弹药已补满！");
                } else {
                    player.sendMessage(ChatColor.RED + "你需要对应的武器！");
                    return;
                }
            } else {
                player.getInventory().addItem(item);
                player.sendMessage(ChatColor.GREEN + "已获得道具: " + ChatColor.AQUA + itemName);
            }
        } else {
            player.sendMessage(ChatColor.RED + "无效的道具ID！");
        }
    }



    /**
     * 激活指定ID的特殊功能
     *
     * @param itemId 特殊功能ID
     */
    public void activateSpecialFunction(String itemId) {
        if (buyConfig.contains("sp." + itemId)) {
            String functionName = buyConfig.getString("sp." + itemId + ".name", "未知功能");
            if (itemId.equalsIgnoreCase("id1")) { // 全体加速功能
                // 遍历所有在线玩家，给予速度buff2效果，持续15秒
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1, false, false));
                }
                Bukkit.broadcastMessage(ChatColor.GOLD + "全体玩家已获得 " + ChatColor.AQUA + functionName + ChatColor.GOLD + " 效果，持续15秒！");
            } else if (itemId.equalsIgnoreCase("id2")) { // 全体回复功能(1)
                // 遍历所有在线玩家，给予瞬间治疗2效果
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 1, false, false));
                }
                Bukkit.broadcastMessage(ChatColor.GOLD + "全体玩家已获得 " + ChatColor.AQUA + functionName + ChatColor.GOLD + " 效果！");
            } else if (itemId.equalsIgnoreCase("id3")) { // 全体回复功能(2)
                // 遍历所有在线玩家，给予生命回复2效果，持续10秒
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 1, false, false));
                }
                Bukkit.broadcastMessage(ChatColor.GOLD + "全体玩家已获得 " + ChatColor.AQUA + functionName + ChatColor.GOLD + " 效果，持续10秒！");
            } else if (itemId.equalsIgnoreCase("id4")) { // 大量牛奶功能
                // 给所有在线玩家一个牛奶
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.getInventory().addItem(new ItemStack(Material.MILK_BUCKET));
                }
                Bukkit.broadcastMessage(ChatColor.GOLD + "所有玩家已获得一个牛奶！");
                // 显示特殊功能标题
                for (Player p : Bukkit.getOnlinePlayers()) {
                    TitleAPI.sendTitle(p, 5, 20, 5, ChatColor.LIGHT_PURPLE + "大量牛奶！", ChatColor.YELLOW + "所有玩家获得了一个牛奶！");
                }
            }else if (itemId.equalsIgnoreCase("id5")) { // 全体回复(3)
                // 遍历所有在线玩家，给予生命回复2效果，持续10秒
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 1, false, false));
                }
                Bukkit.broadcastMessage(ChatColor.GOLD + "全体玩家已获得 " + ChatColor.AQUA + "全体回复(3)" + ChatColor.GOLD + " 效果，持续10秒！");

                // 显示特殊功能标题
                for (Player p : Bukkit.getOnlinePlayers()) {
                    TitleAPI.sendTitle(p, 5, 20, 5, ChatColor.LIGHT_PURPLE + "全体回复(3)！", ChatColor.YELLOW + "所有玩家获得生命回复2效果！");
                }
            } // 新增：生命提升(1)
            else if (itemId.equalsIgnoreCase("id6")) { // 生命提升(1)
                // 遍历所有在线玩家，提升生命值
                for (Player p : Bukkit.getOnlinePlayers()) {
                    // 获取当前生命值
                    double currentHealth = p.getHealth();
                    // 设置新的生命值，最多不超过40（20 hearts）
                    double newHealth = Math.min(currentHealth + 8, 40.0);
                    p.setHealth(newHealth);
                }
                Bukkit.broadcastMessage(ChatColor.GOLD + "所有玩家的生命已提升！");
            }else if (itemId.equalsIgnoreCase("id7")) { // 生命提升(2)
                // 遍历所有在线玩家，给予伤害吸收buff5级效果，持续6分钟
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 6 * 60 * 20, 4, false, false));
                }
                Bukkit.broadcastMessage(ChatColor.GOLD + "所有玩家已获得 " + ChatColor.AQUA + "生命提升(2)" + ChatColor.GOLD + " 效果，持续6分钟！");

                // 显示特殊功能标题
                for (Player p : Bukkit.getOnlinePlayers()) {
                    TitleAPI.sendTitle(p, 5, 20, 5, ChatColor.LIGHT_PURPLE + "生命提升(2)！", ChatColor.YELLOW + "所有玩家获得伤害吸收5级效果！");
                }
            } else if (itemId.equalsIgnoreCase("id8")) { // 力量
                // 遍历所有在线玩家，给予生命提升buff4级效果，持续时间无限
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 3, false, false));
                }
                Bukkit.broadcastMessage(ChatColor.GOLD + "所有玩家已获得 " + ChatColor.AQUA + "力量" + ChatColor.GOLD + " 效果，持续时间无限！");

                // 显示特殊功能标题
                for (Player p : Bukkit.getOnlinePlayers()) {
                    TitleAPI.sendTitle(p, 5, 20, 5, ChatColor.LIGHT_PURPLE + "力量！", ChatColor.YELLOW + "所有玩家获得力量级效果！");
                }
            }else if (itemId.equalsIgnoreCase("id9")) { // 生命提升(3)
                // 遍历所有在线玩家，给予生命提升buff4级效果，持续时间无限
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 20, false, false));
                }
                Bukkit.broadcastMessage(ChatColor.GOLD + "所有玩家已获得 " + ChatColor.AQUA + "生命提升(3)" + ChatColor.GOLD + " 效果，持续时间无限！");

                // 显示特殊功能标题
                for (Player p : Bukkit.getOnlinePlayers()) {
                    TitleAPI.sendTitle(p, 5, 20, 5, ChatColor.LIGHT_PURPLE + "生命提升(3)！", ChatColor.YELLOW + "所有玩家获得生命提升4级效果！");
                }
            }  else if (itemId.equalsIgnoreCase("id10")) { // 快速传送功能
                // 遍历所有在线玩家，给予一次传送机会
                for (Player p : Bukkit.getOnlinePlayers()) {
                    teleportCountMap.put(p, teleportCountMap.getOrDefault(p, 0) + 1);
                    p.sendMessage(ChatColor.GREEN + "你已获得一次 " + ChatColor.AQUA + "快速传送" + ChatColor.GREEN + " 的机会！");
                    // 发送标题
                    TitleAPI.sendTitle(p, 5, 20, 5, ChatColor.LIGHT_PURPLE + "快速传送！", ChatColor.YELLOW + "你获得了一次快速传送的机会！");
                }
                Bukkit.broadcastMessage(ChatColor.GOLD + "所有玩家已获得 " + ChatColor.AQUA + "快速传送" + ChatColor.GOLD + " 的机会！");
            }else if (itemId.equalsIgnoreCase("id11")) { // 无敌时间
                // 遍历所有在线玩家，给予抗性提升buff19级效果，持续15秒
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 15 * 20, 18, false, false));
                }
                Bukkit.broadcastMessage(ChatColor.GOLD + "所有玩家已获得 " + ChatColor.AQUA + "无敌时间" + ChatColor.GOLD + " 效果，持续15秒！");

                // 显示特殊功能标题
                for (Player p : Bukkit.getOnlinePlayers()) {
                    TitleAPI.sendTitle(p, 5, 20, 5, ChatColor.LIGHT_PURPLE + "无敌时间！", ChatColor.YELLOW + "所有玩家获得抗性提升19级效果！");
                }
            }

            else {
                Bukkit.getLogger().warning("未定义的特殊功能ID: " + itemId);
            }
        } else {
            Bukkit.getLogger().warning("无效的特殊功能ID: " + itemId);
        }
    }



    /**
     * 获取玩家的当前金钱
     *
     * @param player 目标玩家
     * @return 当前金钱数量
     */
    public double getPlayerMoney(Player player) {
        return playerDataConfig.getDouble("money." + player.getUniqueId().toString(), buyConfig.getDouble("default_money", 100.0));
    }

    /**
     * 监听实体伤害事件，防止 ArmorStand 被破坏
     *
     * @param event 实体伤害事件
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ArmorStand) {
            event.setCancelled(true); // 取消伤害事件
        }
    }


    /**
     * 设置玩家的金钱
     *
     * @param player 目标玩家
     * @param amount 金钱数量
     */
    public void setPlayerMoney(Player player, double amount) {
        playerDataConfig.set("money." + player.getUniqueId().toString(), amount);
        saveAllConfigs();
    }

    /**
     * 增加玩家的金钱
     *
     * @param player 目标玩家
     * @param amount 增加的金钱数量
     */
    public void addPlayerMoney(Player player, double amount) {
        double currentMoney = getPlayerMoney(player);
        setPlayerMoney(player, currentMoney + amount);
    }

    /**
     * 减少玩家的金钱
     *
     * @param player 目标玩家
     * @param amount 减少的金钱数量
     * @return 是否成功减少（余额是否足够）
     */
    public boolean subtractPlayerMoney(Player player, double amount) {
        double currentMoney = getPlayerMoney(player);
        if (currentMoney < amount) {
            return false;
        }
        setPlayerMoney(player, currentMoney - amount);
        return true;
    }

    /**
     * 获取玩家当前手持武器的ID
     *
     * @param player 玩家
     * @return 武器ID，如果未找到则返回 null
     */
    String getPlayerGunId(Player player) {
        ItemStack item = player.getInventory().getItemInHand();
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = ChatColor.stripColor(meta.getDisplayName());
                for (String gunId : config.getConfigurationSection("guns").getKeys(false)) {
                    String gunName = config.getString("guns." + gunId + ".name", "未知武器");
                    if (displayName.equals(gunName)) {
                        return gunId;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 监听玩家交互事件，实现射击功能和双击左键检测
     *
     * @param event 玩家交互事件
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInHand();

        // 检查手持物品是否为武器（根据配置中的所有武器材质）
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = ChatColor.stripColor(meta.getDisplayName());
                String gunId = null;
                for (String id : config.getConfigurationSection("guns").getKeys(false)) {
                    String gunName = config.getString("guns." + id + ".name", "未知武器");
                    if (displayName.equals(gunName)) {
                        gunId = id;
                        break;
                    }
                }

                if (gunId != null) {

                    // 更新当前手持武器ID
                    currentGunMap.put(player, gunId);
                    updatePlayerXP(player, gunId);

                    // 检查玩家是否在换弹冷却中
                    boolean isReloading = isReloadingMap.getOrDefault(player, new HashMap<>()).getOrDefault(gunId, false);
                    if (isReloading) {
                        player.sendMessage(ChatColor.RED + "你正在换弹中，无法射击！");
                        return;
                    }

                    // 检查玩家是否在射击冷却中
                    boolean isCooldown = isCooldownMap.getOrDefault(player, new HashMap<>()).getOrDefault(gunId, false);
                    if (isCooldown) {
                        player.sendMessage(ChatColor.RED + "你的武器正在冷却中，无法射击！");
                        return;
                    }

                    // 判断交互类型
                    switch (event.getAction()) {
                        case RIGHT_CLICK_AIR:
                        case RIGHT_CLICK_BLOCK:
                            // 右键点击，尝试射击
                            handleShooting(player, gunId);
                            break;

                        case LEFT_CLICK_AIR:
                        case LEFT_CLICK_BLOCK:
                            // 左键点击，检测双击
                            handleLeftClick(player);
                            break;

                        default:
                            break;
                    }
                }
            }
        }
    }

    /**
     * 监听玩家切换物品栏中的物品，更新经验值显示
     *
     * @param event 玩家切换物品栏事件
     */
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());

        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = ChatColor.stripColor(meta.getDisplayName());
                String gunId = null;
                for (String id : config.getConfigurationSection("guns").getKeys(false)) {
                    String gunName = config.getString("guns." + id + ".name", "未知武器");
                    if (displayName.equals(gunName)) {
                        gunId = id;
                        break;
                    }
                }

                if (gunId != null) {
                    // 更新当前手持武器ID
                    currentGunMap.put(player, gunId);
                    updatePlayerXP(player, gunId);
                } else {
                    // 如果不是武器，清空XP显示
                    player.setExp(0);
                    player.setLevel(0);
                    currentGunMap.remove(player);
                }
            } else {
                // 如果不是武器，清空XP显示
                player.setExp(0);
                player.setLevel(0);
                currentGunMap.remove(player);
            }
        } else {
            // 如果手持物品为空，清空XP显示
            player.setExp(0);
            player.setLevel(0);
            currentGunMap.remove(player);
        }
    }

    /**
     * 处理射击逻辑，包括弹药检查、射击计数、限制和冷却
     *
     * @param player 射击的玩家
     * @param gunId  武器ID
     */
    private void handleShooting(Player player, String gunId) {
        // 检查玩家是否有足够的
        if (!ammoMap.containsKey(player) || !ammoMap.get(player).containsKey(gunId) || ammoMap.get(player).get(gunId) <= 0) {
            player.sendMessage(ChatColor.RED + "你的武器没有子弹了！请补充子弹！");
            return;
        }

        // 获取配置中的参数
        int limitShoot = config.getInt("guns." + gunId + ".limit_shoot", 20);
        int limitShootTime = config.getInt("guns." + gunId + ".limit_shoot_time", 10);
        double cooldown = config.getDouble("guns." + gunId + ".cooldown", 1.0);

        // 获取当前射击次数
        int currentShootCount = shootCountMap.getOrDefault(player, new HashMap<>()).getOrDefault(gunId, 0);

        // 如果达到射击次数限制，进入换弹冷却
        if (currentShootCount >= limitShoot) {
            startReloading(player, gunId, limitShootTime);
            return;
        }

        // 检查是否在射击冷却中
        boolean isCooldown = isCooldownMap.getOrDefault(player, new HashMap<>()).getOrDefault(gunId, false);
        if (isCooldown) {
            player.sendMessage(ChatColor.RED + "你的武器正在冷却中，无法射击！");
            return;
        }

        // 扣除一发子弹
        int currentAmmo = ammoMap.get(player).get(gunId) - 1;
        ammoMap.get(player).put(gunId, currentAmmo);

        // 增加射击次数
        shootCountMap.get(player).put(gunId, currentShootCount + 1);

        // 更新经验值显示
        updatePlayerXP(player, gunId);

        // 获取玩家的显示设置
        DisplaySettings settings = displaySettingsMap.getOrDefault(player, new DisplaySettings(true, true));

        // 播放射击标题效果
        if (settings.isStartTitle()) {
            TitleAPI.sendTitle(player, 5, 20, 5, ChatColor.AQUA + "砰！", ChatColor.YELLOW + "你开了一枪！");
        }

        // 播放射击音效
        String shootSound = config.getString("guns." + gunId + ".shoot_sound_start", "");
        if (!shootSound.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(shootSound);
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                getLogger().warning("无效的射击音效: " + shootSound);
            }
        }

        // 根据武器类型处理不同的子弹粒子效果
        switch (gunId) {
            case "id1":
            case "id2":
                // 处理手枪和步枪的射击效果：单条粒子直线
                handleSingleBullet(player, gunId, settings);
                break;

            case "id3":
            case "id4":
                // 处理冲锋枪和机枪的射击效果：多条粒子
                handleMultipleBullets(player, gunId, settings);
                break;

            case "id5":
                // 处理火箭筒的射击效果
                handleRocketLauncher(player, gunId, settings);
                break;

            case "id6":
                // 处理电击枪的射击效果
                handleElectricGun(player, gunId, settings);
                break;

            case "id7":
                // 处理狙击步枪的射击效果
                handleSniperRifle(player, gunId, settings);
                break;

            case "id8":
                // 处理冷冻枪的射击效果
                handleFreezingGun(player, gunId, settings);
                break;

            case "id9":
                // 处理雷击枪的射击效果
                handleLightningGun(player, gunId, settings);
                break;
            case "id10":
                // 处理压强枪的射击效果
                handlePressureGun(player, gunId, settings);
                break;


            default:
                player.sendMessage(ChatColor.RED + "未知的武器ID！");
                break;
        }

        // 检查是否需要进入换弹冷却
        if (shootCountMap.get(player).get(gunId) >= limitShoot) {
            startReloading(player, gunId, limitShootTime);
            return;
        }

        // 启动射击冷却
        if (cooldown > 0) {
            startCooldown(player, gunId, (int) cooldown);
        }
    }

    /**
     * 处理压强枪的射击效果
     *
     * @param player   射击的玩家
     * @param gunId    武器ID
     * @param settings 显示设置
     */
    private void handlePressureGun(Player player, String gunId, DisplaySettings settings) {
        new BukkitRunnable() {
            Location loc = player.getEyeLocation().clone(); // 子弹起始位置
            int distance = 0; // 子弹飞行距离
            double maxDistance = 30; // 最大射程

            @Override
            public void run() {
                if (distance > maxDistance) {
                    // 达到最大距离，取消任务
                    cancel();
                    return;
                }

                // 计算子弹前进的位置
                loc = loc.add(loc.getDirection().multiply(1));

                // 发送烟雾球体粒子效果
                for (double theta = 0; theta <= Math.PI; theta += Math.PI / 10) {
                    for (double phi = 0; phi < 2 * Math.PI; phi += Math.PI / 10) {
                        double x = 0.5 * Math.sin(theta) * Math.cos(phi);
                        double y = 0.5 * Math.sin(theta) * Math.sin(phi);
                        double z = 0.5 * Math.cos(theta);
                        Location particleLoc = loc.clone().add(x, y, z);
                        ParticleEffect.CLOUD.send(Bukkit.getOnlinePlayers(), particleLoc, 0, 0, 0, 0, 1);
                    }
                }

                // 检测子弹是否击中生物
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        // 对被击中的生物造成子弹伤害
                        double damage = config.getDouble("guns." + gunId + ".damage", 8);
                        target.damage(damage, player);

                        // 播放击中音效
                        String hitSound = config.getString("guns." + gunId + ".shoot_sound_hit", "");
                        if (!hitSound.isEmpty()) {
                            try {
                                Sound sound = Sound.valueOf(hitSound);
                                player.playSound(loc, sound, 1.0f, 1.0f);
                            } catch (IllegalArgumentException e) {
                                getLogger().warning("无效的击中音效: " + hitSound);
                            }
                        }

                        // 显示击中提示
                        if (settings.isHitTitle()) {
                            TitleAPI.sendTitle(player, 5, 20, 5, ChatColor.RED + "击中！", ChatColor.YELLOW + "烟雾击中目标！");
                        }

                        // 特殊效果：击退目标并造成额外伤害
                        double specialDamage = config.getDouble("guns." + gunId + ".special_damage", 15);
                        target.damage(specialDamage, player);
                        target.setVelocity(target.getLocation().getDirection().multiply(-2)); // 击退目标

                        // 取消子弹飞行
                        cancel();
                        return;
                    }
                }

                distance++;
            }
        }.runTaskTimer(this, 0, 1); // 每1 tick更新一次
    }


    /**
     * 处理手枪和步枪的单发子弹射击效果
     *
     * @param player   射击的玩家
     * @param gunId    武器ID
     * @param settings 显示设置
     */
    private void handleSingleBullet(Player player, String gunId, DisplaySettings settings) {
        new BukkitRunnable() {
            Location loc = player.getEyeLocation().clone(); // 子弹起始位置
            int distance = 0; // 子弹飞行距离
            double maxDistance = 50; // 最大射程

            @Override
            public void run() {
                if (distance > maxDistance) {
                    // 达到最大距离，取消任务
                    cancel();
                    return;
                }
                // 计算子弹前进的位置
                loc = loc.add(loc.getDirection().multiply(1));
                // 根据武器类型发送不同的粒子效果
                String particleType = config.getString("guns." + gunId + ".particle", "CRIT");
                ParticleEffect particle = ParticleEffect.CRIT; // 默认粒子效果
                try {
                    particle = ParticleEffect.valueOf(particleType);
                } catch (IllegalArgumentException e) {
                    getLogger().warning("无效的粒子效果: " + particleType + "，使用默认效果 CRIT");
                }
                particle.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);

                // 检测子弹是否击中生物
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        // 对被击中的生物造成伤害
                        double damage = config.getDouble("guns." + gunId + ".damage", 5);
                        ((LivingEntity) entity).damage(damage, player);

                        // 播放击中粒子效果
                        ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);

                        // 播放击中音效
                        String hitSound = config.getString("guns." + gunId + ".shoot_sound_hit", "");
                        if (!hitSound.isEmpty()) {
                            try {
                                Sound sound = Sound.valueOf(hitSound);
                                player.playSound(loc, sound, 1.0f, 1.0f);
                            } catch (IllegalArgumentException e) {
                                getLogger().warning("无效的击中音效: " + hitSound);
                            }
                        }

                        // 显示击中提示
                        if (settings.isHitTitle()) {
                            TitleAPI.sendTitle(player, 5, 20, 5, ChatColor.RED + "击中！", ChatColor.YELLOW + "你击中了一个目标！");
                        }

                        // 取消子弹飞行
                        cancel();
                        return;
                    }
                }
                distance++;
            }
        }.runTaskTimer(this, 0, 1); // 每1 tick更新一次
    }

    /**
     * 处理冲锋枪和机枪的多发子弹射击效果
     *
     * @param player   射击的玩家
     * @param gunId    武器ID
     * @param settings 显示设置
     */
    private void handleMultipleBullets(Player player, String gunId, DisplaySettings settings) {
        int pelletCount = gunId.equals("id3") ? 8 : 16; // 冲锋枪发射8发，机枪发射16发
        double spreadAngle = gunId.equals("id3") ? 30 : 10; // 冲锋枪扩散角度较大，机枪较小

        for (int i = 0; i < pelletCount; i++) {
            // 计算每条粒子的方向
            double angle = Math.random() * spreadAngle - spreadAngle / 2;
            Location loc = player.getEyeLocation().clone();
            loc.setDirection(rotateVectorAroundY(loc.getDirection().clone(), Math.toRadians(angle)));

            // 启动子弹轨迹任务
            new BukkitRunnable() {
                Location bulletLoc = loc.clone();
                int distance = 0; // 子弹飞行距离
                double maxDistance = 50; // 最大射程

                @Override
                public void run() {
                    if (distance > maxDistance) {
                        // 达到最大距离，取消任务
                        cancel();
                        return;
                    }
                    // 计算子弹前进的位置
                    bulletLoc = bulletLoc.add(bulletLoc.getDirection().multiply(1));
                    // 发送粒子效果
                    String particleType = config.getString("guns." + gunId + ".particle", "CRIT");
                    ParticleEffect particle = ParticleEffect.CRIT; // 默认粒子效果
                    try {
                        particle = ParticleEffect.valueOf(particleType);
                    } catch (IllegalArgumentException e) {
                        getLogger().warning("无效的粒子效果: " + particleType + "，使用默认效果 CRIT");
                    }
                    particle.send(Bukkit.getOnlinePlayers(), bulletLoc, 0, 0, 0, 0, 1);

                    // 检测子弹是否击中生物
                    for (Entity entity : bulletLoc.getWorld().getNearbyEntities(bulletLoc, 0.5, 0.5, 0.5)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            // 对被击中的生物造成伤害
                            double damage = config.getDouble("guns." + gunId + ".damage", 5);
                            ((LivingEntity) entity).damage(damage, player);

                            // 播放击中粒子效果
                            ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), bulletLoc, 0, 0, 0, 0, 1);

                            // 播放击中音效
                            String hitSound = config.getString("guns." + gunId + ".shoot_sound_hit", "");
                            if (!hitSound.isEmpty()) {
                                try {
                                    Sound sound = Sound.valueOf(hitSound);
                                    player.playSound(bulletLoc, sound, 1.0f, 1.0f);
                                } catch (IllegalArgumentException e) {
                                    getLogger().warning("无效的击中音效: " + hitSound);
                                }
                            }

                            // 显示击中提示
                            if (settings.isHitTitle()) {
                                TitleAPI.sendTitle(player, 5, 20, 5, ChatColor.RED + "击中！", ChatColor.YELLOW + "你击中了一个目标！");
                            }

                            // 取消子弹飞行
                            cancel();
                            return;
                        }
                    }
                    distance++;
                }
            }.runTaskTimer(this, 0, 1); // 每1 tick更新一次
        }
    }

    /**
     * 处理火箭筒的射击效果，发射真实的烈焰弹并处理爆炸特殊伤害
     *
     * @param player   射击的玩家
     * @param gunId    武器ID
     * @param settings 显示设置
     */
    private void handleRocketLauncher(Player player, String gunId, DisplaySettings settings) {
        Location eyeLoc = player.getEyeLocation();
        // 创建一个Fireball实体
        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setIsIncendiary(false); // 不点燃周围
        fireball.setYield(3.0f); // 爆炸范围增大

        // 将Fireball与玩家关联
        fireballMap.put(fireball, player);

        // 启动火箭弹轨迹的粒子效果（火焰圆圈）
        new BukkitRunnable() {
            Location loc = fireball.getLocation().clone();
            double radius = 1.5; // 火焰圆圈半径
            double angle = 0; // 当前角度
            double angularSpeed = Math.toRadians(10); // 角速度

            @Override
            public void run() {
                if (fireball.isDead() || !fireball.isValid()) {
                    // 火箭弹已死亡或无效，取消任务
                    cancel();
                    return;
                }

                // 获取火箭弹当前位置
                loc = fireball.getLocation().clone();

                // 计算火焰圆圈位置
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                Location circleLoc = loc.clone().add(x, 0, z);

                // 发送火焰粒子效果
                ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), circleLoc, 0, 0, 0, 0, 1);

                // 更新角度
                angle += angularSpeed;
                if (angle >= Math.PI * 2) {
                    angle = 0;
                }
            }
        }.runTaskTimer(this, 0, 1); // 每1 tick更新一次

        // 发送初始粒子效果
        ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), fireball.getLocation(), 0, 0, 0, 0, 1);
        ParticleEffect.LAVA.send(Bukkit.getOnlinePlayers(), fireball.getLocation(), 0, 0, 0, 0, 1);

        // 显示射击标题效果
        if (settings.isStartTitle()) {
            TitleAPI.sendTitle(player, 5, 20, 5, ChatColor.AQUA + "火箭发射！", ChatColor.YELLOW + "你发射了一枚火箭筒！");
        }

        // 播放射击音效
        String shootSound = config.getString("guns." + gunId + ".shoot_sound_start", "");
        if (!shootSound.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(shootSound);
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                getLogger().warning("无效的射击音效: " + shootSound);
            }
        }
    }

    /**
     * 处理电击枪的射击效果，发射药水效果粒子，并在命中后对周围生物造成特殊伤害
     *
     * @param player   射击的玩家
     * @param gunId    武器ID
     * @param settings 显示设置
     */
    private void handleElectricGun(Player player, String gunId, DisplaySettings settings) {
        int bulletCount = 5; // 电击枪发射的子弹数量
        double spreadAngle = 20; // 扩散角度

        for (int i = 0; i < bulletCount; i++) {
            // 计算每条粒子的方向
            double angle = Math.random() * spreadAngle - spreadAngle / 2;
            Location loc = player.getEyeLocation().clone();
            loc.setDirection(rotateVectorAroundY(loc.getDirection().clone(), Math.toRadians(angle)));

            // 启动子弹轨迹任务
            new BukkitRunnable() {
                Location bulletLoc = loc.clone();
                int distance = 0; // 子弹飞行距离
                double maxDistance = 50; // 最大射程

                @Override
                public void run() {
                    if (distance > maxDistance) {
                        // 达到最大距离，取消任务
                        cancel();
                        return;
                    }
                    // 计算子弹前进的位置
                    bulletLoc = bulletLoc.add(bulletLoc.getDirection().multiply(1));
                    // 发送药水效果粒子
                    String particleType = config.getString("guns." + gunId + ".particle", "SPELL_WITCH");
                    ParticleEffect particle = ParticleEffect.SPELL_WITCH; // 默认粒子效果
                    try {
                        particle = ParticleEffect.valueOf(particleType);
                    } catch (IllegalArgumentException e) {
                        getLogger().warning("无效的粒子效果: " + particleType + "，使用默认效果 SPELL_WITCH");
                    }
                    particle.send(Bukkit.getOnlinePlayers(), bulletLoc, 0, 0, 0, 0, 1);

                    // 检测子弹是否击中生物
                    for (Entity entity : bulletLoc.getWorld().getNearbyEntities(bulletLoc, 0.5, 0.5, 0.5)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            // 对被击中的生物造成子弹伤害
                            double damage = config.getDouble("guns." + gunId + ".damage", 5);
                            ((LivingEntity) entity).damage(damage, player);

                            // 播放击中粒子效果
                            ParticleEffect.EXPLOSION_NORMAL.send(Bukkit.getOnlinePlayers(), bulletLoc, 0, 0, 0, 0, 1);

                            // 播放击中音效
                            String hitSound = config.getString("guns." + gunId + ".shoot_sound_hit", "");
                            if (!hitSound.isEmpty()) {
                                try {
                                    Sound sound = Sound.valueOf(hitSound);
                                    player.playSound(bulletLoc, sound, 1.0f, 1.0f);
                                } catch (IllegalArgumentException e) {
                                    getLogger().warning("无效的击中音效: " + hitSound);
                                }
                            }

                            // 显示击中提示
                            if (settings.isHitTitle()) {
                                TitleAPI.sendTitle(player, 5, 20, 5, ChatColor.RED + "击中！", ChatColor.YELLOW + "你击中了一个目标！");
                            }

                            // 进行区域内特殊伤害
                            double specialDamage = config.getDouble("guns." + gunId + ".special_damage", 10);
                            Location impactLoc = bulletLoc.clone();
                            for (Entity nearbyEntity : impactLoc.getWorld().getNearbyEntities(impactLoc, 5, 5, 5)) { // 10x10区域，半径为5
                                if (nearbyEntity instanceof LivingEntity && nearbyEntity != entity && nearbyEntity != player) {
                                    ((LivingEntity) nearbyEntity).damage(specialDamage, player);
                                }
                            }

                            // 取消子弹飞行
                            cancel();
                            return;
                        }
                    }
                    distance++;
                }
            }.runTaskTimer(this, 0, 1); // 每1 tick更新一次
        }
    }

    /**
     * 处理狙击步枪的射击效果
     *
     * @param player   射击的玩家
     * @param gunId    武器ID
     * @param settings 显示设置
     */
    private void handleSniperRifle(Player player, String gunId, DisplaySettings settings) {
        new BukkitRunnable() {
            Location loc = player.getEyeLocation().clone(); // 子弹起始位置
            int distance = 0; // 子弹飞行距离
            double maxDistance = 200; // 超长射程

            @Override
            public void run() {
                if (distance > maxDistance) {
                    // 达到最大距离，取消任务
                    cancel();
                    return;
                }
                // 计算子弹前进的位置
                loc = loc.add(loc.getDirection().multiply(2));

                // 发送爱心粒子效果
                ParticleEffect particle = ParticleEffect.HEART; // 爱心粒子效果
                if (particle != null) {
                    particle.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);
                }

                // 检测子弹是否击中生物
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        // 对被击中的生物造成伤害
                        double damage = config.getDouble("guns." + gunId + ".damage", 10);
                        ((LivingEntity) entity).damage(damage, player);

                        // 播放击中粒子效果
                        ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);

                        // 播放击中音效
                        String hitSound = config.getString("guns." + gunId + ".shoot_sound_hit", "");
                        if (!hitSound.isEmpty()) {
                            try {
                                Sound sound = Sound.valueOf(hitSound);
                                player.playSound(loc, sound, 1.0f, 1.0f);
                            } catch (IllegalArgumentException e) {
                                getLogger().warning("无效的击中音效: " + hitSound);
                            }
                        }

                        // 显示击中提示
                        if (settings.isHitTitle()) {
                            TitleAPI.sendTitle(player, 5, 20, 5, ChatColor.RED + "击中！", ChatColor.YELLOW + "你击中了一个目标！");
                        }

                        // 取消子弹飞行
                        cancel();
                        return;
                    }
                }
                distance += 2; // 增加飞行距离
            }
        }.runTaskTimer(this, 0, 1); // 每1 tick更新一次
    }

    /**
     * 处理冷冻枪的射击效果，发射蓝色+烟雾粒子，并在命中后冻结目标
     *
     * @param player   射击的玩家
     * @param gunId    武器ID
     * @param settings 显示设置
     */
    private void handleFreezingGun(Player player, String gunId, DisplaySettings settings) {
        new BukkitRunnable() {
            Location loc = player.getEyeLocation().clone(); // 子弹起始位置
            int distance = 0; // 子弹飞行距离
            double maxDistance = 50; // 最大射程
            long freezeDurationTicks = 40; // 冻结持续时间（2秒）

            @Override
            public void run() {
                if (distance > maxDistance) {
                    // 达到最大距离，取消任务
                    cancel();
                    return;
                }
                // 计算子弹前进的位置
                loc = loc.add(loc.getDirection().multiply(1));
                // 发送蓝色和烟雾粒子效果
                ParticleEffect.SPELL_INSTANT.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);
                ParticleEffect.SMOKE_LARGE.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);

                // 检测子弹是否击中生物
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        // 对被击中的生物造成伤害
                        double damage = config.getDouble("guns." + gunId + ".damage", 5);
                        target.damage(damage, player);

                        // 播放击中粒子效果
                        ParticleEffect.EXPLOSION_NORMAL.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);

                        // 播放击中音效
                        String hitSound = config.getString("guns." + gunId + ".shoot_sound_hit", "");
                        if (!hitSound.isEmpty()) {
                            try {
                                Sound sound = Sound.valueOf(hitSound);
                                player.playSound(loc, sound, 1.0f, 1.0f);
                            } catch (IllegalArgumentException e) {
                                getLogger().warning("无效的击中音效: " + hitSound);
                            }
                        }

                        // 显示击中提示
                        if (settings.isHitTitle()) {
                            TitleAPI.sendTitle(player, 5, 20, 5, ChatColor.BLUE + "冻结！", ChatColor.YELLOW + "你冻结了一个目标！");
                        }

                        // 冻结目标（添加沉默和减速效果）
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) freezeDurationTicks, 255, false, false));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) freezeDurationTicks, 255, false, false));

                        // 进行特殊伤害
                        String specialDamageStr = config.getString("guns." + gunId + ".special_damage", "");
                        if (!specialDamageStr.isEmpty()) {
                            double specialDamage;
                            try {
                                specialDamage = Double.parseDouble(specialDamageStr);
                            } catch (NumberFormatException e) {
                                getLogger().warning("无效的特殊伤害值: " + specialDamageStr + " 在武器ID: " + gunId);
                                specialDamage = 0;
                            }
                            if (specialDamage > 0) {
                                target.damage(specialDamage, player);
                            }
                        }

                        // 取消子弹飞行
                        cancel();
                        return;
                    }
                }
                distance++;
            }
        }.runTaskTimer(this, 0, 1); // 每1 tick更新一次
    }

    /**
     * 处理雷击枪的射击效果，发射绿色暴击粒子，并在命中后对周围生物造成闪电和特殊伤害
     *
     * @param player   射击的玩家
     * @param gunId    武器ID
     * @param settings 显示设置
     */
    private void handleLightningGun(Player player, String gunId, DisplaySettings settings) {
        new BukkitRunnable() {
            Location loc = player.getEyeLocation().clone(); // 子弹起始位置
            int distance = 0; // 子弹飞行距离
            double maxDistance = 100; // 雷击枪的更长射程

            @Override
            public void run() {
                if (distance > maxDistance) {
                    // 达到最大距离，取消任务
                    cancel();
                    return;
                }
                // 计算子弹前进的位置
                loc = loc.add(loc.getDirection().multiply(1));
                // 发送绿色暴击粒子效果
                ParticleEffect.CRIT_MAGIC.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);

                // 检测子弹是否击中生物
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, 0.5, 0.5, 0.5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        // 对被击中的生物造成子弹伤害
                        double damage = config.getDouble("guns." + gunId + ".damage", 12);
                        target.damage(damage, player);

                        // 播放击中粒子效果
                        ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);

                        // 播放击中音效
                        String hitSound = config.getString("guns." + gunId + ".shoot_sound_hit", "");
                        if (!hitSound.isEmpty()) {
                            try {
                                Sound sound = Sound.valueOf(hitSound);
                                player.playSound(loc, sound, 1.0f, 1.0f);
                            } catch (IllegalArgumentException e) {
                                getLogger().warning("无效的击中音效: " + hitSound);
                            }
                        }

                        // 显示击中提示
                        if (settings.isHitTitle()) {
                            TitleAPI.sendTitle(player, 5, 20, 5, ChatColor.YELLOW + "雷击！", ChatColor.RED + "你对目标造成了雷击！");
                        }

                        // 召唤闪电
                        loc.getWorld().strikeLightning(loc);

                        // 进行区域内特殊伤害
                        double specialDamage = config.getDouble("guns." + gunId + ".special_damage", 25);
                        Location impactLoc = loc.clone();
                        for (Entity nearbyEntity : impactLoc.getWorld().getNearbyEntities(impactLoc, 5, 5, 5)) { // 10x10区域，半径为5
                            if (nearbyEntity instanceof LivingEntity && nearbyEntity != target && nearbyEntity != player) {
                                ((LivingEntity) nearbyEntity).damage(specialDamage, player);
                            }
                        }

                        // 取消子弹飞行
                        cancel();
                        return;
                    }
                }
                distance++;
            }
        }.runTaskTimer(this, 0, 1); // 每1 tick更新一次
    }

    /**
     * 启动换弹冷却，期间玩家无法射击
     *
     * @param player            需要进行换弹的玩家
     * @param gunId             武器ID
     * @param reloadTimeSeconds 换弹冷却时间（秒）
     */
    private void startReloading(Player player, String gunId, int reloadTimeSeconds) {
        // 设置玩家为正在换弹状态
        isReloadingMap.get(player).put(gunId, true);
        // 显示换弹中标题
        TitleAPI.sendTitle(player, 10, reloadTimeSeconds * 20, 10, ChatColor.YELLOW + "换弹中...", "");

        // 启动定时任务，计时换弹冷却
        new BukkitRunnable() {
            @Override
            public void run() {
                // 移除换弹状态
                isReloadingMap.get(player).put(gunId, false);
                // 重置射击次数
                shootCountMap.get(player).put(gunId, 0);
                // 显示换弹完成标题
                TitleAPI.sendTitle(player, 10, 20, 10, ChatColor.GREEN + "换弹完成！", "");
            }
        }.runTaskLater(this, reloadTimeSeconds * 20L); // 换弹时间（秒）转换为 tick
    }

    /**
     * 启动射击冷却，期间玩家无法射击
     *
     * @param player          需要进行冷却的玩家
     * @param gunId           武器ID
     * @param cooldownSeconds 射击冷却时间（秒）
     */
    private void startCooldown(Player player, String gunId, int cooldownSeconds) {
        // 设置玩家为正在冷却状态
        isCooldownMap.get(player).put(gunId, true);

        // 启动定时任务，计时冷却时间
        new BukkitRunnable() {
            @Override
            public void run() {
                // 移除冷却状态
                isCooldownMap.get(player).put(gunId, false);
            }
        }.runTaskLater(this, cooldownSeconds * 20L); // 冷却时间（秒）转换为 tick
    }

    /**
     * 处理左键点击，检测双击以手动进入换弹冷却并重置射击计数
     *
     * @param player 进行左键点击的玩家
     */
    private void handleLeftClick(Player player) {
        long currentTime = System.currentTimeMillis();
        long lastClickTime = lastLeftClickMap.getOrDefault(player, 0L);
        long timeDifference = currentTime - lastClickTime;

        // 双击间隔阈值（毫秒）
        long doubleClickThreshold = 300;

        if (timeDifference <= doubleClickThreshold) {
            // 双击检测通过，进入换弹冷却
            String gunId = currentGunMap.get(player);
            if (gunId == null) {
                player.sendMessage(ChatColor.RED + "未检测到手持武器的ID！");
                return;
            }
            int limitShootTime = config.getInt("guns." + gunId + ".limit_shoot_time", 10);
            startReloading(player, gunId, limitShootTime);
            player.sendMessage(ChatColor.YELLOW + "你已手动触发换弹！");
        }

        // 更新最后一次左键点击时间
        lastLeftClickMap.put(player, currentTime);
    }

    /**
     * 更新玩家的经验值显示，用于显示当前武器的弹药数量
     *
     * @param player 玩家
     * @param gunId  当前手持武器的ID
     */
    private void updatePlayerXP(Player player, String gunId) {
        if (!ammoMap.containsKey(player) || !ammoMap.get(player).containsKey(gunId)) {
            player.setExp(0);
            player.setLevel(0);
            return;
        }
        int currentAmmo = ammoMap.get(player).get(gunId);
        int maxAmmo = config.getInt("guns." + gunId + ".ammo", 10);

        // 计算经验值的进度（0.0 到 1.0）
        float exp = (float) currentAmmo / maxAmmo;
        if (exp > 1.0f) exp = 1.0f;
        if (exp < 0.0f) exp = 0.0f;

        player.setExp(exp);
        player.setLevel(currentAmmo); // 使用等级显示当前子弹数量
    }

    /**
     * 内部类，用于存储玩家的显示设置
     */
    private class DisplaySettings {
        private boolean startTitle; // 是否显示射击开始时的标题
        private boolean hitTitle;   // 是否显示命中目标时的标题

        public DisplaySettings(boolean startTitle, boolean hitTitle) {
            this.startTitle = startTitle;
            this.hitTitle = hitTitle;
        }

        public boolean isStartTitle() {
            return startTitle;
        }

        public void setStartTitle(boolean startTitle) {
            this.startTitle = startTitle;
        }

        public boolean isHitTitle() {
            return hitTitle;
        }

        public void setHitTitle(boolean hitTitle) {
            this.hitTitle = hitTitle;
        }
    }

    /**
     * 手动实现绕Y轴旋转向量的方法
     *
     * @param vector 向量
     * @param angle  旋转角度（弧度）
     * @return 旋转后的向量
     */
    private org.bukkit.util.Vector rotateVectorAroundY(org.bukkit.util.Vector vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = vector.getX() * cos - vector.getZ() * sin;
        double z = vector.getX() * sin + vector.getZ() * cos;
        return new org.bukkit.util.Vector(x, vector.getY(), z).normalize();
    }

    /**
     * 监听ProjectileHitEvent事件，用于处理火箭弹的特殊伤害
     *
     * @param event 投射物命中事件
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Fireball)) {
            return; // 仅处理Fireball实体
        }

        Fireball fireball = (Fireball) event.getEntity();

        // 检查该Fireball是否由插件发射
        if (!fireballMap.containsKey(fireball)) {
            return;
        }

        Player shooter = fireballMap.get(fireball);
        String gunId = currentGunMap.get(shooter);
        if (gunId == null || !config.contains("guns." + gunId)) {
            return;
        }

        // 获取特殊伤害值
        String specialDamageStr = config.getString("guns." + gunId + ".special_damage", "");
        if (specialDamageStr.isEmpty()) {
            return; // 无特殊伤害
        }

        double specialDamage;
        try {
            specialDamage = Double.parseDouble(specialDamageStr);
        } catch (NumberFormatException e) {
            getLogger().warning("无效的特殊伤害值: " + specialDamageStr + " 在武器ID: " + gunId);
            return;
        }

        Location impactLoc = fireball.getLocation();

        // 检测爆炸范围内的生物并施加特殊伤害
        for (Entity entity : impactLoc.getWorld().getNearbyEntities(impactLoc, 3.0, 3.0, 3.0)) { // 爆炸范围半径3
            if (entity instanceof LivingEntity && entity != shooter) {
                ((LivingEntity) entity).damage(specialDamage, shooter);
            }
        }

        // 播放爆炸粒子效果
        ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), impactLoc, 0, 0, 0, 0, 1);

        // 播放爆炸音效
        String hitSound = config.getString("guns." + gunId + ".shoot_sound_hit", "");
        if (!hitSound.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(hitSound);
                shooter.playSound(impactLoc, sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                getLogger().warning("无效的击中音效: " + hitSound);
            }
        }

        // 获取玩家的显示设置
        DisplaySettings settings = displaySettingsMap.getOrDefault(shooter, new DisplaySettings(true, true));

        // 显示击中提示
        if (settings.isHitTitle()) {
            TitleAPI.sendTitle(shooter, 5, 20, 5, ChatColor.RED + "击中！", ChatColor.YELLOW + "你的火箭筒击中了目标！");
        }

        // 移除Fireball与玩家的关联
        fireballMap.remove(fireball);
    }

    /**
     * 给玩家添加指定ID的枪支
     *
     * @param player 目标玩家
     */
    public void giveGunToPlayer(Player player, String itemId) {
        if (buyConfig.contains("wp." + itemId)) {
            String itemName = buyConfig.getString("wp." + itemId + ".name", "未知武器");
            String materialName = buyConfig.getString("wp." + itemId + ".material", "IRON_SWORD");
            Material material = Material.getMaterial(materialName);
            if (material == null) {
                material = Material.STONE_AXE; // 默认材质
            }

            // 创建武器物品
            ItemStack weapon = new ItemStack(material);
            ItemMeta meta = weapon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + itemName); // 设置武器名称
                weapon.setItemMeta(meta); // 应用修改后的元数据
            }

            // 将武器添加到玩家的物品栏
            player.getInventory().addItem(weapon);
            player.sendMessage(ChatColor.GREEN + "已获得武器: " + ChatColor.GOLD + itemName);

            // 初始化弹药数量
            ammoMap.putIfAbsent(player, new HashMap<>());
            ammoMap.get(player).put(itemId, config.getInt("guns." + itemId + ".ammo", 10));

            // 初始化射击次数
            shootCountMap.putIfAbsent(player, new HashMap<>());
            shootCountMap.get(player).put(itemId, 0);

            // 初始化换弹状态
            isReloadingMap.putIfAbsent(player, new HashMap<>());
            isReloadingMap.get(player).put(itemId, false);

            // 初始化冷却状态
            isCooldownMap.putIfAbsent(player, new HashMap<>());
            isCooldownMap.get(player).put(itemId, false);

            // 初始化显示设置
            displaySettingsMap.put(player, new DisplaySettings(true, true));

            // 设置当前手持武器
            currentGunMap.put(player, itemId);
            updatePlayerXP(player, itemId);
        } else {
            player.sendMessage(ChatColor.RED + "无效的武器ID！");
        }
    }






    /**
     * 补充玩家当前手持武器的子弹
     * @param player 目标玩家
     * @return 是否成功补充
     */
    public boolean replenishPlayerAmmo(Player player) {
        String currentGunId = currentGunMap.get(player);
        if (currentGunId == null) {
            player.sendMessage(ChatColor.RED + "你手上没有武器！");
            return false;
        }
        if (!ammoMap.containsKey(player) || !ammoMap.get(player).containsKey(currentGunId)) {
            player.sendMessage(ChatColor.RED + "你的武器数据有误！");
            return false;
        }
        int maxAmmo = config.getInt("guns." + currentGunId + ".ammo", 10);
        ammoMap.get(player).put(currentGunId, maxAmmo);
        player.sendMessage(ChatColor.GREEN + "你的武器已补充满子弹！");
        updatePlayerXP(player, currentGunId);
        return true;
    }

    /**
     * 控制玩家的标题显示设置
     * @param player 目标玩家
     * @param type 显示类型 ("start" 或 "hit")
     * @param enable 是否启用
     * @return 是否成功设置
     */
    public boolean setPlayerDisplay(Player player, String type, boolean enable) {
        DisplaySettings settings = displaySettingsMap.getOrDefault(player, new DisplaySettings(true, true));

        switch (type) {
            case "start":
                settings.setStartTitle(enable);
                player.sendMessage(enable ? ChatColor.GREEN + "已开启射击开始时的标题显示！" : ChatColor.GREEN + "已关闭射击开始时的标题显示！");
                break;
            case "hit":
                settings.setHitTitle(enable);
                player.sendMessage(enable ? ChatColor.GREEN + "已开启命中目标时的标题显示！" : ChatColor.GREEN + "已关闭命中目标时的标题显示！");
                break;
            default:
                player.sendMessage(ChatColor.RED + "无效的显示类型！使用 start 或 hit。");
                return false;
        }

        displaySettingsMap.put(player, settings);
        return true;
    }

    /**
     * 重新加载 Shoot 插件的配置文件
     */
    public void reloadShootConfig() {
        reloadConfig();
        config = getConfig();
        getLogger().info(ChatColor.GREEN + "Shoot 配置文件已重新加载！");
    }

    /**
     * 检查玩家当前武器是否在冷却中
     * @param player 目标玩家
     * @return 是否在冷却中
     */
    public boolean isPlayerOnCooldown(Player player) {
        String gunId = currentGunMap.get(player);
        if (gunId == null) return false;
        return isCooldownMap.getOrDefault(player, new HashMap<>()).getOrDefault(gunId, false);
    }

    /**
     * 获取玩家当前武器的剩余子弹数量
     * @param player 目标玩家
     * @return 剩余子弹数量，若无武器则返回 -1
     */
    public int getPlayerAmmo(Player player) {
        String gunId = currentGunMap.get(player);
        if (gunId == null) return -1;
        if (!ammoMap.containsKey(player) || !ammoMap.get(player).containsKey(gunId)) {
            return -1;
        }
        return ammoMap.get(player).get(gunId);
    }

    /**
     * 设置玩家当前武器的子弹数量
     * @param player 目标玩家
     * @param ammo 要设置的子弹数量
     * @return 是否成功设置
     */
    public boolean setPlayerAmmo(Player player, int ammo) {
        String gunId = currentGunMap.get(player);
        if (gunId == null) return false;
        if (!ammoMap.containsKey(player) || !ammoMap.get(player).containsKey(gunId)) {
            return false;
        }
        ammoMap.get(player).put(gunId, ammo);
        updatePlayerXP(player, gunId);
        return true;
    }

    /**
     * 监听玩家聊天事件，实现快速传送功能
     *
     * @param event 玩家聊天事件
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();

        // 检查玩家是否拥有快速传送机会
        if (teleportCountMap.getOrDefault(sender, 0) > 0) {
            // 尝试根据消息中的玩家名找到目标玩家
            Player target = Bukkit.getPlayerExact(message);
            if (target != null && target.isOnline()) {
                // 传送发送者到目标玩家的位置
                sender.teleport(target.getLocation());
                sender.sendMessage(ChatColor.GREEN + "已传送到 " + ChatColor.AQUA + target.getName() + ChatColor.GREEN + " 的位置！");
                // 减少传送次数
                teleportCountMap.put(sender, teleportCountMap.get(sender) - 1);
                // 取消聊天消息的发送
                event.setCancelled(true);
            }
        }
    }

    /**
     * 打开枪支和弹药的 GUI 界面
     *
     * @param player 玩家
     */
    private void openShootGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 54, ChatColor.BLUE + "枪支和弹药");

        // 添加枪支
        for (String gunId : config.getConfigurationSection("guns").getKeys(false)) {
            String gunName = config.getString("guns." + gunId + ".name", "未知武器");
            String materialName = config.getString("guns." + gunId + ".material", "WOOD_SPADE");
            Material material = Material.getMaterial(materialName);
            if (material == null) {
                material = Material.WOOD_SPADE; // 默认材质
            }

            ItemStack gunItem = new ItemStack(material);
            ItemMeta meta = gunItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + gunName);
                meta.setLore(Arrays.asList(ChatColor.GREEN + "点击获取该武器"));
                gunItem.setItemMeta(meta);
            }

            gui.addItem(gunItem);
        }

        // 添加弹药
        for (String ammoId : config.getConfigurationSection("guns").getKeys(false)) {
            String ammoName = config.getString("guns." + ammoId + ".name", "未知弹药");
            ItemStack ammoItem = new ItemStack(Material.ARROW);
            ItemMeta meta = ammoItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + ammoName + " 弹药");
                meta.setLore(Arrays.asList(ChatColor.GREEN + "点击补充当前手持武器的弹药"));
                ammoItem.setItemMeta(meta);
            }

            gui.addItem(ammoItem);
        }

        player.openInventory(gui);
    }

    /**
     * 打开购买物品的 GUI 界面
     *
     * @param player 玩家
     */
    private void openBuyGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 54, ChatColor.BLUE + "购买物品");

        // 添加护甲
        for (String armorId : buyConfig.getConfigurationSection("ar").getKeys(false)) {
            String armorName = buyConfig.getString("ar." + armorId + ".name", "未知护甲");
            ItemStack armorItem = new ItemStack(Material.LEATHER_CHESTPLATE);
            ItemMeta meta = armorItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + armorName);
                meta.setLore(Arrays.asList(ChatColor.GREEN + "点击购买该护甲"));
                armorItem.setItemMeta(meta);
            }

            gui.addItem(armorItem);
        }

        // 添加武器
        for (String weaponId : buyConfig.getConfigurationSection("wp").getKeys(false)) {
            String weaponName = buyConfig.getString("wp." + weaponId + ".name", "未知武器");
            ItemStack weaponItem = new ItemStack(Material.IRON_SWORD);
            ItemMeta meta = weaponItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + weaponName);
                meta.setLore(Arrays.asList(ChatColor.GREEN + "点击购买该武器"));
                weaponItem.setItemMeta(meta);
            }

            gui.addItem(weaponItem);
        }

        // 添加道具
        for (String itemId : buyConfig.getConfigurationSection("it").getKeys(false)) {
            String itemName = buyConfig.getString("it." + itemId + ".name", "未知道具");
            ItemStack item = new ItemStack(Material.POTION);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + itemName);
                meta.setLore(Arrays.asList(ChatColor.GREEN + "点击购买该道具"));
                item.setItemMeta(meta);
            }

            gui.addItem(item);
        }

        // 添加特殊功能
        for (String specialId : buyConfig.getConfigurationSection("sp").getKeys(false)) {
            String specialName = buyConfig.getString("sp." + specialId + ".name", "未知功能");
            ItemStack specialItem = new ItemStack(Material.NETHER_STAR);
            ItemMeta meta = specialItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + specialName);
                meta.setLore(Arrays.asList(ChatColor.GREEN + "点击购买该功能"));
                specialItem.setItemMeta(meta);
            }

            gui.addItem(specialItem);
        }

        player.openInventory(gui);
    }

    /**
     * 监听 InventoryClickEvent 事件，处理 GUI 点击
     *
     * @param event 事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory gui = event.getInventory();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // 处理枪支和弹药 GUI
        if (gui.getTitle().equals(ChatColor.BLUE + "枪支和弹药")) {
            event.setCancelled(true);

            String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            // 检查是否是枪支
            for (String gunId : config.getConfigurationSection("guns").getKeys(false)) {
                String gunName = config.getString("guns." + gunId + ".name", "未知武器");
                if (itemName.equals(gunName)) {
                    // 给予玩家枪支
                    giveGunToPlayer(player, gunId);
                    player.closeInventory();
                    return;
                }
            }

            // 检查是否是弹药
            for (String ammoId : config.getConfigurationSection("guns").getKeys(false)) {
                String ammoName = config.getString("guns." + ammoId + ".name", "未知弹药");
                if (itemName.equals(ammoName + " 弹药")) {
                    // 补充玩家当前手持武器的弹药
                    replenishPlayerAmmo(player);
                    player.closeInventory();
                    return;
                }
            }
        }

        // 处理购买物品 GUI
        if (gui.getTitle().equals(ChatColor.BLUE + "购买物品")) {
            event.setCancelled(true);

            String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            // 检查是否是护甲
            for (String armorId : buyConfig.getConfigurationSection("ar").getKeys(false)) {
                String armorName = buyConfig.getString("ar." + armorId + ".name", "未知护甲");
                if (itemName.equals(armorName)) {
                    // 给予玩家护甲
                    giveArmorToPlayer(player, armorId);
                    player.closeInventory();
                    return;
                }
            }

            // 检查是否是武器
            for (String weaponId : buyConfig.getConfigurationSection("wp").getKeys(false)) {
                String weaponName = buyConfig.getString("wp." + weaponId + ".name", "未知武器");
                if (itemName.equals(weaponName)) {
                    // 给予玩家武器
                    giveGunToPlayer(player, weaponId);
                    player.closeInventory();
                    return;
                }
            }

            // 检查是否是道具
            for (String itemId : buyConfig.getConfigurationSection("it").getKeys(false)) {
                String itemNameConfig = buyConfig.getString("it." + itemId + ".name", "未知道具");
                if (itemName.equals(itemNameConfig)) {
                    // 给予玩家道具
                    giveItemToPlayer(player, itemId);
                    player.closeInventory();
                    return;
                }
            }

            // 检查是否是特殊功能
            for (String specialId : buyConfig.getConfigurationSection("sp").getKeys(false)) {
                String specialName = buyConfig.getString("sp." + specialId + ".name", "未知功能");
                if (itemName.equals(specialName)) {
                    // 激活特殊功能
                    activateSpecialFunction(specialId);
                    player.closeInventory();
                    return;
                }
            }
        }
    }
}
