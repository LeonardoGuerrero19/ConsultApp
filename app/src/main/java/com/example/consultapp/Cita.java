package com.example.consultapp;

public class Cita {
    private String citaId;  // Campo para almacenar el ID de la cita
    private String servicio;
    private String fecha;
    private String horario;
    private String doctor;
    private String usuarioId;
    private String estado;

    // Constructor vacío requerido por Firestore
    public Cita() {}

    // Constructor con citaId
    public Cita(String citaId, String servicio, String fecha, String horario, String doctor, String usuarioId, String estado) {
        this.citaId = citaId;
        this.servicio = servicio;
        this.fecha = fecha;
        this.horario = horario;
        this.doctor = doctor;
        this.usuarioId = usuarioId;
        this.estado = estado;
    }

    // Getters y setters
    public String getCitaId() {
        return citaId;
    }

    public void setCitaId(String citaId) {
        this.citaId = citaId;
    }

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

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

