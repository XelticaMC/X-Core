/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.10"

    java
    `maven-publish`

    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("com.github.johnrengelman.shadow") version "2.0.4"
}

repositories {
    mavenLocal()
    maven {
	url = uri("https://repo.maven.apache.org/maven2/")
    }

    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }

    maven {
        url = uri("https://jitpack.io")
    }
    
    maven {
       url = uri("https://m2.dv8tion.net/releases")
    }
    
    maven {
        url = uri("https://repo.opencollab.dev/maven-snapshots")
    }

    maven {
        url = uri("https://repo.opencollab.dev/maven-releases")
    }

    maven {
        url = uri("https://repo.phoenix616.dev/")
    }

    maven {
        url = uri("https://nexus.scarsz.me/content/groups/public/")
    }
    maven {
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }
    maven {
        name = "citizens"
        url = uri("http://repo.citizensnpcs.co/")
        isAllowInsecureProtocol = true
    }
}

dependencies {
    compileOnly("net.skinsrestorer:skinsrestorer:14.1.4-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.30")
    compileOnly("io.papermc.paper:paper-api:1.19-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("org.geysermc:connector:1.4.3-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.8.0")
    compileOnly("com.discordsrv:discordsrv:1.25.1")
    compileOnly("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.0")
    compileOnly("com.github.koca2000:NoteBlockAPI:1.6.1")
    compileOnly("net.citizensnpcs:citizensapi:2.0.29-SNAPSHOT")
    compileOnly("com.github.ucchyocean.lc:LunaChat:3.0.16")
    implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT")

    library("com.google.code.gson", "gson", "2.8.7")
    bukkitLibrary("com.google.code.gson", "gson", "2.8.7")
}

group = "work.xeltica.craft.core"
version = "2.38.2"
description = "X-Core"
java.sourceCompatibility = JavaVersion.VERSION_17

bukkit {
    name = "XCore"
    main = "work.xeltica.craft.core.XCorePlugin"
    version = getVersion().toString()
    apiVersion = "1.18"
    softDepend = listOf("SkinsRestorer", "Citizens")
    depend = listOf("kotlin-stdlib", "Geyser-Spigot", "Vault", "floodgate", "DiscordSRV", "HolographicDisplays", "NoteBlockAPI")

    commands {
        register("omikuji") {
            description = "おみくじを引きます。マイクラ内で1日に1回まで引けて、100エビパワーを消費します。"
            usage = "/omikuji"
        }
        register("respawn") {
            description = "メインワールドの初期スポーンに戻ります。"
            usage = "/respawn"
        }
        register("pvp") {
            description = "現在のワールドのPvP設定を変更します。"
            usage = "/pvp <on/off>"
            permission = "otanoshimi.command.pvp"
        }
        register("signedit") {
            description = "看板の指定行を編集します。"
            usage = "/signedit <行番号> <テキスト>"
            permission = "otanoshimi.command.signedit"
        }
        register("givecustomitem") {
            description = "XelticaMCオリジナルアイテムを授与します。"
            usage = "/givetravelticket <playerName> <xphone>"
            permission = "otanoshimi.command.givecustomitem"
        }
        register("givemobball") {
            description = "モブボールを入手します。。"
            usage = "/givemobball <playerName> [amount=1] [type:normal|super|ultra]"
            permission = "otanoshimi.command.givemobball"
        }
        register("report") {
            description = "処罰GUIを表示します。"
            usage = "/report <playerName>"
            permission = "otanoshimi.command.report"
        }
        register("localtime") {
            description = "現在いるワールドの時間を設定します。"
            usage = "/localtime <add|set|query> [day|night|noon|midnight|sunrise|sunset|(数値)]"
            permission = "otanoshimi.command.localtime"
        }
        register("boat") {
            description = "ボートを召喚します。"
            usage = "/boat"
            permission = "otanoshimi.command.boat"
        }
        register("cart") {
            description = "トロッコを召喚します。"
            usage = "/cart"
            permission = "otanoshimi.command.cart"
        }
        register("promo") {
            description = "市民への昇格方法を確認します。"
            usage = "/promo"
            permission = "otanoshimi.command.promo"
        }
        register("cat") {
            description = "CATモードの有効/無効を切り替えるか、現在のモードを取得します。"
            usage = "/cat [on/off]"
            permission = "otanoshimi.command.cat"
        }
        register("hub") {
            description = "ロビーに移動します。"
            usage = "/hub help"
        }
        register("xtp") {
            description = "保存された過去位置を用いてテレポートします。"
            usage = "/xtp <world> [player]"
            permission = "otanoshimi.command.xtp"
            aliases = listOf("xteleport")
        }
        register("xtpreset") {
            description = "xtpコマンドで用いる過去位置をリセットします。プレイヤーを省略した場合、全員分をリセットします。"
            usage = "/xtpreset <world> [player]"
            permission = "otanoshimi.command.xtpreset"
            aliases = listOf("xteleportreset")
        }
        register("xphone") {
            description = "X Phone を入手する"
            usage = "/xphone"
            permission = "otanoshimi.command.xphone"
            aliases = listOf("phone")
        }
        register("live") {
            description = "ライブ配信モードを切り替える"
            usage = "/live <on/off>"
            permission = "otanoshimi.command.live"
        }
        register("epshop") {
            description = "エビパワーストアを開きます。"
            usage = "/epshop"
            permission = "otanoshimi.command.epshop"
        }
        register("hint") {
            description = "ヒントメニューを開きます。"
            usage = "/hint [hint-id]"
            permission = "otanoshimi.command.hint"
        }
        register("counter") {
            description = "カウンター管理"
            usage = "/counter <register/unregister/cancel/bind/info/list/resetdaily>"
            permission = "otanoshimi.command.counter"
        }
        register("ranking") {
            description = "ランキング管理"
            usage = "/ranking <create/delete/query/list/set/unset/hologram>"
            permission = "otanoshimi.command.ranking"
        }
        register("countdown") {
            description = "カウントダウンを表示します。"
            usage = "/countdown <秒数> [プレイヤー名...]"
            permission = "otanoshimi.command.countdown"
        }
        register("qchat") {
            description = "QuickChatの設定"
            usage = "/qchat <register/unregister/list>"
            permission = "otanoshimi.command.qchat"
        }
        register("epeffectshop") {
            description = "エビパワードラッグストアを開きます。"
            usage = "/epeffectshop"
            permission = "otanoshimi.command.epeffectshop"
        }
        register("xreload") {
            description = "X-Coreの設定をリロードします。"
            usage = "/xreload"
            permission = "otanoshimi.command.xreload"
        }
        register("xdebug") {
            description = "X-Core Debug Command"
            usage = "/xdebug"
            permission = "otanoshimi.command.xdebug"
        }
        register("stamp") {
            description = "スタンプラリー用コマンド"
            usage = "/stamp listDonePlayers"
            permission = "otanoshimi.command.stamp"
        }
        register("firework") {
            description = "花火大会用コマンド"
            usage = "/firework <run|center> <scriptName>"
            permission = "otanoshimi.command.firework"
        }
        register("farmfest") {
            description = "秋農業祭り用コマンド"
            usage = "/farmfest <clearFarm|add|init|start|stop>"
            permission = "otanoshimi.command.farmfest"
        }
        register("__core_gui_event__") {
            description = "?"
            usage = "?"
        }
    }

    permissions {
        register("otanoshimi.command.pvp") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.givecustomitem") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.givemobball") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.signedit") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.report") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.localtime") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.boat") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.cart") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.citizen") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.staff") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.cat") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
        }
        register("hub.teleport.sandbox") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("hub.teleport.art") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("hub.teleport.nightmare") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("hub.gatekeeper.citizen") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("hub.gatekeeper.staff") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.debug") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.xtp") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.xtp.other") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.promo") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
        }
        register("otanoshimi.command.promo.other") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.epshop") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
        }
        register("otanoshimi.command.epshop.add") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.epshop.delete") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.hint") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
        }
        register("otanoshimi.command.xphone") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
        }
        register("otanoshimi.command.live") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
        }
        register("otanoshimi.command.nickname") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
        }
        register("otanoshimi.command.counter") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.ranking") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.countdown") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.qchat") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.app.fireworks") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.FALSE
        }
        register("otanoshimi.command.epeffectshop") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
        }
        register("otanoshimi.command.epeffectshop.add") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.epeffectshop.delete") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.xreload") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.xtpreset") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.xdebug") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.stamp") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.firework") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.command.farmfest") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.stamp.create") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
        register("otanoshimi.stamp.destroy") {
            default = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
        }
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.jar {
    archiveFileName.set("${project.name}.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
