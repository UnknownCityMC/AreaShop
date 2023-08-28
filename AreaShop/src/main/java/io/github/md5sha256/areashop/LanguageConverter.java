package io.github.md5sha256.areashop;

import me.wiefferink.interactivemessenger.generators.TellrawGenerator;
import me.wiefferink.interactivemessenger.parsers.YamlParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LanguageConverter {

    public static void performConversion(ConfigurationNode root) throws ConfigurateException {
        if (!root.isMap()) {
            convertNode(root);
            return;
        }
        Map<Object, ? extends ConfigurationNode> map = root.childrenMap();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : map.entrySet()) {
            ConfigurationNode node = entry.getValue();
            if (node.isMap()) {
                performConversion(node);
            } else {
                convertNode(node);
            }
        }
    }

    public static void performConversion(File toConvert, File output) throws IOException {
        YamlConfigurationLoader sourceLoader = YamlConfigurationLoader.builder()
                .file(toConvert)
                .build();
        YamlConfigurationLoader destinationLoader = YamlConfigurationLoader.builder()
                .file(output)
                .build();
        ConfigurationNode root = sourceLoader.load();
        performConversion(root);
        destinationLoader.save(root);
    }

    private static void convertNode(ConfigurationNode node) throws ConfigurateException {
        List<String> messages;
        if (node.isList()) {
            messages = node.getList(String.class, Collections.emptyList());
        } else {
            messages = Collections.singletonList(node.getString());
        }
        List<String> jsonMessages = TellrawGenerator.generate(YamlParser.parse(messages));
        List<String> converted = new ArrayList<>();
        for (String json : jsonMessages) {
            BaseComponent[] bungeeComponents = ComponentSerializer.parse(json);
            Component convertedComponent = BungeeComponentSerializer.get()
                    .deserialize(bungeeComponents);
            String miniMessage = MiniMessage.miniMessage()
                    .serialize(convertedComponent);
            converted.add(miniMessage);
        }
        if (converted.size() == 1) {
            node.set(String.class, converted.get(0));
        } else {
            node.setList(String.class, converted);
        }
    }


}
