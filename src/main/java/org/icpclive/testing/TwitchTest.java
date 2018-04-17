package org.icpclive.testing;

import org.icpclive.Config;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Meepo on 4/2/2018.
 */
public class TwitchTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        String url = null;
        String username = null;
        String password = null;
        String channels = null;
        try {
            properties = Config.loadProperties("mainscreen");
            url = properties.getProperty("twitch.chat.server", "irc.chat.twitch.tv");
            username = properties.getProperty("twitch.chat.username");
            password = properties.getProperty("twitch.chat.password");
            channels = properties.getProperty("twitch.chat.channel", "#" + username);
        } catch (IOException e) {
        }

        Configuration.Builder configuration = new Configuration.Builder()
                .setAutoNickChange(false)
                .setOnJoinWhoEnabled(false)
                .setCapEnabled(true)
                .addServer("irc.twitch.tv")
                .setName("aksenov239")
                .setServerPassword("oauth:g3wy3fjka8wort07e8usp6jg7dedmf")
                .addListener(new ListenerAdapter() {
                    @Override
                    public void onMessage(MessageEvent event) {
                        System.err.println("FUUUUUCK!");
                    }
                });
        PircBotX bot =
                new PircBotX(configuration.addAutoJoinChannel("#aksenov239").buildConfiguration());
        try {
            bot.startBot();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IrcException e) {
            e.printStackTrace();
        }
    }
}
