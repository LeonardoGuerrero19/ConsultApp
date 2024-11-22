package com.example.consultapp;

import java.util.List;

public class Medico {
    private String id;
    private String nombre;
    private String especializacion;
    private String telefono;
    private List<String> horarios; // Cambiar a lista de horarios

    // Constructor vacío requerido por Firebase
    public Medico() {}

    // Constructor completo
    public Medico(String nombre, String especializacion, String telefono, List<String> horarios) {
        this.nombre = nombre;
        this.especializacion = especializacion;
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
}
