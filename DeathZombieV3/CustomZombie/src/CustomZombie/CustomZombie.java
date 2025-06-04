package CustomZombie;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.inventivetalent.particle.ParticleEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;


public class CustomZombie extends JavaPlugin implements CommandExecutor, Listener {

    // 存储不同类型僵尸与其对应任务的映射
    private Map<Zombie, BukkitTask> skeletonZombieTasks = new HashMap<>();
    private Map<Zombie, BukkitTask> mageZombieTasks = new HashMap<>();
    private Map<Zombie, BukkitTask> poisonArrowZombieTasks = new HashMap<>();
    private Map<Zombie, BukkitTask> shockZombieTasks = new HashMap<>();
    private Map<Zombie, BukkitTask> freezeZombieTasks = new HashMap<>();
    private Map<Zombie, BukkitTask> shadowZombieTasks = new HashMap<>();
    private Map<Zombie, BukkitTask> destructionZombieTasks = new HashMap<>();
    private Map<Zombie, BukkitTask> thunderZombieTasks = new HashMap<>();
    // 在 CustomZombie 类中声明用于跟踪变异科学家的映射
    private Map<Zombie, Integer> mutantScientistDamage = new HashMap<>();
    private Map<Zombie, BukkitTask> mutantScientistTasks = new HashMap<>();
    // 在类中添加一个用于存储变异僵尸01任务的映射
    private Map<PigZombie, BukkitTask> mutantZombie01Tasks = new HashMap<>();
    // 在 CustomZombie 类中添加用于跟踪变异僵尸02的任务映射
    private Map<Skeleton, BukkitTask> mutantZombie02Tasks = new HashMap<>();
    // 用于追踪变异法师僵尸的任务映射
    private Map<Zombie, BukkitTask> mutantMageLightningTasks = new HashMap<>();
    private Map<Zombie, BukkitTask> mutantMageAuraTasks = new HashMap<>();
    private Map<Zombie, BukkitTask> mutantMageSummonTasks = new HashMap<>();
    // 在 CustomZombie 类的顶部，添加用于跟踪新增僵尸任务的映射
    private Map<Zombie, BukkitTask> balloonZombieTasks = new HashMap<>();
    private Map<Zombie, BukkitTask> fogZombieTasks = new HashMap<>();
    // 用于跟踪变异烈焰人（IDC3）的任务映射
    private Map<Blaze, BukkitTask> mutantBlazeTasks = new HashMap<>();

    // 用于跟踪变异爬行者（IDC4）的任务映射
    private Map<Creeper, BukkitTask> mutantCreeperTasks = new HashMap<>();
    // 在 CustomZombie 类中添加用于跟踪IDC5和IDC6的任务映射
    private Map<Endermite, BukkitTask> mutantEndermiteTasks = new HashMap<>();
    private Map<Spider, BukkitTask> mutantSpiderTasks = new HashMap<>();
    // 在CustomZombie类中添加用于跟踪变异雷霆僵尸任务的映射
    private Map<Zombie, BukkitTask> mutantThunderZombieTasks = new HashMap<>();
    private Map<Zombie, BukkitTask> mutantShockZombieTasks = new HashMap<>();
    // 1. 声明用于跟踪终极毁灭僵尸任务的映射
    private Map<Zombie, List<BukkitTask>> ultimateDestructionZombieTasks = new HashMap<>();
    // 用于跟踪变异暗影僵尸的任务映射
    private Map<Zombie, BukkitTask> mutantShadowZombieTasks = new HashMap<>();
    // 用于跟踪变异博士的任务映射
    private Map<Zombie, List<BukkitTask>> mutantDoctorTasks = new HashMap<>();
    private Map<Zombie, BukkitTask> twinsZombieTasks = new HashMap<>(); // 新增的映射
    // 在 CustomZombie 类中添加用于跟踪变异博士受到的伤害
    private Map<Zombie, Integer> mutantDoctorDamageReceived = new HashMap<>();
    // 添加静态实例
    private static CustomZombie instance;




    @Override
    public void onEnable() {
        // 设置静态实例
        instance = this;

        // 注册指令和事件监听器
        this.getCommand("czm").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("CustomZombie 插件已启用！");
    }

    /**
     * 获取插件实例
     *
     * @return CustomZombie 实例
     */
    public static CustomZombie getInstance() {
        return instance;
    }


    @Override
    public void onDisable() {
        // 插件禁用时，取消所有僵尸的任务
        for (BukkitTask task : skeletonZombieTasks.values()) {
            task.cancel();
        }
        for (BukkitTask task : shadowZombieTasks.values()) { // 添加此行
            task.cancel();
        }
        for (BukkitTask task : mageZombieTasks.values()) {
            task.cancel();
        }
        for (BukkitTask task : poisonArrowZombieTasks.values()) {
            task.cancel();
        }
        for (BukkitTask task : shockZombieTasks.values()) {
            task.cancel();
        }
        for (BukkitTask task : freezeZombieTasks.values()) {
            task.cancel();
        }
        for (BukkitTask task : shadowZombieTasks.values()) {
            task.cancel();
        }
        for (BukkitTask task : destructionZombieTasks.values()) {
            task.cancel();
        }
        for (BukkitTask task : thunderZombieTasks.values()) {
            task.cancel();
        }
        // 取消变异科学家的任务
        for (BukkitTask task : mutantScientistTasks.values()) {
            task.cancel();
        }
        // 取消所有变异僵尸01的粒子任务
        for (BukkitTask task : mutantZombie01Tasks.values()) {
            task.cancel();
        }
        mutantZombie01Tasks.clear();
        // 取消所有变异僵尸02的粒子任务
        for (BukkitTask task : mutantZombie02Tasks.values()) {
            task.cancel();
        }
        // 取消变异法师僵尸的任务
        for (BukkitTask task : mutantMageLightningTasks.values()) {
            task.cancel();
        }
        for (BukkitTask task : mutantMageAuraTasks.values()) {
            task.cancel();
        }
        for (BukkitTask task : mutantMageSummonTasks.values()) {
            task.cancel();
        }
        // 取消变异末影螨的任务
        for (BukkitTask task : mutantEndermiteTasks.values()) {
            task.cancel();
        }
        mutantEndermiteTasks.clear();

        // 取消变异蜘蛛的任务
        for (BukkitTask task : mutantSpiderTasks.values()) {
            task.cancel();
        }
        // 取消变异暗影僵尸的任务
        for (BukkitTask task : mutantShadowZombieTasks.values()) {
            task.cancel();
        }
        // 取消变异博士的任务
        for (List<BukkitTask> tasks : mutantDoctorTasks.values()) {
            for (BukkitTask task : tasks) {
                task.cancel();
            }
        }
        // 取消双生僵尸的任务
        for (BukkitTask task : twinsZombieTasks.values()) {
            task.cancel();
        }
        twinsZombieTasks.clear();
        mutantDoctorTasks.clear();


        mutantShadowZombieTasks.clear();

        mutantSpiderTasks.clear();
        mutantScientistTasks.clear();
        mutantZombie02Tasks.clear();

        getLogger().info("CustomZombie 插件已禁用！");
    }

    /**
     * 处理指令逻辑
     *
     * @param sender  指令发送者
     * @param command 指令对象
     * @param label   指令别名
     * @param args    指令参数
     * @return 是否成功处理指令
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 判断指令发送者是否为玩家
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用这个指令！");
            return false;
        }

        Player player = (Player) sender;

        // 处理不同的子命令
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "zombie":
                    if (args.length == 2) {
                        String zombieId = args[1].toLowerCase();
                        spawnCustomZombie(player, zombieId);
                        return true;
                    }
                    break;
                case "other":
                    if (args.length == 2) {
                        String otherId = args[1].toLowerCase();
                        spawnOtherEntity(player, otherId);
                        return true;
                    }
                    break;
                case "gui":
                    if (args.length == 1) {
                        openGUI(player);
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }

        player.sendMessage("§c正确的指令格式是: /czm zombie <ID> 或 /czm other <IDC>");
        return false;
    }

    /**
     * 打开自定义僵尸和实体的GUI界面
     *
     * @param player 打开GUI的玩家
     */
    public void openGUI(Player player) {
        // 创建一个54格的仓库GUI
        Inventory gui = Bukkit.createInventory(null, 54, "§6CustomZombie GUI");

        // 定义所有僵尸和实体的ID列表
        List<String> zombieIds = Arrays.asList(
                "id1", "id2", "id3", "id4", "id5", "id6", "id7", "id8", "id9", "id10",
                "id11", "id12", "id13", "id14", "id15", "id16", "id17", "id18", "id19",
                "id20", "id21", "id22", "id23", "id24", "id25"
        );

        List<String> entityIds = Arrays.asList(
                "idc1", "idc2", "idc3", "idc4", "idc5", "idc6"
        );

        // 添加僵尸ID到GUI
        for (String id : zombieIds) {
            ItemStack item = new ItemStack(Material.SKULL_ITEM); // 使用僵尸头作为图标
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§a" + getZombieNameById(id));
            meta.setLore(Arrays.asList("§7点击生成该类型僵尸", "§8ID: " + id));
            item.setItemMeta(meta);
            gui.addItem(item);
        }

        // 添加实体ID到GUI
        for (String idc : entityIds) {
            ItemStack item = new ItemStack(Material.SKULL_ITEM, (short)4); // 使用苦力怕头作为图标
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§b" + getEntityNameByIdc(idc));
            meta.setLore(Arrays.asList("§7点击生成该类型实体", "§8ID: " + idc));
            item.setItemMeta(meta);
            gui.addItem(item);
        }

        // 发送GUI给玩家
        player.openInventory(gui);
    }


    /**
     * 根据僵尸ID获取僵尸名称
     *
     * @param id 僵尸ID
     * @return 僵尸名称
     */
    private String getZombieNameById(String id) {
        switch (id) {
            case "id1":
                return "普通僵尸";
            case "id2":
                return "小僵尸";
            case "id3":
                return "路障僵尸";
            case "id4":
                return "钻斧僵尸";
            case "id5":
                return "剧毒僵尸";
            case "id6":
                return "双生僵尸";
            case "id7":
                return "骷髅僵尸";
            case "id8":
                return "武装僵尸";
            case "id9":
                return "肥胖僵尸";
            case "id10":
                return "法师僵尸";
            case "id11":
                return "自爆僵尸";
            case "id12":
                return "毒箭僵尸";
            case "id13":
                return "电击僵尸";
            case "id14":
                return "冰冻僵尸";
            case "id15":
                return "暗影僵尸";
            case "id16":
                return "毁灭僵尸";
            case "id17":
                return "雷霆僵尸";
            case "id18":
                return "变异科学家";
            case "id19":
                return "变异法师";
            case "id20":
                return "气球僵尸";
            case "id21":
                return "迷雾僵尸";
            case "id22":
                return "变异雷霆僵尸";
            case "id23":
                return "终极毁灭僵尸";
            case "id24":
                return "变异暗影僵尸";
            case "id25":
                return "变异博士";
            default:
                return "未知僵尸";
        }
    }

    /**
     * 根据实体IDC获取实体名称
     *
     * @param idc 实体IDC
     * @return 实体名称
     */
    private String getEntityNameByIdc(String idc) {
        switch (idc) {
            case "idc1":
                return "变异僵尸01";
            case "idc2":
                return "变异僵尸02";
            case "idc3":
                return "变异烈焰人";
            case "idc4":
                return "变异爬行者";
            case "idc5":
                return "变异末影螨";
            case "idc6":
                return "变异蜘蛛";
            default:
                return "未知实体";
        }
    }

    /**
     * 监听GUI点击事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().getTitle().equals("§6CustomZombie GUI")) {
            return;
        }

        event.setCancelled(true); // 取消默认点击行为

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        String displayName = clickedItem.getItemMeta().getDisplayName();

        // 根据显示名称判断点击的是哪个僵尸或实体
        for (String id : Arrays.asList(
                "id1", "id2", "id3", "id4", "id5", "id6", "id7", "id8", "id9", "id10",
                "id11", "id12", "id13", "id14", "id15", "id16", "id17", "id18", "id19",
                "id20", "id21", "id22", "id23", "id24", "id25"
        )) {
            if (displayName.equals("§a" + getZombieNameById(id))) {
                spawnCustomZombie(player, id);
                player.sendMessage("§a已生成 " + getZombieNameById(id) + "！");
                return;
            }
        }

        for (String idc : Arrays.asList("idc1", "idc2", "idc3", "idc4", "idc5", "idc6")) {
            if (displayName.equals("§b" + getEntityNameByIdc(idc))) {
                spawnOtherEntity(player, idc);
                player.sendMessage("§a已生成 " + getEntityNameByIdc(idc) + "！");
                return;
            }
        }
    }

    /**
     * 根据IDC生成自定义实体
     *
     * @param player  指令执行的玩家
     * @param otherId 实体ID（如idc1）
     */
    public void spawnOtherEntity(Player player, String otherId) {
        if (otherId == null) {
            player.sendMessage("§c实体ID不能为空！");
            getLogger().warning("spawnOtherEntity被调用时，otherId为null！");
            return;
        }

        // 获取生成位置，位于玩家旁边（向前2格）
        Location spawnLocation = player.getLocation().add(player.getLocation().getDirection().multiply(2));

        switch (otherId.toLowerCase()) {
            case "idc1": // 变异僵尸01
                spawnMutantZombie01(player, spawnLocation);
                break;
            case "idc2": // 变异僵尸02
                spawnMutantZombie02(player, spawnLocation);
                break;
            case "idc3": // 变异烈焰人
                spawnMutantBlazeMan(player, spawnLocation);
                break;
            case "idc4": // 变异爬行者
                spawnMutantCreeper(player, spawnLocation);
                break;
            case "idc5": // 变异末影螨
                spawnMutantEndermite(player, spawnLocation);
                break;
            case "idc6": // 变异蜘蛛
                spawnMutantSpider(player, spawnLocation);
                break;
            default:
                player.sendMessage("§c无效的实体IDC，请使用idc1 到 idc6");
                getLogger().warning("spawnOtherEntity被调用时，传入了无效的otherId: " + otherId);
                return;
        }

       // player.sendMessage("§a已成功生成 " + otherId.toUpperCase() + " 类型的实体！");
    }

