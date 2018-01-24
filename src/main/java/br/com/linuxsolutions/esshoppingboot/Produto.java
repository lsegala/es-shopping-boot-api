package br.com.linuxsolutions.esshoppingboot;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;

/**
 * Criado por leonardo.segala em 24/10/2017.
 */
@Document(indexName = "shopping", type = "produtos", shards = 1, replicas = 0)
@Setting(settingPath = "/settings.json")
@Mapping(mappingPath = "/mappings.json")
public class Produto {
    public String id;
    public List<String> breadcrumps;
    public String titulo;
    public Double preco;
    public String thumbnail;
    public List<String> imagens;
    public String caracteristicas;
    public String descricao;

    Produto() {
    }
}
