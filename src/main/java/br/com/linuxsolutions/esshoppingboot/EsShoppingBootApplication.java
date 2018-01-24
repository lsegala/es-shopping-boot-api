package br.com.linuxsolutions.esshoppingboot;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@SpringBootApplication
public class EsShoppingBootApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(EsShoppingBootApplication.class, "--debug");
	}

	@Autowired
	private ProdutoRepository produtoRepository;

	@Bean
	public Client client() throws UnknownHostException {
		return new PreBuiltTransportClient(Settings.builder().put("cluster.name", "es-local").build())
				.addTransportAddress(
						new InetSocketTransportAddress(
								InetAddress.getByName("localhost"),
								9300));
	}

	@Bean
	public ElasticsearchOperations elasticsearchTemplate() throws Exception {
		return new ElasticsearchTemplate(client());
	}

	private void entrarLink(String titulo, String href, Elements imgThumb) {
		if(StringUtils.isEmpty(href)){
			return;
		}
		try {
			Document document = Jsoup.connect(href).get();
			Element preco = document.select("fieldset.item-price span.price-tag-fraction").last();
			if(preco == null){
				return;
			}
			Elements breadcrumps = document.select("div.vip-navigation-breadcrumb ul li a");
			Produto produto = new Produto();
			produto.breadcrumps = breadcrumps.stream().map(e -> e.text()).collect(Collectors.toList());
			produto.titulo = titulo;
			produto.preco = NumberUtils.parseNumber(preco.text(), Double.class);
			produto.thumbnail =
					Optional.ofNullable(imgThumb.attr("src"))
							.filter(v -> !StringUtils.isEmpty(v))
							.orElse(imgThumb.attr("data-src"));
			Elements figures = document.select("div.product-gallery figure");
			produto.imagens = new ArrayList<>();
			figures.forEach(figure -> {
				produto.imagens.add(figure.select("a").attr("href"));
			});
			Elements caracteristicas = document.select("section.specs-primary-specs");
			produto.caracteristicas = caracteristicas.html();
			Elements descricao = document.select("section div.item-description__content div.item-description__html-text");
			produto.descricao = descricao.html();

			produtoRepository.save(produto);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void carregarItens(String desc) throws IOException {
		Jsoup.connect("https://lista.mercadolivre.com.br/"+desc).get()
				.select("#searchResults div.rowItem")
				.forEach(result -> {
					Elements img = Jsoup.parse(result.html()).select("div.images-viewer a img");
					Elements link = Jsoup.parse(result.html()).select("div.images-viewer a");
					entrarLink(img.attr("alt"), link.attr("href"), img);
				});
	}

	@Override
	public void run(String... args) throws Exception {
		this.produtoRepository.deleteAll();
		carregarItens("computador");
		carregarItens("notebook");
		carregarItens("celular");
		carregarItens("caixa-de-som-bluetooth");
		carregarItens("fone-ouvido");
	}
}