    /**
     * 生成IDC3: 变异烈焰人
     *
     * @param player   生成实体的玩家
     * @param location 生成位置
     */
    public void spawnMutantBlazeMan(Player player, Location location) {
        // 生成烈焰人实体
        Blaze mutantBlaze = (Blaze) player.getWorld().spawnEntity(location, EntityType.BLAZE);

        // 设置烈焰人的自定义属性
        mutantBlaze.setCustomName("§6变异烈焰人");
        mutantBlaze.setCustomNameVisible(true);
        mutantBlaze.setMaxHealth(100.0);
        mutantBlaze.setHealth(100.0);

        // 添加速度4的药水效果（速度等级3，因为药水效果等级从0开始）
        mutantBlaze.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3));

        // 标记为变异烈焰人
        mutantBlaze.setMetadata("isMutantBlaze", new FixedMetadataValue(this, true));

        // 开始管理变异烈焰人的特殊任务
        startMutantBlazeTasks(mutantBlaze);
    }

    /**
     * 生成IDC5: 变异末影螨
     *
     * @param player   生成实体的玩家
     * @param location 生成位置
     */
    public void spawnMutantEndermite(Player player, Location location) {
        // 生成末影螨实体
        Endermite mutantEndermite = (Endermite) player.getWorld().spawnEntity(location, EntityType.ENDERMITE);

        // 设置末影螨的自定义属性
        mutantEndermite.setCustomName("§6变异末影螨");
        mutantEndermite.setCustomNameVisible(true);
        mutantEndermite.setMaxHealth(30.0);
        mutantEndermite.setHealth(30.0);

        // 添加速度2和跳跃提升2的药水效果
        mutantEndermite.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        mutantEndermite.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));

        // 添加与IDC1相同的粒子效果
        startMutantEndermiteTasks(mutantEndermite);

        // 标记为变异末影螨
        mutantEndermite.setMetadata("isMutantEndermite", new FixedMetadataValue(this, true));
    }

    /**
     * 开始管理变异末影螨的特殊任务
     *
     * @param endermite 变异末影螨实体
     */
    private void startMutantEndermiteTasks(Endermite endermite) {
        // 每3秒释放电流粒子效果并对范围内玩家造成伤害
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (endermite.isDead()) {
                    this.cancel();
                    mutantEndermiteTasks.remove(endermite);
                    return;
                }
                Location loc = endermite.getLocation();

                // 释放电流粒子效果（使用适合的粒子类型替代电流）
                ParticleEffect.CRIT_MAGIC.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 1, 50);

                // 对10x10范围内的玩家造成4点伤害
                for (Player player : endermite.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 10) {
                        player.damage(4.0, endermite);
                    }
                }
            }
        }.runTaskTimer(this, 0, 60); // 每3秒（60 ticks）执行一次

        // 存储任务，以便末影螨死亡时取消任务
        mutantEndermiteTasks.put(endermite, task);
    }

    /**
     * 生成IDC6: 变异蜘蛛
     *
     * @param player   生成实体的玩家
     * @param location 生成位置
     */
    public void spawnMutantSpider(Player player, Location location) {
        // 生成蜘蛛实体
        Spider mutantSpider = (Spider) player.getWorld().spawnEntity(location, EntityType.SPIDER);

        // 设置蜘蛛的自定义属性
        mutantSpider.setCustomName("§6变异蜘蛛");
        mutantSpider.setCustomNameVisible(true);
        mutantSpider.setMaxHealth(50.0);
        mutantSpider.setHealth(50.0);

        // 添加速度1和跳跃提升2的药水效果
        mutantSpider.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
        mutantSpider.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));

        // 添加与IDC1相同的粒子效果
        startMutantSpiderTasks(mutantSpider);

        // 标记为变异蜘蛛
        mutantSpider.setMetadata("isMutantSpider", new FixedMetadataValue(this, true));
    }

    /**
     * 开始管理变异蜘蛛的特殊任务
     *
     * @param spider 变异蜘蛛实体
     */
    private void startMutantSpiderTasks(Spider spider) {
        // 每5秒发射火球并造成伤害
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (spider.isDead()) {
                    this.cancel();
                    mutantSpiderTasks.remove(spider);
                    return;
                }
                Location loc = spider.getLocation();

                // 找到最近的玩家
                Player target = null;
                double minDistance = Double.MAX_VALUE;
                for (Player player : spider.getWorld().getPlayers()) {
                    double distance = player.getLocation().distance(loc);
                    if (distance < minDistance) {
                        minDistance = distance;
                        target = player;
                    }
                }

                if (target != null) {
                    // 发射三支火球
                    for (int i = 0; i < 3; i++) {
                        Fireball fireball = spider.launchProjectile(Fireball.class);
                        fireball.setDirection(target.getLocation().subtract(loc).toVector().normalize().multiply(1.0));
                        fireball.setYield(0); // 设置爆炸范围为0，防止造成方块破坏

                        // 设置火球的生命周期为10秒
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!fireball.isDead()) {
                                    fireball.remove();
                                }
                            }
                        }.runTaskLater(CustomZombie.this, 200); // 200 ticks = 10秒
                    }

                    // 造成5点伤害
                    if (target.getLocation().distance(loc) <= 10) { // 设定一个合理的范围
                        target.damage(5.0, spider);
                    }

                    // 释放火焰粒子效果
                    ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 10);
                }
            }
        }.runTaskTimer(this, 0, 100); // 每5秒（100 ticks）执行一次

        // 存储任务，以便蜘蛛死亡时取消任务
        mutantSpiderTasks.put(spider, task);
    }

    /**
     * 开始管理变异烈焰人的特殊任务
     *
     * @param blaze 变异烈焰人实体
     */
    private void startMutantBlazeTasks(Blaze blaze) {
        // 每3秒向最近的玩家发射烈焰弹，并释放烈焰粒子
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (blaze.isDead()) {
                    this.cancel();
                    mutantBlazeTasks.remove(blaze);
                    return;
                }
                Location loc = blaze.getLocation();
                Player target = null;
                double minDistance = Double.MAX_VALUE;

                // 找到最近的玩家
                for (Player player : blaze.getWorld().getPlayers()) {
                    double distance = player.getLocation().distance(loc);
                    if (distance < minDistance && distance <= 50) { // 设定一个合理的范围
                        minDistance = distance;
                        target = player;
                    }
                }

                if (target != null) {
                    // 发射烈焰弹
                    Fireball fireball = blaze.launchProjectile(Fireball.class);
                    fireball.setDirection(target.getLocation().subtract(loc).toVector().normalize().multiply(1.5));
                    fireball.setYield(0); // 设置爆炸范围为0，防止造成方块破坏

                    // 设置火球的生命周期为10秒
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!fireball.isDead()) {
                                fireball.remove();
                            }
                        }
                    }.runTaskLater(CustomZombie.this, 200); // 200 ticks = 10秒

                    // 释放烈焰粒子
                    ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 10);
                }
            }
        }.runTaskTimer(this, 0, 60); // 每3秒（60 ticks）执行一次

        // 存储任务，以便僵尸死亡时取消任务
        mutantBlazeTasks.put(blaze, task);
    }

    /**
     * 生成IDC4: 变异爬行者
     *
     * @param player   生成实体的玩家
     * @param location 生成位置
     */
    public void spawnMutantCreeper(Player player, Location location) {
        // 生成闪电苦力怕实体
        Creeper mutantCreeper = (Creeper) player.getWorld().spawnEntity(location, EntityType.CREEPER);

        // 设置爬行者的自定义属性
        mutantCreeper.setCustomName("§6变异爬行者");
        mutantCreeper.setCustomNameVisible(true);
        mutantCreeper.setMaxHealth(50.0);
        mutantCreeper.setHealth(50.0);

        // 添加速度6的药水效果（速度等级5）
        mutantCreeper.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 5));

        // 标记为变异爬行者
        mutantCreeper.setMetadata("isMutantCreeper", new FixedMetadataValue(this, true));

        // 开始管理变异爬行者的特殊任务
        startMutantCreeperTasks(mutantCreeper);
    }

    /**
     * 开始管理变异爬行者的特殊任务
     *
     * @param creeper 变异爬行者实体
     */
    private void startMutantCreeperTasks(Creeper creeper) {
        // 每5秒对最近的玩家释放一道闪电伤害
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (creeper.isDead()) {
                    this.cancel();
                    mutantCreeperTasks.remove(creeper);
                    return;
                }
                Location loc = creeper.getLocation();

                // 找到最近的玩家
                Player nearestPlayer = null;
                double minDistance = Double.MAX_VALUE;

                for (Player player : creeper.getWorld().getPlayers()) {
                    double distance = player.getLocation().distance(loc);
                    if (distance < minDistance && distance <= 50) { // 设置一个合理的范围，例如50格
                        minDistance = distance;
                        nearestPlayer = player;
                    }
                }

                if (nearestPlayer != null) {
                    Location playerLoc = nearestPlayer.getLocation();

                    // 释放闪电
                    creeper.getWorld().strikeLightning(playerLoc);

                    // 造成闪电伤害（闪电本身会造成伤害，无需额外处理）

                    // 释放粒子效果（可选）
                    ParticleEffect.CRIT_MAGIC.send(Bukkit.getOnlinePlayers(), playerLoc, 0, 0, 0, 0, 1);

                    // 播放闪电音效
                    creeper.getWorld().playSound(playerLoc, Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);
                }
            }
        }.runTaskTimer(this, 0, 100); // 每5秒（100 ticks）执行一次

        // 存储任务，以便爬行者死亡时取消任务
        mutantCreeperTasks.put(creeper, task);
    }


    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Blaze) {
            Blaze blaze = (Blaze) entity;
            if ("§6变异烈焰人".equals(blaze.getCustomName())) {
                Location loc = blaze.getLocation();
                // 释放紫色粒子效果
                ParticleEffect.PORTAL.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 1, 50);
                // 取消相关任务
                BukkitTask task = mutantBlazeTasks.get(blaze);
                if (task != null) {
                    task.cancel();
                    mutantBlazeTasks.remove(blaze);
                }
            }
        }

        // 处理受伤实体死亡时清理“变异博士”受到的伤害记录
        if (entity instanceof Zombie) {
            Zombie zombie = (Zombie) entity;
            if ("§B变异博士".equals(zombie.getCustomName())) { // 确保名称匹配
                mutantDoctorDamageReceived.remove(zombie);
            }
        }

        // 处理变异暗影僵尸的死亡
        if (entity instanceof Zombie) {
            Zombie zombie = (Zombie) entity;
            if ("§5变异暗影僵尸".equals(zombie.getCustomName())) {
                Location loc = zombie.getLocation();
                // 释放烟粒子效果
                ParticleEffect.SMOKE_NORMAL.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 1, 50);
                // 播放消失音效
                zombie.getWorld().playSound(loc, Sound.CREEPER_DEATH, 1.0F, 1.0F);
                // 取消相关任务
                BukkitTask task = mutantShadowZombieTasks.get(zombie);
                if (task != null) {
                    task.cancel();
                    mutantShadowZombieTasks.remove(zombie);
                }
            }
        }

        // 处理终极毁灭僵尸的死亡
        if (entity instanceof Zombie) {
            Zombie zombie = (Zombie) entity;
            if ("§4终极毁灭僵尸".equals(zombie.getCustomName())) {
                Location loc = zombie.getLocation();
                // 释放黑色烟雾粒子
                ParticleEffect.SMOKE_NORMAL.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 1, 50);
                // 播放爆炸音效
                zombie.getWorld().playSound(loc, Sound.EXPLODE, 1.0F, 1.0F);
                // 召唤4个暗影僵尸
                for (int i = 0; i < 4; i++) {
                    spawnCustomZombieDirect(loc, "id15"); // id15 是暗影僵尸
                }
                // 取消所有相关任务
                List<BukkitTask> tasks = ultimateDestructionZombieTasks.get(zombie);
                if (tasks != null) {
                    for (BukkitTask task : tasks) {
                        task.cancel();
                    }
                    ultimateDestructionZombieTasks.remove(zombie);
                }
            }

            if (entity instanceof Zombie) {
                if ("§c毁灭僵尸".equals(zombie.getCustomName())) {
                    Location loc = zombie.getLocation();
                    // 释放粒子效果
                    ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 1, 50);
                    // 播放爆炸音效
                    zombie.getWorld().playSound(loc, Sound.EXPLODE, 1.0F, 1.0F);
                    // 召唤2个普通僵尸
                    for (int i = 0; i < 2; i++) {
                        spawnCustomZombieDirect(loc, "id1"); // id1 是普通僵尸
                    }
                    // 检查是否在终极毁灭僵尸的范围内
                    for (Zombie ultimateZombie : ultimateDestructionZombieTasks.keySet()) {
                        if (ultimateZombie.isDead()) continue;
                        if (zombie.getLocation().distance(ultimateZombie.getLocation()) <= 10) { // 10格范围内
                            // 召唤1个变异僵尸01 (IDC1)
                            spawnMutantZombie01AtLocation(loc);
                        }
                    }
                    // 取消相关任务，如果有
                    List<BukkitTask> tasks = ultimateDestructionZombieTasks.get(zombie);
                    if (tasks != null) {
                        for (BukkitTask task : tasks) {
                            task.cancel();
                        }
                        ultimateDestructionZombieTasks.remove(zombie);
                    }
                }
            }
        }

        // 处理变异末影螨死亡
        if (entity instanceof Endermite) {
            Endermite endermite = (Endermite) entity;
            if ("§6变异末影螨".equals(endermite.getCustomName())) {
                Location loc = endermite.getLocation();
                // 释放粒子效果（可选择合适的粒子类型）
                ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 1, 50);
                // 取消相关任务
                BukkitTask task = mutantEndermiteTasks.get(endermite);
                if (task != null) {
                    task.cancel();
                    mutantEndermiteTasks.remove(endermite);
                }
            }
        }

        // 处理变异蜘蛛死亡
        if (entity instanceof Spider) {
            Spider spider = (Spider) entity;
            if ("§6变异蜘蛛".equals(spider.getCustomName())) {
                Location loc = spider.getLocation();
                // 释放粒子效果（可选择合适的粒子类型）
                ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 1, 50);
                // 播放爆炸音效
                spider.getWorld().playSound(loc, Sound.EXPLODE, 1.0F, 1.0F);
                // 取消相关任务
                BukkitTask task = mutantSpiderTasks.get(spider);
                if (task != null) {
                    task.cancel();
                    mutantSpiderTasks.remove(spider);
                }
            }
        }


        if (entity instanceof Creeper) {
            Creeper creeper = (Creeper) entity;
            if ("§6变异爬行者".equals(creeper.getCustomName())) {
                Location loc = creeper.getLocation();
                // 释放烟雾粒子效果
                ParticleEffect.SMOKE_NORMAL.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 1, 50);
                // 取消相关任务
                BukkitTask task = mutantCreeperTasks.get(creeper);
                if (task != null) {
                    task.cancel();
                    mutantCreeperTasks.remove(creeper);
                }
            }
        }
    }

    /**
     * 在指定位置生成变异僵尸01 (IDC1)
     *
     * @param location 生成位置
     */
    public void spawnMutantZombie01AtLocation(Location location) {
        // 生成僵尸猪人实体
        PigZombie mutantZombie = (PigZombie) location.getWorld().spawnEntity(location, EntityType.PIG_ZOMBIE);

        // 设置僵尸的自定义属性
        mutantZombie.setCustomName("§6变异僵尸01");
        mutantZombie.setCustomNameVisible(true);
        mutantZombie.setMaxHealth(100.0);
        mutantZombie.setHealth(100.0);

        // 装备：手持附魔锋利1铁剑
        ItemStack ironSword = new ItemStack(Material.IRON_SWORD);
        ironSword.addEnchantment(Enchantment.DAMAGE_ALL, 1); // 锋利1
        mutantZombie.getEquipment().setItemInHand(ironSword);

        // 装备：全套皮革护甲
        mutantZombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
        mutantZombie.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        mutantZombie.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        mutantZombie.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

        // 添加速度2和跳跃提升2的药水效果
        mutantZombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        mutantZombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));

        // 添加黑色烟雾粒子围成的正方体“气场”
        startMutantZombie01Particle(mutantZombie);

        // 标记为变异僵尸01
        mutantZombie.setMetadata("isMutantZombie01", new FixedMetadataValue(this, true));
    }


    @EventHandler
    public void onFireballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Fireball)) {
            return;
        }

        Fireball fireball = (Fireball) event.getEntity();
        if (!(fireball.getShooter() instanceof Blaze)) {
            return;
        }

        Blaze shooter = (Blaze) fireball.getShooter();
        String shooterName = shooter.getCustomName();

        if (shooterName == null || !shooterName.equals("§6变异烈焰人")) {
            return;
        }

        // 获取击中位置
        Location hitLocation = fireball.getLocation();

        // 查找附近的玩家（例如1.5格以内视为被击中）
        Player hitPlayer = null;
        double minDistance = Double.MAX_VALUE;
        for (Player player : shooter.getWorld().getPlayers()) {
            double distance = player.getLocation().distance(hitLocation);
            if (distance < minDistance && distance <= 1.5) { // 1.5格以内
                minDistance = distance;
                hitPlayer = player;
            }
        }

        if (hitPlayer != null) {
            // 造成8点伤害
            hitPlayer.damage(8.0, shooter);
            // 释放烈焰粒子
            ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), hitPlayer.getLocation(), 0, 0, 0, 0, 10);
        }
    }


    /**
     * 生成IDC2: 变异僵尸02
     *
     * @param player   生成实体的玩家
     * @param location 生成位置
     */
    public void spawnMutantZombie02(Player player, Location location) {
        // 生成骷髅实体
        Skeleton mutantZombie02 = (Skeleton) player.getWorld().spawnEntity(location, EntityType.SKELETON);

        // 设置骷髅的自定义属性
        mutantZombie02.setCustomName("§6变异僵尸02");
        mutantZombie02.setCustomNameVisible(true);
        mutantZombie02.setMaxHealth(100.0);
        mutantZombie02.setHealth(100.0);

        // 装备：手持附魔冲击2的弓
        ItemStack enchantedBow = new ItemStack(Material.BOW);
        enchantedBow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 2); // 冲击2
        mutantZombie02.getEquipment().setItemInHand(enchantedBow);

        // 装备：苦力怕头颅
        ItemStack creeperHead = new ItemStack(Material.SKULL_ITEM, 1, (short) 4); // 苦力怕头颅的物品ID为4
        ItemMeta headMeta = creeperHead.getItemMeta();
        headMeta.setDisplayName("§a苦力怕头颅");
        creeperHead.setItemMeta(headMeta);
        mutantZombie02.getEquipment().setHelmet(creeperHead);

        // 添加速度2和跳跃提升4的药水效果
        mutantZombie02.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        mutantZombie02.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 3)); // 跳跃提升4级

        // 添加与IDC1相同的粒子效果
        startMutantZombie02Particle(mutantZombie02);

        // 标记为变异僵尸02
        mutantZombie02.setMetadata("isMutantZombie02", new FixedMetadataValue(this, true));
    }

    /**
     * 为变异僵尸02添加黑色烟雾粒子围成的正方体“气场”
     *
     * @param mutantZombie02 变异僵尸02实体
     */
    private void startMutantZombie02Particle(Skeleton mutantZombie02) {
        BukkitTask particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (mutantZombie02.isDead()) {
                    this.cancel();
                    mutantZombie02Tasks.remove(mutantZombie02);
                    return;
                }
                Location loc = mutantZombie02.getLocation();
                // 生成黑色烟雾粒子围成的正方体
                ParticleEffect.SMOKE_NORMAL.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 1, 10);
            }
        }.runTaskTimer(this, 0, 10); // 每半秒（10 ticks）执行一次

        // 存储任务，以便僵尸死亡时取消任务
        mutantZombie02Tasks.put(mutantZombie02, particleTask);
    }

    /**
     * 监听骷髅实体死亡事件，处理变异僵尸02的特殊功能
     *
     * @param event 骷髅实体死亡事件
     */
    @EventHandler
    public void onSkeletonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Skeleton)) {
            return; // 不是骷髅不处理
        }

        Skeleton skeleton = (Skeleton) event.getEntity();
        String skeletonName = skeleton.getCustomName();

        if (skeletonName == null) {
            return; // 没有自定义名称的骷髅不处理
        }

        // 检查是否为变异僵尸02
        if ("§6变异僵尸02".equals(skeletonName)) {
            Location deathLocation = skeleton.getLocation();

            // 释放黑色烟雾粒子
            ParticleEffect.SMOKE_LARGE.send(Bukkit.getOnlinePlayers(), deathLocation, 1, 0, 0, 1, 50);

            // 播放爆炸音效
            skeleton.getWorld().playSound(deathLocation, Sound.EXPLODE, 1.0F, 1.0F);

            // 你可以在这里添加其他死亡效果，如生成其他实体或施加效果

            // 取消相关任务
            BukkitTask task = mutantZombie02Tasks.get(skeleton);
            if (task != null) {
                task.cancel();
                mutantZombie02Tasks.remove(skeleton);
            }
        }
    }

    /**
     * 生成IDC1: 变异僵尸01
     *
     * @param player   生成实体的玩家
     * @param location 生成位置
     */
    public void spawnMutantZombie01(Player player, Location location) {
        // 生成僵尸猪人实体
        PigZombie mutantZombie = (PigZombie) player.getWorld().spawnEntity(location, EntityType.PIG_ZOMBIE);

        // 设置僵尸的自定义属性
        mutantZombie.setCustomName("§6变异僵尸01");
        mutantZombie.setCustomNameVisible(true);
        mutantZombie.setMaxHealth(100.0);
        mutantZombie.setHealth(100.0);

        // 装备：手持附魔锋利1铁剑
        ItemStack ironSword = new ItemStack(Material.IRON_SWORD);
        ironSword.addEnchantment(Enchantment.DAMAGE_ALL, 1); // 锋利1
        mutantZombie.getEquipment().setItemInHand(ironSword);

        // 装备：全套皮革护甲
        mutantZombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
        mutantZombie.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        mutantZombie.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        mutantZombie.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

        // 添加速度2和跳跃提升2的药水效果
        mutantZombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        mutantZombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));

        // 添加黑色烟雾粒子围成的正方体“气场”
        startMutantZombie01Particle(mutantZombie);

        // 标记为变异僵尸01
        mutantZombie.setMetadata("isMutantZombie01", new FixedMetadataValue(this, true));
    }

    /**
     * 为变异僵尸01添加黑色烟雾粒子围成的正方体“气场”
     *
     * @param mutantZombie 变异僵尸01实体
     */
    private void startMutantZombie01Particle(PigZombie mutantZombie) {
        BukkitTask particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (mutantZombie.isDead()) {
                    this.cancel();
                    mutantZombie01Tasks.remove(mutantZombie);
                    return;
                }
                Location loc = mutantZombie.getLocation();
                // 生成黑色烟雾粒子围成的正方体
                ParticleEffect.SMOKE_NORMAL.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 1, 10);
            }
        }.runTaskTimer(this, 0, 10); // 每半秒（10 ticks）执行一次

        // 存储任务，以便僵尸死亡时取消任务
        mutantZombie01Tasks.put(mutantZombie, particleTask);
    }


    /**
     * 根据ID生成自定义僵尸
     *
     * @param player   指令执行的玩家
     * @param zombieId 僵尸ID（id1, id2, ..., id17）
     */
    public void spawnCustomZombie(Player player, String zombieId) {
        // 获取生成位置，位于玩家旁边（向前2格）
        Location spawnLocation = player.getLocation().add(player.getLocation().getDirection().multiply(2));

        // 生成僵尸实体
        Zombie zombie = (Zombie) player.getWorld().spawnEntity(spawnLocation, EntityType.ZOMBIE);

        // 设置僵尸的自定义属性
        switch (zombieId) {
            case "id1": // 普通僵尸
                zombie.setCustomName("§a普通僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(20.0);
                zombie.setHealth(20.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.WOOD_AXE)); // 手持木斧
                break;

            case "id2": // 小僵尸
                zombie.setCustomName("§b小僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(10.0);
                zombie.setHealth(10.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.WOOD_SPADE)); // 手持木铲（木棍在1.8.8中为木铲）
                // 添加速度2的药水效果（持续时间无限）
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                break;

            case "id3": // 路障僵尸
                zombie.setCustomName("§c路障僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(30.0);
                zombie.setHealth(30.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.WOOD_AXE)); // 手持木斧
                // 设置附有保护1的铁头盔
                ItemStack helmet3 = new ItemStack(Material.IRON_HELMET);
                ItemMeta helmetMeta3 = helmet3.getItemMeta();
                helmetMeta3.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                helmet3.setItemMeta(helmetMeta3);
                zombie.getEquipment().setHelmet(helmet3);
                break;

            case "id4": // 钻斧僵尸
                zombie.setCustomName("§d钻斧僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(20.0);
                zombie.setHealth(20.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_AXE)); // 手持钻石斧
                // 设置附有保护1的铁护腿
                ItemStack leggings4 = new ItemStack(Material.IRON_LEGGINGS);
                ItemMeta leggingsMeta4 = leggings4.getItemMeta();
                leggingsMeta4.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                leggings4.setItemMeta(leggingsMeta4);
                zombie.getEquipment().setLeggings(leggings4);
                break;

            case "id5": // 剧毒僵尸
                zombie.setCustomName("§5剧毒僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(10.0);
                zombie.setHealth(10.0);
                // 手持木棍
                zombie.getEquipment().setItemInHand(new ItemStack(Material.STICK));
                // 穿戴全套皮革护甲
                zombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                zombie.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                zombie.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                // 添加速度2的药水效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                break;

            case "id6": // 双生僵尸
                zombie.setCustomName("§6双生僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(20.0);
                zombie.setHealth(20.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD)); // 手持铁剑
                // 设置铁胸甲
                ItemStack chestplate6 = new ItemStack(Material.IRON_CHESTPLATE);
                zombie.getEquipment().setChestplate(chestplate6);

                // 添加每5分钟生成一个普通僵尸的任务
                BukkitTask spawnTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (zombie.isDead()) {
                            this.cancel();
                            twinsZombieTasks.remove(zombie);
                            return;
                        }
                        Location loc = zombie.getLocation().add(2, 0, 2); // 在双生僵尸附近生成
                        Zombie normalZombie = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
                        normalZombie.setCustomName("§a普通僵尸");
                        normalZombie.setCustomNameVisible(true);
                        normalZombie.setMaxHealth(20.0);
                        normalZombie.setHealth(20.0);
                        normalZombie.getEquipment().setItemInHand(new ItemStack(Material.WOOD_AXE)); // 手持木斧
                        // 生成粒子效果
                        ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);
                    }
                }.runTaskTimer(this, 0, 6000); // 每5分钟（6000 ticks）执行一次

                // 双生僵尸任务存储在正确的映射中
                twinsZombieTasks.put(zombie, spawnTask);
                break;

            case "id7": // 骷髅僵尸
                zombie.setCustomName("§7骷髅僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(30.0);
                zombie.setHealth(30.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD)); // 手持钻石剑
                // 设置铁护腿
                ItemStack leggings7 = new ItemStack(Material.IRON_LEGGINGS);
                zombie.getEquipment().setLeggings(leggings7);
                // 设置铁盔甲
                ItemStack helmet7 = new ItemStack(Material.IRON_HELMET);
                zombie.getEquipment().setHelmet(helmet7);
                // 开始发射箭矢的任务
                startShootingArrows(zombie);
                break;

            case "id8": // 武装僵尸
                zombie.setCustomName("§8武装僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(100.0);
                zombie.setHealth(100.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD)); // 手持钻石剑
                // 设置铁套（全身铁甲）
                zombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                zombie.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                zombie.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
                // 添加速度1的药水效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
                break;

            case "id9": // 肥胖僵尸
                zombie.setCustomName("§9肥胖僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(100.0);
                zombie.setHealth(100.0);
                // 无武器
                zombie.getEquipment().clear();
                // 无装备
                zombie.getEquipment().setHelmet(null);
                zombie.getEquipment().setChestplate(null);
                zombie.getEquipment().setLeggings(null);
                zombie.getEquipment().setBoots(null);
                // 标记为肥胖僵尸
                zombie.setMetadata("isFatZombie", new FixedMetadataValue(this, true));
                // 通过调用 setFatZombieSize 方法调整体型
                setFatZombieSize(zombie);
                break;

            case "id10": // 法师僵尸
                zombie.setCustomName("§4法师僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(50.0);
                zombie.setHealth(50.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.WOOD_AXE)); // 手持木斧
                // 设置全套链甲
                zombie.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                zombie.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                zombie.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
                // 开始召唤双生僵尸的任务
                startSummoningTwins(zombie);
                break;

            case "id11": // 自爆僵尸
                zombie.setCustomName("§4自爆僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(30.0);
                zombie.setHealth(30.0);
                // 无武器
                zombie.getEquipment().clear();
                // 设置带有保护1的皮革头盔
                ItemStack helmet11 = new ItemStack(Material.LEATHER_HELMET);
                ItemMeta helmetMeta11 = helmet11.getItemMeta();
                helmetMeta11.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                helmet11.setItemMeta(helmetMeta11);
                zombie.getEquipment().setHelmet(helmet11);
                break;

            case "id12": // 毒箭僵尸
                zombie.setCustomName("§5毒箭僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(50.0);
                zombie.setHealth(50.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.BOW)); // 手持弓
                // 设置铁护腿和铁盔甲
                zombie.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                zombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                // 开始发射毒箭的任务
                startShootingPoisonArrows(zombie);
                break;

            case "id13": // 电击僵尸
                zombie.setCustomName("§e电击僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(50.0);
                zombie.setHealth(50.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.STICK)); // 手持木棍
                // 设置铁套（全身铁甲）
                zombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                zombie.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                zombie.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
                // 开始释放电流的任务
                startShockEffect(zombie);
                break;

            case "id14": // 冰冻僵尸
                zombie.setCustomName("§b冰冻僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(70.0);
                zombie.setHealth(70.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD)); // 手持钻石剑
                // 设置带着锁链套（假设为链甲）
                zombie.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                zombie.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                zombie.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
                // 开始释放冻结效果的任务
                startFreezeEffect(zombie);
                break;

            // 新增的僵尸类型
            case "id15": // 暗影僵尸
                zombie.setCustomName("§5暗影僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(60.0);
                zombie.setHealth(60.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD)); // 手持铁剑
                // 设置黑色盔甲
                zombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                ItemMeta helmetMeta15 = zombie.getEquipment().getHelmet().getItemMeta();
                helmetMeta15.setDisplayName("§5暗影头盔");
                zombie.getEquipment().getHelmet().setItemMeta(helmetMeta15);
                // 添加隐身效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                // 开始隐匿攻击的任务
                startShadowAttack(zombie);
                break;

            case "id16": // 毁灭僵尸
                zombie.setCustomName("§c毁灭僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(150.0);
                zombie.setHealth(150.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD)); // 手持钻石剑
                // 设置全套铁护甲
                zombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                zombie.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                zombie.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
                // 添加力量效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
                break;

            case "id17": // 雷霆僵尸
                zombie.setCustomName("§b雷霆僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(80.0);
                zombie.setHealth(80.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.BOW)); // 手持弓
                // 设置全套链甲
                zombie.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                zombie.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                zombie.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
                // 开始释放雷电效果的任务
                startThunderEffect(zombie);
                break;
            case "id18": // 变异科学家
                zombie.setCustomName("§6变异科学家");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(600.0);
                zombie.setHealth(600.0);
                // 装备：附魔火焰附加二的钻石剑
                ItemStack diamondSword = new ItemStack(Material.DIAMOND_SWORD);
                diamondSword.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                zombie.getEquipment().setItemInHand(diamondSword);
                // 装备：全套附魔保护三的钻石盔甲，头戴史蒂夫头颅
                ItemStack helmet18 = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                ItemMeta helmetMeta18 = helmet18.getItemMeta();
                helmetMeta18.setDisplayName("§6史蒂夫头颅");
                helmet18.setItemMeta(helmetMeta18);
                ItemStack chestplate18 = new ItemStack(Material.DIAMOND_CHESTPLATE);
                chestplate18.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
                ItemStack leggings18 = new ItemStack(Material.DIAMOND_LEGGINGS);
                leggings18.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
                ItemStack boots18 = new ItemStack(Material.DIAMOND_BOOTS);
                boots18.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
                zombie.getEquipment().setHelmet(helmet18);
                zombie.getEquipment().setChestplate(chestplate18);
                zombie.getEquipment().setLeggings(leggings18);
                zombie.getEquipment().setBoots(boots18);
                // 添加速度2和跳跃提升2的药水效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
                // 标记为变异科学家
                zombie.setMetadata("isMutantScientist", new FixedMetadataValue(this, true));
                // 初始化变异科学家的伤害计数
                mutantScientistDamage.put(zombie, 0);
                // 开始管理变异科学家的特殊功能
                startMutantScientistTasks(zombie);
                break;
            case "id19": // 变异法师
                zombie.setCustomName("§6变异法师");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(400.0);
                zombie.setHealth(400.0);

                // 装备：手持木棍
                zombie.getEquipment().setItemInHand(new ItemStack(Material.STICK));

                // 装备：全套附魔保护三的皮革甲，头戴艾利克斯头颅
                ItemStack helmet19 = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // 艾利克斯头颅假设为某种头颅
                ItemMeta helmetMeta19 = helmet19.getItemMeta();
                helmetMeta19.setDisplayName("§6艾利克斯头颅");
                helmet19.setItemMeta(helmetMeta19);

                ItemStack chestplate19 = new ItemStack(Material.LEATHER_CHESTPLATE);
                chestplate19.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                ItemStack leggings19 = new ItemStack(Material.LEATHER_LEGGINGS);
                leggings19.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                ItemStack boots19 = new ItemStack(Material.LEATHER_BOOTS);
                boots19.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                zombie.getEquipment().setHelmet(helmet19);
                zombie.getEquipment().setChestplate(chestplate19);
                zombie.getEquipment().setLeggings(leggings19);
                zombie.getEquipment().setBoots(boots19);

                // 添加速度6和跳跃提升6的药水效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 5)); // 速度6 buff (等级5)
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 5)); // 跳跃提升6 buff (等级5)

                // 添加脚底火焰粒子圆圈
                startMutantMageFireParticles(zombie);

                // 添加与IDC1僵尸相同的粒子特效
                startMutantMageOtherParticles(zombie);

                // 标记为变异法师僵尸
                zombie.setMetadata("isMutantMage", new FixedMetadataValue(this, true));

                // 开始管理变异法师僵尸的特殊任务
                startMutantMageTasks(zombie);

                break;
            case "id20": // 气球僵尸
                zombie.setCustomName("§e气球僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(25.0);
                zombie.setHealth(25.0);
                // 装备：手持气球棒（自定义名称的木棍）
                ItemStack balloonStick = new ItemStack(Material.STICK);
                ItemMeta stickMeta = balloonStick.getItemMeta();
                stickMeta.setDisplayName("§6气球棒");
                balloonStick.setItemMeta(stickMeta);
                zombie.getEquipment().setItemInHand(balloonStick);
                // 添加飞行效果（缓慢上升）
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 0));
                // 开始气球僵尸的特殊任务
                startBalloonZombieTasks(zombie);
                break;

            case "id21": // 迷雾僵尸
                zombie.setCustomName("§8迷雾僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(40.0);
                zombie.setHealth(40.0);
                // 装备：手持迷雾生成器（自定义名称的铁锹）
                ItemStack fogGenerator = new ItemStack(Material.IRON_SPADE);
                ItemMeta fogMeta = fogGenerator.getItemMeta();
                fogMeta.setDisplayName("§7迷雾生成器");
                fogGenerator.setItemMeta(fogMeta);
                zombie.getEquipment().setItemInHand(fogGenerator);
                // 添加缓慢移动效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));
                // 开始迷雾僵尸的特殊任务
                startFogZombieTasks(zombie);
                break;
            case "id22": // 变异雷霆僵尸
                spawnMutantThunderZombie(player, spawnLocation);
                break;
            case "id23": // 终极毁灭僵尸
                zombie.setCustomName("§4终极毁灭僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(1000.0);
                zombie.setHealth(1000.0);

                // 设置装备：附魔锋利3的钻石剑
                ItemStack diamondSword23 = new ItemStack(Material.DIAMOND_SWORD);
                diamondSword23.addEnchantment(Enchantment.DAMAGE_ALL, 3); // 锋利3
                zombie.getEquipment().setItemInHand(diamondSword23);

                // 设置全套附魔保护3的钻石盔甲（除了头盔）
                ItemStack helmet23 = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // 史蒂夫头颅
                ItemMeta helmetMeta23 = helmet23.getItemMeta();
                helmetMeta23.setDisplayName("§6史蒂夫头颅");
                helmet23.setItemMeta(helmetMeta23);

                ItemStack chestplate23 = new ItemStack(Material.DIAMOND_CHESTPLATE);
                chestplate23.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                ItemStack leggings23 = new ItemStack(Material.DIAMOND_LEGGINGS);
                leggings23.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                ItemStack boots23 = new ItemStack(Material.DIAMOND_BOOTS);
                boots23.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                zombie.getEquipment().setHelmet(helmet23);
                zombie.getEquipment().setChestplate(chestplate23);
                zombie.getEquipment().setLeggings(leggings23);
                zombie.getEquipment().setBoots(boots23);

                // 初始化任务列表
                ultimateDestructionZombieTasks.put(zombie, new ArrayList<>());

                // 添加黑色烟雾粒子围成的正方体"气场"
                startUltimateDestructionZombieParticles(zombie);

                // 添加速度2、跳跃提升2、力量1的药水效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1)); // 速度2
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1)); // 跳跃提升2
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0)); // 力量1

                // 标记为终极毁灭僵尸
                zombie.setMetadata("isUltimateDestructionZombie", new FixedMetadataValue(this, true));

                // 启动终极毁灭僵尸的特殊任务
                startUltimateDestructionZombieTasks(zombie);
                break;
            case "id24": // 变异暗影僵尸
                zombie.setCustomName("§5变异暗影僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(500.0);
                zombie.setHealth(500.0);

                // 设置手持锋利5的钻石剑
                ItemStack diamondSword24 = new ItemStack(Material.DIAMOND_SWORD);
                diamondSword24.addEnchantment(Enchantment.DAMAGE_ALL, 5); // 锋利5
                zombie.getEquipment().setItemInHand(diamondSword24);

                // 设置佩戴保护5+荆棘2的钻石头盔
                ItemStack helmet24 = new ItemStack(Material.DIAMOND_HELMET);
                ItemMeta helmetMeta24 = helmet24.getItemMeta();
                helmetMeta24.setDisplayName("§5变异暗影头盔");
                helmetMeta24.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 10, true); // 保护5
                helmetMeta24.addEnchant(Enchantment.THORNS, 5, true); // 荆棘2
                helmet24.setItemMeta(helmetMeta24);
                zombie.getEquipment().setHelmet(helmet24);

                // 设置钻石护甲（胸甲、护腿和靴子）自定义名称
                ItemStack chestplate24 = new ItemStack(Material.DIAMOND_CHESTPLATE);
                ItemMeta chestMeta24 = chestplate24.getItemMeta();
                chestMeta24.setDisplayName("§5变异暗影胸甲");
                chestMeta24.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 10, true); // 保护5
                chestplate24.setItemMeta(chestMeta24);
                zombie.getEquipment().setChestplate(chestplate24);


                // 添加隐身效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));

                // 添加速度3的药水效果（速度等级2，因为等级从0开始）
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));

                // 添加力量1的药水效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));

                // 标记为变异暗影僵尸
                zombie.setMetadata("isMutantShadowZombie", new FixedMetadataValue(this, true));

                // 开始管理变异暗影僵尸的特殊任务
                startMutantShadowZombieTasks(zombie);
                break;
            case "id25": // 变异博士
                zombie.setCustomName("§5变异博士");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(2048.0);
                zombie.setHealth(2048.0);

                // 装备：手持附魔火焰附加二的钻石剑
                ItemStack diamondSword25 = new ItemStack(Material.DIAMOND_SWORD);
                diamondSword25.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                zombie.getEquipment().setItemInHand(diamondSword25);

                // 装备：全套附魔保护三的钻石盔甲（头带史蒂夫头颅）
                ItemStack helmet25 = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // 史蒂夫头颅
                ItemMeta helmetMeta25 = helmet25.getItemMeta();
                helmetMeta25.setDisplayName("§5史蒂夫头颅");
                helmet25.setItemMeta(helmetMeta25);

                ItemStack chestplate25 = new ItemStack(Material.DIAMOND_CHESTPLATE);
                chestplate25.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                ItemStack leggings25 = new ItemStack(Material.DIAMOND_LEGGINGS);
                leggings25.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                ItemStack boots25 = new ItemStack(Material.DIAMOND_BOOTS);
                boots25.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                zombie.getEquipment().setHelmet(helmet25);
                zombie.getEquipment().setChestplate(chestplate25);
                zombie.getEquipment().setLeggings(leggings25);
                zombie.getEquipment().setBoots(boots25);

                // 添加速度6的药水效果（持续时间无限）
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 5)); // 速度6 buff (等级5)

                // 添加跳跃提升效果（根据需求，可以添加跳跃提升buff）
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2)); // 跳跃提升2 buff

                // 添加身体四周产生大量特效粒子
                startMutantDoctorParticles(zombie);

                // 标记为变异博士
                zombie.setMetadata("isMutantDoctor", new FixedMetadataValue(this, true));

                // 初始化变异博士的任务列表
                mutantDoctorTasks.put(zombie, new ArrayList<>());

                // 开始管理变异博士的特殊功能任务
                startMutantDoctorTasks(zombie);
                break;



            default:
                player.sendMessage("§c无效的僵尸ID，请使用id1, id2, ..., id25");
                zombie.remove(); // 移除无效的僵尸
                return;
        }



        // 生成粒子效果（此处可根据需要调整粒子类型和参数）
        ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), spawnLocation, 0, 0, 0, 0, 1);

        player.sendMessage("§a已成功生成 " + zombieId.toUpperCase() + " 类型的僵尸！");
    }

    /**
     * 开始管理变异博士的特殊功能任务
     *
     * @param doctor 变异博士实体
     */
    private void startMutantDoctorTasks(Zombie doctor) {
        // 初始化任务列表，如果尚未存在
        mutantDoctorTasks.putIfAbsent(doctor, new ArrayList<>());
        List<BukkitTask> tasks = mutantDoctorTasks.get(doctor);

        // 1. 每30秒从id1~id24中随机召唤一个僵尸
        BukkitTask summonZombieTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (doctor.isDead()) {
                    this.cancel();
                    mutantDoctorTasks.remove(doctor);
                    return;
                }
                Location loc = doctor.getLocation().add(2, 0, 2); // 在变异博士附近生成

                // 随机选择一个ID从id1到id24
                String[] zombieIds = {"id1", "id2", "id3", "id4", "id5", "id6", "id7", "id8", "id9",
                        "id10", "id11", "id12", "id13", "id14", "id15", "id16", "id17",
                        "id18", "id19", "id20", "id21", "id22", "id23", "id24"};
                String randomZombieId = zombieIds[new Random().nextInt(zombieIds.length)];
                spawnCustomZombieDirect(loc, randomZombieId); // 使用已有的方法生成僵尸

                // 生成粒子效果（可选）
                ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);
            }
        }.runTaskTimer(this, 0, 6000); // 每30秒（6000 ticks）执行一次

        tasks.add(summonZombieTask);


        // 3. 当玩家在20*20范围内时，自动向玩家发射弓箭或烈焰弹
        BukkitTask attackTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (doctor.isDead()) {
                    this.cancel();
                    List<BukkitTask> doctorTasks = mutantDoctorTasks.get(doctor);
                    if (doctorTasks != null) {
                        doctorTasks.remove(this);
                    }
                    return;
                }
                Location loc = doctor.getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 20) {
                        // 随机选择发射弓箭或烈焰弹
                        if (new Random().nextBoolean()) {
                            // 发射弓箭
                            Arrow arrow = doctor.launchProjectile(Arrow.class);
                            arrow.setVelocity(doctor.getLocation().getDirection().multiply(2));
                        } else {
                            // 发射烈焰弹
                            Fireball fireball = doctor.launchProjectile(Fireball.class);
                            fireball.setDirection(player.getLocation().subtract(loc).toVector().normalize().multiply(1.5));
                            fireball.setYield(0); // 防止破坏方块
                            // 设置烈焰弹的生命周期为10秒
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (!fireball.isDead()) {
                                        fireball.remove();
                                    }
                                }
                            }.runTaskLater(CustomZombie.this, 40); // 200 ticks = 10秒
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0, 60); // 每3秒（60 ticks）执行一次
        tasks.add(attackTask);

        // 4. 每3分钟对玩家造成一次巨额电击伤害（16血以上）
        BukkitTask massiveShockTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (doctor.isDead()) {
                    this.cancel();
                    List<BukkitTask> doctorTasks = mutantDoctorTasks.get(doctor);
                    if (doctorTasks != null) {
                        doctorTasks.remove(this);
                    }
                    return;
                }
                Location loc = doctor.getLocation();
                for (Player player : doctor.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 20) {
                        player.damage(16.0, doctor);
                        // 释放电流粒子效果
                        ParticleEffect.CRIT.send(Bukkit.getOnlinePlayers(), player.getLocation(), 0, 0, 0, 0, 50);
                        // 播放电击音效
                        player.getWorld().playSound(player.getLocation(), Sound.DOOR_OPEN, 1.0F, 1.0F);
                    }
                }
            }
        }.runTaskTimer(this, 0, 3600); // 每3分钟（3600 ticks）执行一次
        tasks.add(massiveShockTask);

        // 5. 每4分钟冻结周围所有玩家5秒，使其无法移动和攻击
        BukkitTask freezePlayersTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (doctor.isDead()) {
                    this.cancel();
                    List<BukkitTask> doctorTasks = mutantDoctorTasks.get(doctor);
                    if (doctorTasks != null) {
                        doctorTasks.remove(this);
                    }
                    return;
                }
                Location loc = doctor.getLocation();
                for (Player player : doctor.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 20) {
                        // 添加冻结效果（无移动和攻击，使用 Slowness 和 Jump Boost）
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 255)); // 5秒
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, 255)); // 5秒
                        // 播放冻结音效
                        player.getWorld().playSound(player.getLocation(), Sound.WATER, 1.0F, 1.0F);
                    }
                }
            }
        }.runTaskTimer(this, 0, 4800); // 每4分钟（4800 ticks）执行一次
        tasks.add(freezePlayersTask);

        // 6. 每3秒对自己10*10的范围内释放电流（粒子效果替代，造成4点伤害）
        BukkitTask areaShockTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (doctor.isDead()) {
                    this.cancel();
                    List<BukkitTask> doctorTasks = mutantDoctorTasks.get(doctor);
                    if (doctorTasks != null) {
                        doctorTasks.remove(this);
                    }
                    return;
                }
                Location loc = doctor.getLocation();
                // 释放电流粒子效果
                ParticleEffect.CRIT.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 100);
                // 对10*10范围内的玩家造成4点伤害
                for (Player player : doctor.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 10) {
                        player.damage(4.0, doctor);
                    }
                }
            }
        }.runTaskTimer(this, 0, 60); // 每3秒（60 ticks）执行一次
        tasks.add(areaShockTask);

        // 7. 每收到50点伤害召唤一个变异科学家
        // 该功能将在 onEntityDamageByEntity 事件中处理
    }




    /**
     * 开始管理变异博士的粒子效果任务
     *
     * @param doctor 变异博士实体
     */
    private void startMutantDoctorParticles(Zombie doctor) {
        // 检查实体是否为 null
        if (doctor == null || doctor.isDead()) {
            getLogger().warning("尝试为 null 或已死亡的变异博士启动粒子任务。");
            return;
        }

        // 创建一个任务来持续生成粒子效果
        BukkitTask particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                // 如果实体已死亡，取消任务并移除映射
                if (doctor.isDead()) {
                    this.cancel();
                    mutantDoctorTasks.remove(doctor);
                    return;
                }

                // 获取实体的位置，确保位置不为 null
                Location loc = doctor.getLocation();
                if (loc == null) {
                    getLogger().warning("变异博士的位置为 null，取消粒子任务。");
                    this.cancel();
                    mutantDoctorTasks.remove(doctor);
                    return;
                }

                // 生成黑色烟雾粒子围成的正方体“气场”
                try {
                    ParticleEffect.SMOKE_NORMAL.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 1, 10);
                } catch (Exception e) {
                    getLogger().severe("生成粒子效果时发生错误: " + e.getMessage());
                    this.cancel();
                    mutantDoctorTasks.remove(doctor);
                }
            }
        }.runTaskTimer(this, 0, 10); // 每半秒（10 ticks）执行一次

        // 存储任务，以便变异博士死亡时取消任务
        mutantDoctorTasks.computeIfAbsent(doctor, k -> new ArrayList<>()).add(particleTask);
    }


    /**
     * 开始管理变异暗影僵尸的特殊任务
     *
     * @param zombie 变异暗影僵尸实体
     */
    private void startMutantShadowZombieTasks(Zombie zombie) {
        // 定义任务列表以存储所有相关任务
        List<BukkitTask> tasks = new ArrayList<>();

        // 1. 每5秒瞬移到最近的玩家附近并造成高额伤害，同时播放攻击音效
        BukkitTask teleportAttackTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantShadowZombieTasks.remove(zombie);
                    return;
                }

                Location zombieLocation = zombie.getLocation();
                Player nearestPlayer = null;
                double minDistance = Double.MAX_VALUE;

                // 查找最近的玩家
                for (Player player : zombie.getWorld().getPlayers()) {
                    double distance = player.getLocation().distance(zombieLocation);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestPlayer = player;
                    }
                }

                if (nearestPlayer != null) {
                    // 计算目标位置（玩家附近）
                    Location targetLocation = nearestPlayer.getLocation().add(0, 0, 0); // 可根据需要调整偏移量
                    zombie.teleport(targetLocation);

                    // 造成高额伤害（例如20点）
                    nearestPlayer.damage(20.0, zombie);

                    // 播放攻击音效
                    zombie.getWorld().playSound(targetLocation, Sound.ENDERDRAGON_GROWL, 1.0F, 1.0F);

                    // 释放烟粒子效果
                    ParticleEffect.SMOKE_NORMAL.send(Bukkit.getOnlinePlayers(), targetLocation, 1, 0, 0, 0, 10);
                }
            }
        }.runTaskTimer(this, 0, 100); // 每5秒（100 ticks）执行一次
        tasks.add(teleportAttackTask);

        // 2. 每10秒生成一个暗影僵尸（调用暗影僵尸代码）
        BukkitTask spawnShadowZombieTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantShadowZombieTasks.remove(zombie);
                    return;
                }

                Location spawnLocation = zombie.getLocation().add(2, 0, 2); // 在暗影僵尸附近生成
                spawnCustomZombieDirect(spawnLocation, "id15"); // id15 是暗影僵尸
            }
        }.runTaskTimer(this, 0, 200); // 每10秒（200 ticks）执行一次
        tasks.add(spawnShadowZombieTask);

        // 3. 每2秒使所有玩家获得失明和反胃9级buff，持续2秒
        BukkitTask applyNegativeEffectsTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantShadowZombieTasks.remove(zombie);
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 8)); // 失明3秒（60 ticks），等级9
                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 8)); // 反胃3秒（60 ticks），等级9
                }
            }
        }.runTaskTimer(this, 0, 40); // 每2秒（40 ticks）执行一次
        tasks.add(applyNegativeEffectsTask);

        // 将所有任务存储到映射中
        mutantShadowZombieTasks.put(zombie, teleportAttackTask);
        // 注：这里仅存储第一个任务，如果需要存储所有任务，可以修改数据结构
    }


    // 3. 实现 startUltimateDestructionZombieTasks 方法
    private void startUltimateDestructionZombieTasks(Zombie zombie) {
        List<BukkitTask> tasks = new ArrayList<>();
        // 1. 攻击玩家时，玩家获得buff
        BukkitTask attackBuffTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    ultimateDestructionZombieTasks.get(zombie).remove(this);
                    return;
                }
                // 您可以在这里添加攻击时给玩家添加buff的逻辑
                // 例如监听攻击事件并添加buff
            }
        }.runTaskTimer(this, 0, 20); // 每秒检查一次

        ultimateDestructionZombieTasks.get(zombie).add(attackBuffTask);
        // 2. 每30秒生成3个毁灭僵尸
        BukkitTask spawnDestructionZombiesTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    ultimateDestructionZombieTasks.get(zombie).remove(this);
                    return;
                }
                Location loc = zombie.getLocation();
                for (int i = 0; i < 3; i++) {
                    spawnCustomZombieDirect(loc, "id16"); // id16 是毁灭僵尸
                }
                // 生成粒子效果
                ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 10);
            }
        }.runTaskTimer(this, 0, 600); // 每30秒（600 ticks）执行一次

        ultimateDestructionZombieTasks.get(zombie).add(spawnDestructionZombiesTask);

        // 3. 每40秒在玩家旁边生成一次小型爆炸（粒子+音效），造成5点伤害
        BukkitTask smallExplosionTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    ultimateDestructionZombieTasks.get(zombie).remove(this);
                    return;
                }
                Location loc = zombie.getLocation();
                // 生成小型爆炸粒子效果
                ParticleEffect.EXPLOSION_LARGE.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 0, 10);
                // 播放爆炸音效
                zombie.getWorld().playSound(loc, Sound.EXPLODE, 1.0F, 1.0F);
                // 对周围玩家造成5点伤害
                for (Player player : zombie.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 5) { // 5格范围内
                        player.damage(5.0, zombie);
                    }
                }
            }
        }.runTaskTimer(this, 0, 800); // 每40秒（800 ticks）执行一次

        ultimateDestructionZombieTasks.get(zombie).add(smallExplosionTask);

        // 存储所有任务
        ultimateDestructionZombieTasks.put(zombie, tasks);
    }

    private void startUltimateDestructionZombieParticles(Zombie zombie) {
        // 确保任务列表存在
        if (!ultimateDestructionZombieTasks.containsKey(zombie)) {
            ultimateDestructionZombieTasks.put(zombie, new ArrayList<>());
        }

        BukkitTask particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    List<BukkitTask> tasks = ultimateDestructionZombieTasks.get(zombie);
                    if (tasks != null) {
                        tasks.remove(this);
                    }
                    return;
                }
                Location loc = zombie.getLocation();
                // 生成黑色烟雾粒子围成的正方体
                ParticleEffect.SMOKE_NORMAL.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 0, 10);
            }
        }.runTaskTimer(this, 0, 10); // 每半秒（10 ticks）执行一次

        ultimateDestructionZombieTasks.get(zombie).add(particleTask);
    }


    // 4. 处理攻击事件，应用虚弱2 buff 并击退玩家4格
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Zombie)) {
            return;
        }
        Entity entity = event.getEntity();

        if (!(entity instanceof LivingEntity)) {
            return;
        }
        // Zombie damager = (Zombie) event.getDamager();
        Entity target = event.getEntity();
        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();


        // 检查是否为终极毁灭僵尸攻击
        if (damager instanceof Zombie) {
            Zombie zombie = (Zombie) damager;
            if ("§4终极毁灭僵尸".equals(zombie.getCustomName())) {
                if (target instanceof Player) {
                    Player player = (Player) target;
                    // 为玩家添加虚弱2 buff，持续3秒
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1)); // 60 ticks = 3秒
                    // 击退玩家4格距离
                    Location zombieLoc = zombie.getLocation();
                    Location playerLoc = player.getLocation();
                    Vector knockback = playerLoc.toVector().subtract(zombieLoc.toVector()).normalize().multiply(4);
                    player.setVelocity(knockback);
                    // 播放攻击音效
                    player.playSound(playerLoc, Sound.ZOMBIE_METAL, 2.0F, 2.0F);
                }
            }
        }

