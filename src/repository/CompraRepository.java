package repository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import exception.PersistenciaException;
import model.Compra;

public class CompraRepository {

    private static final String CAMINHO = "src/dados/Compras.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public List<Compra> carregarTodas() {
        File arquivo = new File(CAMINHO);
        if (!arquivo.exists()) {
            return new ArrayList<>();
        }

        try (FileReader leitor = new FileReader(arquivo)) {
            Compra[] compras = gson.fromJson(leitor, Compra[].class);
            if (compras == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(Arrays.asList(compras));
        } catch (IOException | JsonParseException e) {
            throw new PersistenciaException("Erro ao ler o arquivo de compras: " + e.getMessage(), e);
        }
    }

    /** Grava em arquivo temporário e troca de forma atômica: o arquivo nunca fica pela metade. */
    public void salvarTodas(List<Compra> compras) {
        Path destino = Paths.get(CAMINHO);
        Path temporario = Paths.get(CAMINHO + ".tmp");

        try {
            Files.createDirectories(destino.getParent());
            try (FileWriter escritor = new FileWriter(temporario.toFile())) {
                gson.toJson(compras, escritor);
            }
            Files.move(temporario, destino,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao salvar o arquivo de compras: " + e.getMessage(), e);
        }
    }
}