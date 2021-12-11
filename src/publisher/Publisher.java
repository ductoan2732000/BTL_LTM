package publisher;

import util.ConfigCommon;

import java.io.IOException;
import java.util.Scanner;

public class Publisher {
    public Publisher(String id, String topic){

    }

    /*
    public static void main(String[] args) throws IOException {

        Scanner keyboard = new Scanner(System.in);
        System.out.println("Enter number publishers: ");
        int numbers = keyboard.nextInt();
        String topic = keyboard.nextLine();
        Publisher publishers[] = new Publisher[numbers];
        for (int i = 0; i < numbers; i++){
            System.out.println("Enter a topic: ");
            topic = keyboard.nextLine();
            while(true) {
                System.out.println("input is '" + topic + "'");
                if (!topic.isEmpty()) {

                    publishers[i] = new Publisher(Integer.toString(i), topic);
                    topic = "";
                    break;
                }
            }
        }
    }
    */
}
