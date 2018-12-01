import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by ssjjcao on 2018/11/30.
 */
public class CRFTest {
    public static void main(String[] args) throws FileNotFoundException {
        ArrayList<String>[] dataSet = readData("data/train.utf8");
        CRF crf = new CRF("data/template.utf8");

        ArrayList<String> sentences = dataSet[0];
        ArrayList<String> results = dataSet[1];

        //train
        for (int iter = 1; iter <= 10; iter++) {
            for (int i = 0; i < 19000; i++) {
                String sentence = sentences.get(i);
                String result = results.get(i);
                crf.train(sentence, result);
            }
            System.out.println("iter " + iter + " ok");
        }

        //test
        int total = 0;
        float correct = 0;
        for (int i = 19000; i < 23444; i++) {
            String sentence = sentences.get(i);
            String result = results.get(i);
            total += sentence.length();
            String myRes = crf.segment(sentence);
            correct += getSameNum(result, myRes);
        }
        float accuracy = correct / total;
        System.out.println("accuracy in test set is: " + accuracy);
    }

    private static int getSameNum(String s1, String s2) {
        int count = 0;
        int num = s1.length();
        for (int i = 0; i < num; i++) {
            if (s1.substring(i, i + 1).equals(s2.substring(i, i + 1))) {
                count++;
            }
        }
        return count;
    }

    private static ArrayList<String>[] readData(String fileName) throws FileNotFoundException {
        ArrayList<String> sentences = new ArrayList<>();
        ArrayList<String> results = new ArrayList<>();

        Scanner scanner = new Scanner(new File(fileName));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] data = line.split(" ");
            if (data.length == 2) {
                StringBuilder senBuilder = new StringBuilder();
                StringBuilder resBuilder = new StringBuilder();
                while (data.length == 2) {
                    senBuilder.append(data[0]);
                    resBuilder.append(data[1]);
                    if (scanner.hasNextLine()) {
                        data = scanner.nextLine().split(" ");
                    } else {
                        break;
                    }
                }
                sentences.add(senBuilder.toString());
                results.add(resBuilder.toString());
            }
        }
        scanner.close();

        ArrayList<String>[] dataArray = new ArrayList[2];
        dataArray[0] = sentences;
        dataArray[1] = results;
        return dataArray;
    }
}
