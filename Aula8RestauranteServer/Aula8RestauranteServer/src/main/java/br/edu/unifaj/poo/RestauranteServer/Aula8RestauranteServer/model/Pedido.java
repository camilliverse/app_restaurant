package br.edu.unifaj.poo.RestauranteServer.Aula8RestauranteServer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pedido {
    private String nomeProduto;
    private Integer quantidade;
    private Double valor;
}
