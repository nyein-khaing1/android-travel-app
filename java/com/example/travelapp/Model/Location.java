package com.example.travelapp.Model;

public class Location {
    private int Id;
    private String Name;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Location(){

    }

    @Override
    public String toString() {
        return Name;
    }
}