// 检查是否是变异博士
        if (damager instanceof Zombie) {
            Zombie doctor = (Zombie) damager;
            if (doctor.hasMetadata("isMutantDoctor")) {
                if (damaged instanceof Player) {
                    Player player = (Player) damaged;

                    // 5. 每次攻击使玩家获得虚弱2buff持续3秒，并且击退玩家4格距离（产生音效）
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1)); // 3秒
                    // 计算击退方向
                    Vector knockback = player.getLocation().getDirection().multiply(-4).setY(0.5);
                    player.setVelocity(knockback);
                    // 播放击退音效
                    player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_METAL, 2.0F, 2.0F);

                    // 8. 每收到50点伤害召唤一个变异科学家
                    // 获取当前累计伤害
                    int currentDamage = mutantScientistDamage.getOrDefault(doctor, 0) + (int) event.getFinalDamage();
                    mutantScientistDamage.put(doctor, currentDamage);

                    if (currentDamage >= 50) {
                        // 重置累计伤害
                        mutantScientistDamage.put(doctor, 0);
                        // 召唤一个变异科学家
                        spawnCustomZombieDirect(doctor.getLocation(), "id18");
                        doctor.getWorld().playSound(doctor.getLocation(), Sound.ZOMBIE_METAL, 1.0F, 1.0F);
                    }
                }
            }
        }



    }


    /**
     * 监听实体受到伤害事件，处理变异博士的特殊功能
     *
     * @param event 实体受到伤害事件
     */
    @EventHandler
    public void onDoctorDamage(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();

        // 检查被攻击的实体是否为变异博士
        if (damaged instanceof Zombie) {
            Zombie doctor = (Zombie) damaged;
            if (doctor.hasMetadata("isMutantDoctor")) { // 确认实体是变异博士
                double damage = event.getFinalDamage();

                // 获取当前累计的伤害值
                int currentDamage = mutantDoctorDamageReceived.getOrDefault(doctor, 0);
                currentDamage += (int) damage;

                // 检查是否累计达到40点伤害
                if (currentDamage >= 40) {
                    // 计算需要召唤的次数
                    int summonTimes = currentDamage / 40;
                    // 更新剩余的累计伤害
                    currentDamage = currentDamage % 40;

                    for (int i = 0; i < summonTimes; i++) {
                        // 随机选择一个IDC1~IDC6
                        String[] idcIds = {"idc1", "idc2", "idc3", "idc4", "idc5", "idc6"};
                        String randomIdcId = idcIds[new Random().nextInt(idcIds.length)];
                        spawnOtherEntity(doctor.getKiller(), randomIdcId); // 使用已有的方法生成实体
                    }

                    // 更新累计伤害
                    mutantDoctorDamageReceived.put(doctor, currentDamage);
                } else {
                    // 更新累计伤害
                    mutantDoctorDamageReceived.put(doctor, currentDamage);
                }
            }
        }
    }

    /**
     * 为变异法师僵尸添加脚底火焰粒子圆圈
     *
     * @param zombie 变异法师僵尸实体
     */
    private void startMutantMageFireParticles(Zombie zombie) {
        BukkitTask fireParticleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantMageLightningTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                // 在脚底生成火焰粒子围成的圆圈
                ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 1, 10);
            }
        }.runTaskTimer(this, 0, 10); // 每半秒（10 ticks）执行一次

        // 存储任务
        mutantMageLightningTasks.put(zombie, fireParticleTask);
    }

    /**
     * 为变异法师僵尸添加与IDC1僵尸相同的粒子特效
     *
     * @param zombie 变异法师僵尸实体
     */
    private void startMutantMageOtherParticles(Zombie zombie) {
        BukkitTask otherParticleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantMageAuraTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                // 添加与IDC1僵尸相同的黑色烟雾粒子围成的正方体“气场”
                ParticleEffect.SMOKE_NORMAL.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 1, 10);
            }
        }.runTaskTimer(this, 0, 10); // 每半秒（10 ticks）执行一次

        // 存储任务
        mutantMageAuraTasks.put(zombie, otherParticleTask);
    }

    /**
     * 开始管理变异法师僵尸的特殊任务
     *
     * @param zombie 变异法师僵尸实体
     */
    private void startMutantMageTasks(Zombie zombie) {
        // 任务1：每5秒对6*6范围内的玩家进行闪电击中
        BukkitTask lightningTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantMageLightningTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                for (Player player : zombie.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 6) {
                        // 召唤闪电
                        loc.getWorld().strikeLightning(player.getLocation());
                    }
                }
            }
        }.runTaskTimer(this, 0, 100); // 每5秒（100 ticks）执行一次

        // 存储任务
        mutantMageLightningTasks.put(zombie, lightningTask);

        // 任务2：持续检查10*10范围内的玩家，施加失明和减速效果
        BukkitTask auraTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantMageAuraTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                for (Player player : zombie.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 10) {
                        // 施加失明和减速效果
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, false, false));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, false));
                    } else {
                        // 移除失明和减速效果
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                        player.removePotionEffect(PotionEffectType.SLOW);
                    }
                }
            }
        }.runTaskTimer(this, 0, 20); // 每1秒（20 ticks）执行一次

        // 存储任务
        mutantMageAuraTasks.put(zombie, auraTask);

        // 任务3：每10秒检测5*5范围内是否有玩家，如果有则生成两个法师僵尸（ID10）和一个冰冻僵尸（ID14）
        BukkitTask summonTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantMageSummonTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                boolean hasPlayer = false;
                for (Player player : zombie.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 5) {
                        hasPlayer = true;
                        break;
                    }
                }
                if (hasPlayer) {
                    // 生成两个法师僵尸（ID10）和一个冰冻僵尸（ID14）
                    spawnCustomZombieDirect(loc, "id10");
                    spawnCustomZombieDirect(loc, "id10"); // 生成第二个法师僵尸
                    spawnCustomZombieDirect(loc, "id14");
                }
            }
        }.runTaskTimer(this, 0, 400); // 每20秒（400 ticks）执行一次

        // 存储任务
        mutantMageSummonTasks.put(zombie, summonTask);
    }

    /**
     * 直接在指定位置生成自定义僵尸，不依赖于玩家
     *
     * @param location 生成位置
     * @param zombieId 僵尸ID（id1, id2, ..., id19）
     */
    public void spawnCustomZombieDirect(Location location, String zombieId) {
        // 生成僵尸实体
        Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);

        // 设置僵尸的自定义属性
        switch (zombieId) {
            case "id1": // 普通僵尸
                zombie.setCustomName("§a普通僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(20.0);
                zombie.setHealth(20.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.WOOD_AXE)); // 手持木斧
                break;

            case "id2": // 小僵尸
                zombie.setCustomName("§b小僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(10.0);
                zombie.setHealth(10.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.WOOD_SPADE)); // 手持木铲（木棍在1.8.8中为木铲）
                // 添加速度2的药水效果（持续时间无限）
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                break;

            case "id3": // 路障僵尸
                zombie.setCustomName("§c路障僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(30.0);
                zombie.setHealth(30.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.WOOD_AXE)); // 手持木斧
                // 设置附有保护1的铁头盔
                ItemStack helmet3 = new ItemStack(Material.IRON_HELMET);
                ItemMeta helmetMeta3 = helmet3.getItemMeta();
                helmetMeta3.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                helmet3.setItemMeta(helmetMeta3);
                zombie.getEquipment().setHelmet(helmet3);
                break;

            case "id4": // 钻斧僵尸
                zombie.setCustomName("§d钻斧僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(20.0);
                zombie.setHealth(20.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_AXE)); // 手持钻石斧
                // 设置附有保护1的铁护腿
                ItemStack leggings4 = new ItemStack(Material.IRON_LEGGINGS);
                ItemMeta leggingsMeta4 = leggings4.getItemMeta();
                leggingsMeta4.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                leggings4.setItemMeta(leggingsMeta4);
                zombie.getEquipment().setLeggings(leggings4);
                break;

            case "id5": // 剧毒僵尸
                zombie.setCustomName("§5剧毒僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(10.0);
                zombie.setHealth(10.0);
                // 手持木棍
                zombie.getEquipment().setItemInHand(new ItemStack(Material.STICK));
                // 穿戴全套皮革护甲
                zombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                zombie.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                zombie.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                // 添加速度2的药水效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                break;

            case "id6": // 双生僵尸
                zombie.setCustomName("§6双生僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(20.0);
                zombie.setHealth(20.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD)); // 手持铁剑
                // 设置铁胸甲
                ItemStack chestplate6 = new ItemStack(Material.IRON_CHESTPLATE);
                zombie.getEquipment().setChestplate(chestplate6);

                // 添加每5分钟生成一个普通僵尸的任务
                BukkitTask spawnTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (zombie.isDead()) {
                            this.cancel();
                            shadowZombieTasks.remove(zombie);
                            return;
                        }
                        Location loc = zombie.getLocation().add(2, 0, 2); // 在双生僵尸附近生成
                        Zombie normalZombie = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
                        normalZombie.setCustomName("§a普通僵尸");
                        normalZombie.setCustomNameVisible(true);
                        normalZombie.setMaxHealth(20.0);
                        normalZombie.setHealth(20.0);
                        normalZombie.getEquipment().setItemInHand(new ItemStack(Material.WOOD_AXE)); // 手持木斧
                        // 生成粒子效果
                        ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);
                    }
                }.runTaskTimer(this, 0, 6000); // 每5分钟（6000 ticks）执行一次

                // 双生僵尸任务存储在正确的映射中
                twinsZombieTasks.put(zombie, spawnTask);
                break;

            case "id7": // 骷髅僵尸
                zombie.setCustomName("§7骷髅僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(30.0);
                zombie.setHealth(30.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD)); // 手持钻石剑
                // 设置铁护腿
                ItemStack leggings7 = new ItemStack(Material.IRON_LEGGINGS);
                zombie.getEquipment().setLeggings(leggings7);
                // 设置铁盔甲
                ItemStack helmet7 = new ItemStack(Material.IRON_HELMET);
                zombie.getEquipment().setHelmet(helmet7);
                // 开始发射箭矢的任务
                startShootingArrows(zombie);
                break;

            case "id8": // 武装僵尸
                zombie.setCustomName("§8武装僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(100.0);
                zombie.setHealth(100.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD)); // 手持钻石剑
                // 设置铁套（全身铁甲）
                zombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                zombie.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                zombie.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
                // 添加速度1的药水效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
                break;

            case "id9": // 肥胖僵尸
                zombie.setCustomName("§9肥胖僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(100.0);
                zombie.setHealth(100.0);
                // 无武器
                zombie.getEquipment().clear();
                // 无装备
                zombie.getEquipment().setHelmet(null);
                zombie.getEquipment().setChestplate(null);
                zombie.getEquipment().setLeggings(null);
                zombie.getEquipment().setBoots(null);
                // 标记为肥胖僵尸
                zombie.setMetadata("isFatZombie", new FixedMetadataValue(this, true));
                // 通过调用 setFatZombieSize 方法调整体型
                setFatZombieSize(zombie);
                break;

            case "id10": // 法师僵尸
                zombie.setCustomName("§4法师僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(50.0);
                zombie.setHealth(50.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.WOOD_AXE)); // 手持木斧
                // 设置全套链甲
                zombie.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                zombie.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                zombie.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
                // 开始召唤双生僵尸的任务
                startSummoningTwins(zombie);
                break;

            case "id11": // 自爆僵尸
                zombie.setCustomName("§4自爆僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(30.0);
                zombie.setHealth(30.0);
                // 无武器
                zombie.getEquipment().clear();
                // 设置带有保护1的皮革头盔
                ItemStack helmet11 = new ItemStack(Material.LEATHER_HELMET);
                ItemMeta helmetMeta11 = helmet11.getItemMeta();
                helmetMeta11.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
                helmet11.setItemMeta(helmetMeta11);
                zombie.getEquipment().setHelmet(helmet11);
                break;

            case "id12": // 毒箭僵尸
                zombie.setCustomName("§5毒箭僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(50.0);
                zombie.setHealth(50.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.BOW)); // 手持弓
                // 设置铁护腿和铁盔甲
                zombie.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                zombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                // 开始发射毒箭的任务
                startShootingPoisonArrows(zombie);
                break;

            case "id13": // 电击僵尸
                zombie.setCustomName("§e电击僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(50.0);
                zombie.setHealth(50.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.STICK)); // 手持木棍
                // 设置铁套（全身铁甲）
                zombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                zombie.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                zombie.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
                // 开始释放电流的任务
                startShockEffect(zombie);
                break;

            case "id14": // 冰冻僵尸
                zombie.setCustomName("§b冰冻僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(70.0);
                zombie.setHealth(70.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD)); // 手持钻石剑
                // 设置带着锁链套（假设为链甲）
                zombie.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                zombie.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                zombie.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
                // 开始释放冻结效果的任务
                startFreezeEffect(zombie);
                break;

            // 新增的僵尸类型
            case "id15": // 暗影僵尸
                zombie.setCustomName("§5暗影僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(60.0);
                zombie.setHealth(60.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD)); // 手持铁剑
                // 设置黑色盔甲
                zombie.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                ItemMeta helmetMeta15 = zombie.getEquipment().getHelmet().getItemMeta();
                helmetMeta15.setDisplayName("§5暗影头盔");
                zombie.getEquipment().getHelmet().setItemMeta(helmetMeta15);
                // 添加隐身效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                // 开始隐匿攻击的任务
                startShadowAttack(zombie);
                break;

            case "id16": // 毁灭僵尸
                zombie.setCustomName("§c毁灭僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(150.0);
                zombie.setHealth(150.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD)); // 手持钻石剑
                // 设置全套铁护甲
                zombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                zombie.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                zombie.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
                // 添加力量效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
                break;

            case "id17": // 雷霆僵尸
                zombie.setCustomName("§b雷霆僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(80.0);
                zombie.setHealth(80.0);
                zombie.getEquipment().setItemInHand(new ItemStack(Material.BOW)); // 手持弓
                // 设置全套链甲
                zombie.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                zombie.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                zombie.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
                // 开始释放雷电效果的任务
                startThunderEffect(zombie);
                break;
            case "id18": // 变异科学家
                zombie.setCustomName("§6变异科学家");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(600.0);
                zombie.setHealth(600.0);
                // 装备：附魔火焰附加二的钻石剑
                ItemStack diamondSword = new ItemStack(Material.DIAMOND_SWORD);
                diamondSword.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                zombie.getEquipment().setItemInHand(diamondSword);
                // 装备：全套附魔保护三的钻石盔甲，头戴史蒂夫头颅
                ItemStack helmet18 = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                ItemMeta helmetMeta18 = helmet18.getItemMeta();
                helmetMeta18.setDisplayName("§6史蒂夫头颅");
                helmet18.setItemMeta(helmetMeta18);
                ItemStack chestplate18 = new ItemStack(Material.DIAMOND_CHESTPLATE);
                chestplate18.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
                ItemStack leggings18 = new ItemStack(Material.DIAMOND_LEGGINGS);
                leggings18.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
                ItemStack boots18 = new ItemStack(Material.DIAMOND_BOOTS);
                boots18.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
                zombie.getEquipment().setHelmet(helmet18);
                zombie.getEquipment().setChestplate(chestplate18);
                zombie.getEquipment().setLeggings(leggings18);
                zombie.getEquipment().setBoots(boots18);
                // 添加速度2和跳跃提升2的药水效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
                // 标记为变异科学家
                zombie.setMetadata("isMutantScientist", new FixedMetadataValue(this, true));
                // 初始化变异科学家的伤害计数
                mutantScientistDamage.put(zombie, 0);
                // 开始管理变异科学家的特殊功能
                startMutantScientistTasks(zombie);
                break;
            case "id19": // 变异法师
                zombie.setCustomName("§6变异法师");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(400.0);
                zombie.setHealth(400.0);

                // 装备：手持木棍
                zombie.getEquipment().setItemInHand(new ItemStack(Material.STICK));

                // 装备：全套附魔保护三的皮革甲，头戴艾利克斯头颅
                ItemStack helmet19 = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // 艾利克斯头颅假设为某种头颅
                ItemMeta helmetMeta19 = helmet19.getItemMeta();
                helmetMeta19.setDisplayName("§6艾利克斯头颅");
                helmet19.setItemMeta(helmetMeta19);

                ItemStack chestplate19 = new ItemStack(Material.LEATHER_CHESTPLATE);
                chestplate19.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                ItemStack leggings19 = new ItemStack(Material.LEATHER_LEGGINGS);
                leggings19.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                ItemStack boots19 = new ItemStack(Material.LEATHER_BOOTS);
                boots19.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                zombie.getEquipment().setHelmet(helmet19);
                zombie.getEquipment().setChestplate(chestplate19);
                zombie.getEquipment().setLeggings(leggings19);
                zombie.getEquipment().setBoots(boots19);

                // 添加速度6和跳跃提升6的药水效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 5)); // 速度6 buff (等级5)
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 5)); // 跳跃提升6 buff (等级5)

                // 添加脚底火焰粒子圆圈
                startMutantMageFireParticles(zombie);

                // 添加与IDC1僵尸相同的粒子特效
                startMutantMageOtherParticles(zombie);

                // 标记为变异法师僵尸
                zombie.setMetadata("isMutantMage", new FixedMetadataValue(this, true));

                // 开始管理变异法师僵尸的特殊任务
                startMutantMageTasks(zombie);

                break;
            case "id20": // 气球僵尸
                zombie.setCustomName("§e气球僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(25.0);
                zombie.setHealth(25.0);
                // 装备：手持气球棒（自定义名称的木棍）
                ItemStack balloonStick = new ItemStack(Material.STICK);
                ItemMeta stickMeta = balloonStick.getItemMeta();
                stickMeta.setDisplayName("§6气球棒");
                balloonStick.setItemMeta(stickMeta);
                zombie.getEquipment().setItemInHand(balloonStick);
                // 添加飞行效果（缓慢上升）
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 0));
                // 开始气球僵尸的特殊任务
                startBalloonZombieTasks(zombie);
                break;

            case "id21": // 迷雾僵尸
                zombie.setCustomName("§8迷雾僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(40.0);
                zombie.setHealth(40.0);
                // 装备：手持迷雾生成器（自定义名称的铁锹）
                ItemStack fogGenerator = new ItemStack(Material.IRON_SPADE);
                ItemMeta fogMeta = fogGenerator.getItemMeta();
                fogMeta.setDisplayName("§7迷雾生成器");
                fogGenerator.setItemMeta(fogMeta);
                zombie.getEquipment().setItemInHand(fogGenerator);
                // 添加缓慢移动效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));
                // 开始迷雾僵尸的特殊任务
                startFogZombieTasks(zombie);
                break;
            case "id23": // 终极毁灭僵尸
                zombie.setCustomName("§4终极毁灭僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(1000.0);
                zombie.setHealth(1000.0);

                // 设置装备：附魔锋利3的钻石剑
                ItemStack diamondSword23 = new ItemStack(Material.DIAMOND_SWORD);
                diamondSword23.addEnchantment(Enchantment.DAMAGE_ALL, 3); // 锋利3
                zombie.getEquipment().setItemInHand(diamondSword23);

                // 设置全套附魔保护3的钻石盔甲（除了头盔）
                ItemStack helmet23 = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // 史蒂夫头颅
                ItemMeta helmetMeta23 = helmet23.getItemMeta();
                helmetMeta23.setDisplayName("§6史蒂夫头颅");
                helmet23.setItemMeta(helmetMeta23);

                ItemStack chestplate23 = new ItemStack(Material.DIAMOND_CHESTPLATE);
                chestplate23.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                ItemStack leggings23 = new ItemStack(Material.DIAMOND_LEGGINGS);
                leggings23.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                ItemStack boots23 = new ItemStack(Material.DIAMOND_BOOTS);
                boots23.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                zombie.getEquipment().setHelmet(helmet23);
                zombie.getEquipment().setChestplate(chestplate23);
                zombie.getEquipment().setLeggings(leggings23);
                zombie.getEquipment().setBoots(boots23);

                // 初始化任务列表
                ultimateDestructionZombieTasks.put(zombie, new ArrayList<>());

                // 添加黑色烟雾粒子围成的正方体"气场"
                startUltimateDestructionZombieParticles(zombie);

                // 添加速度2、跳跃提升2、力量1的药水效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1)); // 速度2
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1)); // 跳跃提升2
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0)); // 力量1

                // 标记为终极毁灭僵尸
                zombie.setMetadata("isUltimateDestructionZombie", new FixedMetadataValue(this, true));

                // 启动终极毁灭僵尸的特殊任务
                startUltimateDestructionZombieTasks(zombie);
                break;
            case "id24": // 变异暗影僵尸
                zombie.setCustomName("§5变异暗影僵尸");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(500.0);
                zombie.setHealth(500.0);

                // 设置手持锋利5的钻石剑
                ItemStack diamondSword24 = new ItemStack(Material.DIAMOND_SWORD);
                diamondSword24.addEnchantment(Enchantment.DAMAGE_ALL, 5); // 锋利5
                zombie.getEquipment().setItemInHand(diamondSword24);

                // 设置佩戴保护5+荆棘2的钻石头盔
                ItemStack helmet24 = new ItemStack(Material.DIAMOND_HELMET);
                ItemMeta helmetMeta24 = helmet24.getItemMeta();
                helmetMeta24.setDisplayName("§5变异暗影头盔");
                helmetMeta24.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 10, true); // 保护5
                helmetMeta24.addEnchant(Enchantment.THORNS, 5, true); // 荆棘2
                helmet24.setItemMeta(helmetMeta24);
                zombie.getEquipment().setHelmet(helmet24);

                // 设置钻石护甲（胸甲、护腿和靴子）自定义名称
                ItemStack chestplate24 = new ItemStack(Material.DIAMOND_CHESTPLATE);
                ItemMeta chestMeta24 = chestplate24.getItemMeta();
                chestMeta24.setDisplayName("§5变异暗影胸甲");
                chestMeta24.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 10, true); // 保护5
                chestplate24.setItemMeta(chestMeta24);
                zombie.getEquipment().setChestplate(chestplate24);


                // 添加隐身效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));

                // 添加速度3的药水效果（速度等级2，因为等级从0开始）
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));

                // 添加力量1的药水效果
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));

                // 标记为变异暗影僵尸
                zombie.setMetadata("isMutantShadowZombie", new FixedMetadataValue(this, true));

                // 开始管理变异暗影僵尸的特殊任务
                startMutantShadowZombieTasks(zombie);
                break;
            case "id25": // 变异博士
                zombie.setCustomName("§5变异博士");
                zombie.setCustomNameVisible(true);
                zombie.setMaxHealth(2048.0);
                zombie.setHealth(2048.0);

                // 装备：手持附魔火焰附加二的钻石剑
                ItemStack diamondSword25 = new ItemStack(Material.DIAMOND_SWORD);
                diamondSword25.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                zombie.getEquipment().setItemInHand(diamondSword25);

                // 装备：全套附魔保护三的钻石盔甲（头带史蒂夫头颅）
                ItemStack helmet25 = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // 史蒂夫头颅
                ItemMeta helmetMeta25 = helmet25.getItemMeta();
                helmetMeta25.setDisplayName("§5史蒂夫头颅");
                helmet25.setItemMeta(helmetMeta25);

                ItemStack chestplate25 = new ItemStack(Material.DIAMOND_CHESTPLATE);
                chestplate25.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                ItemStack leggings25 = new ItemStack(Material.DIAMOND_LEGGINGS);
                leggings25.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                ItemStack boots25 = new ItemStack(Material.DIAMOND_BOOTS);
                boots25.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                zombie.getEquipment().setHelmet(helmet25);
                zombie.getEquipment().setChestplate(chestplate25);
                zombie.getEquipment().setLeggings(leggings25);
                zombie.getEquipment().setBoots(boots25);

                // 添加速度6的药水效果（持续时间无限）
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 5)); // 速度6 buff (等级5)

                // 添加跳跃提升效果（根据需求，可以添加跳跃提升buff）
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2)); // 跳跃提升2 buff

                // 添加身体四周产生大量特效粒子
                startMutantDoctorParticles(zombie);

                // 标记为变异博士
                zombie.setMetadata("isMutantDoctor", new FixedMetadataValue(this, true));

                // 初始化变异博士的任务列表
                mutantDoctorTasks.put(zombie, new ArrayList<>());

                // 开始管理变异博士的特殊功能任务
                startMutantDoctorTasks(zombie);
                break;

            default:
                // 其他ID不处理
                zombie.remove();
                return;
        }

        // 生成粒子效果
        ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), location, 0, 0, 0, 0, 1);
    }

    /**
     * 启动毁灭僵尸的Buff任务，使其周围10x10范围内的毁灭僵尸获得力量2和速度2 buff
     *
     * @param zombie 毁灭僵尸实体
     */
    private void startDestructionZombieBuffTask(Zombie zombie) {
        BukkitTask buffTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    // 检查映射是否存在，再移除任务
                    List<BukkitTask> tasks = ultimateDestructionZombieTasks.getOrDefault(zombie, new ArrayList<>());
                    tasks.remove(this);
                    if (tasks.isEmpty()) {
                        ultimateDestructionZombieTasks.remove(zombie);
                    }
                    return;
                }
                Location loc = zombie.getLocation();
                for (Entity entity : loc.getWorld().getEntities()) {
                    if (entity instanceof Zombie) {
                        Zombie nearbyZombie = (Zombie) entity;
                        if ("§c毁灭僵尸".equals(nearbyZombie.getCustomName()) && nearbyZombie.getLocation().distance(loc) <= 10) {
                            nearbyZombie.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600, 1));
                            nearbyZombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1));
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0, 600);

        // 如果任务列表为空，初始化新列表
        List<BukkitTask> tasks = ultimateDestructionZombieTasks.computeIfAbsent(zombie, k -> new ArrayList<>());
        tasks.add(buffTask);
    }


    /**
     * 开始让骷髅僵尸定期发射箭矢
     *
     * @param zombie 需要发射箭矢的骷髅僵尸
     */
    private void startShootingArrows(Zombie zombie) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    skeletonZombieTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                // 找到最近的玩家
                Player target = null;
                double minDistance = Double.MAX_VALUE;
                for (Player player : zombie.getWorld().getPlayers()) {
                    double distance = player.getLocation().distance(loc);
                    if (distance < minDistance) {
                        minDistance = distance;
                        target = player;
                    }
                }
                if (target != null) {
                    // 发射三支箭矢
                    for (int i = 0; i < 3; i++) {
                        Arrow arrow = zombie.launchProjectile(Arrow.class);
                        arrow.setShooter(zombie);
                        // 设置箭矢消失时间为10秒
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!arrow.isDead()) {
                                    arrow.remove();
                                }
                            }
                        }.runTaskLater(CustomZombie.this, 200); // 200 ticks = 10秒
                    }
                }
            }
        }.runTaskTimer(this, 0, 60); // 每3秒（60 ticks）执行一次

        // 存储任务，以便僵尸死亡时取消任务
        skeletonZombieTasks.put(zombie, task);
    }

    /**
     * 开始让法师僵尸定期召唤双生僵尸
     *
     * @param zombie 需要召唤双生僵尸的法师僵尸
     */
    private void startSummoningTwins(Zombie zombie) {
        BukkitTask spawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie == null || zombie.isDead() || !zombie.isValid()) { // 添加检查
                    this.cancel();
                    twinsZombieTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation().add(2, 0, 2); // 在双生僵尸附近生成
                Zombie normalZombie = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
                normalZombie.setCustomName("§a普通僵尸");
                normalZombie.setCustomNameVisible(true);
                normalZombie.setMaxHealth(20.0);
                normalZombie.setHealth(20.0);
                normalZombie.getEquipment().setItemInHand(new ItemStack(Material.WOOD_AXE)); // 手持木斧
                // 生成粒子效果
                ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);
            }
        }.runTaskTimer(this, 0, 6000); // 每5分钟（6000 ticks）执行一次

        // 存储任务，以便双生僵尸死亡时取消任务
        twinsZombieTasks.put(zombie, spawnTask);
    }

    /**
     * 开始让毒箭僵尸定期发射带有剧毒的箭矢
     *
     * @param zombie 需要发射毒箭的毒箭僵尸
     */
    private void startShootingPoisonArrows(Zombie zombie) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    poisonArrowZombieTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                // 找到最近的玩家
                Player target = null;
                double minDistance = Double.MAX_VALUE;
                for (Player player : zombie.getWorld().getPlayers()) {
                    double distance = player.getLocation().distance(loc);
                    if (distance < minDistance) {
                        minDistance = distance;
                        target = player;
                    }
                }
                if (target != null) {
                    // 发射三支带有剧毒的箭矢
                    for (int i = 0; i < 3; i++) {
                        Arrow arrow = zombie.launchProjectile(Arrow.class);
                        arrow.setShooter(zombie);
                        // 设置箭矢消失时间为10秒
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!arrow.isDead()) {
                                    arrow.remove();
                                }
                            }
                        }.runTaskLater(CustomZombie.this, 200); // 200 ticks = 10秒
                    }
                }
            }
        }.runTaskTimer(this, 0, 60); // 每3秒（60 ticks）执行一次

        // 存储任务，以便僵尸死亡时取消任务
        poisonArrowZombieTasks.put(zombie, task);
    }

    /**
     * 开始让电击僵尸定期释放电流
     *
     * @param zombie 需要释放电流的电击僵尸
     */
    private void startShockEffect(Zombie zombie) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    shockZombieTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                // 释放电流粒子效果
                ParticleEffect.CRIT_MAGIC.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 1, 50);
                // 对10x10范围内的玩家造成4点伤害
                for (Player player : zombie.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 10) {
                        player.damage(4.0, zombie);
                    }
                }
            }
        }.runTaskTimer(this, 0, 60); // 每3秒（60 ticks）执行一次

        // 存储任务，以便僵尸死亡时取消任务
        shockZombieTasks.put(zombie, task);
    }

    /**
     * 开始让冰冻僵尸定期释放冻结效果
     *
     * @param zombie 需要释放冻结效果的冰冻僵尸
     */
    private void startFreezeEffect(Zombie zombie) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    freezeZombieTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                // 释放冻结粒子效果
                ParticleEffect.SNOWBALL.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 1, 50);
                // 播放冻结音效
                loc.getWorld().playSound(loc, Sound.GLASS, 1.0F, 1.0F);
                // 对10x10范围内的玩家造成4点伤害并施加减速2的buff持续2秒
                for (Player player : zombie.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 10) {
                        player.damage(4.0, zombie);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1)); // 2秒（40 ticks），减速2
                    }
                }
            }
        }.runTaskTimer(this, 0, 60); // 每3秒（60 ticks）执行一次

        // 存储任务，以便僵尸死亡时取消任务
        freezeZombieTasks.put(zombie, task);
    }

    /**
     * 开始让暗影僵尸执行隐匿攻击的任务
     *
     * @param zombie 需要执行隐匿攻击的暗影僵尸
     */
    private void startShadowAttack(Zombie zombie) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    shadowZombieTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                // 找到最近的玩家
                Player target = null;
                double minDistance = Double.MAX_VALUE;
                for (Player player : zombie.getWorld().getPlayers()) {
                    double distance = player.getLocation().distance(loc);
                    if (distance < minDistance && distance <= 15) { // 在15格范围内
                        minDistance = distance;
                        target = player;
                    }
                }
                if (target != null) {
                    // 暗影僵尸瞬移到玩家附近并进行攻击
                    Location targetLoc = target.getLocation();
                    Location teleportLoc = targetLoc.add(0, 1, 0);
                    zombie.teleport(teleportLoc);
                    // 播放瞬移粒子效果
                    ParticleEffect.PORTAL.send(Bukkit.getOnlinePlayers(), teleportLoc, 0, 0, 0, 0, 1);
                    // 对玩家造成高额伤害
                    target.damage(15.0, zombie);
                    // 播放攻击音效
                    target.playSound(target.getLocation(), Sound.BLAZE_HIT, 1.0F, 1.0F);
                }
            }
        }.runTaskTimer(this, 0, 100); // 每5秒（100 ticks）执行一次

        // 存储任务，以便僵尸死亡时取消任务
        shadowZombieTasks.put(zombie, task);
    }

    /**
     * 开始让雷霆僵尸定期释放雷电
     *
     * @param zombie 需要释放雷电的雷霆僵尸
     */
    private void startThunderEffect(Zombie zombie) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    thunderZombieTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                // 释放雷电粒子效果
                ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);
                // 播放雷电音效
                loc.getWorld().playSound(loc, Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);
                // 对10x10范围内的玩家造成8点伤害
                for (Player player : zombie.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 10) {
                        player.damage(8.0, zombie);
                    }
                }
            }
        }.runTaskTimer(this, 0, 150); // 每7.5秒（150 ticks）执行一次

        // 存储任务，以便僵尸死亡时取消任务
        thunderZombieTasks.put(zombie, task);
    }

    // 合并后的事件处理方法
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // 判断被攻击实体是否为玩家，攻击者是否为僵尸或僵尸猪人
        if (event.getEntity() instanceof Player && (event.getDamager() instanceof Zombie || event.getDamager() instanceof PigZombie)) {
            Player player = (Player) event.getEntity();
            Entity damager = event.getDamager();
            String damagerName = "";
            Zombie zombie = (Zombie) event.getDamager();

            if (damager instanceof Zombie) {
                damagerName = ((Zombie) damager).getCustomName();
            } else if (damager instanceof PigZombie) {
                damagerName = ((PigZombie) damager).getCustomName();
            }

            if (damagerName == null) {
                return; // 如果攻击者没有自定义名称，则不处理
            }
            // 处理变异僵尸01的伤害和效果
            if (damagerName.equals("§6变异僵尸01")) {
                // 造成2点伤害
                event.setDamage(2.0);

                // 施加剧毒效果，持续3秒
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1)); // 60 ticks = 3秒，剧毒2

                // 你也可以添加其他特效，例如播放声音或生成粒子
                player.getWorld().playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);
                ParticleEffect.SPELL_MOB.send(Bukkit.getOnlinePlayers(), player.getLocation(), 1, 0, 0, 0, 10);
            }
            // 获取僵尸的自定义名称
            String zombieName = zombie.getCustomName();

            if (zombieName == null) {
                return; // 如果僵尸没有自定义名称，则不处理
            }

            // 处理变异科学家的伤害计数和召唤逻辑
            if (zombie.hasMetadata("isMutantScientist")) {
                // 获取当前伤害计数
                int currentDamage = mutantScientistDamage.getOrDefault(zombie, 0);
                // 增加受到的伤害
                currentDamage += event.getFinalDamage();
                mutantScientistDamage.put(zombie, currentDamage);
                // 检查是否达到50点伤害
                if (currentDamage >= 50) {
                    // 重置伤害计数
                    mutantScientistDamage.put(zombie, 0);
                    // 随机生成武装僵尸 (ID8) 或毁灭僵尸 (ID16)
                    boolean summonArmedZombie = Math.random() < 0.5;
                    Zombie summonedZombie;
                    if (summonArmedZombie) {
                        // 生成武装僵尸 (ID8)
                        summonedZombie = (Zombie) zombie.getWorld().spawnEntity(zombie.getLocation(), EntityType.ZOMBIE);
                        summonedZombie.setCustomName("§8武装僵尸");
                        summonedZombie.setCustomNameVisible(true);
                        summonedZombie.setMaxHealth(100.0);
                        summonedZombie.setHealth(100.0);
                        summonedZombie.getEquipment().setItemInHand(new ItemStack(Material.DIAMOND_SWORD));
                        summonedZombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                        summonedZombie.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                        summonedZombie.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                        summonedZombie.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
                        // 添加速度1的药水效果
                        summonedZombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
                    } else {
                        // 生成毁灭僵尸 (ID16)
                        summonedZombie = (Zombie) zombie.getWorld().spawnEntity(zombie.getLocation(), EntityType.ZOMBIE);
                        summonedZombie.setCustomName("§c毁灭僵尸");
                        summonedZombie.setCustomNameVisible(true);
                        summonedZombie.setMaxHealth(150.0);
                        summonedZombie.setHealth(150.0);
                        // 设置钻石剑并附魔
                        ItemStack destructionSword = new ItemStack(Material.DIAMOND_SWORD);
                        destructionSword.addEnchantment(Enchantment.DAMAGE_ALL, 5); // 示例附魔
                        summonedZombie.getEquipment().setItemInHand(destructionSword);
                        // 设置全套铁护甲
                        summonedZombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                        summonedZombie.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                        summonedZombie.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                        summonedZombie.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
                        // 添加力量效果
                        summonedZombie.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
                    }
                    // 释放粒子效果
                    ParticleEffect.SMOKE_NORMAL.send(Bukkit.getOnlinePlayers(), zombie.getLocation(), 0, 0, 0, 0, 50);
                }
            }

            // 处理自爆僵尸的伤害
            if ("§4自爆僵尸".equals(damagerName)) {
                // 取消当前事件，避免递归调用
                event.setCancelled(true);

                // 检查僵尸是否已经爆炸过，防止重复爆炸
                if (!damager.hasMetadata("hasExploded")) {
                    damager.setMetadata("hasExploded", new FixedMetadataValue(this, true));
                    Location loc = damager.getLocation();
                    // 造成10点爆炸伤害给附近玩家（6格范围）
                    for (Player nearbyPlayer : damager.getWorld().getPlayers()) {
                        if (nearbyPlayer.getLocation().distance(loc) <= 6) { // 影响范围为6格
                            nearbyPlayer.damage(10.0, damager);
                        }
                    }
                    // 播放爆炸音效，并释放红色粒子
                    damager.getWorld().playSound(loc, Sound.EXPLODE, 1.0F, 1.0F);
                    ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 0, 1);
                    // 移除自爆僵尸
                    damager.remove();
                }
            }


            // 根据僵尸类型设置伤害值
            switch (zombieName) {
                case "§a普通僵尸":
                case "§c路障僵尸":
                case "§7骷髅僵尸":
                case "§9肥胖僵尸":
                    event.setDamage(4.0); // 普通僵尸、路障僵尸、骷髅僵尸和肥胖僵尸造成4点伤害
                    break;

                case "§b小僵尸":
                    event.setDamage(2.0); // 小僵尸造成2点伤害
                    break;

                case "§d钻斧僵尸":
                    event.setDamage(8.0); // 钻斧僵尸造成8点伤害
                    break;

                case "§5剧毒僵尸":
                    event.setDamage(1.0); // 剧毒僵尸造成1点伤害
                    // 施加剧毒效果，持续3秒，剧毒1级
                    player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0)); // 60 ticks = 3秒，剧毒1
                    break;


                case "§6双生僵尸":
                    event.setDamage(2.0); // 双生僵尸造成2点伤害
                    break;

                case "§8武装僵尸":
                    event.setDamage(8.0); // 武装僵尸造成8点伤害
                    break;

                case "§4法师僵尸":
                    event.setDamage(1.0); // 法师僵尸造成1点伤害
                    break;

                case "§4自爆僵尸":
                    event.setDamage(1.0); // 僵尸造成1点伤害
                    break;

                case "§5毒箭僵尸":
                    event.setDamage(4.0); // 毒箭僵尸造成4点伤害
                    break;

                case "§e电击僵尸":
                    event.setDamage(4.0); // 电击僵尸造成4点伤害
                    break;

                case "§b冰冻僵尸":
                    event.setDamage(5.0); // 冰冻僵尸造成5点伤害
                    break;

                // 新增僵尸类型的伤害处理
                case "§5暗影僵尸":
                    event.setDamage(12.0); // 暗影僵尸造成12点伤害
                    break;

                case "§c毁灭僵尸":
                    event.setDamage(15.0); // 毁灭僵尸造成15点伤害
                    break;

                case "§b雷霆僵尸":
                    event.setDamage(8.0); // 雷霆僵尸造成8点伤害
                    break;

                default:
                    // 其他僵尸类型不处理
                    break;
            }

            // 处理自爆僵尸靠近玩家时的自爆逻辑
            if (zombieName.equals("§4自爆僵尸")) {
                // 由于在上面的case中已经处理了自爆逻辑，这里无需再次处理
                // 避免重复触发
            }
        }
    }

    /**
     * 监听箭矢命中事件，用于处理毒箭效果
     *
     * @param event 箭矢命中事件
     */
    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) {
            return;
        }

        Arrow arrow = (Arrow) event.getEntity();

        if (!(arrow.getShooter() instanceof Zombie)) {
            return;
        }

        Zombie shooter = (Zombie) arrow.getShooter();
        String shooterName = shooter.getCustomName();

        if (shooterName == null) {
            return;
        }

        // 获取箭矢的击中位置
        Location hitLocation = arrow.getLocation();

        if (shooterName.equals("§5毒箭僵尸")) {
            // 查找附近的玩家作为箭矢的击中目标
            Player hitPlayer = null;
            double minDistance = Double.MAX_VALUE;
            for (Player player : shooter.getWorld().getPlayers()) {
                double distance = player.getLocation().distance(hitLocation);
                if (distance < minDistance && distance < 1.5) { // 1.5格以内视为击中
                    minDistance = distance;
                    hitPlayer = player;
                }
            }
            if (hitPlayer != null) {
                // 施加剧毒2效果持续3秒
                hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1)); // 3秒（60 ticks），剧毒2
            }
        }

        // 处理新增僵尸类型的箭矢效果
        if (shooterName.equals("§b雷霆僵尸")) {
            // 释放雷电效果
            ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), hitLocation, 0, 0, 0, 0, 1);
            // 播放雷电音效
            shooter.getWorld().playSound(hitLocation, Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);
            // 对5x5范围内的玩家造成6点伤害
            for (Player player : shooter.getWorld().getPlayers()) {
                if (player.getLocation().distance(hitLocation) <= 5) {
                    player.damage(6.0, shooter);
                }
            }
            // 移除箭矢
            arrow.remove();
        }
    }

    /**
     * 监听僵尸死亡事件，处理特殊功能
     *
     * @param event 僵尸死亡事件
     */
    @EventHandler
    public void onZombieDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Zombie)) {
            return; // 不是僵尸不处理
        }

        Zombie zombie = (Zombie) event.getEntity();
        String zombieName = zombie.getCustomName();

        if (zombieName == null) {
            return; // 没有自定义名称的僵尸不处理
        }

        Location deathLocation = zombie.getLocation();

        // 处理气球僵尸的死亡效果
        if ("§e气球僵尸".equals(zombieName)) {
            // 释放五彩气球粒子
            ParticleEffect.SPELL_MOB.send(Bukkit.getOnlinePlayers(), deathLocation, 0, 0, 0, 0, 100);
            // 播放气球破裂音效
            zombie.getWorld().playSound(deathLocation, Sound.BAT_DEATH, 1.0F, 1.0F);
        }

        // 处理迷雾僵尸的死亡效果
        if ("§8迷雾僵尸".equals(zombieName)) {
            // 生成大量雾气粒子
            ParticleEffect.SNOW_SHOVEL.send(Bukkit.getOnlinePlayers(), deathLocation, 0, 0, 0, 0, 100);
            // 播放迷雾生成音效
            zombie.getWorld().playSound(deathLocation, Sound.FIRE, 1.0F, 1.0F);
        }

        // 移除气球僵尸的任务
        if (zombieName.equals("§e气球僵尸")) {
            BukkitTask task = balloonZombieTasks.get(zombie);
            if (task != null) {
                task.cancel();
                balloonZombieTasks.remove(zombie);
            }
        }


        // 移除迷雾僵尸的任务
        if (zombieName.equals("§8迷雾僵尸")) {
            BukkitTask task = fogZombieTasks.get(zombie);
            if (task != null) {
                task.cancel();
                fogZombieTasks.remove(zombie);
            }
        }

        // 检查是否为变异科学家
        if ("§6变异科学家".equals(zombieName)) {
            // 召唤4个暗影僵尸 (ID15)
            for (int i = 0; i < 4; i++) {
                Zombie shadowZombie = (Zombie) deathLocation.getWorld().spawnEntity(deathLocation, EntityType.ZOMBIE);
                shadowZombie.setCustomName("§5暗影僵尸");
                shadowZombie.setCustomNameVisible(true);
                shadowZombie.setMaxHealth(60.0);
                shadowZombie.setHealth(60.0);
                shadowZombie.getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD));
                // 设置暗影头盔
                ItemStack helmet15 = new ItemStack(Material.LEATHER_HELMET);
                ItemMeta helmetMeta15 = helmet15.getItemMeta();
                helmetMeta15.setDisplayName("§5暗影头盔");
                helmet15.setItemMeta(helmetMeta15);
                shadowZombie.getEquipment().setHelmet(helmet15);
                // 添加隐身效果
                shadowZombie.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                // 开始暗影僵尸的隐匿攻击任务
                startShadowAttack(shadowZombie);
            }
            // 释放暗黑粒子效果
            ParticleEffect.SMOKE_LARGE.send(Bukkit.getOnlinePlayers(), deathLocation, 0, 0, 0, 0, 1000);
        }

        // 在 onZombieDeath 方法的末尾添加移除变异科学家的映射
        if (zombie.hasMetadata("isMutantScientist")) {
            mutantScientistDamage.remove(zombie);
            zombie.removeMetadata("isMutantScientist", this);
        }

        if (zombie instanceof PigZombie) {
            PigZombie pigZombie = (PigZombie) zombie;
            String pigZombieName = pigZombie.getCustomName();
            // 处理 PigZombie 的相关逻辑


            if (pigZombieName.equals("§6变异僵尸01")) {
                Location deathLocation1 = pigZombie.getLocation();

                // 释放黑色烟雾粒子
                ParticleEffect.SMOKE_LARGE.send(Bukkit.getOnlinePlayers(), deathLocation1, 1, 0, 0, 1, 50);

                // 播放爆炸音效
                pigZombie.getWorld().playSound(deathLocation1, Sound.EXPLODE, 1.0F, 1.0F);

                // 你可以在这里添加其他死亡效果，如生成其他实体或施加效果
            }
            // 在 onZombieDeath 方法中添加处理 ID19 变异法师的逻辑
            if ("§6变异法师".equals(zombieName)) {

                // 释放火焰粒子和烟雾粒子
                ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), deathLocation, 0, 0, 0, 0, 1000);
                ParticleEffect.SMOKE_LARGE.send(Bukkit.getOnlinePlayers(), deathLocation, 0, 0, 0, 0, 1000);

                // 施加混乱效果给附近的玩家
                for (Player player : deathLocation.getWorld().getPlayers()) {
                    if (player.getLocation().distance(deathLocation) <= 5) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 1)); // 5秒（100 ticks），混乱
                    }
                }

                // 取消相关任务
                BukkitTask lightningTask = mutantMageLightningTasks.get(zombie);
                if (lightningTask != null) {
                    lightningTask.cancel();
                    mutantMageLightningTasks.remove(zombie);
                }

                BukkitTask auraTask = mutantMageAuraTasks.get(zombie);
                if (auraTask != null) {
                    auraTask.cancel();
                    mutantMageAuraTasks.remove(zombie);
                }

                BukkitTask summonTask = mutantMageSummonTasks.get(zombie);
                if (summonTask != null) {
                    summonTask.cancel();
                    mutantMageSummonTasks.remove(zombie);
                }
            }
            // 处理 Poison Zombie 和 Twin Zombie 的死亡逻辑
            switch (zombieName) {
                case "§5剧毒僵尸":
                    // 对5x5范围内的玩家施加剧毒2持续3秒
                    for (Player player : deathLocation.getWorld().getPlayers()) {
                        if (player.getLocation().distance(deathLocation) <= 5) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1)); // 3秒（60 ticks），剧毒2
                        }
                    }
                    // 释放紫色粒子
                    ParticleEffect.PORTAL.send(Bukkit.getOnlinePlayers(), deathLocation, 0, 0, 0, 0, 1500);
                    break;

                case "§7骷髅僵尸":
                    // 无特殊死亡效果
                    break;

                case "§8武装僵尸":
                    // 无特殊死亡效果
                    break;

                case "§9肥胖僵尸":
                    // 无特殊死亡效果
                    break;

                case "§4法师僵尸":
                    // 无特殊死亡效果
                    break;

                case "§4自爆僵尸":
                    // 自爆僵尸死亡后播放爆炸音效，并释放红色粒子
                    deathLocation.getWorld().playSound(deathLocation, Sound.EXPLODE, 1.0F, 1.0F);
                    ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), deathLocation, 0, 0, 0, 0, 1);
                    break;

                case "§5毒箭僵尸":
                    // 毒箭僵尸死亡后播放毒气音效，并释放红色粒子
                    deathLocation.getWorld().playSound(deathLocation, Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);
                    ParticleEffect.REDSTONE.send(Bukkit.getOnlinePlayers(), deathLocation, 1, 0, 0, 1, 1000);
                    break;

                case "§e电击僵尸":
                    // 无特殊死亡效果
                    break;

                case "§b冰冻僵尸":
                    // 无特殊死亡效果
                    break;

                // 新增僵尸类型的死亡处理
                case "§5暗影僵尸":
                    // 释放暗影粒子效果
                    ParticleEffect.SPELL_MOB.send(Bukkit.getOnlinePlayers(), deathLocation, 0, 0, 0, 0, 1000);
                    // 施加混乱效果给附近的玩家
                    for (Player player : deathLocation.getWorld().getPlayers()) {
                        if (player.getLocation().distance(deathLocation) <= 5) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 1)); // 5秒（100 ticks），混乱
                        }
                    }
                    break;

                case "§c毁灭僵尸":
                    // 释放毁灭粒子效果
                    ParticleEffect.EXPLOSION_NORMAL.send(Bukkit.getOnlinePlayers(), deathLocation, 0, 0, 0, 0, 1);
                    // 对10x10范围内的玩家造成20点伤害
                    for (Player player : deathLocation.getWorld().getPlayers()) {
                        if (player.getLocation().distance(deathLocation) <= 10) {
                            player.damage(20.0, zombie);
                        }
                    }
                    break;

                case "§b雷霆僵尸":
                    // 释放雷电粒子效果
                    ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), deathLocation, 0, 0, 0, 0, 1);
                    // 播放雷电音效
                    deathLocation.getWorld().playSound(deathLocation, Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);
                    break;


                default:
                    // 其他僵尸类型不需要特殊处理
                    break;
            }

            // 取消相关任务
            if (zombieName.equals("§7骷髅僵尸")) {
                BukkitTask task = skeletonZombieTasks.get(zombie);
                if (task != null) {
                    task.cancel();
                    skeletonZombieTasks.remove(zombie);
                }
            }

            if (zombieName.equals("§4法师僵尸")) {
                BukkitTask task = mageZombieTasks.get(zombie);
                if (task != null) {
                    task.cancel();
                    mageZombieTasks.remove(zombie);
                }
            }

            if (zombieName.equals("§5毒箭僵尸")) {
                BukkitTask task = poisonArrowZombieTasks.get(zombie);
                if (task != null) {
                    task.cancel();
                    poisonArrowZombieTasks.remove(zombie);
                }
            }

            if (zombieName.equals("§e电击僵尸")) {
                BukkitTask task = shockZombieTasks.get(zombie);
                if (task != null) {
                    task.cancel();
                    shockZombieTasks.remove(zombie);
                }
            }

            if (zombieName.equals("§b冰冻僵尸")) {
                BukkitTask task = freezeZombieTasks.get(zombie);
                if (task != null) {
                    task.cancel();
                    freezeZombieTasks.remove(zombie);
                }
            }

            // 处理新增僵尸类型的任务取消
            if (zombieName.equals("§5暗影僵尸")) {
                BukkitTask task = shadowZombieTasks.get(zombie);
                if (task != null) {
                    task.cancel();
                    shadowZombieTasks.remove(zombie);
                }
            }

            if (zombieName.equals("§c毁灭僵尸")) {
                BukkitTask task = destructionZombieTasks.get(zombie);
                if (task != null) {
                    task.cancel();
                    destructionZombieTasks.remove(zombie);
                }
            }

            if (zombieName.equals("§b雷霆僵尸")) {
                BukkitTask task = thunderZombieTasks.get(zombie);
                if (task != null) {
                    task.cancel();
                    thunderZombieTasks.remove(zombie);
                }
            }
            if (pigZombieName.equals("§6变异僵尸01")) {
                // 取消粒子任务
                BukkitTask task = mutantZombie01Tasks.get(pigZombie);
                if (task != null) {
                    task.cancel();
                    mutantZombie01Tasks.remove(pigZombie);
                }

                // 如果是肥胖僵尸，移除体型标记
                if (zombie.hasMetadata("isFatZombie")) {
                    zombie.removeMetadata("isFatZombie", this);
                }


            }
            if (zombie.hasMetadata("isMutantMage")) {
                zombie.removeMetadata("isMutantMage", this);
            }

        }
    }


    /**
     * 使用 Metadata 和 PotionEffect 模拟肥胖僵尸的体型
     * 注意：由于 Spigot 1.8.8 不支持直接调整实体体型，此方法通过添加持续的视觉效果来模拟
     *
     * @param zombie 需要调整大小的僵尸实体
     */
    private void setFatZombieSize(Zombie zombie) {
        // 添加持续的药水效果来模拟体型增大（例如，稍微改变视觉效果）
        // 注意：这并不能真正改变实体的物理大小，只是视觉上的模拟
        // 可以使用跳跃高度减少和隐身效果来模拟
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, -10, false, false));
    }


    /**
     * 监听箭矢命中事件，处理电击僵尸和冰冻僵尸的特殊效果
     *
     * @param event 箭矢命中事件
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) {
            return;
        }

        Arrow arrow = (Arrow) event.getEntity();

        if (!(arrow.getShooter() instanceof Zombie)) {
            return;
        }

        Zombie shooter = (Zombie) arrow.getShooter();
        String shooterName = shooter.getCustomName();

        if (shooterName == null) {
            return;
        }

        Location hitLocation = arrow.getLocation();

        if (shooterName.equals("§e电击僵尸")) {
            // 释放电流粒子效果
            ParticleEffect.CRIT_MAGIC.send(Bukkit.getOnlinePlayers(), hitLocation, 0, 0, 0, 1, 100);
            // 对10x10范围内的玩家造成4点伤害
            for (Player player : shooter.getWorld().getPlayers()) {
                if (player.getLocation().distance(hitLocation) <= 10) {
                    player.damage(4.0, shooter);
                }
            }
            // 移除箭矢
            arrow.remove();
        }

        if (shooterName.equals("§b冰冻僵尸")) {
            // 释放冻结粒子效果
            ParticleEffect.SNOWBALL.send(Bukkit.getOnlinePlayers(), hitLocation, 0, 0, 0, 1, 100);
            // 播放冻结音效
            shooter.getWorld().playSound(hitLocation, Sound.GLASS, 1.0F, 1.0F);
            // 对10x10范围内的玩家造成4点伤害并施加减速2的buff持续2秒
            for (Player player : shooter.getWorld().getPlayers()) {
                if (player.getLocation().distance(hitLocation) <= 10) {
                    player.damage(4.0, shooter);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1)); // 2秒（40 ticks），减速2
                }
            }
            // 移除箭矢
            arrow.remove();
        }

        // 处理新增僵尸类型的箭矢效果
        if (shooterName.equals("§b雷霆僵尸")) {
            // 释放雷电粒子效果
            ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), hitLocation, 0, 0, 0, 0, 1);
            // 播放雷电音效
            shooter.getWorld().playSound(hitLocation, Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);
            // 对5x5范围内的玩家造成6点伤害
            for (Player player : shooter.getWorld().getPlayers()) {
                if (player.getLocation().distance(hitLocation) <= 5) {
                    player.damage(6.0, shooter);
                }
            }
            // 移除箭矢
            arrow.remove();
        }
    }

    /**
     * 开始管理变异科学家的特殊功能
     *
     * @param zombie 变异科学家僵尸
     */
    private void startMutantScientistTasks(Zombie zombie) {
        // 任务1：每20秒对所有玩家造成10点伤害并播放爆炸音效
        BukkitTask periodicDamageTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantScientistTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                for (Player player : zombie.getWorld().getPlayers()) {
                    player.damage(10.0, zombie);
                    player.playSound(player.getLocation(), Sound.EXPLODE, 1.0F, 1.0F);
                }
            }
        }.runTaskTimer(this, 0, 400); // 每20秒（400 ticks）

        // 任务2：持续检查玩家是否在10x10范围内，并应用或移除效果
        BukkitTask auraEffectTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantScientistTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                for (Player player : zombie.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 10) {
                        // 施加反胃和减速效果
                        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 40, 1, false, false));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, false));
                    } else {
                        // 移除反胃和减速效果
                        player.removePotionEffect(PotionEffectType.CONFUSION);
                        player.removePotionEffect(PotionEffectType.SLOW);
                    }
                }
            }
        }.runTaskTimer(this, 0, 20); // 每1秒（20 ticks）

        // 将任务存储到映射中
        mutantScientistTasks.put(zombie, periodicDamageTask);
        mutantScientistTasks.put(zombie, auraEffectTask);
    }

    /**
     * 开始气球僵尸的特殊任务
     *
     * @param zombie 气球僵尸实体
     */
    private void startBalloonZombieTasks(Zombie zombie) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    balloonZombieTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                // 生成五彩气球粒子
                ParticleEffect.SPELL_WITCH.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 1, 10);
                // 随机弹跳效果
                zombie.setVelocity(zombie.getVelocity().add(new Vector(Math.random() - 0.5, 0, Math.random() - 0.5)));
            }
        }.runTaskTimer(this, 0, 20); // 每1秒（20 ticks）执行一次

        // 存储任务，以便僵尸死亡时取消任务
        balloonZombieTasks.put(zombie, task);
    }

    /**
     * 开始迷雾僵尸的特殊任务
     *
     * @param zombie 迷雾僵尸实体
     */
    private void startFogZombieTasks(Zombie zombie) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    fogZombieTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                // 生成迷雾粒子效果
                ParticleEffect.SNOW_SHOVEL.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 1, 0, 50);
                // 施加减速和失明效果给附近玩家（5格范围内）
                for (Player player : zombie.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 5) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1, false, false));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false));
                    }
                }
            }
        }.runTaskTimer(this, 0, 40); // 每2秒（40 ticks）执行一次

        // 存储任务，以便僵尸死亡时取消任务
        fogZombieTasks.put(zombie, task);
    }

    /**
     * 生成ID22: 变异雷霆僵尸
     *
     * @param player   生成实体的玩家
     * @param location 生成位置
     */
    public void spawnMutantThunderZombie(Player player, Location location) {
        // 生成僵尸实体
        Zombie mutantThunderZombie = (Zombie) player.getWorld().spawnEntity(location, EntityType.ZOMBIE);

        // 设置僵尸的自定义属性
        mutantThunderZombie.setCustomName("§6变异雷霆僵尸");
        mutantThunderZombie.setCustomNameVisible(true);
        mutantThunderZombie.setMaxHealth(800.0);
        mutantThunderZombie.setHealth(800.0);

        // 装备：手持弓
        ItemStack bow = new ItemStack(Material.BOW);
        mutantThunderZombie.getEquipment().setItemInHand(bow);

        // 装备：全套附魔保护2的锁链套（无头，头戴僵尸头颅）
        // 设置锁链盔甲
        ItemStack helmet = new ItemStack(Material.SKULL_ITEM, 1, (short) 2); // 僵尸头颅
        ItemMeta helmetMeta = helmet.getItemMeta();
        helmetMeta.setDisplayName("§6僵尸头颅");
        helmet.setItemMeta(helmetMeta);
        mutantThunderZombie.getEquipment().setHelmet(helmet);

        ItemStack chestplate = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
        chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        mutantThunderZombie.getEquipment().setChestplate(chestplate);

        ItemStack leggings = new ItemStack(Material.CHAINMAIL_LEGGINGS);
        leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        mutantThunderZombie.getEquipment().setLeggings(leggings);

        ItemStack boots = new ItemStack(Material.CHAINMAIL_BOOTS);
        boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        mutantThunderZombie.getEquipment().setBoots(boots);

        // 添加速度3的药水效果（持续时间无限）
        mutantThunderZombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));

        // 添加与变异法师僵尸相似的粒子效果
        startMutantThunderZombieParticles(mutantThunderZombie);

        // 标记为变异雷霆僵尸
        mutantThunderZombie.setMetadata("isMutantThunderZombie", new FixedMetadataValue(this, true));

        // 开始管理变异雷霆僵尸的特殊任务
        startMutantThunderZombieTasks(mutantThunderZombie);
    }

    /**
     * 为变异雷霆僵尸添加粒子效果
     *
     * @param zombie 变异雷霆僵尸实体
     */
    private void startMutantThunderZombieParticles(Zombie zombie) {
        BukkitTask particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantThunderZombieTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                // 生成与变异法师类似的粒子效果
                ParticleEffect.SMOKE_NORMAL.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 1, 10);
                ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), loc, 0, 0, 0, 1, 10);
            }
        }.runTaskTimer(this, 0, 10); // 每半秒（10 ticks）执行一次

        // 存储任务，以便僵尸死亡时取消任务
        mutantThunderZombieTasks.put(zombie, particleTask);
    }

    /**
     * 开始管理变异雷霆僵尸的特殊任务
     *
     * @param zombie 变异雷霆僵尸实体
     */
    private void startMutantThunderZombieTasks(Zombie zombie) {
        // 任务1：每15秒释放雷电攻击
        BukkitTask lightningTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantThunderZombieTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                // 释放雷电
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(zombie.getWorld())) {
                        loc.getWorld().strikeLightning(player.getLocation());
                        // 播放雷电音效
                        player.playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);
                        // 释放雷电粒子效果
                        ParticleEffect.CRIT_MAGIC.send(Bukkit.getOnlinePlayers(), player.getLocation(), 0, 0, 0, 1, 10);
                    }
                }
            }
        }.runTaskTimer(this, 0, 300); // 每15秒（300 ticks）执行一次

        // 任务2：每15秒随机传送到随机玩家并释放雷电
        BukkitTask teleportTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantThunderZombieTasks.remove(zombie);
                    return;
                }
                Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
                if (players.length == 0) return;
                // 随机选择一个玩家
                Player target = players[new Random().nextInt(players.length)];
                // 传送到玩家附近
                Location targetLoc = target.getLocation().add(new Random().nextInt(5) - 2, 0, new Random().nextInt(5) - 2);
                zombie.teleport(targetLoc);
                // 释放雷电
                target.getWorld().strikeLightning(target.getLocation());
                // 播放雷电音效
                target.playSound(target.getLocation(), Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);
                // 释放雷电粒子效果
                ParticleEffect.CRIT_MAGIC.send(Bukkit.getOnlinePlayers(), target.getLocation(), 0, 0, 0, 1, 10);
            }
        }.runTaskTimer(this, 0, 300); // 每15秒（300 ticks）执行一次

        // 任务3：每分钟召唤4个雷霆僵尸和2个变异爬行者
        BukkitTask summonTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantThunderZombieTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                // 召唤4个雷霆僵尸
                for (int i = 0; i < 4; i++) {
                    spawnCustomZombieDirect(loc, "id17"); // id17 为雷霆僵尸
                }
                // 召唤2个变异爬行者
                for (int i = 0; i < 2; i++) {
                    spawnOtherEntityDirect(loc, "id4"); // id4 为变异爬行者
                }
            }
        }.runTaskTimer(this, 0, 1200); // 每分钟（1200 ticks）执行一次

        // 任务4：每10秒改变周围玩家的时间流速（模拟攻击速度和移动速度减慢50%）
        BukkitTask slowTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantThunderZombieTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getLocation().distance(loc) <= 10) {
                        // 施加减速和攻击速度减慢的效果
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1, false, false));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 100, 1, false, false));
                    }
                }
            }
        }.runTaskTimer(this, 0, 200); // 每10秒（200 ticks）执行一次

        // 任务5：每2分钟冻结周围所有玩家5秒
        BukkitTask freezeTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    this.cancel();
                    mutantThunderZombieTasks.remove(zombie);
                    return;
                }
                Location loc = zombie.getLocation();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getLocation().distance(loc) <= 10) {
                        // 施加冻结效果
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 10, false, false));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, 10, false, false));
                    }
                }
            }
        }.runTaskTimer(this, 0, 2400); // 每2分钟（2400 ticks）执行一次

        // 存储所有任务，以便僵尸死亡时取消
        mutantThunderZombieTasks.put(zombie, lightningTask);
        mutantThunderZombieTasks.put(zombie, teleportTask);
        mutantThunderZombieTasks.put(zombie, summonTask);
        mutantThunderZombieTasks.put(zombie, slowTask);
        mutantThunderZombieTasks.put(zombie, freezeTask);
    }

    /**
     * 直接在指定位置生成自定义实体（用于召唤任务）
     *
     * @param location 生成位置
     * @param otherId  实体ID（如id4）
     */
    public void spawnOtherEntityDirect(Location location, String otherId) {
        switch (otherId) {
            case "id17": // 雷霆僵尸
                spawnCustomZombieDirect(location, "id17");
                break;
            case "id4": // 变异爬行者
                spawnMutantCreeperDirect(location);
                break;
            // 添加其他需要直接生成的实体ID
            default:
                // 无效的实体ID
                break;
        }
    }

    /**
     * 生成ID4: 变异爬行者（直接生成，无需玩家）
     *
     * @param location 生成位置
     */
    public void spawnMutantCreeperDirect(Location location) {
        Creeper mutantCreeper = (Creeper) location.getWorld().spawnEntity(location, EntityType.CREEPER);
        mutantCreeper.setCustomName("§6变异爬行者");
        mutantCreeper.setCustomNameVisible(true);
        mutantCreeper.setMaxHealth(50.0);
        mutantCreeper.setHealth(50.0);

        // 添加速度6的药水效果
        mutantCreeper.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 5));

        // 标记为变异爬行者
        mutantCreeper.setMetadata("isMutantCreeper", new FixedMetadataValue(this, true));

        // 开始管理变异爬行者的特殊任务
        startMutantCreeperTasks(mutantCreeper);
    }

    /**
     * 监听僵尸实体死亡事件，处理变异雷霆僵尸的死亡效果
     *
     * @param event 实体死亡事件
     */
    @EventHandler
    public void onThunderZombieDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Zombie) {
            Zombie zombie = (Zombie) entity;
            if ("§6变异雷霆僵尸".equals(zombie.getCustomName())) {
                Location loc = zombie.getLocation();
                // 释放红色粒子效果
                ParticleEffect.EXPLOSION_HUGE.send(Bukkit.getOnlinePlayers(), loc, 1, 0, 0, 1, 50);
                // 播放爆炸音效
                zombie.getWorld().playSound(loc, Sound.EXPLODE, 1.0F, 1.0F);
                // 取消所有相关任务
                if (mutantThunderZombieTasks.containsKey(zombie)) {
                    BukkitTask task = mutantThunderZombieTasks.get(zombie);
                    if (task != null) {
                        task.cancel();
                    }
                    mutantThunderZombieTasks.remove(zombie);
                }
            }
        }
    }
}
