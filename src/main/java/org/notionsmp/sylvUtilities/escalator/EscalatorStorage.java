package org.notionsmp.sylvUtilities.escalator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.notionsmp.sylvUtilities.SylvUtilities;

public final class EscalatorStorage {
    private static final Type LIST_TYPE = TypeToken.getParameterized(List.class, EscalatorData.class).getType();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File file;
    private final SylvUtilities plugin;

    public EscalatorStorage(final SylvUtilities plugin) {
        this.plugin = plugin;
        final File dataFolder = new File(plugin.getDataFolder(), "data");
        dataFolder.mkdirs();
        this.file = new File(dataFolder, "escalators.json");
    }

    public List<Escalator> load() {
        if (!this.file.exists()) {
            this.save(List.of());
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(this.file)) {
            final List<EscalatorData> data = readList(reader);
            if (data == null) {
                return new ArrayList<>();
            }
            final List<Escalator> escalators = new ArrayList<>();
            for (final EscalatorData entry : data) {
                final Escalator escalator = entry.toEscalator();
                if (escalator != null) {
                    escalators.add(escalator);
                }
            }
            return escalators;
        } catch (final Exception ignored) {
            this.plugin.getLogger().warning("Failed to load escalators from " + this.file.getAbsolutePath());
            return new ArrayList<>();
        }
    }

    public void save(final Collection<Escalator> escalators) {
        final List<EscalatorData> data = new ArrayList<>();
        for (final Escalator escalator : escalators) {
            data.add(EscalatorData.from(escalator));
        }
        try (FileWriter writer = new FileWriter(this.file)) {
            this.gson.toJson(data, writer);
        } catch (final Exception ignored) {
            this.plugin.getLogger().warning("Failed to save escalators to " + this.file.getAbsolutePath());
        }
    }

    private static List<EscalatorData> readList(final Reader reader) {
        final JsonElement json = JsonParser.parseReader(reader);
        if (json == null || !json.isJsonArray()) {
            return new ArrayList<>();
        }
        return new Gson().fromJson(json, LIST_TYPE);
    }

    private static final class Vec {
        private double x;
        private double y;
        private double z;

        private Location toLocation(final World world) {
            return new Location(world, this.x, this.y, this.z);
        }

        private static Vec fromLocation(final Location location) {
            final Vec vec = new Vec();
            vec.x = location.getX();
            vec.y = location.getY();
            vec.z = location.getZ();
            return vec;
        }
    }

    private static final class EscalatorData {
        private String id;
        private String world;
        private Vec top;
        private Vec bottom;
        private String direction;
        private double speed;
        private String sound;

        private static EscalatorData from(final Escalator escalator) {
            final EscalatorData data = new EscalatorData();
            data.id = escalator.id();
            data.world = escalator.world().getName();
            data.top = Vec.fromLocation(escalator.top());
            data.bottom = Vec.fromLocation(escalator.bottom());
            data.direction = escalator.direction().name();
            data.speed = escalator.speedBlocksPerSecond();
            data.sound = escalator.soundKey();
            return data;
        }

        private Escalator toEscalator() {
            if (this.id == null || this.id.isBlank() || this.world == null || this.world.isBlank() || this.top == null || this.bottom == null) {
                return null;
            }
            final World w = Bukkit.getWorld(this.world);
            if (w == null) {
                return null;
            }
            final EscalatorDirection dir = EscalatorDirection.parse(this.direction);
            if (dir == null) {
                return null;
            }
            final String key = this.sound == null || this.sound.isBlank() ? null : this.sound;
            return new Escalator(this.id, this.top.toLocation(w), this.bottom.toLocation(w), dir, this.speed, key);
        }
    }
}
