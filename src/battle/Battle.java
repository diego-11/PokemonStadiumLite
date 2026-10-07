package battle;

import model.Pokemon;

import java.util.Random;

/**
 * Contiene la lógica del combate por turnos entre dos {@link Pokemon}.
 *
 * Diseño de la fórmula de daño (documentado según requisito):
 * ──────────────────────────────────────────────────────────
 *   damage = ATK_atacante * rand(0..1) - DEF_defensor * rand(0..1)
 *   Mínimo garantizado: 1 punto de daño por turno.
 *   Crítico (10% de prob.): multiplica el daño × 1.5.
 *   Efectividad de tipo (solo primer tipo):
 *     Agua > Fuego | Fuego > Planta | Planta > Agua  → × 1.3
 *     Inversa de lo anterior                          → × 0.7
 *     Resto de combinaciones                          → × 1.0
 *
 * La clase notifica cada evento a través de {@link BattleListener}
 * para mantener la UI completamente desacoplada.
 *
 * IMPORTANTE: {@code fight()} debe ejecutarse en un hilo secundario
 * (p. ej. SwingWorker) para no bloquear el hilo de la UI.
 */
public class Battle {

    private static final double CRIT_CHANCE    = 0.10;  // 10 %
    private static final double CRIT_MULT      = 1.5;
    private static final double EFFECTIVE_MULT = 1.3;
    private static final double WEAK_MULT      = 0.7;
    private static final int    MAX_TURNS      = 150;   // evita batallas infinitas
    private static final int    TURN_DELAY_MS  = 900;   // pausa entre turnos (ms)

    private final Pokemon        pokemon1;
    private final Pokemon        pokemon2;
    private final BattleListener listener;
    private final Random         random;

    public Battle(Pokemon pokemon1, Pokemon pokemon2, BattleListener listener) {
        this.pokemon1 = pokemon1;
        this.pokemon2 = pokemon2;
        this.listener = listener;
        this.random   = new Random();
    }

    /**
     * Ejecuta el combate completo.
     * Bloquea el hilo hasta que un Pokémon llegue a 0 HP o se alcance el límite de turnos.
     */
    public void fight() {
        // ── Determinar quién ataca primero ───────────────────────────────────
        Pokemon attacker, defender;
        if (pokemon1.getSpeed() > pokemon2.getSpeed()) {
            attacker = pokemon1;
            defender = pokemon2;
        } else if (pokemon2.getSpeed() > pokemon1.getSpeed()) {
            attacker = pokemon2;
            defender = pokemon1;
        } else {
            // Empate → aleatorio
            if (random.nextBoolean()) {
                attacker = pokemon1;
                defender = pokemon2;
            } else {
                attacker = pokemon2;
                defender = pokemon1;
            }
        }

        // ── Bucle de combate ─────────────────────────────────────────────────
        int turn = 0;
        while (pokemon1.getCurrentHp() > 0
                && pokemon2.getCurrentHp() > 0
                && turn < MAX_TURNS) {
            turn++;

            // Calcular daño
            boolean critical = random.nextDouble() < CRIT_CHANCE;
            double  modifier = typeModifier(attacker, defender);

            double atkRoll  = attacker.getAttack()  * random.nextDouble();
            double defRoll  = defender.getDefense() * random.nextDouble();
            int    damage   = (int) Math.max(1, atkRoll - defRoll);

            if (critical) damage = (int) Math.ceil(damage * CRIT_MULT);
            damage = (int) Math.max(1, Math.round(damage * modifier));

            // Aplicar daño
            int newHp = Math.max(0, defender.getCurrentHp() - damage);
            defender.setCurrentHp(newHp);

            // Notificar eventos
            listener.onTurn(attacker.getName(), defender.getName(), damage, critical, modifier);
            listener.onHpChanged(defender.getName(), defender.getCurrentHp());

            // ¿Fin de la batalla?
            if (defender.getCurrentHp() <= 0) {
                listener.onBattleEnded(attacker.getName());
                return;
            }

            // Pausa entre turnos
            try {
                Thread.sleep(TURN_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            // Alternar atacante y defensor
            Pokemon tmp = attacker;
            attacker = defender;
            defender = tmp;
        }

        // Si se agotaron los turnos, gana quien tenga más HP
        if (turn >= MAX_TURNS) {
            String winner = pokemon1.getCurrentHp() >= pokemon2.getCurrentHp()
                    ? pokemon1.getName() : pokemon2.getName();
            listener.onBattleEnded(winner + " (por HP restante)");
        }
    }

    // ── Efectividad de tipo ───────────────────────────────────────────────────

    /**
     * Calcula el multiplicador de tipo según el primer tipo del atacante
     * y el primer tipo del defensor.
     */
    private double typeModifier(Pokemon attacker, Pokemon defender) {
        if (attacker.getTypes().isEmpty() || defender.getTypes().isEmpty()) return 1.0;

        String atkType = attacker.getTypes().get(0);
        String defType = defender.getTypes().get(0);

        if (isEffective(atkType, defType))   return EFFECTIVE_MULT;
        if (isEffective(defType, atkType))   return WEAK_MULT;
        return 1.0;
    }

    /**
     * Agua > Fuego, Fuego > Planta, Planta > Agua.
     */
    private boolean isEffective(String atkType, String defType) {
        return (atkType.equals("water") && defType.equals("fire"))
            || (atkType.equals("fire")  && defType.equals("grass"))
            || (atkType.equals("grass") && defType.equals("water"));
    }
}
