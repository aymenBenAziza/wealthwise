package com.wealthwise.models;

public class Category {

    private int     id;
    private String  name;
    private String  icon;
    private String  color;
    private boolean isCustom;
    private Integer userId;   // NULL = global, NOT NULL = user custom

    // ── Constructors ──────────────────────────────────────────────────────────
    public Category() {}

    // global category (admin)
    public Category(String name, String icon, String color) {
        this.name     = name;
        this.icon     = icon;
        this.color    = color;
        this.isCustom = false;
        this.userId   = null;
    }

    // custom category (user)
    public Category(String name, String icon, String color, int userId) {
        this.name     = name;
        this.icon     = icon;
        this.color    = color;
        this.isCustom = true;
        this.userId   = userId;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int     getId()                   { return id; }
    public void    setId(int id)             { this.id = id; }

    public String  getName()                 { return name; }
    public void    setName(String name)      { this.name = name; }

    public String  getIcon()                 { return icon; }
    public void    setIcon(String icon)      { this.icon = icon; }

    public String  getColor()                { return color; }
    public void    setColor(String color)    { this.color = color; }

    public boolean isCustom()                { return isCustom; }
    public void    setCustom(boolean custom) { this.isCustom = custom; }

    public Integer getUserId()               { return userId; }
    public void    setUserId(Integer userId) { this.userId = userId; }

    // used by ComboBox to display category name
    @Override
    public String toString() { return name; }
}