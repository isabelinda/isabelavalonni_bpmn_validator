package com.bpmncheck.service;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.model.bpmn.instance.FlowNode;
/*
 * Classe para fazer todas as combinações possíveis dos nós passados na lista de entrada
 */
public class InclusiveComb {
    private int r ;
    private List<FlowNode> entrada ;
    private int MAX ;
    private int N ;
 
    /*
     * Se r é zero entao iremos fazer todas
     * as combinacoes (com qualquer quantidade de elementos)
     */
    public InclusiveComb(List<FlowNode> entrada, int r) {
        this.r = r ;
        this.entrada = entrada ;
        this.MAX = ~(1 << entrada.size()) ;
        this.N = 1;
    }
 
    /*
     * Retorna true quando ha pelo menos uma combinacao disponivel
     */
    public boolean hasNext() {
        if ( r != 0 ) {
            while ( ((this.N & this.MAX) != 0) && (countbits() != r) ) N+=1 ;
        }
 
        return (this.N & this.MAX) != 0;
    }
 
    /*
     * Retorna a quantidade de bits ativos (= 1) de N.
     */
    private int countbits() {
        int i;
        int c;
 
        i = 1;
        c = 0;
        while ( (this.MAX & i) != 0 ) {
            if ( (this.N & i) != 0) {
                c++;
            }
            i = i << 1 ;
        }
 
        return c ;
    }
 
    /*
     * Util para obter o tamanho da saida. Esse
     * tamanho é o numero de posicoes do vetor
     * retornado por next.
     */
    public int getSaidaLength() {
        if (r != 0) {
            return r;
        }
 
        return this.countbits();
    }
 
    /*
     * Retorna uma combinacao.
     *
     * ATENCAO: Sempre use next() quando se
     * tem certeza que ha uma combinacao
     * disponivel. Ou seja, sempre use next()
     * quando hasNext() retornar true.
     */
    public List<FlowNode> next() {
        int saida_index, entrada_index, i;
 
        List<FlowNode> saida = new ArrayList<>();
 
        entrada_index = 0;
        saida_index = 0;
        i = 1;
 
        while ((this.MAX & i) != 0) {
            if ((this.N & i) != 0) {
                saida.add(saida_index, entrada.get(entrada_index));
                saida_index += 1;
            }
            entrada_index += 1;
            i = i << 1;
        }
 
        N += 1;
 
        return saida;
    }
}
