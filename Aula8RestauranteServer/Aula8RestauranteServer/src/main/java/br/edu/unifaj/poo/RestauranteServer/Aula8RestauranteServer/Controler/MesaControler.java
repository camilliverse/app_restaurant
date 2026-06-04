package br.edu.unifaj.poo.RestauranteServer.Aula8RestauranteServer.Controler;

import br.edu.unifaj.poo.RestauranteServer.Aula8RestauranteServer.Service.MesaServico;
import br.edu.unifaj.poo.RestauranteServer.Aula8RestauranteServer.model.Mesa;
import br.edu.unifaj.poo.RestauranteServer.Aula8RestauranteServer.model.Pedido;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurant/mesa")
public class MesaControler {

    private final MesaServico mesaServico;
    public MesaControler(MesaServico mesaServico){this.mesaServico = mesaServico;}

    @GetMapping("/{id}")
    public ResponseEntity<Mesa> BuscarMesa(@PathVariable Integer id){
        Mesa mesa = mesaServico.BuscarMesa(id);
        if (mesa == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(mesa);
    }

    @GetMapping("")
    public ResponseEntity<List<Mesa>> BuscarTodas() {
        return ResponseEntity.ok(mesaServico.BuscarTodas());
    }

    @PostMapping("")
    public ResponseEntity<Mesa> AdicionarMesa(@RequestBody Mesa mesa){
        Mesa novaMesa = mesaServico.AdicionarMesa(mesa);

        if(novaMesa == null){
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(novaMesa);
    }

    @PostMapping("/{id}/pedido")
    public ResponseEntity<?> AdicionarPedido(@PathVariable Integer id, @RequestBody Pedido pedido){

        Mesa mesaAtualizada = mesaServico.AdicionarPedido(id, pedido);

        if (mesaAtualizada == null) {
            return ResponseEntity.badRequest().body("Mesa não encontrada ou erro ao adicionar pedido");
        }

        return ResponseEntity.ok(mesaAtualizada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mesa> AtualizarMesa(@PathVariable Integer id, @RequestBody Mesa mesa){
        Mesa atualizarMesa = mesaServico.AtualizarMesa(id, mesa);

        if(atualizarMesa == null){
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(atualizarMesa);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Mesa> LimparMesa(@PathVariable Integer id){
        Mesa mesa = mesaServico.BuscarMesa(id);
        if (mesa == null){
            return ResponseEntity.notFound().build();
        }
        Mesa mesaLimpa = mesaServico.LimparMesa(id);
        return ResponseEntity.ok().build();
    }
}

