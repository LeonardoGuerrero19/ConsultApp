package com.example.c;

public class Medico {
    private String nombre;
    private String especialidad;
    private String telefono;

    public Medico(String nombre, String especialidad, String telefono) {
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public String getTelefono() {
        return telefono;
    }
}

