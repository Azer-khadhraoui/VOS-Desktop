package vos.gestionCandidat.services;

import java.util.List;

public interface IService <T>{
    void ajouter(T t);
    void modifier(T t);
    void supprimer(int id);
    T getById(int id);
    List<T> getAll();

}
