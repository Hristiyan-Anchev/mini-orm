package orm;

import annotations.Column;
import annotations.Entity;
import annotations.Id;
import interfaces.DbContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.stream.Collectors;

public class EntityManager<T> implements DbContext<T> {
    Connection connection;
    private static final String INSERT_STATEMENT =
            "INSERT INTO `%s` (%s) VALUES (%s)";
    private static final String UPDATE_STATEMENT =
            "UPDATE `%s` SET %s WHERE id = %s";

    private static final String FIND_ALL_STATEMENT =
            "SELECT * FROM %s ";
    private static final String FIND_ALL_WHERE =
            FIND_ALL_STATEMENT + " WHERE %s ";
    private static final String FIND_FIRST_STATEMENT =
            FIND_ALL_STATEMENT + "LIMIT 1";
    private static final String FIND_FIRST_STATEMENT_WHERE =
            FIND_ALL_WHERE + "LIMIT 1";

    public EntityManager(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean persist(T entity) throws IllegalAccessException, SQLException {
        Field primary = this.getId(entity.getClass());
        primary.setAccessible(true);
        Object value = primary.get(entity);

        if (value == null || (int) value <= 0) {
            return doInsert(entity, primary);
        }
        return doUpdate(entity, primary);

    }

    private boolean doUpdate(T entity, Field primary) throws IllegalAccessException, SQLException {
        Field[] fields = getEntityFields(entity, true);
        fields[0].setAccessible(true);
        String id = null;
        if (fields[0].isAnnotationPresent(Id.class)) {
            id = String.valueOf(fields[0].get(entity));
        }


        String setExpressions = Arrays.stream(fields).map((f) -> {
            f.setAccessible(true);
            String colName = null;
            String colValue = null;
            if (f.isAnnotationPresent(Column.class)) {
                colName = f.getAnnotation(Column.class).name();


                try {
                    colValue = String.valueOf(f.get(entity));

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return String.format("`%s` = '%s'", colName, colValue);
        }).skip(1).collect(Collectors.joining(", ")) + " ";

        String updateQuery = String.format(UPDATE_STATEMENT,
                getTableName(entity),
                setExpressions,
                id
        );

        return connection
                .prepareStatement(updateQuery).executeUpdate() > 0;
    }

    private boolean doInsert(T entity, Field primary) throws SQLException {
        Field[] fields = getEntityFields(entity, false);
//

        String query = String.format(INSERT_STATEMENT,
                getTableName(entity),
                getColumnNames(entity, fields),
                getFieldsValues(fields, entity)
        );

        return this.connection
                .prepareStatement(query)
                .execute();

    }

    @Override
    public List<T> find(Class<T> table) throws SQLException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        return getObjectCollectionByQuery(FIND_ALL_STATEMENT,null,table);
    }

    @Override
    public List<T> find(Class<T> table, String where) throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return getObjectCollectionByQuery(FIND_ALL_WHERE,where,table);
    }

    @Override
    public T findFirst(Class<T> table) throws InvocationTargetException, SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        return getObjectCollectionByQuery(FIND_FIRST_STATEMENT,null,table).get(0);
    }

    @Override
    public T findFirst(Class<T> table, String where) throws InvocationTargetException, SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException {

        return getObjectCollectionByQuery(FIND_FIRST_STATEMENT_WHERE,where,table).get(0);
    }


    //******************************************************************

    private List<T> getObjectCollectionByQuery(String queryString,String where,Class<T> table) throws SQLException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        ResultSet res = where == null ? getResultSetForQuery(queryString,table)
                                : getResultSetForQuery(queryString,where,table);

        // fields classes needed to pass to the constructor
        Class<?>[] fieldsClasses = getFieldsClasses( table);

        Constructor<T> dataObjectConstructor = table.getConstructor(fieldsClasses);

        //field types needed so we can get extract the right type of data from the result set
        String[] fieldTypes = getFieldTypes(table);

        return getResultSetObjectCollection(res,fieldTypes,dataObjectConstructor);
    }

    private Field[] getEntityFields(T entity, boolean withId) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> {

                    return withId == false ? !f.isAnnotationPresent(Id.class) : true;
                }).toArray(Field[]::new);
    }

    private String getFieldsValues(Field[] fields, T entity) {
        return Arrays.stream(fields).map(f -> {
            f.setAccessible(true);
            try {
                return "'" + f.get(entity).toString() + "'";
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return "";
            }
        }).collect(Collectors.joining(","));

    }

    private Field getId(Class entity) {
        return Arrays.stream(entity.getDeclaredFields())
                .filter(f -> {
                    return f.isAnnotationPresent(Id.class);
                }).findFirst().orElseThrow(() ->
                        new UnsupportedOperationException("Entity has no primary key")
                );
    }

    private String getTableName(T entity) {
        return entity.getClass().getAnnotation(Entity.class).name();
    }

    private String getColumnNames(T entity, Field[] fields) {
        return Arrays.stream(fields).map(f -> {
            return f.getDeclaredAnnotation(Column.class).name();
        }).collect(Collectors.joining(","));
    }

    private ResultSet getResultSetForQuery(String statement, Class<T> table) throws SQLException {
        String tableName = table.getAnnotation(Entity.class).name();
        String findAllQuery = String.format(statement, tableName);

        return connection.prepareStatement(findAllQuery).executeQuery();
    }

    private ResultSet getResultSetForQuery(String statement,String where ,Class<T> table) throws SQLException {
        String tableName = table.getAnnotation(Entity.class).name();
        String findAllQuery = String.format(statement, tableName,where);

        return connection.prepareStatement(findAllQuery).executeQuery();
    }

    private List<T> getResultSetObjectCollection(ResultSet res, String[] fieldTypes,Constructor<T> dataObjectConstructor) throws SQLException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<T> objectsResultSet = new ArrayList<>();
        while (res.next()) {
            Object[] tableRowObject =getTableRowValues(res,fieldTypes);
            T dataObject = dataObjectConstructor.newInstance(tableRowObject);
            objectsResultSet.add(dataObject);
        }
        return objectsResultSet;
    }

    private String[] getFieldTypes(Class<T> table) {
        return Arrays.stream(table.getDeclaredFields())
                .filter(f -> {
                    return f.isAnnotationPresent(Column.class) || f.isAnnotationPresent(Id.class);
                }).map(f ->{

                    String typeName = f.getGenericType().getTypeName();
                    String typeSimpleName = typeName.substring(typeName.lastIndexOf('.') + 1);
                    return typeSimpleName;
                })
                .toArray(String[]::new);
    }

    private Class<?>[] getFieldsClasses(Class<T> table) {
        return (Class<?>[]) Arrays.stream(table.getDeclaredFields()).filter(f -> {
            return f.isAnnotationPresent(Column.class) || f.isAnnotationPresent(Id.class);
        }).map(f -> {
            return f.getType();

        }).toArray(Class[]::new);
    }

    private Object[] getTableRowValues(ResultSet res, String[] tableTypes) throws SQLException {
        List<Object> arguments = new ArrayList<>();

        for (String currentType : tableTypes) {

            switch (currentType) {
                case "String" -> {
                    arguments.add(res.getString(arguments.size() + 1));
                }
                case "Integer" -> {
                    arguments.add(res.getInt(arguments.size() + 1));
                }
                case "Date" -> {
                    arguments.add(res.getDate(arguments.size() + 1));
                }
                case "Double" -> {
                    arguments.add(res.getDouble(arguments.size() + 1));
                }
            }
        }

        return arguments.toArray();
    }

}

