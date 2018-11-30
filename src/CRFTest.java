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
