package com.example.c;

public class CitaMedico {
    private String numeroCita;
    private String nombrePaciente;
    private String horaCita;

    public CitaMedico(String numeroCita, String nombrePaciente, String horaCita) {
        this.numeroCita = numeroCita;
        this.nombrePaciente = nombrePaciente;
        this.horaCita = horaCita;
    }

    public String getNumeroCita() {
        return numeroCita;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public String getHoraCita() {
        return horaCita;
    }
}

