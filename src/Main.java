import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.sasha.eventsys.SimpleEventHandler;
import com.sasha.eventsys.SimpleListener;
import com.sasha.reminecraft.Configuration;
import com.sasha.reminecraft.api.RePlugin;
import com.sasha.reminecraft.api.event.ServerOtherPlayerJoinEvent;
import com.sasha.reminecraft.api.event.ServerOtherPlayerQuitEvent;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.logging.ILogger;
import com.sasha.reminecraft.logging.LoggerBuilder;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Main extends RePlugin implements SimpleListener {


    public Config CFG = new Config();

    public ILogger logger = LoggerBuilder.buildProperLogger("TabLoggerPlugin");

    private ScheduledExecutorService executor;
    private ScheduledFuture<?>[] future = new ScheduledFuture[2];

    ArrayList<String> playerData = new ArrayList<>();
    ArrayList<String> joinQuits = new ArrayList<>();


    @Override
    public void onPluginInit() {
        this.getReMinecraft().EVENT_BUS.registerListener(this);
        executor = Executors.newScheduledThreadPool(2);
    }

    @Override
    public void onPluginEnable() {
        future[0] = executor.scheduleAtFixedRate(() -> {
            writeToFile();
        }, CFG.var_savePeriodMillis, CFG.var_savePeriodMillis, TimeUnit.MILLISECONDS);

        future[1] = executor.scheduleAtFixedRate(() -> {
            testTab();
        }, CFG.var_testTabPeriodMillis, CFG.var_testTabPeriodMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onPluginDisable() {
        for (ScheduledFuture f : future) {
            f.cancel(false);
        }
    }

    @Override
    public void onPluginShutdown() {
        // in case there is still some data:
        if(joinQuits.size() + playerData.size() > 0)
            writeToFile();
        this.getReMinecraft().EVENT_BUS.deregisterListener(this);
        executor.shutdownNow();
    }

    @Override
    public void registerCommands() {

    }

    @Override
    public void registerConfig() {
        this.getReMinecraft().configurations.add(CFG);
    }


    @SimpleEventHandler
    public void onEvent(ServerOtherPlayerJoinEvent e){
        if(!CFG.var_saveJoins)
            return;
        joinQuits.add("[" + System.currentTimeMillis() + "]: joinEvent: " + getPlayerData(e.getName(), e.getUuid().toString()));
    }

    @SimpleEventHandler
    public void onEvent(ServerOtherPlayerQuitEvent e){
        if(!CFG.var_saveQuits)
            return;

        joinQuits.add("[" + System.currentTimeMillis() + "]: quitEvent: " + getPlayerData(e.getName(), e.getUuid().toString()));
    }



    /**
        idea:
            save gamemode if not survival
            save displayname once (depending on profil info)
            save pink too depending on config

                .getDisplayName() -> null
                .getPing() -> useful -> save recursifly
                .getGameMode() -> SURVIVAL -> test recursefly
                .getGameMode() -> TODO
                    .getId() -> UUID
                    .getName() -> name
                    .getTextures().size() -> 0
                    .getProperties().size() -> 1

     */

    private void testTab() {
        //logger.log(ReClient.ReClientCache.INSTANCE.tabFooter.getText());
        // TODO  get tps ... and other infos


        testPlayerData();
/*
        @Configuration.ConfigSetting public boolean var_saveTabTps = true;
        @Configuration.ConfigSetting public boolean var_saveTabPing = true;
        @Configuration.ConfigSetting public boolean var_saveTabPlayers = true;
        @Configuration.ConfigSetting public boolean var_saveTabUptime = true;
        @Configuration.ConfigSetting public boolean var_saveHeader = true;
        @Configuration.ConfigSetting public boolean var_saveFooter = true;*/
    }

    public void testPlayerData(){

        if(!CFG.var_savePlayerData)
            return;

        String toAdd;
        for (PlayerListEntry p : ReClient.ReClientCache.INSTANCE.playerListEntries) {
            toAdd = "[" + System.currentTimeMillis() + "]: " + getPlayerData(p.getProfile().getName(), p.getProfile().getIdAsString()) + ": ";

            if(CFG.var_savePing)
                toAdd += "{" + p.getPing() + "}";

            if(CFG.var_testGamemode)
                if(p.getGameMode() != GameMode.SURVIVAL)
                    toAdd += "{" + p.getGameMode() + "}";

            playerData.add(toAdd);
        }
    }


    public String getPlayerData(String name, String uuid){
        String rV = "";
        if(CFG.var_savePlayerName)
            rV += "{" + name + "}";

        if(CFG.var_savePlayerUuid) {
            if(rV.length() != 0)
                rV += " ";
            rV += "{" + uuid + "}";
        }
        return rV;
    }


    /**
     *  Method to be called at a fixed rate,
     *  writes everything to a file and switches to a new file if needed
     */
    public void writeToFile() {
        saveJoinQuits();
        savePlayerData();
    }

    public void saveJoinQuits(){
        FileHelper.addStrings(FileHelper.getFilepath() + CFG.var_fileJoinQuits + FileHelper.getDate() + ".txt", joinQuits);
        joinQuits = new ArrayList<>();
    }

    public void savePlayerData(){
        FileHelper.addStrings(FileHelper.getFilepath() + CFG.var_filePlayerData + FileHelper.getDate() + ".txt", playerData);
        playerData = new ArrayList<>();
    }








}


class Config extends Configuration {


    @ConfigSetting public boolean var_saveInit = true;
    @ConfigSetting public boolean var_saveEnable = true; // TODO <-- this
    @ConfigSetting public boolean var_saveDisable = true;
    @ConfigSetting public boolean var_saveShutdown = true;

    @ConfigSetting public String var_fileJoinQuits = "data/JoinQuits";
    @ConfigSetting public boolean var_saveJoins = true;
    @ConfigSetting public boolean var_saveQuits = true;

    @ConfigSetting public boolean var_savePlayerName = true;
    @ConfigSetting public boolean var_savePlayerUuid = true;

    @ConfigSetting public String var_filePlayerData = "data/playerData";
    @ConfigSetting public boolean var_savePlayerData = true;
    @ConfigSetting public boolean var_savePing = true; // only if savePlayerData true
    @ConfigSetting public boolean var_testGamemode = true; // saves gm if not survival; only if savePlayerData true
    @ConfigSetting public boolean var_saveTabTps = true;
    @ConfigSetting public boolean var_saveTabPing = true;
    @ConfigSetting public boolean var_saveTabPlayers = true;
    @ConfigSetting public boolean var_saveTabUptime = true;
    @ConfigSetting public boolean var_saveHeader = true;
    @ConfigSetting public boolean var_saveFooter = true;

    @ConfigSetting public int var_savePeriodMillis = 5 * 60 * 1000;
    @ConfigSetting public int var_testTabPeriodMillis = 30 * 1000;




    Config() {
        super("TabLogger");
    }
}