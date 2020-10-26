package interfaces;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

public interface DbContext<T> {
    boolean persist(T entity) throws IllegalAccessException, SQLException;
    List<T> find(Class<T> table) throws SQLException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException;
    List<T> find(Class<T> table, String where) throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException;
    T findFirst(Class<T> table) throws InvocationTargetException, SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException;
    T findFirst(Class<T> table,String where) throws InvocationTargetException, SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException;

}
