package org.notionsmp.sylvUtilities.escalator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

    public EscalatorStorage(final SylvUtilities plugin) {
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
            final List<EscalatorData> data = this.gson.fromJson(reader, LIST_TYPE);
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
        }
    }

    private record Vec(double x, double y, double z) {
        Location toLocation(final World world) {
            return new Location(world, this.x, this.y, this.z);
        }

        static Vec fromLocation(final Location location) {
            return new Vec(location.getX(), location.getY(), location.getZ());
        }
    }

    private record EscalatorData(String id, String world, Vec top, Vec bottom, String direction, double speed) {
        static EscalatorData from(final Escalator escalator) {
            return new EscalatorData(
                escalator.id(),
                escalator.world().getName(),
                Vec.fromLocation(escalator.top()),
                Vec.fromLocation(escalator.bottom()),
                escalator.direction().name(),
                escalator.speedBlocksPerSecond()
            );
        }

        Escalator toEscalator() {
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
            return new Escalator(this.id, this.top.toLocation(w), this.bottom.toLocation(w), dir, this.speed);
        }
    }
}

