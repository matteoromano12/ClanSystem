package com.discepolo.clansystem.database;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClaimedChunk;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClanRepository {

    private final DatabaseManager database;
    private final Logger logger;

    public ClanRepository(DatabaseManager database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    public void insertClan(Clan clan) {
        String sql = "INSERT INTO clans (name, tag, created_at) VALUES (?, ?, ?)";

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, clan.getName());
            ps.setString(2, clan.getTag());
            ps.setLong(3, clan.getCreatedAt());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    clan.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nel salvataggio del clan " + clan.getName(), e);
        }
    }

    public void deleteClan(int clanId) {
        String sql = "DELETE FROM clans WHERE id = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clanId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nell'eliminazione del clan " + clanId, e);
        }
    }

    public void updateHome(int clanId, Location home) {
        String sql = "UPDATE clans SET home_world = ?, home_x = ?, home_y = ?, "
                + "home_z = ?, home_yaw = ?, home_pitch = ? WHERE id = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (home == null) {
                ps.setNull(1, Types.VARCHAR);
                ps.setNull(2, Types.DOUBLE);
                ps.setNull(3, Types.DOUBLE);
                ps.setNull(4, Types.DOUBLE);
                ps.setNull(5, Types.FLOAT);
                ps.setNull(6, Types.FLOAT);
            } else {
                ps.setString(1, home.getWorld().getName());
                ps.setDouble(2, home.getX());
                ps.setDouble(3, home.getY());
                ps.setDouble(4, home.getZ());
                ps.setFloat(5, home.getYaw());
                ps.setFloat(6, home.getPitch());
            }
            ps.setInt(7, clanId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nell'aggiornamento della home del clan " + clanId, e);
        }
    }

    public void insertMember(int clanId, ClanMember member) {
        String sql = "INSERT INTO clan_members (uuid, clan_id, name, role, joined_at) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, member.getUuid().toString());
            ps.setInt(2, clanId);
            ps.setString(3, member.getLastKnownName());
            ps.setString(4, member.getRole().name());
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nel salvataggio del membro " + member.getLastKnownName(), e);
        }
    }

    public void deleteMember(UUID uuid) {
        String sql = "DELETE FROM clan_members WHERE uuid = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nell'eliminazione del membro " + uuid, e);
        }
    }

    public void updateMemberRole(UUID uuid, String role) {
        String sql = "UPDATE clan_members SET role = ? WHERE uuid = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nell'aggiornamento del ruolo di " + uuid, e);
        }
    }

    public void insertClaim(int clanId, ClaimedChunk chunk) {
        String sql = "INSERT INTO clan_claims (clan_id, world, chunk_x, chunk_z, claimed_at) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clanId);
            ps.setString(2, chunk.getWorld());
            ps.setInt(3, chunk.getX());
            ps.setInt(4, chunk.getZ());
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nel salvataggio del claim", e);
        }
    }

    public void deleteClaim(ClaimedChunk chunk) {
        String sql = "DELETE FROM clan_claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?";

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chunk.getWorld());
            ps.setInt(2, chunk.getX());
            ps.setInt(3, chunk.getZ());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nell'eliminazione del claim", e);
        }
    }

    public Map<Integer, Clan> loadClans() {
        Map<Integer, Clan> result = new HashMap<>();
        String sql = "SELECT * FROM clans";

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Clan clan = new Clan(rs.getString("name"), rs.getString("tag"));
                clan.setId(rs.getInt("id"));

                String worldName = rs.getString("home_world");
                if (worldName != null) {
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        clan.setHome(new Location(world,
                                rs.getDouble("home_x"), rs.getDouble("home_y"), rs.getDouble("home_z"),
                                rs.getFloat("home_yaw"), rs.getFloat("home_pitch")));
                    }
                }

                result.put(clan.getId(), clan);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nel caricamento dei clan", e);
        }
        return result;
    }

    public void loadMembers(Map<Integer, Clan> clansById, Map<UUID, Clan> clansByPlayer) {
        String sql = "SELECT * FROM clan_members";

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Clan clan = clansById.get(rs.getInt("clan_id"));
                if (clan == null) continue;

                UUID uuid = UUID.fromString(rs.getString("uuid"));
                ClanRole role = ClanRole.valueOf(rs.getString("role"));

                clan.addMember(new ClanMember(uuid, rs.getString("name"), role));
                clansByPlayer.put(uuid, clan);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nel caricamento dei membri", e);
        }
    }


    public void loadClaims(Map<Integer, Clan> clansById, Map<ClaimedChunk, Clan> claimsIndex) {
        String sql = "SELECT * FROM clan_claims";

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Clan clan = clansById.get(rs.getInt("clan_id"));
                if (clan == null) continue;

                ClaimedChunk chunk = new ClaimedChunk(
                        rs.getString("world"), rs.getInt("chunk_x"), rs.getInt("chunk_z"));

                clan.addClaim(chunk);
                claimsIndex.put(chunk, clan);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nel caricamento dei territori", e);
        }
    }
}