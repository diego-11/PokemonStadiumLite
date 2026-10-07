package battle;

/**
 * Interface de eventos del combate (patrón Observer / Listener).
 *
 * La UI implementa esta interface y se actualiza SOLO a partir
 * de las notificaciones que emite {@link Battle}.
 */
public interface BattleListener {

    /**
     * Llamado al finalizar cada turno.
     *
     * @param attacker nombre del atacante
     * @param defender nombre del defensor
     * @param damage   daño aplicado (ya con crítico y tipo)
     * @param critical {@code true} si el golpe fue crítico (x1.5)
     * @param modifier multiplicador de tipo (0.7 / 1.0 / 1.3)
     */
    void onTurn(String attacker, String defender, int damage,
                boolean critical, double modifier);

    /**
     * Llamado después de que el HP de un Pokémon cambia.
     *
     * @param pokemon  nombre del Pokémon afectado
     * @param hpActual HP restante (≥ 0)
     */
    void onHpChanged(String pokemon, int hpActual);

    /**
     * Llamado cuando un Pokémon llega a 0 HP y la batalla termina.
     *
     * @param winner nombre del ganador
     */
    void onBattleEnded(String winner);
}
