package com.wealthwise.dao;

import com.wealthwise.models.Category;
import com.wealthwise.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao implements IDao<Category> {

    private final Connection conn = DBConnection.getInstance().getConnection();

    // ── C — INSERT ────────────────────────────────────────────────────────────
    @Override
    public void add(Category c) {
        String sql = "INSERT INTO category (name, icon, color, is_custom, user_id) " +
                "VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getIcon());
            ps.setString(3, c.getColor());
            ps.setBoolean(4, c.isCustom());
            if (c.getUserId() != null) {
                ps.setInt(5, c.getUserId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── U — UPDATE ────────────────────────────────────────────────────────────
    @Override
    public void update(Category c) {
        String sql = "UPDATE category SET name=?, icon=?, color=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getIcon());
            ps.setString(3, c.getColor());
            ps.setInt(4, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── D — DELETE ────────────────────────────────────────────────────────────
    @Override
    public void delete(int id) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM category WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── R — GET BY ID ─────────────────────────────────────────────────────────
    @Override
    public Category getById(int id) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM category WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── R — GET ALL ───────────────────────────────────────────────────────────
    @Override
    public List<Category> getAll() {
        List<Category> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM category ORDER BY is_custom ASC, name ASC")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── R — GET GLOBAL + USER CUSTOM ─────────────────────────────────────────
    // returns global categories (user_id IS NULL) AND user-specific ones
    // global first, then custom, both sorted alphabetically
    public List<Category> getByUser(int userId) {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM category " +
                "WHERE user_id IS NULL OR user_id=? " +
                "ORDER BY is_custom ASC, name ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── R — GET ONLY USER CUSTOM CATEGORIES ──────────────────────────────────
    public List<Category> getCustomByUser(int userId) {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM category WHERE user_id=? ORDER BY name ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── MAP ROW → Category ────────────────────────────────────────────────────
    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setIcon(rs.getString("icon"));
        c.setColor(rs.getString("color"));
        c.setCustom(rs.getBoolean("is_custom"));
        int uid = rs.getInt("user_id");
        c.setUserId(rs.wasNull() ? null : uid);
        return c;
    }
}