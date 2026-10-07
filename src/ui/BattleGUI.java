package ui;

import api.PokeApiClient;
import battle.Battle;
import battle.BattleListener;
import model.Pokemon;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal de "Pokémon Stadium Lite".
 *
 * Implementa {@link BattleListener} para recibir los eventos del combate
 * y actualizar la UI en el Event Dispatch Thread (EDT) de forma segura.
 *
 * Flujo:
 *  1. El usuario carga un Pokémon en cada {@link PokemonPanel} (Load o Random).
 *  2. Cuando ambos están listos, se habilita el botón "Fight!".
 *  3. Al presionar "Fight!", la batalla corre en un SwingWorker (hilo de fondo).
 *  4. Cada evento del combate llega aquí y se despacha al EDT con invokeLater.
 */
public class BattleGUI extends JFrame implements BattleListener {

    // ── Componentes principales ───────────────────────────────────────────────
    private PokemonPanel panel1;
    private PokemonPanel panel2;
    private JButton      fightButton;
    private JTextArea    logArea;

    // ── Servicios ─────────────────────────────────────────────────────────────
    private final PokeApiClient apiClient;

    // ── Constructor ───────────────────────────────────────────────────────────

    public BattleGUI() {
        super("⚔ Pokémon Stadium Lite ⚔");
        apiClient = new PokeApiClient();
        construirUI();
    }

    // ── Construcción de la UI ─────────────────────────────────────────────────

    private void construirUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // — Título ————————————————————————————————————————————————————————
        JLabel titulo = new JLabel("⚔  Pokémon Stadium Lite  ⚔", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        titulo.setForeground(new Color(40, 60, 160));
        titulo.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        add(titulo, BorderLayout.NORTH);

        // — Paneles de Pokémon ————————————————————————————————————————————
        JPanel arenaPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel1 = new PokemonPanel("🔵  Jugador 1", this);
        panel2 = new PokemonPanel("🔴  Jugador 2", this);
        arenaPanel.add(panel1);
        arenaPanel.add(panel2);
        add(arenaPanel, BorderLayout.CENTER);

        // — Panel sur: Fight! + Log ———————————————————————————————————————
        JPanel southPanel = new JPanel(new BorderLayout(0, 6));

        // Botón Fight!
        fightButton = new JButton("⚔  Fight!");
        fightButton.setEnabled(false);
        fightButton.setFont(new Font("Arial", Font.BOLD, 17));
        fightButton.setBackground(new Color(220, 60, 60));
        fightButton.setForeground(Color.WHITE);
        fightButton.setFocusPainted(false);
        fightButton.addActionListener(e -> iniciarBatalla());

        JPanel fightWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fightWrap.add(fightButton);
        southPanel.add(fightWrap, BorderLayout.NORTH);

        // Log de batalla
        logArea = new JTextArea(9, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(248, 248, 248));
        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setBorder(BorderFactory.createTitledBorder("📋 Log de Batalla"));
        southPanel.add(scrollLog, BorderLayout.CENTER);

        add(southPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);   // centrar en pantalla
    }

    // ── Lógica de la batalla ──────────────────────────────────────────────────

    /** Verifica si ambos Pokémon están cargados y activa/desactiva "Fight!". */
    public void verificarAmbosCargados() {
        fightButton.setEnabled(
                panel1.isPokemonCargado() && panel2.isPokemonCargado());
    }

    /** Inicia la batalla en un SwingWorker para no bloquear el EDT. */
    private void iniciarBatalla() {
        // Reiniciar HP
        panel1.getPokemon().resetHp();
        panel2.getPokemon().resetHp();
        panel1.refrescarHpDesdeModelo();
        panel2.refrescarHpDesdeModelo();

        logArea.setText("");
        agregarLog("── Nueva batalla: "
                + capitalizar(panel1.getPokemon().getName())
                + " vs "
                + capitalizar(panel2.getPokemon().getName())
                + " ──");

        fightButton.setEnabled(false);

        Battle battle = new Battle(panel1.getPokemon(), panel2.getPokemon(), this);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                battle.fight();
                return null;
            }

            @Override
            protected void done() {
                // Rehabilitar "Fight!" en el EDT una vez finaliza el worker
                fightButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    // ── Implementación de BattleListener (llamados desde hilo de fondo) ───────

    @Override
    public void onTurn(String attacker, String defender, int damage,
                       boolean critical, double modifier) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("⚔ ").append(capitalizar(attacker))
              .append(" → ").append(capitalizar(defender))
              .append("  Daño: ").append(damage);
            if (critical)     sb.append("  💥 ¡CRÍTICO! (×1.5)");
            if (modifier > 1.0) sb.append("  🔥 Muy efectivo (×").append(modifier).append(")");
            else if (modifier < 1.0) sb.append("  💧 Poco efectivo (×").append(modifier).append(")");
            agregarLog(sb.toString());
        });
    }

    @Override
    public void onHpChanged(String pokemon, int hpActual) {
        SwingUtilities.invokeLater(() -> {
            agregarLog("   " + capitalizar(pokemon) + " HP restante: " + hpActual);
            // Actualizar barra del panel correspondiente
            if (panel1.getPokemon() != null
                    && panel1.getPokemon().getName().equals(pokemon)) {
                panel1.actualizarHp(hpActual);
            } else if (panel2.getPokemon() != null
                    && panel2.getPokemon().getName().equals(pokemon)) {
                panel2.actualizarHp(hpActual);
            }
        });
    }

    @Override
    public void onBattleEnded(String winner) {
        SwingUtilities.invokeLater(() -> {
            agregarLog("");
            agregarLog("🏆  ¡" + capitalizar(winner) + " gana la batalla! 🏆");
            JOptionPane.showMessageDialog(
                    this,
                    "🏆  ¡" + capitalizar(winner) + " gana!",
                    "Fin del combate",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private void agregarLog(String linea) {
        logArea.append(linea + "\n");
        // Desplazar al final automáticamente
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private String capitalizar(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // ── Getter para PokemonPanel ──────────────────────────────────────────────
    public PokeApiClient getApiClient() { return apiClient; }

    // ── Punto de entrada ─────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Look and feel del sistema operativo
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }

            new BattleGUI().setVisible(true);
        });
    }
}
