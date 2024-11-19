package com.example.consultapp;

public class Cita {
    private String servicio;
    private String fecha;
    private String horario;

    // Constructor vacío requerido por Firestore
    public Cita() {}

    public Cita(String servicio, String fecha, String horario) {
        this.servicio = servicio;
        this.fecha = fecha;
        this.horario = horario;
    }

    // Getters y setters
    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }
}
