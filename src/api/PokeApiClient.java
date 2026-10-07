package api;

import model.Pokemon;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Cliente que consume la PokeAPI REST y convierte la respuesta JSON
 * en objetos {@link Pokemon}.
 *
 * Endpoint usado: https://pokeapi.co/api/v2/pokemon/{name|id}
 */
public class PokeApiClient {

    private static final String BASE_URL       = "https://pokeapi.co/api/v2/pokemon/";
    private static final int    MAX_POKEMON_ID = 898;   // Gen 1-8

    private final HttpClient client;
    private final Random     random;

    public PokeApiClient() {
        client = HttpClient.newHttpClient();
        random = new Random();
    }

    /**
     * Consulta un Pokémon por nombre o ID.
     *
     * @param nombreOId nombre (p. ej. "pikachu") o número como String
     * @return objeto {@link Pokemon} con los datos parseados
     * @throws IOException          si hay error de red o el Pokémon no existe
     * @throws InterruptedException si el hilo es interrumpido
     */
    public Pokemon consultarPokemon(String nombreOId) throws IOException, InterruptedException {
        String endpoint = BASE_URL + nombreOId.toLowerCase().trim();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parsePokemon(response.body());
        } else {
            throw new IOException("Pokémon no encontrado: " + nombreOId
                    + " (HTTP " + response.statusCode() + ")");
        }
    }

    /**
     * Consulta un Pokémon aleatorio (ID entre 1 y 898).
     */
    public Pokemon consultarPokemonRandom() throws IOException, InterruptedException {
        int id = random.nextInt(MAX_POKEMON_ID) + 1;
        return consultarPokemon(String.valueOf(id));
    }

    // ── Parseo JSON ──────────────────────────────────────────────────────────

    private Pokemon parsePokemon(String body) {
        JSONObject json = new JSONObject(body);

        int    id   = json.getInt("id");
        String name = json.getString("name");

        // Tipos
        List<String> types = new ArrayList<>();
        json.getJSONArray("types").forEach(t -> {
            JSONObject entry = (JSONObject) t;
            types.add(entry.getJSONObject("type").getString("name"));
        });

        // Estadísticas
        int hp = 0, attack = 0, defense = 0, speed = 0;
        for (Object obj : json.getJSONArray("stats")) {
            JSONObject statEntry = (JSONObject) obj;
            String statName  = statEntry.getJSONObject("stat").getString("name");
            int    statValue = statEntry.getInt("base_stat");

            switch (statName) {
                case "hp"      -> hp      = statValue;
                case "attack"  -> attack  = statValue;
                case "defense" -> defense = statValue;
                case "speed"   -> speed   = statValue;
            }
        }

        // Sprite
        String spriteUrl = json.getJSONObject("sprites")
                               .optString("front_default", "");

        return new Pokemon(id, name, types, hp, attack, defense, speed, spriteUrl);
    }
}
