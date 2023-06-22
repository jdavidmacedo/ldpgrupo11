/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proj;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joaod
 */
public class Tabuleiro {
    private List<Peca> pecasJogadas;
    
    public Tabuleiro() {
        pecasJogadas = new ArrayList<>();
    }
    
    public boolean podeJogarPeca(Peca peca, int lado) {
        if (pecasJogadas.isEmpty()) {
            return true; // Primeira jogada, qualquer peça pode ser jogada
        } else {
            Peca pecaEsq = pecasJogadas.get(0);
            Peca pecaDir = pecasJogadas.get(pecasJogadas.size() - 1);
            
            if (lado == 0) {
                return peca.getNumDir() == pecaEsq.getNumEsq();
            } else {
                return peca.getNumEsq() == pecaDir.getNumDir();
            }
        }
    }
    
    public void jogarPeca(Peca peca, int lado) {
        if (lado == 0) {
            pecasJogadas.add(0, peca);
        } else {
            pecasJogadas.add(peca);
        }
    }
    
    // Outros métodos relacionados ao tabuleiro...
}
