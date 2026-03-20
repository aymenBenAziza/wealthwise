package com.wealthwise.dao;

import com.wealthwise.models.Transaction;
import com.wealthwise.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao implements IDao<Transaction> {

    private final Connection conn = DBConnection.getInstance().getConnection();

    // ── C — INSERT ────────────────────────────────────────────────────────────
    @Override
    public void add(Transaction t) {
        String sql = "INSERT INTO transaction " +
                "(user_id, category_id, amount, type, transaction_date, note) " +
                "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getUserId());
            ps.setInt(2, t.getCategoryId());
            ps.setBigDecimal(3, t.getAmount());
            ps.setString(4, t.getType());
            ps.setDate(5, Date.valueOf(t.getTransactionDate()));
            ps.setString(6, t.getNote());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── U — UPDATE ────────────────────────────────────────────────────────────
    @Override
    public void update(Transaction t) {
        String sql = "UPDATE transaction SET category_id=?, amount=?, type=?, " +
                "transaction_date=?, note=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getCategoryId());
            ps.setBigDecimal(2, t.getAmount());
            ps.setString(3, t.getType());
            ps.setDate(4, Date.valueOf(t.getTransactionDate()));
            ps.setString(5, t.getNote());
            ps.setInt(6, t.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── D — DELETE ────────────────────────────────────────────────────────────
    @Override
    public void delete(int id) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM transaction WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── R — GET BY ID ─────────────────────────────────────────────────────────
    @Override
    public Transaction getById(int id) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM transaction WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── R — GET ALL ───────────────────────────────────────────────────────────
    @Override
    public List<Transaction> getAll() {
        List<Transaction> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM transaction ORDER BY transaction_date DESC")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── R — GET BY USER ───────────────────────────────────────────────────────
    public List<Transaction> getByUserId(int userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE user_id=? " +
                "ORDER BY transaction_date DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── R — FILTER BY TYPE ────────────────────────────────────────────────────
    public List<Transaction> getByUserAndType(int userId, String type) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transaction WHERE user_id=? AND type=? " +
                "ORDER BY transaction_date DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── R — SEARCH BY NOTE OR CATEGORY NAME ──────────────────────────────────
    public List<Transaction> search(int userId, String keyword) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.* FROM transaction t " +
                "LEFT JOIN category c ON t.category_id = c.id " +
                "WHERE t.user_id=? " +
                "AND (t.note LIKE ? OR c.name LIKE ?) " +
                "ORDER BY t.transaction_date DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── MAP ROW → Transaction ─────────────────────────────────────────────────
    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setCategoryId(rs.getInt("category_id"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setType(rs.getString("type"));
        t.setTransactionDate(rs.getDate("transaction_date").toLocalDate());
        t.setNote(rs.getString("note"));
        t.setReceiptUrl(rs.getString("receipt_url"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) t.setCreatedAt(ts.toLocalDateTime());
        return t;
    }
}