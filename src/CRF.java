import javafx.scene.Parent;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by ssjjcao on 2018/11/30.
 */
public class CRF {
    private HashMap<String, Integer> scoreMap;
    private ArrayList<int[]>[] template;

    public CRF(String templateFileName) throws FileNotFoundException {
        this.scoreMap = new HashMap<>();
        this.template = readTemplate(templateFileName);
    }

    private ArrayList<int[]>[] readTemplate(String fileName) throws FileNotFoundException {
        ArrayList<int[]> unigram = new ArrayList<>();
        ArrayList<int[]> bigram = new ArrayList<>();

        Scanner scanner = new Scanner(new File(fileName));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            ArrayList<Integer> arrayList = new ArrayList<>();
            int index = 0;
            while (index != -1) {
                index = line.indexOf("[", index + 1);
                if (index != -1) {
                    int indexTo = line.indexOf(",", index);
                    arrayList.add(Integer.parseInt(line.substring(index + 1, indexTo)));
                }
            }
            if (arrayList.size() > 0) {
                int num = arrayList.size();
                int[] gram = new int[num];
                for (int i = 0; i < num; i++) {
                    gram[i] = arrayList.get(i);
                }

                String flag = line.substring(0, 1);
                if (flag.equals("U")) {
                    unigram.add(gram);
                } else if (flag.equals("B")) {
                    bigram.add(gram);
                }
            }
        }

        ArrayList<int[]>[] grams = new ArrayList[2];
        grams[0] = unigram;
        grams[1] = bigram;
        return grams;
    }

    public String segment(String sentence) {
        int len = sentence.length();
        String[][] statusFrom = new String[4][len];
        int[][] maxScore = new int[4][len];

        for (int col = 0; col < len; col++) {
            for (int row = 0; row < 4; row++) {
                String thisStatus = getStatus(row);
                if (col == 0) {
                    int uniScore = getUniScore(sentence, 0, thisStatus);
                    int biScore = getBiScore(sentence, 0, " ", thisStatus);
                    maxScore[row][0] = uniScore + biScore;
                    statusFrom[row][0] = null;
                } else {
                    int scores[] = new int[4];
                    for (int i = 0; i < 4; i++) {
                        String preStatus = getStatus(i);
                        int transScore = maxScore[i][col - 1];
                        int uniScore = getUniScore(sentence, col, thisStatus);
                        int biScore = getBiScore(sentence, col, preStatus, thisStatus);
                        scores[i] = transScore + uniScore + biScore;
                    }
                    int maxIndex = getMaxIndex(scores);
                    maxScore[row][col] = scores[maxIndex];
                    statusFrom[row][col] = getStatus(maxIndex);
                }
            }
        }

        String[] resBuf = new String[len];
        int[] scoreBuf = new int[4];
        for (int i = 0; i < 4; i++) {
            scoreBuf[i] = maxScore[i][len - 1];
        }
        resBuf[len - 1] = getStatus(getMaxIndex(scoreBuf));

        for (int backIndex = len - 2; backIndex >= 0; backIndex--) {
            resBuf[backIndex] = statusFrom[statusToRow(resBuf[backIndex + 1])][backIndex + 1];
        }

        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < len; i++) {
            temp.append(resBuf[i]);
        }
        return temp.toString();
    }

    private int getUniScore(String sentence, int thisPos, String thisStatus) {
        int uniScore = 0;
        ArrayList<int[]> uniTemplate = getUniTemplate();
        for (int[] template : uniTemplate) {
            String key = makeKey(template, sentence, thisPos, thisStatus);
            if (scoreMap.get(key) != null) {
                uniScore += scoreMap.get(key);
            }
        }
        return uniScore;
    }

    private int getBiScore(String sentence, int thisPos, String preStatus, String thisStatus) {
        int biScore = 0;
        ArrayList<int[]> biTemplate = getBiTemplate();
        for (int[] template : biTemplate) {
            String key = makeKey(template, sentence, thisPos, preStatus + thisStatus);
            if (scoreMap.get(key) != null) {
                biScore += scoreMap.get(key);
            }
        }
        return biScore;
    }

    private String makeKey(int[] template, String sentence, int pos, String statusCovered) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int offset : template) {
            int index = pos + offset;
            if (index < 0) {
                stringBuilder.append(" ");
            } else {
                String thisCharacter = sentence.substring(index, index + 1);
                stringBuilder.append(thisCharacter);
            }
        }
        stringBuilder.append("/").append(statusCovered);
        return stringBuilder.toString();
    }

    public void train(String sentence, String theoryRes) {
        String myRes = segment(sentence);
        int len = sentence.length();
        for (int i = 0; i < len; i++) {
            String myResI = myRes.substring(i, i + 1);
            String theoryResI = theoryRes.substring(i, i + 1);
            if (!myResI.equals(theoryResI)) {
                for (int[] uniTem : getUniTemplate()) {
                    String uniMyKey = makeKey(uniTem, sentence, i, myResI);
                    if (scoreMap.get(uniMyKey) == null) {
                        scoreMap.put(uniMyKey, -1);
                    } else {
                        int myRawVal = scoreMap.get(uniMyKey);
                        scoreMap.put(uniMyKey, myRawVal - 1);
                    }

                    String uniTheoryKey = makeKey(uniTem, sentence, i, theoryResI);
                    if (scoreMap.get(uniTheoryKey) == null) {
                        scoreMap.put(uniTheoryKey, 1);
                    } else {
                        int theoryRawVal = scoreMap.get(uniTheoryKey);
                        scoreMap.put(uniTheoryKey, theoryRawVal + 1);
                    }
                }

                for (int[] biTem : getBiTemplate()) {
                    String biMyKey;
                    if (i >= 1) {
                        biMyKey = makeKey(biTem, sentence, i, myRes.substring(i - 1, i + 1));
                    } else {
                        biMyKey = makeKey(biTem, sentence, i, " " + myResI);
                    }
                    if (scoreMap.get(biMyKey) == null) {
                        scoreMap.put(biMyKey, -1);
                    } else {
                        int myRawVal = scoreMap.get(biMyKey);
                        scoreMap.put(biMyKey, myRawVal - 1);
                    }

                    String biTheoryKey;
                    if (i >= 1) {
                        biTheoryKey = makeKey(biTem, sentence, i, theoryRes.substring(i - 1, i + 1));
                    } else {
                        biTheoryKey = makeKey(biTem, sentence, i, " " + theoryResI);
                    }
                    if (scoreMap.get(biTheoryKey) == null) {
                        scoreMap.put(biTheoryKey, 1);
                    } else {
                        int theoryRawVal = scoreMap.get(biTheoryKey);
                        scoreMap.put(biTheoryKey, theoryRawVal + 1);
                    }
                }
            }
        }
    }

    private String getStatus(int row) {
        switch (row) {
            case 0:
                return "B";
            case 1:
                return "I";
            case 2:
                return "E";
            case 3:
                return "S";
            default:
                return null;
        }
    }

    private int statusToRow(String status) {
        switch (status) {
            case "B":
                return 0;
            case "I":
                return 1;
            case "E":
                return 2;
            case "S":
                return 3;
            default:
                return -1;
        }
    }

    private int getMaxIndex(int[] array) {
        int num = array.length;
        int index = 0;
        for (int i = 0; i < num; i++) {
            if (array[i] > array[index]) {
                index = i;
            }
        }
        return index;
    }

    private ArrayList<int[]> getUniTemplate() {
        return template[0];
    }

    private ArrayList<int[]> getBiTemplate() {
        return template[1];
    }
}
