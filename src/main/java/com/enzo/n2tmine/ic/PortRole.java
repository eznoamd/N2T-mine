package com.enzo.n2tmine.ic;

/**
 * Papel de uma porta interna. Cada porta e OU entrada OU saida, nunca as duas
 * ao mesmo tempo -- e isso que impede a realimentacao (a face de entrada nunca
 * "escuta" o proprio sinal que ela injetou).
 */
public enum PortRole {
    INPUT,   // so injeta o sinal de fora pra dentro
    OUTPUT;  // so le o circuito interno e emite pra fora

    public PortRole next() {
        return this == INPUT ? OUTPUT : INPUT;
    }

    public String displayPt() {
        return this == INPUT ? "ENTRADA" : "SAÍDA";
    }
}
