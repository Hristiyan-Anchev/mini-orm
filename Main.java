import orm.EntityManager;
import entities.User;
import interfaces.DbContext;
import orm.Connector;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    private static final String DB_NAME = "soft_uni";

    public static void main(String[] args) throws IOException, SQLException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        List<String> dbConfig = getDbConfig();
        Connector.createConnection(dbConfig.get(0),dbConfig.get(1),DB_NAME);
        DbContext<User> entityManager =
                new EntityManager<User>(Connector.getConnection());
        populateDatabase(entityManager);

        List<User> users = entityManager.find(User.class);
        List<User> usersWhere = entityManager.find(User.class,"age > 26");
        User findFirstUser = entityManager.findFirst(User.class);
        User findFirstWhere = entityManager.findFirst(User.class, " age > 26");

        System.out.println();
    }

    public static void populateDatabase(DbContext<User> entityManager) throws SQLException, IllegalAccessException {
        Random r = new Random();
        int i = -1;

        while(i++ < 10){
            entityManager.persist(
                    new User(null,genName(r),r.nextInt(100),
                            new Date(ThreadLocalRandom.current().nextInt() * 1000L) )
            );
        }
    }
    public static String genName(Random r){
        String seedName ="";
        int k = -1;
        while(k++ < 8){
            //todo generate random ASCII char ... letters only;
            int randomNumber = r.nextInt(122);

            while(!((randomNumber >= 65 && randomNumber <= 90)  || (randomNumber >= 97 && randomNumber <= 122))){
                randomNumber = r.nextInt(122);
            }
            seedName += Character.toString(randomNumber);
        }
        return seedName;
    }
    public static List<String> getDbConfig() throws FileNotFoundException {
        List<String> result = new ArrayList<>();

        String filePath = "src/main/java/db-conf.env";
        File file = new File(filePath);
        FileReader fr = new FileReader(file);

        try (BufferedReader br = new BufferedReader(fr)) {
            var line = br.readLine();

            while (line != null) {
                result.add(line);
                line = br.readLine();
            }

        } catch (IOException e) {
            System.out.println("oopsie something went wrong :: ");
            e.printStackTrace();
        }

        return result;
    }

}
