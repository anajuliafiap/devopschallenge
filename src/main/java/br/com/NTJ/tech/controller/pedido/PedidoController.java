package br.com.NTJ.tech.controller.pedido;


import br.com.NTJ.tech.dto.categoria.DetalhesCategoria;
import br.com.NTJ.tech.dto.historicoPedido.CadastroHistoricoPedido;
import br.com.NTJ.tech.dto.historicoPedido.DetalhesPedidoHistorico;
import br.com.NTJ.tech.dto.pedido.*;
import br.com.NTJ.tech.dto.produto.CadastroProduto;
import br.com.NTJ.tech.dto.produto.DetalhesProdutoPedido;
import br.com.NTJ.tech.model.historicoPedido.HistoricoPedido;
import br.com.NTJ.tech.model.pedido.Pedido;
import br.com.NTJ.tech.model.produto.Produto;
import br.com.NTJ.tech.repository.cliente.ClienteRepository;
import br.com.NTJ.tech.repository.historicoPedido.HistoricoPedidoRepository;
import br.com.NTJ.tech.repository.pedido.PedidoRepository;
import br.com.NTJ.tech.repository.produto.ProdutoRepository;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("pedidos")
@Tag(name = "Pedido", description = "Operações relacionadas ao Claud.IA")
public class PedidoController {

    @Autowired
    private PedidoRepository repository;

    @Autowired
    private HistoricoPedidoRepository historicoPedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping
    public ResponseEntity<List<DetalhesPedido>> listar(Pageable pageable){
        var lista = repository.findAll(pageable)
                .stream().map(DetalhesPedido::new).toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("{id}")
    @Parameters({
            @Parameter(name="id", description = "Pesquisa pedido por id", required = true)
    })
    public ResponseEntity<DetalhesPedido> buscar(@PathVariable("id") Long id){
        var pedido = repository.getReferenceById(id);
        return ResponseEntity.ok(new DetalhesPedido(pedido));
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastro de pedido", description = "cadastra um pedido")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Cadastro com Sucesso", content =
    @Content(schema = @Schema(implementation = DetalhesCategoria.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Não Autorizado ou Token Inválido", content =
                    { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")})
    public ResponseEntity<DetalhesPedido> cadastrar(@RequestBody CadastroPedido pedidoPost,
                                                     UriComponentsBuilder uri){
        var pedido = new Pedido(pedidoPost);
        repository.save(pedido);
        var url = uri.path("/pedidos/{id}").buildAndExpand(pedido.getCodigo()).toUri();
        return ResponseEntity.created(url).body(new DetalhesPedido(pedido));
    }

    //Post da tabela historico pedido
    @PostMapping("{id}/historicoPedido")
    @Transactional
    public  ResponseEntity<DetalhesPedidoHistorico> postPedidoHistorico   (@PathVariable("id")Long id,
                                                                          @RequestBody @Valid CadastroHistoricoPedido dto,
                                                                          UriComponentsBuilder uriBuilder){
        var pedido = repository .getReferenceById(id);
        var historicoPedido = new HistoricoPedido(dto, pedido);
        historicoPedidoRepository.save(historicoPedido);
        var uri = uriBuilder.path("historicoPedido/{id}").buildAndExpand(historicoPedido.getCodigo()).toUri();
        return ResponseEntity.created(uri).body(new DetalhesPedidoHistorico(historicoPedido));
    }

    //Post da tabela produto
    @PostMapping("{id}/produtos")
    @Transactional
    public  ResponseEntity<DetalhesProdutoPedido> postProdutoPedido(@PathVariable("id")Long id,
                                                                    @RequestBody @Valid CadastroProduto dto,
                                                                    UriComponentsBuilder uriBuilder){
        var pedido = repository.getReferenceById(id);
        var produto = new Produto(dto, pedido);
        repository.save(pedido);
        var uri = uriBuilder.path("produtos/{id}").buildAndExpand(produto.getCodigo()).toUri();
        return ResponseEntity.created(uri).body(new DetalhesProdutoPedido(produto));
    }

    @PutMapping("{id}")
    @Transactional
    public ResponseEntity<DetalhesPedido> atualizar(@PathVariable("id") Long id,
                                                     @RequestBody CadastroPedido pedidoPut){
        var pedido = repository.getReferenceById(id);
        pedido.atualizarDados(pedidoPut);
        return ResponseEntity.ok(new DetalhesPedido(pedido));
    }

    @DeleteMapping("{id}")
    @Transactional
    @Hidden
    public ResponseEntity<Void> deletar(@PathVariable("id") Long id){
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
