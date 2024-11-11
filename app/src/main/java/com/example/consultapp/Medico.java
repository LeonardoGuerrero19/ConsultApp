package com.example.consultapp;

public class Medico {
    private String id;
    private String nombre;
    private String especializacion;
    private String telefono;
    private String horario;

    // Constructor vacío para Firestore
    public Medico() {}

    public Medico(String nombre, String especializacion, String telefono, String horario) {
        this.nombre = nombre;
        this.especializacion = especializacion;
        this.telefono = telefono;
        this.horario = horario;
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

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }
}
