package com.example.consultapp;

import java.util.List;

public class Medico {
    private String id;
    private String nombre;
    private String especializacion;
    private String cedula;
    private String telefono;
    private List<String> horarios; // Cambiar a lista de horarios
    private boolean emailVerified; // Nuevo campo


    // Constructor vacío requerido por Firebase
    public Medico() {}

    // Constructor completo
    public Medico(String nombre, String especializacion, String cedula, String telefono, List<String> horarios) {
        this.nombre = nombre;
        this.especializacion = especializacion;
        this.cedula = cedula;
        this.telefono = telefono;
        this.horarios = horarios;
    }

    // Getter y Setter para id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEspecializacion() {
        return especializacion;
    }

    public String getCedula() {
        return cedula;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<String> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<String> horarios) {
        this.horarios = horarios;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}