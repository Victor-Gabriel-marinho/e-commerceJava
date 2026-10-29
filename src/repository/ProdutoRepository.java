package repository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import exception.PersistenciaException;
import model.Produto;

public class ProdutoRepository {

    private static final String CAMINHO = "src/dados/Produtos.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public List<Produto> carregarTodos() {
        File arquivo = new File(CAMINHO);
        if (!arquivo.exists()) {
            return new ArrayList<>();
        }

        try (FileReader leitor = new FileReader(arquivo)) {
            Produto[] produtos = gson.fromJson(leitor, Produto[].class);
            if (produtos == null) {     // arquivo vazio
                return new ArrayList<>();
            }
            return new ArrayList<>(Arrays.asList(produtos));
        } catch (IOException | JsonParseException e) {
            throw new PersistenciaException("Erro ao ler o arquivo de produtos: " + e.getMessage(), e);
        }
    }

    public void salvarTodos(List<Produto> produtos) {
        File arquivo = new File(CAMINHO);
        arquivo.getParentFile().mkdirs();   // cria src/dados se não existir

        try (FileWriter escritor = new FileWriter(arquivo)) {
            gson.toJson(produtos, escritor);
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao salvar o arquivo de produtos: " + e.getMessage(), e);
        }
    }

}