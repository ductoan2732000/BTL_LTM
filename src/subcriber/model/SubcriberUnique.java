/**
 * @author: tdtoan 28.11.2021
 * model chứa thông tin 1 client
 */
package subcriber.model;
import java.util.Random;
public class SubcriberUnique {
    protected Integer id;
    protected String name;
    public static Integer lastId;
    public SubcriberUnique(Integer id, String name){
        this.id = id;
        this.name = name;
    }
    public SubcriberUnique(){
        this.id = this.lastId + 1;
        this.name = this.RandomString();
    }
    public Integer getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public String RandomString(){
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        // create random string builder
        StringBuilder sb = new StringBuilder();

        // create an object of Random class
        Random random = new Random();

        // specify length of random string
        int length = 7;

        for(int i = 0; i < length; i++) {

            // generate random index number
            int index = random.nextInt(alphabet.length());

            // get character specified by index
            // from the string
            char randomChar = alphabet.charAt(index);

            // append the character to string builder
            sb.append(randomChar);
        }

        String randomString = sb.toString();
        return randomString;
    }
}
