package model;

import java.util.List;

/**
 * Modelo que representa un Pokémon con sus estadísticas base,
 * tipos, sprite URL y HP actual durante la batalla.
 */
public class Pokemon {

    private final int id;
    private final String name;
    private final List<String> types;
    private final int maxHp;
    private int currentHp;
    private final int attack;
    private final int defense;
    private final int speed;
    private final String spriteUrl;

    public Pokemon(int id, String name, List<String> types,
                   int hp, int attack, int defense, int speed, String spriteUrl) {
        this.id       = id;
        this.name     = name;
        this.types    = types;
        this.maxHp    = hp;
        this.currentHp = hp;
        this.attack   = attack;
        this.defense  = defense;
        this.speed    = speed;
        this.spriteUrl = spriteUrl;
    }

    /** Restaura el HP al valor máximo (para reiniciar una batalla). */
    public void resetHp() {
        this.currentHp = this.maxHp;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int    getId()        { return id; }
    public String getName()      { return name; }
    public List<String> getTypes() { return types; }
    public int    getMaxHp()     { return maxHp; }
    public int    getCurrentHp() { return currentHp; }
    public int    getAttack()    { return attack; }
    public int    getDefense()   { return defense; }
    public int    getSpeed()     { return speed; }
    public String getSpriteUrl() { return spriteUrl; }

    public void setCurrentHp(int currentHp) {
        this.currentHp = Math.max(0, currentHp);
    }

    @Override
    public String toString() {
        return name + " (HP:" + currentHp + "/" + maxHp
                + " ATK:" + attack + " DEF:" + defense + " SPD:" + speed + ")";
    }
}
