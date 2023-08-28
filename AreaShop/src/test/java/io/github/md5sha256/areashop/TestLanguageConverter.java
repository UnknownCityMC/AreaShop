package io.github.md5sha256.areashop;

import be.seeseemelk.mockbukkit.MockBukkit;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.interactivemessenger.generators.TellrawGenerator;
import me.wiefferink.interactivemessenger.parsers.YamlParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestLanguageConverter {


    @BeforeAll
    public static void setup() {
        MockBukkit.mock();
    }

    @AfterAll
    public static void teardown() {
        MockBukkit.unmock();
    }

    private static void performTransformation(ConfigurationNode root) throws ConfigurateException {
        if (!root.isMap()) {
            return;
        }
        Map<Object, ? extends ConfigurationNode> map = root.childrenMap();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : map.entrySet()) {
            ConfigurationNode node = entry.getValue();
            if (node.isMap()) {
                performTransformation(node);
            } else {
                convertNode(node);
            }
        }
    }

    private static void serializeModernToBungee(ConfigurationNode node) throws ConfigurateException {
        List<String> messages;
        if (node.isList()) {
            messages = node.getList(String.class, Collections.emptyList());
        } else {
            messages = Collections.singletonList(node.getString());
        }
        List<String> converted = new ArrayList<>(messages.size());
        for (String miniMessage : messages) {
            Component component = MiniMessage.miniMessage().deserialize(miniMessage);
            BaseComponent[] baseComponents = BungeeComponentSerializer.get().serialize(component);
            String serial = ComponentSerializer.toString(baseComponents);
            converted.add(serial);
        }
        if (converted.size() == 1) {
            node.set(converted.get(0));
        } else {
            node.setList(String.class, converted);
        }
    }

    private static void convertNode(ConfigurationNode node) throws ConfigurateException {
        List<String> messages;
        if (node.isList()) {
            messages = node.getList(String.class, Collections.emptyList());
        } else {
            messages = Collections.singletonList(node.getString());
        }
        List<String> raw = TellrawGenerator.generate(YamlParser.parse(messages));
        List<String> converted = new ArrayList<>(raw.size());
        for (String json : raw) {
            BaseComponent[] components = ComponentSerializer.parse(json);
            String serialized = ComponentSerializer.toString(components);
            converted.add(serialized);
        }
        if (converted.size() == 1) {
            node.set(converted.get(0));
        } else {
            node.setList(String.class, converted);
        }
    }

    private static void compareRoot(ConfigurationNode legacy,
                                    ConfigurationNode modern) throws ConfigurateException {
        if (!legacy.isMap() && !modern.isMap()) {
            return;
        }
        Map<Object, ? extends ConfigurationNode> map = legacy.childrenMap();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : map.entrySet()) {
            ConfigurationNode node = entry.getValue();
            if (node.isMap()) {
                compareRoot(node, modern.node(node.path()));
            } else {
                compare(node, modern.node(node.path()));
            }
        }
    }

    private static void compare(ConfigurationNode legacy,
                                ConfigurationNode modern) throws ConfigurateException {
        List<String> legacyStrings;
        List<String> modernStrings;
        if (legacy.isList()) {
            Assertions.assertTrue(modern.isList());
            legacyStrings = legacy.getList(String.class, Collections.emptyList());
            modernStrings = modern.getList(String.class, Collections.emptyList());
        } else {
            legacyStrings = Collections.singletonList(legacy.getString());
            modernStrings = Collections.singletonList(modern.getString());
        }
        for (int i = 0; i < legacyStrings.size(); i++) {
            String legacySerial = legacyStrings.get(i);
            String modernSerial = modernStrings.get(i);
            BaseComponent[] legacyComponents = ComponentSerializer.parse(legacySerial);
            Component modernComponent = MiniMessage.miniMessage().deserialize(modernSerial);
            String legacySection = LegacyComponentSerializer.legacySection().serialize(modernComponent);
            Assertions.assertEquals(BaseComponent.toLegacyText(legacyComponents), legacySection);
        }

    }

    private File[] getLangFiles() {
        URL langRoot = AreaShop.class.getClassLoader().getResource("lang");
        if (langRoot == null) {
            throw new RuntimeException("Cannot find lang files!");
        }
        URI uri;
        try {
            uri = langRoot.toURI();
        } catch (URISyntaxException ex) {
            throw new RuntimeException("URI syntax!", ex);
        }
        File file = new File(uri);
        return file.listFiles(sub -> sub.getName().endsWith(".yml"));
    }

    private List<ConfigurationNode> loadLangFiles() throws IOException {
        File[] files = getLangFiles();
        if (files == null) {
            throw new RuntimeException("Could not find any lang files!");
        }
        List<ConfigurationNode> nodes = new ArrayList<>(files.length);
        for (File langFile : files) {
            ConfigurationNode node = YamlConfigurationLoader.builder()
                    .file(langFile)
                    .build()
                    .load();
            nodes.add(node);
        }
        return nodes;
    }

    private String serializeNode(ConfigurationNode node) {
        StringWriter writer = new StringWriter();
        try {
            YamlConfigurationLoader.builder().sink(() -> new BufferedWriter(writer)).build()
                    .save(node);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        return writer.getBuffer().toString();
    }

    public void testConversion() {
        List<ConfigurationNode> langFiles;
        try {
            langFiles = loadLangFiles();
        } catch (IOException ex) {
            Assertions.fail(ex);
            return;
        }
        for (ConfigurationNode node : langFiles) {
            ConfigurationNode legacy = node.copy();
            ConfigurationNode modern = node.copy();
            try {
                performTransformation(legacy);
                LanguageConverter.performConversion(modern);
                compareRoot(legacy, modern);
            } catch (IOException ex) {
                Assertions.fail(ex);
                break;
            }

        }
    }

}
