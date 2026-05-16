package at.fhtw.seb.persistence;

import java.util.List;

public interface Repository<T> {
    T save (T entry);
    T findById(Integer id);
    List<T> findAll();
    T delete(Integer id);
}