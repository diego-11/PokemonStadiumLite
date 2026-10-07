package ui;

import api.PokeApiClient;
import model.Pokemon;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Panel visual que representa a un Pokémon en la arena de combate.
 *
 * Contiene:
 *  - Campo de texto + botones "Load" y "Random"
 *  - Sprite (imagen frontal)
 *  - Nombre y tipos
 *  - Barra de HP con color dinámico
 *  - Etiquetas de ATK, DEF y Speed
 */
public class PokemonPanel extends JPanel {

    // ── Estado ───────────────────────────────────────────────────────────────
    private Pokemon       pokemon;
    private boolean       pokemonLoaded = false;
    private final BattleGUI parent;

    // ── Componentes de entrada ────────────────────────────────────────────────
    private JTextField nameField;
    private JButton    loadButton;
    private JButton    randomButton;

    // ── Componentes de visualización ──────────────────────────────────────────
    private JLabel       spriteLabel;
    private JLabel       nameLabel;
    private JLabel       typesLabel;
    private JProgressBar hpBar;
    private JLabel       hpLabel;
    private JLabel       atkLabel;
    private JLabel       defLabel;
    private JLabel       speedLabel;

    public PokemonPanel(String playerTitle, BattleGUI parent) {
        this.parent = parent;
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), playerTitle));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(260, 420));
        buildComponents();
        wireListeners();
    }

    // ── Construcción de la UI ────────────────────────────────────────────────

    private void buildComponents() {

        // — Fila de entrada: [TextField] [Load] [Random] ——————————————————
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        nameField    = new JTextField(9);
        nameField.setToolTipText("Nombre del Pokémon (ej: pikachu)");
        loadButton   = new JButton("Load");
        randomButton = new JButton("Random");
        inputPanel.add(nameField);
        inputPanel.add(loadButton);
        inputPanel.add(randomButton);
        add(inputPanel);

        // — Sprite ————————————————————————————————————————————————————————
        spriteLabel = new JLabel("Sin imagen", SwingConstants.CENTER);
        spriteLabel.setPreferredSize(new Dimension(130, 130));
        spriteLabel.setMaximumSize(new Dimension(130, 130));
        spriteLabel.setAlignmentX(CENTER_ALIGNMENT);
        spriteLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(spriteLabel);

        add(Box.createVerticalStrut(4));

        // — Nombre ————————————————————————————————————————————————————————
        nameLabel = new JLabel("---", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(nameLabel);

        // — Tipos —————————————————————————————————————————————————————————
        typesLabel = new JLabel("Tipos: ---", SwingConstants.CENTER);
        typesLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        typesLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(typesLabel);

        add(Box.createVerticalStrut(6));

        // — Barra de HP ————————————————————————————————————————————————————
        JPanel hpPanel = new JPanel(new BorderLayout(4, 0));
        hpPanel.setMaximumSize(new Dimension(230, 26));
        hpPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JLabel hpTitle = new JLabel("HP ");
        hpBar = new JProgressBar(0, 100);
        hpBar.setStringPainted(false);
        hpBar.setForeground(new Color(60, 200, 60));
        hpBar.setBackground(Color.LIGHT_GRAY);

        hpLabel = new JLabel(" 0/0");
        hpLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));

        hpPanel.add(hpTitle,  BorderLayout.WEST);
        hpPanel.add(hpBar,    BorderLayout.CENTER);
        hpPanel.add(hpLabel,  BorderLayout.EAST);
        add(hpPanel);

        add(Box.createVerticalStrut(6));

        // — Stats ——————————————————————————————————————————————————————————
        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 2, 2));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Stats"));
        statsPanel.setMaximumSize(new Dimension(230, 80));

        atkLabel   = new JLabel("  ATK: ---");
        defLabel   = new JLabel("  DEF: ---");
        speedLabel = new JLabel("  SPD: ---");

        statsPanel.add(atkLabel);
        statsPanel.add(defLabel);
        statsPanel.add(speedLabel);
        add(statsPanel);
    }

    // ── Listeners de botones ──────────────────────────────────────────────────

    private void wireListeners() {
        loadButton.addActionListener(e -> {
            String nombre = nameField.getText().trim();
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Escribe el nombre del Pokémon antes de cargar.",
                        "Campo vacío", JOptionPane.WARNING_MESSAGE);
                return;
            }
            cargarPokemon(nombre);
        });

        randomButton.addActionListener(e -> cargarPokemonRandom());
    }

    // ── Carga de datos con SwingWorker (no bloquea la UI) ────────────────────

    /** Carga un Pokémon por nombre usando un hilo de fondo. */
    private void cargarPokemon(String nombre) {
        setEstadoCargando(true);

        SwingWorker<Pokemon, Void> worker = new SwingWorker<>() {
            @Override
            protected Pokemon doInBackground() throws Exception {
                return parent.getApiClient().consultarPokemon(nombre);
            }

            @Override
            protected void done() {
                try {
                    mostrarPokemon(get());
                } catch (Exception ex) {
                    String msg = ex.getCause() != null
                            ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(PokemonPanel.this,
                            "Error: " + msg,
                            "Pokémon no encontrado", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setEstadoCargando(false);
                }
            }
        };
        worker.execute();
    }

    /** Carga un Pokémon aleatorio usando un hilo de fondo. */
    private void cargarPokemonRandom() {
        setEstadoCargando(true);

        SwingWorker<Pokemon, Void> worker = new SwingWorker<>() {
            @Override
            protected Pokemon doInBackground() throws Exception {
                return parent.getApiClient().consultarPokemonRandom();
            }

            @Override
            protected void done() {
                try {
                    mostrarPokemon(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PokemonPanel.this,
                            "Error de red al obtener Pokémon aleatorio.",
                            "Error de red", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setEstadoCargando(false);
                }
            }
        };
        worker.execute();
    }

    // ── Actualización de la UI ────────────────────────────────────────────────

    /** Muestra los datos de un Pokémon en el panel. */
    public void mostrarPokemon(Pokemon p) {
        this.pokemon      = p;
        this.pokemonLoaded = true;

        // Nombre (capitalizado)
        String nombre = p.getName().substring(0, 1).toUpperCase() + p.getName().substring(1);
        nameLabel.setText(nombre);

        // Tipos
        typesLabel.setText("Tipos: " + String.join(" / ", p.getTypes()));

        // Stats
        atkLabel.setText("  ATK: "   + p.getAttack());
        defLabel.setText("  DEF: "   + p.getDefense());
        speedLabel.setText("  SPD: " + p.getSpeed());

        // HP bar
        hpBar.setMaximum(p.getMaxHp());
        hpBar.setValue(p.getCurrentHp());
        hpLabel.setText(" " + p.getCurrentHp() + "/" + p.getMaxHp());
        refrescarColorBarra();

        // Sprite en hilo de fondo
        if (p.getSpriteUrl() != null && !p.getSpriteUrl().isEmpty()) {
            new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    URL url = new URL(p.getSpriteUrl());
                    return new ImageIcon(url);
                }
                @Override
                protected void done() {
                    try {
                        Image img = get().getImage()
                                         .getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                        spriteLabel.setIcon(new ImageIcon(img));
                        spriteLabel.setText("");
                    } catch (Exception ex) {
                        spriteLabel.setIcon(null);
                        spriteLabel.setText("Sin imagen");
                    }
                }
            }.execute();
        } else {
            spriteLabel.setIcon(null);
            spriteLabel.setText("Sin imagen");
        }

        // Notificar al padre para habilitar "Fight!"
        parent.verificarAmbosCargados();
    }

    /** Actualiza el HP en la barra y la etiqueta. */
    public void actualizarHp(int nuevoHp) {
        if (pokemon == null) return;
        pokemon.setCurrentHp(nuevoHp);
        hpBar.setValue(pokemon.getCurrentHp());
        hpLabel.setText(" " + pokemon.getCurrentHp() + "/" + pokemon.getMaxHp());
        refrescarColorBarra();
    }

    /** Fuerza la actualización visual del HP desde el estado interno del modelo. */
    public void refrescarHpDesdeModelo() {
        if (pokemon == null) return;
        actualizarHp(pokemon.getCurrentHp());
    }

    /** Cambia el color de la barra según el porcentaje de HP restante. */
    private void refrescarColorBarra() {
        if (pokemon == null) return;
        double ratio = (double) pokemon.getCurrentHp() / pokemon.getMaxHp();
        if (ratio > 0.50) {
            hpBar.setForeground(new Color(60, 200, 60));   // verde
        } else if (ratio > 0.25) {
            hpBar.setForeground(new Color(240, 190, 0));   // amarillo
        } else {
            hpBar.setForeground(new Color(210, 40, 40));   // rojo
        }
    }

    /** Deshabilita/habilita los botones durante una carga. */
    private void setEstadoCargando(boolean cargando) {
        loadButton.setEnabled(!cargando);
        randomButton.setEnabled(!cargando);
        if (cargando) nameLabel.setText("Cargando...");
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public Pokemon getPokemon()       { return pokemon; }
    public boolean isPokemonCargado() { return pokemonLoaded; }
}
