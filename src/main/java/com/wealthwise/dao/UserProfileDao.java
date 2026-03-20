package com.wealthwise.dao;

import com.wealthwise.models.UserProfile;
import com.wealthwise.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserProfileDao implements IDao<UserProfile> {

    private final Connection conn = DBConnection.getInstance().getConnection();

    // ── C — INSERT ────────────────────────────────────────────────────────────
    @Override
    public void add(UserProfile p) {
        String sql = "INSERT INTO user_profile (user_id, monthly_income, " +
                "preferred_currency, language) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getUserId());
            ps.setBigDecimal(2, p.getMonthlyIncome());
            ps.setString(3, p.getPreferredCurrency());
            ps.setString(4, p.getLanguage());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── U — UPDATE ────────────────────────────────────────────────────────────
    @Override
    public void update(UserProfile p) {
        String sql = "UPDATE user_profile SET monthly_income=?, " +
                "preferred_currency=?, language=? WHERE user_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, p.getMonthlyIncome());
            ps.setString(2, p.getPreferredCurrency());
            ps.setString(3, p.getLanguage());
            ps.setInt(4, p.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── D — DELETE ────────────────────────────────────────────────────────────
    @Override
    public void delete(int userId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM user_profile WHERE user_id=?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── R — GET BY USER ID ────────────────────────────────────────────────────
    public UserProfile getByUserId(int userId) {
        String sql = "SELECT * FROM user_profile WHERE user_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── R — GET BY ID ─────────────────────────────────────────────────────────
    @Override
    public UserProfile getById(int id) {
        return getByUserId(id);
    }

    // ── R — GET ALL ───────────────────────────────────────────────────────────
    @Override
    public List<UserProfile> getAll() {
        List<UserProfile> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM user_profile")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── CHECK IF PROFILE EXISTS ───────────────────────────────────────────────
    public boolean existsForUser(int userId) {
        String sql = "SELECT COUNT(*) FROM user_profile WHERE user_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── SAVE OR UPDATE (upsert) ───────────────────────────────────────────────
    // creates profile if doesn't exist, updates if it does
    public void saveOrUpdate(UserProfile p) {
        if (existsForUser(p.getUserId())) {
            update(p);
        } else {
            add(p);
        }
    }

    // ── MAP ROW → UserProfile ─────────────────────────────────────────────────
    private UserProfile mapRow(ResultSet rs) throws SQLException {
        UserProfile p = new UserProfile();
        p.setId(rs.getInt("id"));
        p.setUserId(rs.getInt("user_id"));
        p.setMonthlyIncome(rs.getBigDecimal("monthly_income"));
        p.setPreferredCurrency(rs.getString("preferred_currency"));
        p.setLanguage(rs.getString("language"));
        return p;
    }
}