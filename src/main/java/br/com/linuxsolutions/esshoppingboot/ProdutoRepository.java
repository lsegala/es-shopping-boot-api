package br.com.linuxsolutions.esshoppingboot;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Criado por leonardo.segala em 24/10/2017.
 */
public interface ProdutoRepository extends ElasticsearchRepository<Produto, String> {

}